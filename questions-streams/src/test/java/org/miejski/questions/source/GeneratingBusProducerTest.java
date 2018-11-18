package org.miejski.questions.source;

import org.junit.jupiter.api.Test;
import org.miejski.questions.source.create.SourceQuestionCreated;
import org.miejski.questions.source.create.SourceQuestionCreatedPayload;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTimeout;

class GeneratingBusProducerTest {


    private final int questionID = 1;
    private final String content = "content";
    private final ZonedDateTime now = ZonedDateTime.now();
    private final SourceQuestionCreated us = new SourceQuestionCreated("us", new SourceQuestionCreatedPayload(questionID, content, now));

    class LastSeenCache implements Consumer<Object> {
        private AtomicReference<Object> obj = new AtomicReference<>();

        @Override
        public void accept(Object o) {
            obj.set(o);
        }

        public Object getLast() {
            return obj.get();
        }
    }

    @Test
    void eventsAreProduced() throws InterruptedException, ExecutionException, TimeoutException {
        LastSeenCache consumer = new LastSeenCache();
        SourceEventProducer<SourceQuestionCreated> questionCreatedProducer = () -> us;
        GeneratingBusProducer<SourceQuestionCreated> producer = new GeneratingBusProducer<>(questionCreatedProducer, consumer);
        Future<Boolean> finished = producer.start();

        assertTimeout(Duration.ofSeconds(1), () -> {
            while (true) {
                if (consumer.getLast() != null) {
                    return;
                }
                Thread.sleep(20);
            }
        });

        producer.stop();
        finished.get(1, TimeUnit.SECONDS);
    }
}