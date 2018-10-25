package org.miejski.questions.consistency;

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
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.questions.state.QuestionState;
import org.miejski.questions.QuestionsTopology;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.simple.objects.consistecy.EventsPercentages;
import org.miejski.simple.objects.serdes.GenericField;
import org.miejski.simple.objects.serdes.GenericFieldSerde;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConsistencyTest {

    private final String objectID = "1";
    private static TopologyTestDriver testDriver;
    private static KeyValueStore<String, QuestionState> store;
    private final GenericFieldSerde genericFieldSerde = new GenericFieldSerde(QuestionObjectMapper.build());
    private final String market = "us";
    
    private ConsumerRecordFactory<String, GenericField> genericObjectFactory = new ConsumerRecordFactory<>(new StringSerializer(), GenericFieldSerde.serde().serializer());

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
    void shouldMaintainConsistentResultsForAllOperations() {
        // given
        EventsPercentages eventsPercentages = new EventsPercentages(50, 30, 20);
        GeneratedData generatedData = new Generator(eventsPercentages, 150, 10, this.market).generate();
        List<QuestionModifier> events = generatedData.getEvents();
        Map<String, QuestionState> expectedStates = generatedData.finalState();
        Collections.shuffle(events);
        events.stream()
                .map(e -> genericObjectFactory.create(QuestionsTopology.FINAL_TOPIC, e.ID(), genericFieldSerde.toGenericField(e)))
                .forEach(e -> testDriver.pipeInput(e));

        // when
        KeyValueIterator<String, QuestionState> allInStore = store.all();

        // then
        Map<String, QuestionState> actualStore = toMap(store.all());
        Map<String, List<QuestionModifier>> unmathingStates = new HashMap<>();

        while (allInStore.hasNext()) {
            KeyValue<String, QuestionState> next = allInStore.next();
            if (!next.value.equals(expectedStates.get(next.key))) {
                unmathingStates.put(next.key, generatedData.getEventsFor(next.key));
            }
        }
        Assertions.assertTrue(unmathingStates.isEmpty());
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