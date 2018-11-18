package org.miejski.simple.objects.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.miejski.questions.QuestionObjectMapper;

import java.util.Map;

public class GenericJSONSer<T> implements org.apache.kafka.common.serialization.Serializer<T> {
    private final ObjectMapper objectMapper;

    public GenericJSONSer() {
        this.objectMapper = QuestionObjectMapper.build();
    }


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        System.out.println("Configure JSONSerde");
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public void close() {
        System.out.println("closing JSONSerde");
    }
}