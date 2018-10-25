package org.miejski.questions.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.simple.objects.serdes.GenericJSONSer;

import java.io.IOException;
import java.util.Map;

public class QuestionStateSerde {
    public static Serde<QuestionState> questionStateSerde() {
        return Serdes.serdeFrom(new GenericJSONSer<>(), new QuestionStateDe());
    }
}

class QuestionStateDe implements org.apache.kafka.common.serialization.Deserializer<QuestionState> {
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