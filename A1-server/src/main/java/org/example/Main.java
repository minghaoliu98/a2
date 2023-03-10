package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class Main {


    private final static String QUEUE_NAME = "Queue1";
    private final static String RABBITMQ_URL = "54.186.65.165";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_URL);
        factory.setVirtualHost("mark");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}