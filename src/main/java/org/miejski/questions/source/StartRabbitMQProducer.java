package org.miejski.questions.source;

import org.miejski.questions.QuestionObjectMapper;
import org.miejski.questions.source.rabbitmq.RabbitMQJsonProducer;

import java.util.function.Consumer;

public class StartRabbitMQProducer {

    public void start(String market, Consumer<Object> consumer) {
        new Thread(() -> {
            RandomQuestionIDProvider randomQuestionIDProvider = new RandomQuestionIDProvider(10000);

            QuestionCreateProducer createProducer = new QuestionCreateProducer(market, randomQuestionIDProvider);
            try {
                while(true) {
                    consumer.accept(createProducer.create());
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        RabbitMQJsonProducer consumer = RabbitMQJsonProducer.localRabbitMQProducer(QuestionObjectMapper.build());
        consumer.connect();
        consumer.setup();
        new StartRabbitMQProducer().start("us", consumer);
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
