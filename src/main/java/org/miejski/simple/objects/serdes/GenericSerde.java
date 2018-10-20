package org.miejski.simple.objects.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.miejski.Modifier;
import org.miejski.questions.QuestionObjectMapper;

import java.io.IOException;
import java.util.Map;

public class GenericSerde { // TODO replace with GeneralJsonSerde

    private Map<String, Class> serializers;

    public GenericSerde(Map<String, Class> serializers) {
        this.serializers = serializers;
    }

    public GenericField toGenericField(Modifier objectModifier) {
        String serializer = objectModifier.getClass().getSimpleName();

        if (!serializers.containsKey(serializer)) {
            throw new RuntimeException("Unknown serializer: " + serializer);
        }
        try {
            return new GenericField(serializer, QuestionObjectMapper.build().writeValueAsString(objectModifier));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error marshalling data!!!!!!");
        }
    }


    public static Serde<GenericField> serde() {
        return Serdes.serdeFrom(new GenericJSONSer<>(), new GenericSerde.ObjectGenericDe());
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
