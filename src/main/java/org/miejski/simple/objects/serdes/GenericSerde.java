package org.miejski.simple.objects.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.simple.objects.events.ObjectCreation;
import org.miejski.simple.objects.events.ObjectDelete;
import org.miejski.simple.objects.events.ObjectModifier;
import org.miejski.simple.objects.events.ObjectUpdate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TODO here - do generic SERDE and then aggregateByKey and reduce with this serializer?????
public class GenericSerde {

    public static GenericField toGenericField(ObjectModifier objectModifier) throws JsonProcessingException {
        HashMap<String, Class> serializers = new HashMap<>();
        serializers.put(ObjectCreation.class.getSimpleName(), ObjectCreation.class);
        serializers.put(ObjectUpdate.class.getSimpleName(), ObjectUpdate.class);
        serializers.put(ObjectDelete.class.getSimpleName(), ObjectDelete.class);

        String serializer = objectModifier.getClass().getSimpleName();

        if (!serializers.containsKey(serializer)) {
            throw new RuntimeException("Unknown serializer: " + serializer);
        }
        return new GenericField(serializer, QuestionObjectMapper.build().writeValueAsString(objectModifier));
    }


    public static Serde<GenericField> serde() {

        HashMap<String, Class> serializers = new HashMap<>();
        serializers.put(ObjectCreation.class.getSimpleName(), ObjectCreation.class);
        serializers.put(ObjectUpdate.class.getSimpleName(), ObjectUpdate.class);
        serializers.put(ObjectDelete.class.getSimpleName(), ObjectDelete.class);

        return Serdes.serdeFrom(new ObjectGenericSer(serializers), new GenericSerde.ObjectGenericDe());
    }

    static class ObjectGenericSer implements org.apache.kafka.common.serialization.Serializer<GenericField> {
        private final ObjectMapper objectMapper;

        public ObjectGenericSer(Map<String, Class> classes) {
            this.objectMapper = QuestionObjectMapper.build();
        }

        @Override
        public void configure(Map configs, boolean isKey) {
            System.out.println("Configure ObjectGenericSer");
        }

        @Override
        public byte[] serialize(String topic, GenericField obj) {
            try {
                return objectMapper.writeValueAsBytes(obj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new byte[0];
        }

        @Override
        public void close() {
            System.out.println("closing ObjectGenericSer");
        }
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
