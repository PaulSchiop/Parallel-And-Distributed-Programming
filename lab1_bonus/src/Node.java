import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node <T>{
    T value;
    Node<T> next;
    Node<T> prev;
    final Lock lock = new ReentrantLock();

    public Node(T value) {
        this.value = value;
        this.next = null;
        this.prev = null;
    }

    @Override
    public String toString() {
        // Note: Accessing value without a lock is generally unsafe if the value itself could be changed.
        // For this problem, the value is immutable once set in the constructor.
        return "Node(" + value + ")";
    }
}
