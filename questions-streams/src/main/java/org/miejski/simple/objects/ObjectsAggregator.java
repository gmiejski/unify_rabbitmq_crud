package org.miejski.simple.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.kstream.Aggregator;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.simple.objects.events.ObjectModifier;
import org.miejski.simple.objects.serdes.GenericField;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;

public class ObjectsAggregator implements Aggregator<String, GenericField, ObjectState> {

    private final ObjectMapper objectMapper;
    private HashMap<String, Class> serializers;

    public ObjectsAggregator(HashMap<String, Class> serializers) {
        this.serializers = serializers;
        this.objectMapper = QuestionObjectMapper.build();
    }

    @Override
    public ObjectState apply(String key, GenericField value, ObjectState aggregate) {
        try {
            Object o = this.objectMapper.readValue(value.getData(), serializers.get(value.getObjectName()));
            ObjectModifier modifier = (ObjectModifier) o;
            return modifier.doSomething(aggregate);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aggregate;
    }
}
