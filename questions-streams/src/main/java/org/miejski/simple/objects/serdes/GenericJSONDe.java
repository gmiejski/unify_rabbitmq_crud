package org.miejski.simple.objects.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.miejski.Modifier;
import org.miejski.questions.QuestionObjectMapper;

import java.io.IOException;
import java.util.Map;

public class GenericJSONDe<T extends Modifier<T>> implements org.apache.kafka.common.serialization.Deserializer<T> {
    private final ObjectMapper objectMapper;
    private Class c;

    public GenericJSONDe(Class<T> c) {
        this.c = c;
        this.objectMapper = QuestionObjectMapper.build();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        System.out.println("configure ObjectDe");
    }

    @Override
    public  T deserialize(String topic, byte[] data) {
        try {
            return (T)objectMapper.readValue(data, this.c);
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
