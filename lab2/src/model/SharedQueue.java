package model;

import java.util.LinkedList;
import java.util.Queue;

//methods: put, take, isEmpty, isFull
public class SharedQueue {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int maxSize;
    private boolean closed = false;

    public SharedQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    public synchronized void put(int product) throws InterruptedException{
        while (queue.size() == maxSize) {
            wait();
        }

        queue.add(product);
        notifyAll();
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    public synchronized Integer take() throws InterruptedException{
        while (queue.isEmpty() && !closed) {
            wait();
        }

        if (queue.isEmpty() && closed) {
            return null;
        }

        Integer product = queue.poll();
        notifyAll();
        return product;
    }
}
