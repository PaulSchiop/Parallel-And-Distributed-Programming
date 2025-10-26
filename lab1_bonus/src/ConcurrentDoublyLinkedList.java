public class ConcurrentDoublyLinkedList<T> {
    private final  Node<T> head;
    private final  Node<T> tail;

    public ConcurrentDoublyLinkedList() {
        head = new Node<>(null);
        tail = new Node<>(null);
        head.next = tail;
        tail.prev = head;
    }

    public Node<T> moveNext(Node<T> node){
        return node.next;
    }

    public Node<T> movePrev(Node<T> node){
        return node.prev;
    }

    public Node<T> insertAfter(Node<T> node, T value){
        if (node == tail) {
            throw new IllegalArgumentException("Cannot insert after tail");
        }

        Node<T> newNode = new Node<>(value);

        node.lock.lock();
        try {
            Node<T> nextNode = node.next;
            nextNode.lock.lock();
            try {
                newNode.next = nextNode;
                newNode.prev = node;
                nextNode.prev = newNode;
                node.next = newNode;
            } finally {
                nextNode.lock.unlock();
            }
        } finally {
            node.lock.unlock();
        }
        return newNode;
    }

    public Node<T> insertBefore(Node<T> node, T value){
        if (node == head) {
            throw new IllegalArgumentException("Cannot insert before head");
        }

        Node<T> newNode = new Node<>(value);

        Node<T> prevNode = node.prev;

        prevNode.lock.lock();
        try {
            node.lock.lock();
            if (node.prev != prevNode)
                throw new RuntimeException();
            try {
                newNode.next = node;
                newNode.prev = prevNode;
                prevNode.next = newNode;
                node.prev = newNode;
            } finally {
                prevNode.lock.unlock();
            }
        }
        catch (RuntimeException e) {
            node.lock.unlock();
            prevNode.lock.unlock();
            insertBefore(node, value);
        }

        finally {
            node.lock.unlock();
        }
        return newNode;
    }

    public void printList() {
        StringBuilder sb = new StringBuilder("HEAD -> ");
        Node<T> current = head.next;
        while (current != tail) {
            sb.append("[").append(current.value).append("] -> ");
            current = current.next;
        }
        sb.append("TAIL");
        System.out.println(sb.toString());
    }

    public boolean checkConsistency() {
        Node<T> current = head;
        while (current.next != null) {
            if (current.next.prev != current) {
                System.err.println("Consistency check failed: " + current + ".next.prev is not " + current);
                return false;
            }
            current = current.next;
        }
        return true;
    }

    public Node<T> getFirst() {
        return head.next;
    }
}
