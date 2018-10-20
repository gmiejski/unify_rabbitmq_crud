package org.miejski.questions;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miejski.questions.events.QuestionCreated;
import org.miejski.simple.objects.ObjectState;
import org.miejski.simple.objects.ObjectsTopology;
import org.miejski.simple.objects.events.ObjectCreation;
import org.miejski.simple.objects.events.ObjectDelete;
import org.miejski.simple.objects.events.ObjectUpdate;
import org.miejski.simple.objects.serdes.GenericField;
import org.miejski.simple.objects.serdes.GenericSerde;

import java.time.ZonedDateTime;
import java.util.Properties;

public class QuestionsTest {

    private final String market = "1";
    private final int questionID = 100;
    private final String content = "some content";
    private static TopologyTestDriver testDriver;
    private static KeyValueStore<String, QuestionState> store;

    private ConsumerRecordFactory<String, GenericField> genericObjectFactory = new ConsumerRecordFactory<>(new StringSerializer(), GenericSerde.serde().serializer());


    @BeforeEach
    void setUp() {
        Topology topology = new QuestionsTopology().buildTopology();

        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, this.getClass().getCanonicalName());
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());

        testDriver = new TopologyTestDriver(topology, props);
        store = testDriver.getKeyValueStore(QuestionsTopology.QUESTIONS_STORE_NAME);
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldProperlyUseGenericQuestionEventsInOneTopic() {
        ConsumerRecord<byte[], byte[]> genericCreate = genericObjectFactory.create(QuestionsTopology.FINAL_TOPIC, market, GenericSerde.toGenericField(new QuestionCreated(market, questionID, content, ZonedDateTime.now())));

        testDriver.pipeInput(genericCreate);

        QuestionState state = store.get(QuestionID.from(market, questionID));
        Assertions.assertEquals(market, state.getMarket());
        Assertions.assertEquals(questionID, state.getQuestionID());
        Assertions.assertEquals(content, state.getContent());
    }
}
