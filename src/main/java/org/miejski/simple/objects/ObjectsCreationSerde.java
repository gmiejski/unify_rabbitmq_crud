package org.miejski.simple.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.simple.objects.events.ObjectCreation;

import java.io.IOException;
import java.util.Map;

public class ObjectsCreationSerde {

    public static Serde<ObjectCreation> serde() {
        return Serdes.serdeFrom(new ObjectsCreationSerde.ObjectsSer(), new ObjectsCreationSerde.ObjectsDe());
    }

    static class ObjectsSer implements org.apache.kafka.common.serialization.Serializer<ObjectCreation> {
        private final ObjectMapper objectMapper;

        public ObjectsSer() {
            this.objectMapper = QuestionObjectMapper.build();
        }

        @Override
        public void configure(Map configs, boolean isKey) {
            System.out.println("Configure ObjectSer");
        }

        @Override
        public byte[] serialize(String topic, ObjectCreation obj) {
            try {
                return objectMapper.writeValueAsBytes(obj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new byte[0];
        }

        @Override
        public void close() {
            System.out.println("closing ObjectSer");
        }
    }

    static class ObjectsDe implements org.apache.kafka.common.serialization.Deserializer<ObjectCreation> {
        private final ObjectMapper objectMapper;

        public ObjectsDe() {
            this.objectMapper = QuestionObjectMapper.build();
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            System.out.println("configure ObjectDe");
        }

        @Override
        public ObjectCreation deserialize(String topic, byte[] data) {
            try {
                return objectMapper.readValue(data, ObjectCreation.class);
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
