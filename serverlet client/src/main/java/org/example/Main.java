package org.example;

import com.opencsv.CSVWriter;



import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;


public class Main {
    //for serverlet
    private static String url = "http://54.186.13.89:8080/A1-server_war/swipe/leftorright/";

    //private static String url = "http://lulu-303613101.us-west-2.elb.amazonaws.com/A1-server_war/swipe/leftorright/";
    //for springboots2
   // private static String url = "http://localhost:8080/A1_server_war_exploded/swipe/leftorright/";
    private static String FILE_PATH = "C:\\Users\\lmh98\\Desktop\\aws\\";
    private static final int TOTAL_REQ = 200000;

    private static final int MIN_THREAD = 180;
    private static final int MAX_THREAD = 180;


    public static void main(String[] args) throws InterruptedException, IOException {
        for (int numThread = MIN_THREAD; numThread <= MAX_THREAD + 1; numThread += 50){
            CountDownLatch progress = new CountDownLatch(TOTAL_REQ);
            PostRequest req = new PostRequest(TOTAL_REQ, numThread, url, progress);
            long baseTime = System.currentTimeMillis();
            req.run(baseTime);
            long endTime = System.currentTimeMillis();
            int failed = req.getFailed();
            //writeDataLineByLine(req.getStartL(),req.getLatency());
            //printGraphData(req.getStartL(),req.getLatency(), ""+numThread);
            printPart1Result(numThread, failed, endTime, baseTime);
            printPart2Result(req.getLatency(),numThread, failed, endTime, baseTime);
            progress.await();
        }
    }

    private static void printGraphData(List<Integer> startL, List<Integer> latency, String file_name) {
        List<Integer> graph = new ArrayList<>();
        for(int i = 0; i < latency.size(); i++) {
            int time = latency.get(i) + startL.get(i);
            int index = time / 1000 + 1;
            while (index >= graph.size()) {
                graph.add(0);
            }
            graph.set(index, graph.get(index) + 1);
        }
        File file = new File(FILE_PATH + file_name + ".csv");
        try {
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);
            for(int i = 0; i < graph.size(); i++) {
                String data[] = {""+ graph.get(i)};
                writer.writeNext(data);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printPart1Result(int numThread, int failed, long endTime, long baseTime){
        System.out.println("Total Request " + TOTAL_REQ);
        System.out.println("Num Thread " + numThread);
        System.out.println("Part1 Result:");
        System.out.println("number of successful requests sent " + (TOTAL_REQ - failed));
        System.out.println("number of unsuccessful requests " + failed);
        System.out.println("the total run time  " + (endTime - baseTime) + " ms");
        if (numThread == 1) {
            System.out.println("average latency " + (endTime - baseTime) / TOTAL_REQ  + " ms");
            System.out.println("According to Littles theory and target 150 thread");
            System.out.println("The output persecond :" + 150.0 / ((endTime - baseTime) / TOTAL_REQ) * 1000);
        }
        double reqPerSecond = (double) (TOTAL_REQ - failed) / (endTime - baseTime) * 1000;
        System.out.println("the total throughput in requests per second  " + reqPerSecond);
    }

    private static void printPart2Result(List<Integer> latency, int numThread, int failed, long endTime, long baseTime){

        Collections.sort(latency);
        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = 0;
        for(int i = 0; i < latency.size(); i++) {
            int t = latency.get(i);
            if (t == 0){
                continue;
            }
            sum += t;
            min = Math.min(min, t);
            max = Math.max(max, t);
        }
         int index = (int) Math.ceil(0.99 * latency.size());
        int p99 = latency.get(index-1);
        double mean = (double) sum / latency.size();
        double medium = (double) (latency.get(latency.size()/2) + latency.get(latency.size()/2 - 1)) / 2.0;
        System.out.println("--------------------------------------------------");
        System.out.println("Num Thread " + numThread);
        System.out.println("Mean latency " + mean + " || Medium latency" + medium);
        System.out.println("Min latency " + min + " || Max latency " + max);
        System.out.println("99 percentile " + p99);
        double reqPerSecond = (double) (TOTAL_REQ - failed) / (endTime - baseTime) * 1000;
        System.out.println("the total throughput in requests per second  " + reqPerSecond);
    }


    private static void writeDataLineByLine(List<Integer> startL, List<Integer> latency) {
        // first create file object for file placed at location
        // specified by filepath
        File file = new File(FILE_PATH);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);
            for(int i = 0; i < latency.size(); i++) {
                String suc = "SUCCESS";
                if (startL.get(i) == -1) {
                    suc = "failed";
                }
                String data[] = {startL.get(i).toString(), latency.get(i).toString(), "POST", suc};
                writer.writeNext(data);
            }
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}