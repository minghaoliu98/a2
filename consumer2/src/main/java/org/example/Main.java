package org.example;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Main {
    private static ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Integer>> like = new ConcurrentHashMap<>();
    private final static String QUEUE_NAME = "Queue2";
    private static final int NUM_THREADS = 100;
    private static Gson gson = new Gson();

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        String RABBITMQ_URL = "34.221.204.214";
        factory.setHost(RABBITMQ_URL);
        factory.setVirtualHost("mark");
        Connection connection = factory.newConnection();
        Runnable runnable = () -> {
            try {
                Channel channel = connection.createChannel();
                channel.basicQos(1);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    saveData(gson.fromJson(message, JsonObject.class));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                };
                channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        for (int i = 0; i < NUM_THREADS; i++) {
            new Thread(runnable).start();
        }
        System.out.println("it work");
    }
    public static void saveData(JsonObject json) {
        String swipe = json.get("swipe").getAsString();
        int swiper = json.get("swiper").getAsInt();
        int swipee = json.get("swipee").getAsInt();
        System.out.println(swipee);
        if (!like.containsKey(swipee)) {
            like.put(swipee, new ConcurrentLinkedQueue<>());
        }
        if (swipe.equals("Right")) {
            while (like.get(swipee).size() >= 100) {
                like.get(swipee).poll();
            }
            like.get(swipee).add(swiper);
        }
    }
}
