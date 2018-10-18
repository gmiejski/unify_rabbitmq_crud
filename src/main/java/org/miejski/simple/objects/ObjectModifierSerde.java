package org.miejski.simple.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.simple.objects.events.ObjectModifier;

import java.io.IOException;
import java.util.Map;

public class ObjectModifierSerde {

    static Serde<ObjectModifier> serde() {
        return Serdes.serdeFrom(new ObjectModifierSer(), new ObjectsModifierDe());
    }

    static class ObjectModifierSer implements org.apache.kafka.common.serialization.Serializer<ObjectModifier> {
        private final ObjectMapper objectMapper;

        public ObjectModifierSer() {
            this.objectMapper = QuestionObjectMapper.build();
        }

        @Override
        public void configure(Map configs, boolean isKey) {
            System.out.println("Configure ObjectModifierSer");
        }

        @Override
        public byte[] serialize(String topic, ObjectModifier obj) {
            try {
                return objectMapper.writeValueAsBytes(obj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new byte[0];
        }

        @Override
        public void close() {
            System.out.println("closing ObjectModifierSer");
        }
    }

    static class ObjectsModifierDe implements org.apache.kafka.common.serialization.Deserializer<ObjectModifier> {
        private final ObjectMapper objectMapper;

        public ObjectsModifierDe() {
            this.objectMapper = QuestionObjectMapper.build();
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            System.out.println("configure ObjectDe");
        }

        @Override
        public ObjectModifier deserialize(String topic, byte[] data) {
            try {
                return objectMapper.readValue(data, ObjectModifier.class);
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
