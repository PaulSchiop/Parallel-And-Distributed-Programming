import java.util.concurrent.atomic.AtomicInteger;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 10;
        int opsPerThread = 1;

        System.out.println("Starting concurrent linked list test...");
        System.out.printf("Threads: %d, Operations per thread: %d%n", numThreads, opsPerThread);

        ConcurrentDoublyLinkedList<Integer> list = new ConcurrentDoublyLinkedList<>();
        AtomicInteger valueCounter = new AtomicInteger(1);

        // Start with an initial node
        list.insertAfter(list.movePrev(list.getFirst()), 0);

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new ListOperator(list, opsPerThread, valueCounter));
        }

        long startTime = System.currentTimeMillis();
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        long endTime = System.currentTimeMillis();

        System.out.println("\nAll threads finished.");
        System.out.println("Time taken: " + (endTime - startTime) + " ms");

        list.printList();

        System.out.println("\nRunning final consistency check...");
        if (list.checkConsistency()) {
            System.out.println("Consistency check PASSED!");
        } else {
            System.err.println("Consistency check FAILED.");
        }
    }
}