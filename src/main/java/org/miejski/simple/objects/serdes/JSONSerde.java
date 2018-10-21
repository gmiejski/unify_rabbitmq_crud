package org.miejski.simple.objects.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.simple.objects.ObjectState;
import org.miejski.simple.objects.events.ObjectModifier;

import java.io.IOException;
import java.util.Map;


public class JSONSerde {

    public static Serde<ObjectModifier> objectModifierSerde(Class<? extends ObjectModifier> c) {
        return Serdes.serdeFrom(new GenericJSONSer<>(), new GenericJSONDe<>(c));
    }

    public static Serde<ObjectState> objectStateSerde() {
        return Serdes.serdeFrom(new GenericJSONSer<>(), new ObjectStateDe());
    }
}

class ObjectStateDe implements org.apache.kafka.common.serialization.Deserializer<ObjectState> {
    private final ObjectMapper objectMapper;

    public ObjectStateDe() {
        this.objectMapper = QuestionObjectMapper.build();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        System.out.println("configure ObjectDe");
    }

    @Override
    public ObjectState deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, ObjectState.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        System.out.println("closing ObjectDe");
    }
}


