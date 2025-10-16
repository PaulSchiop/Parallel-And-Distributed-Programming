import java.util.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ListOperator implements Runnable {
    private final ConcurrentDoublyLinkedList<Integer> list;
    private final int nrOperations;
    private final Random random = new Random();
    private final List<Node<Integer>> knownNodes = new ArrayList<>();
    private final AtomicInteger opCounter;

    public ListOperator(ConcurrentDoublyLinkedList<Integer> list, int nrOperations, AtomicInteger opCounter) {
        this.list = list;
        this.nrOperations = nrOperations;
        this.opCounter = opCounter;
        knownNodes.add(list.getFirst());
    }

    @Override
    public void run() {
        for (int i = 0; i < nrOperations; i++) {
            // Pick a random known node to operate on
            Node<Integer> targetNode = knownNodes.get(random.nextInt(knownNodes.size()));

            // Randomly choose an operation
            int operation = random.nextInt(4);
            Node<Integer> newNode = null;

            try {
                switch (operation) {
                    case 0: // insert-after
                        newNode = list.insertAfter(targetNode, opCounter.getAndIncrement());
                        break;
                    case 1: // insert-before
                        newNode = list.insertBefore(targetNode, opCounter.getAndIncrement());
                        break;
                    case 2: // move-next
                        Node<Integer> next = list.moveNext(targetNode);
                        if (next.value != null) { // Don't add the tail sentinel
                            knownNodes.add(next);
                        }
                        break;
                    case 3: // move-prev
                        Node<Integer> prev = list.movePrev(targetNode);
                        if (prev.value != null) { // Don't add the head sentinel
                            knownNodes.add(prev);
                        }
                        break;
                }

                if (newNode != null) {
                    knownNodes.add(newNode);
                }

            } catch (IllegalArgumentException e) {
                // Ignore attempts to insert before head or after tail, which is expected in a concurrent scenario.
            }

            if (knownNodes.size() > 100) {
                knownNodes.remove(0);
            }
        }
    }
}
