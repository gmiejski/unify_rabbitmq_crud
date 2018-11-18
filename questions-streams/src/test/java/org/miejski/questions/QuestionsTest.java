package org.miejski.questions;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miejski.questions.bus2kafka.Bus2KafkaMappingTopology;
import org.miejski.questions.events.QuestionCreated;
import org.miejski.questions.events.QuestionDeleted;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.questions.events.QuestionUpdated;
import org.miejski.questions.source.create.SourceQuestionCreated;
import org.miejski.questions.source.create.SourceQuestionCreatedPayload;
import org.miejski.questions.source.delete.SourceQuestionDeleted;
import org.miejski.questions.source.delete.SourceQuestionDeletedPayload;
import org.miejski.questions.source.update.SourceQuestionUpdated;
import org.miejski.questions.source.update.SourceQuestionUpdatedPayload;
import org.miejski.questions.state.QuestionState;
import org.miejski.simple.objects.serdes.GenericField;
import org.miejski.simple.objects.serdes.GenericFieldSerde;
import org.miejski.simple.objects.serdes.JSONSerde;

import java.time.ZonedDateTime;
import java.util.Properties;

public class QuestionsTest {

    private final String market = "us";
    private final int questionID = 100;
    private final String content = "some content";
    private final String updateContent = "newContent";
    private static TopologyTestDriver testDriver;
    private static KeyValueStore<String, QuestionState> store;
    private final GenericFieldSerde genericFieldSerde = new GenericFieldSerde(QuestionObjectMapper.build());

    private ConsumerRecordFactory<String, QuestionModifier> createRecordFactory = new ConsumerRecordFactory<>(new StringSerializer(), JSONSerde.questionsModifierSerde(QuestionCreated.class).serializer());
    private ConsumerRecordFactory<String, QuestionModifier> updateRecordFactory = new ConsumerRecordFactory<>(new StringSerializer(), JSONSerde.questionsModifierSerde(QuestionUpdated.class).serializer());
    private ConsumerRecordFactory<String, QuestionModifier> deleteRecordFactory = new ConsumerRecordFactory<>(new StringSerializer(), JSONSerde.questionsModifierSerde(QuestionDeleted.class).serializer());
    private ConsumerRecordFactory<String, GenericField> genericObjectFactory = new ConsumerRecordFactory<>(new StringSerializer(), GenericFieldSerde.serde().serializer());

    private String questionRef = QuestionID.from(market, questionID);


    @BeforeEach
    void setUp() {

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        Topology topology = new QuestionsStateTopology().buildTopology(streamsBuilder);
        Topology topology2 = new Bus2KafkaMappingTopology().buildTopology(streamsBuilder);

        System.out.println(topology2.describe());

        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, this.getClass().getCanonicalName());
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());

        testDriver = new TopologyTestDriver(topology, props);
        store = testDriver.getKeyValueStore(QuestionsStateTopology.QUESTIONS_AGGREGATION_STORE_NAME);
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldProperlyUseGenericQuestionEventsInOneTopic() {
        QuestionCreated objectModifier = new QuestionCreated(market, questionID, content, ZonedDateTime.now());
        ConsumerRecord<byte[], byte[]> genericCreate = genericObjectFactory.create(QuestionsStateTopology.FINAL_TOPIC, questionRef, genericFieldSerde.toGenericField(objectModifier));

        testDriver.pipeInput(genericCreate);

        QuestionState state = store.get(QuestionID.from(market, questionID));
        Assertions.assertEquals(market, state.getMarket());
        Assertions.assertEquals(questionID, state.getQuestionID());
        Assertions.assertEquals(content, state.getContent());

        ConsumerRecord<byte[], byte[]> genericUpdate = genericObjectFactory.create(QuestionsStateTopology.FINAL_TOPIC, questionRef, genericFieldSerde.toGenericField(new QuestionUpdated(market, questionID, updateContent, ZonedDateTime.now())));
        testDriver.pipeInput(genericUpdate);

        state = store.get(QuestionID.from(market, questionID));
        Assertions.assertEquals(market, state.getMarket());
        Assertions.assertEquals(questionID, state.getQuestionID());
        Assertions.assertEquals(updateContent, state.getContent());

        ConsumerRecord<byte[], byte[]> genericDelete = genericObjectFactory.create(QuestionsStateTopology.FINAL_TOPIC, questionRef, genericFieldSerde.toGenericField(new QuestionDeleted(market, questionID, ZonedDateTime.now())));
        testDriver.pipeInput(genericDelete);

        state = store.get(QuestionID.from(market, questionID));
        Assertions.assertEquals(market, state.getMarket());
        Assertions.assertEquals(questionID, state.getQuestionID());
        Assertions.assertEquals(updateContent, state.getContent());
        Assertions.assertTrue(state.isDeleted());
    }

    @Test
    void shouldReadEachEventSeparately() {
        testDriver.pipeInput(createRecordFactory.create(QuestionsStateTopology.CREATE_TOPIC, questionRef, new QuestionCreated(market, questionID, content, ZonedDateTime.now())));
        testDriver.pipeInput(updateRecordFactory.create(QuestionsStateTopology.UPDATE_TOPIC, questionRef, new QuestionUpdated(market, questionID, updateContent, ZonedDateTime.now())));
        testDriver.pipeInput(deleteRecordFactory.create(QuestionsStateTopology.DELETE_TOPIC, questionRef, new QuestionDeleted(market, questionID, ZonedDateTime.now())));

        QuestionState state = store.get(QuestionID.from(market, questionID));

        Assertions.assertEquals(market, state.getMarket());
        Assertions.assertEquals(questionID, state.getQuestionID());
        Assertions.assertEquals(updateContent, state.getContent());
        Assertions.assertTrue(state.isDeleted());
    }

    @Test
    void sourceCreateTest() {
        testDriver.pipeInput(createRecordFactory.create(Bus2KafkaMappingTopology.CREATE_TOPIC, null, new SourceQuestionCreated(market, new SourceQuestionCreatedPayload(questionID, content, ZonedDateTime.now()))));

        QuestionState state = store.get(QuestionID.from(market, questionID));

        Assertions.assertEquals(market, state.getMarket());
        Assertions.assertEquals(questionID, state.getQuestionID());
        Assertions.assertEquals(content, state.getContent());
        Assertions.assertFalse(state.isDeleted());
    }

    @Test
    void sourceUpdateTest() {
        testDriver.pipeInput(createRecordFactory.create(Bus2KafkaMappingTopology.UPDATE_TOPIC, null, new SourceQuestionUpdated(market, new SourceQuestionUpdatedPayload(questionID, content, ZonedDateTime.now()))));

        QuestionState state = store.get(QuestionID.from(market, questionID));

        Assertions.assertEquals(market, state.getMarket());
        Assertions.assertEquals(questionID, state.getQuestionID());
        Assertions.assertEquals(content, state.getContent());
        Assertions.assertFalse(state.isDeleted());
    }

    @Test
    void sourceDeleteTest() {
        testDriver.pipeInput(createRecordFactory.create(Bus2KafkaMappingTopology.DELETE_TOPIC, null, new SourceQuestionDeleted(market, new SourceQuestionDeletedPayload(questionID, ZonedDateTime.now()))));

        QuestionState state = store.get(QuestionID.from(market, questionID));

        Assertions.assertEquals(market, state.getMarket());
        Assertions.assertEquals(questionID, state.getQuestionID());
        Assertions.assertTrue(state.isDeleted());
    }
}
