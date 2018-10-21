package org.miejski.questions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.kstream.Aggregator;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.simple.objects.serdes.GenericField;

import java.io.IOException;
import java.util.HashMap;

public class QuestionsAggregator implements Aggregator<String, GenericField, QuestionState> {

    private final ObjectMapper objectMapper;
    private HashMap<String, Class> serializers;

    public QuestionsAggregator(HashMap<String, Class> serializers) {
        this.serializers = serializers;
        this.objectMapper = QuestionObjectMapper.build();
    }

    @Override
    public QuestionState apply(String key, GenericField value, QuestionState aggregate) {
        if (!serializers.containsKey(value.getObjectName())) {
            throw new RuntimeException("Cannot desedialize generic value with object of type: " + value.getObjectName());
        }

        try {
            Object o = this.objectMapper.readValue(value.getData(), serializers.get(value.getObjectName()));
            QuestionModifier modifier = (QuestionModifier) o;
            return modifier.doSomething(aggregate);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aggregate;

    }
}
