package org.miejski.questions.source.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.Closeable;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitMQJsonProducer implements Consumer<Object>, Closeable {

    public final static String QUESTION_CREATED_QUEUE = "QuestionCreated";
    private final ConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;
    private Connection connection;
    private Channel channel;

    public RabbitMQJsonProducer(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
    }

    public static RabbitMQJsonProducer localRabbitMQProducer(ObjectMapper objectMapper) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        return new RabbitMQJsonProducer(factory, objectMapper);
    }

    public void connect() {
        try {
            this.connection = this.connectionFactory.newConnection();
            this.channel = this.connection.createChannel();
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept(Object o) {
        try {
            String message = this.objectMapper.writeValueAsString(o);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().deliveryMode(1).build();
            Map<String, Object> a = new HashMap<>();
            a.put("asdasddasads", "adssdsadads");

            AMQP.BasicProperties.Builder headers = MessageProperties.MINIMAL_PERSISTENT_BASIC.builder().headers(a);
            headers = headers.appId("dusssssspa2");
            headers = headers.clusterId("");

            AMQP.BasicProperties b = new AMQP.BasicProperties
                    ("text/plain"
                            , null
                            , a
                            , 1
                            , 0
                            , null
                            , null
                            , null
                            , null
                            , new Date()
                            , null
                            , "guest"
                            , "app id1"
                            , "cluster 12"
                    );

            channel.basicPublish("", QUESTION_CREATED_QUEUE, b, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setup() {
        try {
            channel.queueDeclare(QUESTION_CREATED_QUEUE, false, false, false, null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        Optional.ofNullable(this.channel).ifPresent(x -> {
            try {
                x.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        });

        Optional.ofNullable(this.connection).ifPresent(x -> {
            try {
                x.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}