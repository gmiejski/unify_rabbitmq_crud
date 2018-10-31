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
    public final static String QUESTION_UPDATED_QUEUE = "QuestionUpdated";
    public final static String QUESTION_DELETED_QUEUE = "QuestionDeleted";

    private final ConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;
    private String queueName;
    private Connection connection;
    private Channel channel;

    public RabbitMQJsonProducer(ConnectionFactory connectionFactory, ObjectMapper objectMapper, String queueName) {
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    public static RabbitMQJsonProducer localRabbitMQProducer(ObjectMapper objectMapper, String queueName) { // TODO move queue name to accept param?
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        return new RabbitMQJsonProducer(factory, objectMapper, queueName);
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

            // TODO when you set priority to 0, everything fucks up
            channel.basicPublish("", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setup() {
        try {
            channel.queueDeclare(this.queueName, false, false, false, null);
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
