package org.miejski.questions;

import com.google.common.collect.Streams;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.miejski.Modifier;
import org.miejski.questions.source.AllAtOnceBusProducer;
import org.miejski.questions.source.BusProducer;
import org.miejski.questions.source.MultiSourceEventProducer;
import org.miejski.questions.source.RandomQuestionIDProvider;
import org.miejski.questions.source.create.QuestionCreateProducer;
import org.miejski.questions.source.create.QuestionCreated;
import org.miejski.questions.source.delete.QuestionDeleted;
import org.miejski.questions.source.rabbitmq.RabbitMQJsonProducer;
import org.miejski.questions.source.update.QuestionUpdated;
import org.miejski.questions.source.update.QuestionUpdatedProducer;
import org.miejski.questions.state.QuestionState;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
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
    }

    @Test
    @Disabled
    void allEventsAreConsistentInTheEnd() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        maxQuestionID = 1000;
        int eventsPerTypeCount = 1000;

        final KafkaStreams streams = new KafkaStreams(new QuestionsTopology().buildTopology(), getLocalProperties());

        QuestionStateRunner.addShutdownHook(streams, latch);
        // TODO prepare queues

        ProducersWithState producersWithState = producersAndFinalState(maxQuestionID, eventsPerTypeCount);
        Future producerFinished = producersWithState.start();
        producerFinished.get(10, TimeUnit.SECONDS);

        // when
        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }

        ReadOnlyKeyValueStore<Integer, QuestionState> questionsStore =
                streams.store(QuestionsTopology.QUESTIONS_STORE_NAME, QueryableStoreTypes.keyValueStore());

        // then
        Assertions.assertTimeout(Duration.ofSeconds(60), () -> assertStateMatches(producersWithState.getStates(), questionsStore));
    }

    private ProducersWithState producersAndFinalState(int maxQuestionID, int eachEventTypeCount) {
        RandomQuestionIDProvider idProvider = new RandomQuestionIDProvider(maxQuestionID);

        MultiSourceEventProducer<QuestionCreated> questionCreateProducer = new MultiSourceEventProducer<>(new QuestionCreateProducer(market, idProvider));
        MultiSourceEventProducer<QuestionUpdated> questionUpdateProducer = new MultiSourceEventProducer<>(new QuestionUpdatedProducer(market, idProvider));

        List<QuestionCreated> createEvents = questionCreateProducer.create(eachEventTypeCount); // TODO unify timestamps of create
        List<QuestionUpdated> updateEvents = questionUpdateProducer.create(eachEventTypeCount);

        List<BusProducer> busProducers = generateProducers(createEvents, updateEvents);

        return new ProducersWithState(busProducers, expectedState(createEvents, updateEvents));
    }

    private List<QuestionState> expectedState(List<QuestionCreated> createEvents, List<QuestionUpdated> updateEvents) {

        Stream<Map.Entry<String, List<Modifier<QuestionState>>>> stream = Streams.concat(createEvents.stream(), updateEvents.stream())
                .collect(groupingBy(x -> x.ID()))
                .entrySet().stream();

        List<QuestionState> collect = stream.map(x -> x.getValue().stream()
                .reduce(new QuestionState(), (questionState, questionStateModifier) -> questionStateModifier.doSomething(questionState), (questionState, questionState2) -> questionState)
        ).collect(Collectors.<QuestionState>toList());

        return collect;
    }

    private List<BusProducer> generateProducers(List<QuestionCreated> createEvents, List<QuestionUpdated> updateEvents) {

        EventStoreConsumer<QuestionCreated> createdStore = new EventStoreConsumer<>();
        EventStoreConsumer<QuestionUpdated> updatedStore = new EventStoreConsumer<>();
        EventStoreConsumer<QuestionDeleted> deletedStore = new EventStoreConsumer<>();

        RabbitMQJsonProducer createRabbitConsumer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_CREATED_QUEUE);
        createRabbitConsumer.connect();
        createRabbitConsumer.setup();
        RabbitMQJsonProducer updatedRabbitConsumer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_UPDATED_QUEUE);
        updatedRabbitConsumer.connect();
        updatedRabbitConsumer.setup();

        AllAtOnceBusProducer<QuestionCreated> created = new AllAtOnceBusProducer<>(market, createEvents, new DuplicateConsumer<>(Arrays.asList(createRabbitConsumer, createdStore)));
        AllAtOnceBusProducer<QuestionUpdated> updated = new AllAtOnceBusProducer<>(market, updateEvents, new DuplicateConsumer<>(Arrays.asList(updatedRabbitConsumer, updatedStore)));

        return Arrays.asList(created, updated);
    }

    private Properties getLocalProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "question-states");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        return props;
    }

    private void assertStateMatches(List<QuestionState> states, ReadOnlyKeyValueStore<Integer, QuestionState> store) {
        while (true) {

        }
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