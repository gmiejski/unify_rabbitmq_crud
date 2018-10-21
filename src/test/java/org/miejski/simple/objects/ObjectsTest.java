package org.miejski.simple.objects;

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
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.simple.objects.events.ObjectCreation;
import org.miejski.simple.objects.events.ObjectDelete;
import org.miejski.simple.objects.events.ObjectModifier;
import org.miejski.simple.objects.events.ObjectUpdate;
import org.miejski.simple.objects.serdes.GenericField;
import org.miejski.simple.objects.serdes.GenericFieldSerde;
import org.miejski.simple.objects.serdes.JSONSerde;

import java.time.ZonedDateTime;
import java.util.Properties;

public class ObjectsTest {

    private final String key = "1";
    private static TopologyTestDriver testDriver;
    private static KeyValueStore<String, ObjectState> store;

    private ConsumerRecordFactory<String, ObjectModifier> createRecordFactory = new ConsumerRecordFactory<>(new StringSerializer(), JSONSerde.objectModifierSerde(ObjectCreation.class).serializer());
    private ConsumerRecordFactory<String, ObjectModifier> updateRecordFactory = new ConsumerRecordFactory<>(new StringSerializer(), JSONSerde.objectModifierSerde(ObjectUpdate.class).serializer());
    private ConsumerRecordFactory<String, ObjectModifier> deleteRecordFactory = new ConsumerRecordFactory<>(new StringSerializer(), JSONSerde.objectModifierSerde(ObjectDelete.class).serializer());
    private ConsumerRecordFactory<String, GenericField> genericObjectFactory = new ConsumerRecordFactory<>(new StringSerializer(), GenericFieldSerde.serde().serializer());

    private GenericFieldSerde genericObjectSerde;


    @BeforeEach
    void setUp() {
        Topology topology = new ObjectsTopology().buildTopology();

        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, this.getClass().getCanonicalName());
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());

        testDriver = new TopologyTestDriver(topology, props);
        store = testDriver.getKeyValueStore(ObjectsTopology.OBJECTS_STORE_NAME);

        genericObjectSerde = new GenericFieldSerde(QuestionObjectMapper.build());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldProperlyUseGenericObjectsInOneTopic() {
        ConsumerRecord<byte[], byte[]> genericCreate = genericObjectFactory.create(ObjectsTopology.FINAL_TOPIC, key, genericObjectSerde.toGenericField(new ObjectCreation(key, 20, ZonedDateTime.now())));

        testDriver.pipeInput(genericCreate);

        ObjectState state = store.get(key);
        Assertions.assertEquals(20, state.getValue());


        ConsumerRecord<byte[], byte[]> genericUpdate = genericObjectFactory.create(ObjectsTopology.FINAL_TOPIC, key, genericObjectSerde.toGenericField(new ObjectUpdate(key, 7, ZonedDateTime.now())));
        testDriver.pipeInput(genericUpdate);

        state = store.get(key);
        Assertions.assertEquals(7, state.getValue());
        Assertions.assertFalse(state.isDeleted());

        ConsumerRecord<byte[], byte[]> genericDelete = genericObjectFactory.create(ObjectsTopology.FINAL_TOPIC, key, genericObjectSerde.toGenericField(new ObjectDelete(key, ZonedDateTime.now())));
        testDriver.pipeInput(genericDelete);
        state = store.get(key);
        Assertions.assertTrue(state.isDeleted());
    }

    @Test
    void shouldProduceProperOutputFromSeparateTopics() {
        testDriver.pipeInput(createRecordFactory.create(ObjectsTopology.CREATE_TOPIC, key, new ObjectCreation(key, 200, ZonedDateTime.now())));
        testDriver.pipeInput(updateRecordFactory.create(ObjectsTopology.UPDATE_TOPIC, key, new ObjectUpdate(key, 70, ZonedDateTime.now())));
        testDriver.pipeInput(deleteRecordFactory.create(ObjectsTopology.DELETE_TOPIC, key, new ObjectDelete(key, ZonedDateTime.now())));

        ObjectState state = store.get(key);

        Assertions.assertEquals(70, state.getValue());
        Assertions.assertTrue(state.isDeleted());
    }
}
