package org.miejski.questions.source;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class StartRabbitMQProducerTest {

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
    void eventsAreProduced() {
        LastSeenCache consumer = new LastSeenCache();

        new StartRabbitMQProducer().start("us", consumer);

        assertTimeout(Duration.ofSeconds(1), () -> {
            while (true) {
                if (consumer.getLast() != null ) {
                    return;
                }
                Thread.sleep(20);
            }
        });
    }
}