package org.miejski.questions.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.miejski.questions.QuestionObjectMapper;

import java.io.IOException;
import java.util.Map;

public class QuestionStateDe implements org.apache.kafka.common.serialization.Deserializer<QuestionState> {
    private final ObjectMapper objectMapper;

    public QuestionStateDe() {
        this.objectMapper = QuestionObjectMapper.build();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        System.out.println("configure ObjectDe");
    }

    @Override
    public QuestionState deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, QuestionState.class);
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