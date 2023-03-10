package org.example;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class PostRequest {
    private int TOTAL_REQ;
    private int NUM_THREAD;
    private int failed = 0;
    private String URL;
    private List<Integer> startL = new ArrayList<>();
    private List<Integer> latency = new ArrayList<>();
    private CountDownLatch completed;
    private CountDownLatch progress;
    private CloseableHttpClient client;

    public PostRequest(int TOTAL_REQ, int NUM_THREAD, String URL, CountDownLatch progress) {
        this.TOTAL_REQ = TOTAL_REQ;
        this.NUM_THREAD = NUM_THREAD;
        this.completed = new CountDownLatch(NUM_THREAD);
        this.progress = progress;
        this.URL = URL;
    }

    public List<Integer> getStartL(){
        return this.startL;
    }

    public List<Integer> getLatency(){
        return this.latency;
    }

    public int getFailed(){
        return this.failed;
    }
    public void run(long baseTime) throws InterruptedException, IOException {
        PoolingHttpClientConnectionManager connManager
                = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(NUM_THREAD);
        connManager.setMaxTotal(NUM_THREAD);
        client = HttpClients.custom()
                .setConnectionManager(connManager)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(5, false))
                .build();
        Runnable test = new Runnable() {
            @Override
            public void run() {
                int runs = Integer.valueOf(Thread.currentThread().getName());
                for(int i = 0; i < runs; i++){
                    long startTime = System.currentTimeMillis();
                    final HttpPost httpPost = new HttpPost(URL);
                    try {
                        StringEntity entity = new StringEntity(creatJson());
                        httpPost.setEntity(entity);
                        CloseableHttpResponse response = client.execute(httpPost);
                        int code = response.getStatusLine().getStatusCode();
                        //System.out.println(EntityUtils.toString(response.getEntity(), "UTF-8"));
                        httpPost.releaseConnection();
                        if (code == 201) {
                            long endTime = System.currentTimeMillis();
                            saveResult((int)(startTime - baseTime), (int)(endTime - startTime));
                            System.out.println(progress.getCount());
                        } else {
                            failed += 1;
                            saveResult(-1, -1);
                            System.out.println("one request failed");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                completed.countDown();
            }

        };
        int i = 0;
        int runPerThread = TOTAL_REQ/NUM_THREAD;
        int remaining = TOTAL_REQ % NUM_THREAD;

        for (int j = 0; j < NUM_THREAD; j++) {
            int runs = runPerThread;
            if (j < remaining) {
                runs += 1;
            }
            //System.out.println(runs);
            new Thread(test, ""+runs).start();
        }
        completed.await();
        client.close();
    }


    synchronized private void saveResult(int start, int lag) {
        startL.add(start);
        latency.add(lag);
        progress.countDown();
    }

    private static String creatJson(){
        JSONObject json = new JSONObject();
        String swipe = "Left";
        if (randInt(0, 1) == 0){
            swipe = "Right";
        }
        json.put("swipe", swipe);
        json.put("swiper", randInt(1, 5000));
        json.put("swipee", randInt(1, 1000000));
        String comment = "";
        for (int i = 0; i < 256; i++){
            char t = (char)((int)randInt(33,126));
            comment += t;
        }
        json.put("comment", comment);
        return json.toString();
    }
    private static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
}
