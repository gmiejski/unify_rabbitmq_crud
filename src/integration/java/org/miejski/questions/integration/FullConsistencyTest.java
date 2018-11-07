package org.miejski.questions.integration;

import com.google.common.collect.Streams;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.questions.QuestionStateRunner;
import org.miejski.questions.QuestionsStateTopology;
import org.miejski.questions.bus2kafka.Bus2KafkaMappingTopology;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.questions.source.AllAtOnceBusProducer;
import org.miejski.questions.source.BusProducer;
import org.miejski.questions.source.MultiSourceEventProducer;
import org.miejski.questions.source.RandomQuestionIDProvider;
import org.miejski.questions.source.create.SourceQuestionCreateProducer;
import org.miejski.questions.source.create.SourceQuestionCreated;
import org.miejski.questions.source.delete.SourceQuestionDeleted;
import org.miejski.questions.source.rabbitmq.RabbitMQJsonProducer;
import org.miejski.questions.source.update.SourceQuestionUpdated;
import org.miejski.questions.source.update.SourceQuestionUpdatedProducer;
import org.miejski.questions.state.QuestionState;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;


public class FullConsistencyTest {
    private final String market = "us";
    private CountDownLatch latch;
    private int maxQuestionID;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);
    }

    @Test
    void allEventsAreConsistentInTheEnd() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        maxQuestionID = 1000;
        int eventsPerTypeCount = 1000;

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        new QuestionsStateTopology().buildTopology(streamsBuilder);
        new Bus2KafkaMappingTopology().buildTopology(streamsBuilder);

        Topology topology = streamsBuilder.build();
        final KafkaStreams streams = new KafkaStreams(topology, getLocalProperties());

        QuestionStateRunner.addShutdownHook(streams, latch);
        // TODO prepare queues

        ProducersWithState producersWithState = producersAndFinalState(maxQuestionID, eventsPerTypeCount);
        Future producerFinished = producersWithState.start();
        producerFinished.get(10, TimeUnit.SECONDS);

        // when
        new Thread(() -> {
            try {
                streams.start();
                latch.await();
            } catch (Throwable e) {
                System.exit(1);
            }
        }).start();

        while (!streams.state().equals(KafkaStreams.State.RUNNING)) {
            System.out.println("waiting for streams to start");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ReadOnlyKeyValueStore<String, QuestionState> questionsStore =
                streams.store(QuestionsStateTopology.QUESTIONS_STORE_NAME, QueryableStoreTypes.keyValueStore());

        // then
        Assertions.assertTrue(eventually(Duration.ofMinutes(1), () ->assertStateMatches(producersWithState.getStates(), questionsStore)));
    }

    private boolean eventually(Duration maxTime, Supplier<Boolean> s) {
        Duration totalTime = Duration.ZERO;
        while(true) {
            try {
                Thread.sleep(2000);
                if (s.get()) {
                    return true;
                }
                totalTime = totalTime.plus(Duration.ofSeconds(2));
                if (totalTime.compareTo(maxTime) > 0) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private ProducersWithState producersAndFinalState(int maxQuestionID, int eachEventTypeCount) {
        RandomQuestionIDProvider idProvider = new RandomQuestionIDProvider(maxQuestionID);

        MultiSourceEventProducer<SourceQuestionCreated> questionCreateProducer = new MultiSourceEventProducer<>(new SourceQuestionCreateProducer(market, idProvider));
        MultiSourceEventProducer<SourceQuestionUpdated> questionUpdateProducer = new MultiSourceEventProducer<>(new SourceQuestionUpdatedProducer(market, idProvider));

        List<SourceQuestionCreated> createEvents = questionCreateProducer.create(eachEventTypeCount);
        List<SourceQuestionUpdated> updateEvents = questionUpdateProducer.create(eachEventTypeCount);

        Map<String, List<KeyValue<String, SourceQuestionCreated>>> collect = createEvents.stream()
                .map(x -> new KeyValue<>(x.ID(), x))
                .collect(Collectors.groupingBy(x -> x.key));

        createEvents = collect.entrySet().stream().map(x -> x.getValue().get(0).value).collect(Collectors.toList());

        List<SourceQuestionCreated> finalCreateEvents = createEvents;
        List<SourceQuestionCreated> replayedCreateEvents = IntStream.range(0, eachEventTypeCount).boxed()
                .map(x -> finalCreateEvents.get(x % finalCreateEvents.size()))
                .collect(Collectors.toList());

        List<BusProducer> busProducers = generateProducers(replayedCreateEvents, Collections.emptyList()); // TODO change to proper list

        return new ProducersWithState(busProducers, expectedState(replayedCreateEvents, Collections.emptyList())); // TODO change to proper list
    }

    private List<QuestionState> expectedState(List<SourceQuestionCreated> createEvents, List<SourceQuestionUpdated> updateEvents) {

        Stream<Map.Entry<String, List<QuestionModifier>>> stream = Streams.concat(createEvents.stream(), updateEvents.stream())
                .collect(groupingBy(x -> x.ID()))
                .entrySet().stream();

        List<QuestionState> collect = stream.map(x -> x.getValue().stream()
                .reduce(new QuestionState(), (questionState, questionStateModifier) -> questionStateModifier.doSomething(questionState), (questionState, questionState2) -> questionState)
        ).collect(Collectors.toList());

        return collect;
    }

    private List<BusProducer> generateProducers(List<SourceQuestionCreated> createEvents, List<SourceQuestionUpdated> updateEvents) {

        EventStoreConsumer<SourceQuestionCreated> createdStore = new EventStoreConsumer<>();
        EventStoreConsumer<SourceQuestionUpdated> updatedStore = new EventStoreConsumer<>();
        EventStoreConsumer<SourceQuestionDeleted> deletedStore = new EventStoreConsumer<>();

        RabbitMQJsonProducer createRabbitConsumer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_CREATED_QUEUE);
        createRabbitConsumer.connect();
        createRabbitConsumer.setup();
        RabbitMQJsonProducer updatedRabbitConsumer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_UPDATED_QUEUE);
        updatedRabbitConsumer.connect();
        updatedRabbitConsumer.setup();

        AllAtOnceBusProducer<SourceQuestionCreated> created = new AllAtOnceBusProducer<>(market, createEvents, new DuplicateConsumer<>(Arrays.asList(createRabbitConsumer, createdStore)));
        AllAtOnceBusProducer<SourceQuestionUpdated> updated = new AllAtOnceBusProducer<>(market, updateEvents, new DuplicateConsumer<>(Arrays.asList(updatedRabbitConsumer, updatedStore)));

        return Arrays.asList(created, updated);
    }

    private Properties getLocalProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "question-states");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        return props;
    }


    private boolean assertStateMatches(List<QuestionState> states, ReadOnlyKeyValueStore<String, QuestionState> store) {

        Map<String, QuestionState> expectedMap = states.stream().collect(Collectors.toMap(QuestionState::id, Function.identity()));

        try {
            Thread.sleep(2000);
            Map<String, QuestionState> storeQuestions = toMap(store.all());
            List<QuestionState> notMatchingQuestions = states.stream()
                    .filter(x -> !storeQuestions.containsKey(x.id()) || !storeQuestions.get(x.id()).equals(x))
                    .collect(toList());
            if (!notMatchingQuestions.isEmpty()) {
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    private Map<String, QuestionState> toMap(KeyValueIterator<String, QuestionState> all) {
        HashMap<String, QuestionState> result = new HashMap<>();
        while (all.hasNext()) {
            KeyValue<String, QuestionState> next = all.next();
            result.put(next.key, next.value);
        }
        return result;
    }
}

class ProducersWithState {
    List<BusProducer> producers;
    List<QuestionState> states;

    public ProducersWithState(List<BusProducer> producers, List<QuestionState> states) {
        this.producers = producers;
        this.states = states;
    }

    public List<BusProducer> getProducers() {
        return producers;
    }

    public List<QuestionState> getStates() {
        return states;
    }

    public CompletableFuture start() {
        List<CompletableFuture<Boolean>> collect = producers.stream().map(x -> x.start()).collect(toList());
        return CompletableFuture.allOf(collect.toArray(new CompletableFuture[collect.size()]));
    }
}

class DuplicateConsumer<T> implements Consumer<T> {

    private List<Consumer> consumers;

    public DuplicateConsumer(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public void accept(T t) {
        consumers.stream().forEach(consumer -> consumer.accept(t));
    }
}

class EventStoreConsumer<T> implements Consumer<T> {

    private List<T> events = new ArrayList<>();

    @Override
    public void accept(T t) {
        this.events.add(t);
    }

    public List<T> getEvents() {
        return events;
    }
}

