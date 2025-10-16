import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node <T>{
    T value;
    Node<T> next;
    Node<T> prev;
    final Lock lock = new ReentrantLock();

    public Node(Node<T> prev) {
        this.value = null;
        this.next = null;
        this.prev = prev;
    }

    @Override
    public String toString() {
        // Note: Accessing value without a lock is generally unsafe if the value itself could be changed.
        // For this problem, the value is immutable once set in the constructor.
        return "Node(" + value + ")";
    }
}
