package org.miejski.questions.source;

import org.miejski.questions.QuestionObjectMapper;
import org.miejski.questions.source.create.SourceQuestionCreated;
import org.miejski.questions.source.create.SourceQuestionCreatedPayload;
import org.miejski.questions.source.rabbitmq.RabbitMQJsonProducer;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class GeneratingBusProducer<T> implements BusProducer {

    private final SourceEventProducer<T> producer;
    private final Consumer<Object> consumer;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private Thread runningThread;
    private CompletableFuture<Boolean> completed;

    public GeneratingBusProducer(SourceEventProducer<T> producer, Consumer<Object> consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    public CompletableFuture<Boolean> start() {
        completed = new CompletableFuture<>(); // TODO - make thread safe :D

        runningThread = new Thread(() -> {
            try {
                while (isRunning.get()) {
                    consumer.accept(this.producer.create());
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            completed.complete(true);
        });
        runningThread.start();
        return completed;
    }

    public void stop() {
        this.isRunning.set(false);
        while (runningThread.isAlive()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        completed.complete(true);
    }

    public static void main(String[] args) {
        RabbitMQJsonProducer consumer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build(), RabbitMQJsonProducer.QUESTION_CREATED_QUEUE);
        consumer.setup();
        GeneratingBusProducer<SourceQuestionCreated> generatingBusProducer = new GeneratingBusProducer<>(() -> new SourceQuestionCreated("us", new SourceQuestionCreatedPayload(1, "sad", ZonedDateTime.now())), consumer);
        generatingBusProducer.start();
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
