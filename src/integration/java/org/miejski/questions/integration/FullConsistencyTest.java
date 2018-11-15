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
import org.miejski.questions.JsonSerde;
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
import org.miejski.questions.source.delete.SourceQuestionDeletedProducer;
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
        setupRabbitQueues();
    }

    @Test
    void allEventsAreConsistentInTheEnd() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        maxQuestionID = 1000;
        int eventsPerTypeCount = 1000;

        final KafkaStreams streams = prepareStream(latch);

        ProducersWithState fromFileProducers = rabbitMQProducersFromFile(maxQuestionID, eventsPerTypeCount);

        Future producerFinished = fromFileProducers.start();
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
        waitForStreamsStart(streams);


        // then
        Assertions.assertTrue(eventually(Duration.ofMinutes(3), () -> assertStateMatches(fromFileProducers.getStates(), streams)));
    }

    private ProducersWithState rabbitMQProducersFromFile(int maxQuestionID, int eventsCount) {
        RandomQuestionIDProvider idProvider = new RandomQuestionIDProvider(maxQuestionID);
        MultiSourceEventProducer<SourceQuestionCreated> questionCreateProducer = new MultiSourceEventProducer<>(new SourceQuestionCreateProducer(market, idProvider));
        MultiSourceEventProducer<SourceQuestionUpdated> questionUpdateProducer = new MultiSourceEventProducer<>(new SourceQuestionUpdatedProducer(market, idProvider));
        MultiSourceEventProducer<SourceQuestionDeleted> questionDeleteProducer = new MultiSourceEventProducer<>(new SourceQuestionDeletedProducer(market, idProvider));

        Map<String, List<? extends QuestionModifier>> objectsMap = new HashMap<>();
        objectsMap.put("create", questionCreateProducer.create(eventsCount, SourceQuestionCreated::ID));
        objectsMap.put("update", questionUpdateProducer.create(eventsCount));
        objectsMap.put("delete", questionDeleteProducer.create(eventsCount));

        JsonSerde jsonSerde = new JsonSerde();
        jsonSerde.dumpAllEvents(objectsMap, false);

        List<QuestionModifier> createEvents = jsonSerde.readEvents("create", SourceQuestionCreated.class);
        List<QuestionModifier> updateEvents = jsonSerde.readEvents("update", SourceQuestionUpdated.class);
        List<QuestionModifier> deleteEvents = jsonSerde.readEvents("delete", SourceQuestionDeleted.class);

        Collections.shuffle(createEvents);
        Collections.shuffle(updateEvents);
        Collections.shuffle(deleteEvents);

        List<BusProducer> busProducers = Arrays.asList(
                generateRabbitMQProducer(createEvents, RabbitMQJsonProducer.QUESTION_CREATED_QUEUE),
                generateRabbitMQProducer(updateEvents, RabbitMQJsonProducer.QUESTION_UPDATED_QUEUE),
                generateRabbitMQProducer(deleteEvents, RabbitMQJsonProducer.QUESTION_DELETED_QUEUE)
        );
        return new ProducersWithState(busProducers, expectedState(Streams.concat(createEvents.stream(), updateEvents.stream(), deleteEvents.stream()).collect(toList())));
    }

    private ReadOnlyKeyValueStore<String, QuestionState> getStateStore(KafkaStreams streams) {
        return streams.store(QuestionsStateTopology.QUESTIONS_STORE_NAME, QueryableStoreTypes.keyValueStore());
    }

    private void waitForStreamsStart(KafkaStreams streams) {
        while (!streams.state().equals(KafkaStreams.State.RUNNING)) {
            System.out.println("waiting for streams to start");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private KafkaStreams prepareStream(CountDownLatch latch) {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        new QuestionsStateTopology().buildTopology(streamsBuilder);
        new Bus2KafkaMappingTopology().buildTopology(streamsBuilder);

        Topology topology = streamsBuilder.build();
        final KafkaStreams streams = new KafkaStreams(topology, getLocalProperties());

        QuestionStateRunner.addShutdownHook(streams, latch);
        return streams;
    }

    private boolean eventually(Duration maxTime, Supplier<Boolean> s) {
        Duration totalTime = Duration.ZERO;
        while (true) {
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

    private List<QuestionState> expectedState(List<QuestionModifier> modifiers) {
        Stream<Map.Entry<String, List<QuestionModifier>>> stream = modifiers.stream()
                .collect(groupingBy(QuestionModifier::ID))
                .entrySet().stream();
        return stream.map(this::toQuestionState).collect(toList());
    }

    private QuestionState toQuestionState(Map.Entry<String, List<QuestionModifier>> questionModifiers) {
        return questionModifiers.getValue().stream()
                .reduce(new QuestionState(), (questionState, questionStateModifier) -> questionStateModifier.doSomething(questionState), (questionState, questionState2) -> questionState);
    }

    private <T> BusProducer generateRabbitMQProducer(List<T> events, String rabbitmqQueueName) {
        EventStoreConsumer<T> eventStore = new EventStoreConsumer<>();
        RabbitMQJsonProducer rabitConsumer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), rabbitmqQueueName);
        rabitConsumer.connect();
        return new AllAtOnceBusProducer<>(market, events, new DuplicateConsumer<>(Arrays.asList(rabitConsumer, eventStore)));
    }

    private Properties getLocalProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "question-states");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        return props;
    }

    private boolean assertStateMatches(List<QuestionState> states, KafkaStreams streams) {
        Map<String, QuestionState> expectedMap = states.stream().collect(Collectors.toMap(QuestionState::id, Function.identity()));
        try {
            ReadOnlyKeyValueStore<String, QuestionState> store = getStateStore(streams);
            Map<String, QuestionState> storeQuestions = toMap(store.all());
            List<QuestionState> notMatchingQuestions = states.stream()
                    .filter(x -> !storeQuestions.containsKey(x.id()) || !storeQuestions.get(x.id()).equals(x))
                    .collect(toList());
            if (!notMatchingQuestions.isEmpty()) {
                return false;
            }
        } catch (Exception e) {
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

    private void setupRabbitQueues() {
        RabbitMQJsonProducer createRabbitProducer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_CREATED_QUEUE);
        createRabbitProducer.connect();
        createRabbitProducer.setup();
        createRabbitProducer.close();
        RabbitMQJsonProducer updatedRabbitProducer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_UPDATED_QUEUE);
        updatedRabbitProducer.connect();
        updatedRabbitProducer.setup();
        updatedRabbitProducer.close();
        RabbitMQJsonProducer deleteRabbitProducer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_DELETED_QUEUE);
        deleteRabbitProducer.connect();
        deleteRabbitProducer.setup();
        deleteRabbitProducer.close();
    }
}

class ProducersWithState {
    List<BusProducer> producers;
    List<QuestionState> states;

    public ProducersWithState(List<BusProducer> producers, List<QuestionState> states) {
        this.producers = producers;
        this.states = states;
    }

    public List<QuestionState> getStates() {
        return states;
    }

    public CompletableFuture start() {
        return CompletableFuture.allOf(producers.stream().map(BusProducer::start).toArray(CompletableFuture[]::new));
    }
}

class DuplicateConsumer<T> implements Consumer<T> {

    private List<Consumer> consumers;

    public DuplicateConsumer(List<Consumer> consumers) {
        this.consumers = consumers;
    }

    @Override
    public void accept(T t) {
        consumers.forEach(consumer -> consumer.accept(t));
    }
}

class EventStoreConsumer<T> implements Consumer<T> {

    private List<T> events = new ArrayList<>();

    @Override
    public void accept(T t) {
        this.events.add(t);
    }

}

