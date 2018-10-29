package org.miejski.questions.source;

import org.miejski.questions.QuestionObjectMapper;
import org.miejski.questions.source.QuestionCreated;
import org.miejski.questions.source.rabbitmq.RabbitMQJsonProducer;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class StartRabbitMQProducer<T> {

    private final String market;
    private final SourceEventProducer<T> producer;
    private final Consumer<Object> consumer;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private Thread runningThread;
    private CompletableFuture<Boolean> completed;

    public StartRabbitMQProducer(String market, SourceEventProducer<T> producer, Consumer<Object> consumer) {
        this.market = market;
        this.producer = producer;
        this.consumer = consumer;
    }

    public CompletableFuture<Boolean> start() {
        runningThread = new Thread(() -> {
            try {
                while (isRunning.get()) {
                    consumer.accept(this.producer.create());
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        runningThread.start();
        completed = new CompletableFuture<>();
        return completed;
    }

    public void stop() {
        this.isRunning.set(false);
        while(runningThread.isAlive()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        completed.complete(true);
    }

    public static void main(String[] args) {
        RabbitMQJsonProducer consumer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build());
        consumer.connect();
        consumer.setup();
        StartRabbitMQProducer<QuestionCreated> startRabbitMQProducer = new StartRabbitMQProducer<>("us", () -> new QuestionCreated("us", new QuestionCreatedPayload(1, "sad", ZonedDateTime.now())), consumer);
        startRabbitMQProducer.start();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
