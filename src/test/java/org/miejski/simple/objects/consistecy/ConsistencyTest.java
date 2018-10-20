package org.miejski.simple.objects.consistecy;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miejski.simple.objects.GenericObjectsSerde;
import org.miejski.simple.objects.ObjectState;
import org.miejski.simple.objects.ObjectsTopology;
import org.miejski.simple.objects.events.ObjectModifier;
import org.miejski.simple.objects.serdes.GenericField;
import org.miejski.simple.objects.serdes.GenericSerde;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConsistencyTest {

    private final String objectID = "1";
    private static TopologyTestDriver testDriver;
    private static KeyValueStore<String, ObjectState> store;

    private ConsumerRecordFactory<String, GenericField> genericObjectFactory = new ConsumerRecordFactory<>(new StringSerializer(), GenericSerde.serde().serializer());

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
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldMaintainConsistentResultsForAllOperations() {
        // given
        EventsPercentages eventsPercentages = new EventsPercentages(50, 30, 20);
        GeneratedData generatedData = new Generator(eventsPercentages, 150, 10).generate();
        List<ObjectModifier> events = generatedData.getEvents();
        Map<String, ObjectState> expectedStates = generatedData.finalState();
        Collections.shuffle(events);
        events.stream()
                .map(e -> genericObjectFactory.create(ObjectsTopology.FINAL_TOPIC, e.ID(), GenericObjectsSerde.build().toGenericField(e)))
                .forEach(e -> testDriver.pipeInput(e));


        // when
        KeyValueIterator<String, ObjectState> allInStore = store.all();

        // then
        Map<String, ObjectState> actualStore = toMap(store.all());
        Map<String, List<ObjectModifier>> unmathingStates = new HashMap<>();

        while (allInStore.hasNext()) {
            KeyValue<String, ObjectState> next = allInStore.next();
            if (!next.value.equals(expectedStates.get(next.key))) {
                unmathingStates.put(next.key, generatedData.getEventsFor(next.key));
            }
        }
        Assertions.assertTrue(unmathingStates.isEmpty());
    }

    private Map<String, ObjectState> toMap(KeyValueIterator<String, ObjectState> all) {
        HashMap<String, ObjectState> result = new HashMap<>();
        while (all.hasNext()) {
            KeyValue<String, ObjectState> next = all.next();
            result.put(next.key, next.value);
        }
        return result;
    }
}