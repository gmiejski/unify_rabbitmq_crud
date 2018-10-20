package org.miejski.simple.objects;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.miejski.TopologyBuilder;
import org.miejski.simple.objects.events.ObjectCreation;
import org.miejski.simple.objects.events.ObjectDelete;
import org.miejski.simple.objects.events.ObjectModifier;
import org.miejski.simple.objects.events.ObjectUpdate;
import org.miejski.simple.objects.serdes.GenericField;
import org.miejski.simple.objects.serdes.GenericSerde;
import org.miejski.simple.objects.serdes.JSONSerde;

import java.util.HashMap;
import java.util.Map;

public class ObjectsTopology implements TopologyBuilder {

    public static final String CREATE_TOPIC = "create_topic";
    public static final String UPDATE_TOPIC = "update_topic";
    public static final String DELETE_TOPIC = "delete_topic";
    public static final String FINAL_TOPIC = "final_topic";
    public static final String OBJECTS_STORE_NAME = "objectsFinal";


    @Override
    public Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        HashMap<String, Class> serializers = new HashMap<>();
        serializers.put(ObjectCreation.class.getSimpleName(), ObjectCreation.class);
        serializers.put(ObjectUpdate.class.getSimpleName(), ObjectUpdate.class);
        serializers.put(ObjectDelete.class.getSimpleName(), ObjectDelete.class);
        GenericSerde genericObjectSerde = new GenericSerde(serializers);

        forwardToGenericTopic(streamsBuilder, CREATE_TOPIC, ObjectCreation.class, genericObjectSerde);
        forwardToGenericTopic(streamsBuilder, UPDATE_TOPIC, ObjectUpdate.class, genericObjectSerde);
        forwardToGenericTopic(streamsBuilder, DELETE_TOPIC, ObjectDelete.class, genericObjectSerde); // TODO genericObjectSerde should not be needed over here at all

        KStream<String, GenericField> allGenerics = streamsBuilder.stream(FINAL_TOPIC, Consumed.with(Serdes.String(), GenericSerde.serde()));

        ObjectsAggregator aggregator = new ObjectsAggregator(serializers);

        Materialized<String, ObjectState, KeyValueStore<Bytes, byte[]>> store = Materialized.<String, ObjectState, KeyValueStore<Bytes, byte[]>>as(OBJECTS_STORE_NAME).withKeySerde(Serdes.String()).withValueSerde(JSONSerde.objectStateSerde());
        allGenerics.groupByKey()
                .aggregate(ObjectState::new, aggregator, store);
        return streamsBuilder.build();
    }

    private void forwardToGenericTopic(StreamsBuilder streamsBuilder, String inputTopic, Class<? extends ObjectModifier> inputTopicClass, GenericSerde genericSerde) {
        streamsBuilder.stream(inputTopic, Consumed.with(Serdes.String(), JSONSerde.objectModifierSerde(inputTopicClass)))
        .mapValues(genericSerde::toGenericField).to(FINAL_TOPIC, Produced.with(Serdes.String(), GenericSerde.serde()));
    }
}
