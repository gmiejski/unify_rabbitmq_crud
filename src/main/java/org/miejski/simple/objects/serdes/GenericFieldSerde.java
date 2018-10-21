package org.miejski.simple.objects.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.miejski.Modifier;
import org.miejski.questions.QuestionObjectMapper;

import java.io.IOException;
import java.util.Map;

public class GenericFieldSerde {
    private ObjectMapper objectMapper; // TODO replace with GeneralJsonSerde

    public GenericFieldSerde(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GenericField toGenericField(Modifier objectModifier) {
        String serializer = objectModifier.getClass().getSimpleName();

        try {
            return new GenericField(serializer, this.objectMapper.writeValueAsString(objectModifier));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error marshalling data!!!!!!");
        }
    }


    public static Serde<GenericField> serde() {
        return Serdes.serdeFrom(new GenericJSONSer<>(), new GenericFieldSerde.ObjectGenericDe());
    }

    static class ObjectGenericDe implements org.apache.kafka.common.serialization.Deserializer<GenericField> {
        private final ObjectMapper objectMapper;

        public ObjectGenericDe() {
            this.objectMapper = QuestionObjectMapper.build();
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            System.out.println("configure ObjectDe");
        }

        @Override
        public GenericField deserialize(String topic, byte[] data) {
            try {
                return objectMapper.readValue(data, GenericField.class);
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


}
