package org.miejski.questions;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.miejski.questions.source.create.SourceQuestionCreateProducer;
import org.miejski.questions.source.create.SourceQuestionCreated;
import org.miejski.questions.source.RandomQuestionIDProvider;
import org.miejski.questions.source.GeneratingBusProducer;
import org.miejski.questions.source.rabbitmq.RabbitMQJsonProducer;
import org.miejski.questions.state.QuestionState;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class QuestionStateRunner {


    private final static String market = "us";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "question-states");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        final Topology topology = new QuestionsStateTopology().buildTopology();

        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

//        List<GeneratingBusProducer> producers = buildProducers(new RandomQuestionIDProvider(10000));
//        List<Future> allRunning = producers.stream().map(GeneratingBusProducer::start).collect(Collectors.toList());

        printAllKeys(streams).start();
        addShutdownHook(streams, latch);

        try {
            streams.start();
            latch.await();
//            producers.forEach(x -> x.stop());
//            allRunning.forEach(x -> {
//                try {
//                    x.get(5, TimeUnit.SECONDS);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                } catch (TimeoutException e) {
//                    e.printStackTrace();
//                }
//            });
        } catch (Throwable e) {
            System.exit(1);
        }
    }

    private static List<GeneratingBusProducer> buildProducers(RandomQuestionIDProvider provider) {
        SourceQuestionCreateProducer createProducer = new SourceQuestionCreateProducer(market, provider);
        GeneratingBusProducer<SourceQuestionCreated> generatingBusProducer = new GeneratingBusProducer<>(market, createProducer, RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_CREATED_QUEUE));
        return Arrays.asList(generatingBusProducer);
    }

    public static void addShutdownHook(KafkaStreams streams, CountDownLatch latch) {
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });
    }

    private static Thread printAllKeys(KafkaStreams streams) {
        return new Thread(() -> {

            while (!streams.state().equals(KafkaStreams.State.RUNNING)) {
                System.out.println("waiting for streams to start");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Started - can read local state now.");

            while (true) {
                ReadOnlyKeyValueStore<Integer, QuestionState> keyValueStore = // TODO czemu Integer a nie String
                        streams.store(QuestionsStateTopology.QUESTIONS_STORE_NAME, QueryableStoreTypes.keyValueStore());

                KeyValueIterator<Integer, QuestionState> range = keyValueStore.all();
                while (range.hasNext()) {
                    KeyValue<Integer, QuestionState> next = range.next();
                    System.out.println(next.value.toString());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
