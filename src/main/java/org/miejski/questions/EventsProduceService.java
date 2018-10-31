package org.miejski.questions;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.miejski.questions.events.QuestionCreated;
import org.miejski.simple.objects.serdes.GenericJSONSer;

import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.Random;

public class EventsProduceService {

    private final Properties props;
    private final String kafkaTopic;
    private final int maxObjectID;
    private final int eventsPerSecond;

    public EventsProduceService(String kafkaTopic, int maxObjectID, int eventsPerSecond) {
        this.kafkaTopic = kafkaTopic;
        this.maxObjectID = maxObjectID;
        this.eventsPerSecond = eventsPerSecond;
        this.props = new Properties();
        this.props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
    }

    public void createSingle(int x) {
        GenericJSONSer<QuestionCreated> serializer = new GenericJSONSer<>();
        final KafkaProducer<Integer, QuestionCreated> producer = new KafkaProducer<>(props, Serdes.Integer().serializer(), serializer);
        try {

            for (int i = 0; i < x; i++) {
                int questionID = new Random().nextInt(maxObjectID);
                ProducerRecord<Integer, QuestionCreated> producerRecord = new ProducerRecord<>(kafkaTopic, questionID, new QuestionCreated("us", questionID, RandomStringUtils.randomAscii(10), ZonedDateTime.now()));
                producer.send(producerRecord);
            }
        } finally {
            serializer.close();
            producer.close();
        }
    }

    public static void main(String[] args) {
        new EventsProduceService(QuestionsTopology.CREATE_TOPIC, 10, 100).createSingle(100);
    }
}
