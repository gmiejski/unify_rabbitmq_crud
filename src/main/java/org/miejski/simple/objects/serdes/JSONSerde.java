package org.miejski.simple.objects.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.miejski.questions.QuestionObjectMapper;
import org.miejski.simple.objects.ObjectState;

import java.io.IOException;
import java.util.Map;


public class JSONSerde {

    public static Serde<ObjectState> serde() {
        return Serdes.serdeFrom(new JSONSer<>(), new JSONDe());
    }
}

class JSONSer<T> implements org.apache.kafka.common.serialization.Serializer<T> {
    private final ObjectMapper objectMapper;

    public JSONSer() {
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


class JSONDe implements org.apache.kafka.common.serialization.Deserializer<ObjectState> {
    private final ObjectMapper objectMapper;

    public JSONDe() {
        this.objectMapper = QuestionObjectMapper.build();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        System.out.println("configure ObjectDe");
    }

    @Override
    public ObjectState deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, ObjectState.class);
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

