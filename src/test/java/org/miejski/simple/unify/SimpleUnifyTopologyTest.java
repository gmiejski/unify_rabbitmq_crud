package org.miejski.simple.unify;

import org.apache.kafka.common.serialization.IntegerSerializer;
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

import java.util.Properties;

class SimpleUnifyTopologyTest {

    private final String numberKey = "1";
    private static TopologyTestDriver testDriver;
    private static KeyValueStore<String, Integer> store;

    private ConsumerRecordFactory<String, Integer> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new IntegerSerializer());

    @BeforeEach
    void setUp() {
        Topology topology = new SimpleUnifyTopology().buildTopology();

        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "maxAggregation");
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());

        testDriver = new TopologyTestDriver(topology, props);
        store = testDriver.getKeyValueStore(SimpleUnifyTopology.NUMBERS_STORE_NAME);
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldProperlyHaveSingleState() {
        testDriver.pipeInput(recordFactory.create(SimpleUnifyTopology.NEW_TOPIC, numberKey, 10));
        Integer val = store.get("1");
        Assertions.assertEquals(10, val.intValue());
    }

    @Test
    void shouldUpdateWhenUpdateEventsArePassed() {
        testDriver.pipeInput(recordFactory.create(SimpleUnifyTopology.NEW_TOPIC, numberKey, 10));
        Assertions.assertEquals(10, store.get(numberKey).intValue());

        testDriver.pipeInput(recordFactory.create(SimpleUnifyTopology.UPDATE_TOPIC, numberKey, 20));
        Assertions.assertEquals(30, store.get(numberKey).intValue());
    }
}