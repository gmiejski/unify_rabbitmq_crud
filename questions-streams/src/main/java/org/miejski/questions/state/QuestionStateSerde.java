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
