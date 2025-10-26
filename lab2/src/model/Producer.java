package model;

import java.util.ArrayList;
import java.util.List;

public class Producer implements Runnable{
    private final SharedQueue queue;
    private List<Integer> vector1 = new ArrayList<>();
    private List<Integer> vector2 = new ArrayList<>();

    public Producer(SharedQueue queue, List<Integer> vector1, List<Integer> vector2) {
        this.queue = queue;
        this.vector1 = vector1;
        this.vector2 = vector2;
    }

    public Producer(SharedQueue queue) {
        this.queue = queue;
    }

    public List<Integer> getVector2() {
        return vector2;
    }

    public void setVector2(List<Integer> vector2) {
        this.vector2 = vector2;
    }

    public List<Integer> getVector1() {
        return vector1;
    }

    public void setVector1(List<Integer> vector1) {
        this.vector1 = vector1;
    }

    @Override
    public void run() {
        try{
            for (int i = 0; i < vector1.size(); i++) {
                int element1 = vector1.get(i);
                int element2 = vector2.get(i);
                int product = element1 * element2;
                queue.put(product);
            }
        } catch (InterruptedException e) {
            System.out.println("Producer interrupted");
            Thread.currentThread().interrupt();
        }
        finally {
            queue.close();
        }
    }
}
