package model;

import java.util.ArrayList;
import java.util.List;

public class Consumer implements Runnable{
    private final SharedQueue queue;
    private int result = 0;

    public Consumer(SharedQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            while (true) {
                Integer product = queue.take();
                if (product == null) {
                    break;
                }
                result += product;
            }
        } catch (InterruptedException e) {
            System.out.println("Consumer interrupted");
            Thread.currentThread().interrupt();
        }
    }

    public int getResult() {
        return result;
    }
}
