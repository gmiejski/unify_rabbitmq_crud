package org.miejski.simple;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.jupiter.api.Test;

import java.util.Properties;

class SimpleMapTopologyTest {


    @Test
    void simplestMapOperationTest() {
        // given
        Topology topology = new SimpleMapTopology().buildTopology();
        Properties props = getTestProperties();
        TopologyTestDriver testDriver = new TopologyTestDriver(topology, props);
        ConsumerRecordFactory<String, Integer> factory = new ConsumerRecordFactory<>("input-topic", new StringSerializer(), new IntegerSerializer());

        // when
        testDriver.pipeInput(factory.create("input-topic", "123123132", 42));

        // then
        ProducerRecord<String, Integer> outputRecord = testDriver.readOutput("output-topic", new StringDeserializer(), new IntegerDeserializer());
        OutputVerifier.compareKeyValue(outputRecord, "123123132", 84);
    }

    private Properties getTestProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        return props;
    }
}