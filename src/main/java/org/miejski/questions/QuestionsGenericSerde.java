package org.miejski.questions;

import org.miejski.questions.events.QuestionCreated;
import org.miejski.simple.objects.serdes.GenericSerde;

import java.util.HashMap;
import java.util.Map;

public class QuestionsGenericSerde { // TODO replace with JSONSerde
    public static GenericSerde build() {
        Map<String, Class> serializers = new HashMap<>();
        serializers.put(QuestionCreated.class.getSimpleName(), QuestionCreated.class);
        return new GenericSerde(serializers);
    }
}


