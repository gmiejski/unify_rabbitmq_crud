package org.miejski.questions.bu2kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miejski.questions.QuestionID;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.questions.QuestionsStateTopology;
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
import org.miejski.simple.objects.serdes.JSONSerde;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Properties;

class Bus2KafkaMappingTopologyTest {

    private final String market = "us";
    private final int questionID = 100;
    private final String content = "some content";
    private static TopologyTestDriver testDriver;

    private ConsumerRecordFactory<String, QuestionModifier> createRecordFactory = new ConsumerRecordFactory<>(new StringSerializer(), JSONSerde.questionsModifierSerde(SourceQuestionCreated.class).serializer());
    private ConsumerRecordFactory<String, QuestionModifier> updatedRecordFactory = new ConsumerRecordFactory<>(new StringSerializer(), JSONSerde.questionsModifierSerde(SourceQuestionUpdated.class).serializer());

    ObjectMapper objectMapper = QuestionObjectMapper.build();

    @BeforeEach
    void setUp() {
        Topology topology = new Bus2KafkaMappingTopology().buildTopology();

        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, this.getClass().getCanonicalName());
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());

        testDriver = new TopologyTestDriver(topology, props);
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void testCreateQuestionEventsProcessed() {
        SourceQuestionCreated source = new SourceQuestionCreated(market, new SourceQuestionCreatedPayload(questionID, content, ZonedDateTime.now()));
        ConsumerRecord<byte[], byte[]> genericCreate = createRecordFactory.create(Bus2KafkaMappingTopology.CREATE_TOPIC, null, source);

        testDriver.pipeInput(genericCreate);

        ProducerRecord<String, QuestionCreated> output = testDriver.readOutput(QuestionsStateTopology.CREATE_TOPIC, Serdes.String().deserializer(), new JsonDeserializerAdapter<QuestionCreated>(objectMapper) {
            @Override
            public QuestionCreated deserialize(byte[] data) throws IOException {
                return objectMapper.readValue(data, QuestionCreated.class);
            }
        });

        Assertions.assertNotNull(output.value());
        Assertions.assertEquals(QuestionID.from(source.getMarket(), source.ID()), output.value().ID());
        Assertions.assertEquals(source.getPayload().getContent(), output.value().getContent());
        Assertions.assertTrue(
                source.getPayload().getCreateDate().isEqual(output.value().getCreateDate()));
        Assertions.assertEquals(source.getMarket(), output.value().getMarket());
    }

    @Test
    void testUpdateQuestionEventsProcessed() {
        SourceQuestionUpdated source = new SourceQuestionUpdated(market, new SourceQuestionUpdatedPayload(questionID, content, ZonedDateTime.now()));
        ConsumerRecord<byte[], byte[]> genericCreate = updatedRecordFactory.create(Bus2KafkaMappingTopology.UPDATE_TOPIC, null, source);

        testDriver.pipeInput(genericCreate);

        ProducerRecord<String, QuestionUpdated> output = testDriver.readOutput(QuestionsStateTopology.UPDATE_TOPIC, Serdes.String().deserializer(), new JsonDeserializerAdapter<QuestionUpdated>(objectMapper) {
            @Override
            public QuestionUpdated deserialize(byte[] data) throws IOException {
                return objectMapper.readValue(data, QuestionUpdated.class);
            }
        });

        Assertions.assertNotNull(output.value());
        Assertions.assertEquals(QuestionID.from(source.getMarket(), source.ID()), output.value().ID());
        Assertions.assertEquals(source.getPayload().getContent(), output.value().getContent());
        Assertions.assertTrue(
                source.getPayload().getEditedAt().isEqual(output.value().getUpdateDate()));
        Assertions.assertEquals(source.getMarket(), output.value().getMarket());
    }

    @Test
    void testDeleteQuestionEventsProcessed() {
        SourceQuestionDeleted source = new SourceQuestionDeleted(market, new SourceQuestionDeletedPayload(questionID, content, ZonedDateTime.now()));
        ConsumerRecord<byte[], byte[]> genericCreate = updatedRecordFactory.create(Bus2KafkaMappingTopology.DELETE_TOPIC, null, source);

        testDriver.pipeInput(genericCreate);

        ProducerRecord<String, QuestionDeleted> output = testDriver.readOutput(QuestionsStateTopology.DELETE_TOPIC, Serdes.String().deserializer(), new JsonDeserializerAdapter<QuestionDeleted>(objectMapper) {
            @Override
            public QuestionDeleted deserialize(byte[] data) throws IOException {
                return objectMapper.readValue(data, QuestionDeleted.class);
            }
        });

        Assertions.assertNotNull(output.value());
        Assertions.assertEquals(QuestionID.from(source.getMarket(), source.ID()), output.value().ID());
        Assertions.assertTrue(
                source.getPayload().getDeletedAt().isEqual(output.value().getDeleteDate()));
        Assertions.assertEquals(source.getMarket(), output.value().getMarket());
    }
}

abstract class JsonDeserializerAdapter<T> implements Deserializer<T> {

    ObjectMapper objectMapper;

    public JsonDeserializerAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return this.deserialize(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    abstract public T deserialize(byte[] data) throws IOException;
}