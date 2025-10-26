// src/Main.java
import model.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final int VECTOR_SIZE = 10000000;

        System.out.println("Running tests for VECTOR_SIZE = " + VECTOR_SIZE);
        System.out.println("(Using synchronized, wait, and notifyAll)");
        System.out.println("-------------------------------------------------");

        runTest(10, VECTOR_SIZE);
        runTest(100, VECTOR_SIZE);
        runTest(1000, VECTOR_SIZE);
        runTest(10000, VECTOR_SIZE);

        System.out.println("-------------------------------------------------");
        System.out.println("Test complete.");
    }

    private static void runTest(int queueSize, int vectorSize) {
        List<Integer> v1 = new ArrayList<>(vectorSize);
        List<Integer> v2 = new ArrayList<>(vectorSize);
        int expectedSum = 0;

        for (int i = 0; i < vectorSize; i++) {
            v1.add(1);
            v2.add(2);
            expectedSum += (1 * 2);
        }

        SharedQueue queue = new SharedQueue(queueSize);
        Producer producerRunnable = new Producer(queue, v1, v2);
        Consumer consumerRunnable = new Consumer(queue);

        Thread producerThread = new Thread(producerRunnable);
        Thread consumerThread = new Thread(consumerRunnable);

        long startTime = System.nanoTime();

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        double durationMillis = (endTime - startTime) / 1_000_000.0;

        double computedSum = consumerRunnable.getResult();

        System.out.printf("Queue Size: %-7d | Time: %8.2f ms | Result: %.1f\n",
                queueSize, durationMillis, computedSum);
    }
}
