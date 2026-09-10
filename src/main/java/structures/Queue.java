package structures;

/**
 * A generic First-In-First-Out (FIFO) queue implementation.
 *
 * @param <T> the type of elements stored in the queue
 */
public class Queue<T> {

    private Node<T> front;
    private Node<T> rear;
    private int size;

    /**
     * Creates an empty queue.
     *
     * @complexity O(1)
     */
    public Queue() {
        front = null;
        rear = null;
        size = 0;
    }

    /**
     * Adds an element to the rear of the queue.
     *
     * @param data the element to add
     * @complexity O(1)
     */
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);

        if (rear == null) {
            front = newNode;
            rear = newNode;
        } else {
            rear.setNext(newNode);
            rear = newNode;
        }

        size++;
    }

    /**
     * Removes and returns the element at the front of the queue.
     *
     * @return the removed element
     * @throws IllegalStateException if the queue is empty
     * @complexity O(1)
     */
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }

        T data = front.getData();
        front = front.getNext();
        size--;

        if (front == null) {
            rear = null;
        }

        return data;
    }

    /**
     * Returns the element at the front without removing it.
     *
     * @return the first element in the queue
     * @throws IllegalStateException if the queue is empty
     * @complexity O(1)
     */
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }

        return front.getData();
    }

    /**
     * Checks whether the queue contains no elements.
     *
     * @return true if the queue is empty, otherwise false
     * @complexity O(1)
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of elements currently stored in the queue.
     *
     * @return the number of elements
     * @complexity O(1)
     */
    public int size() {
        return size;
    }

    /**
     * Removes all elements from the queue.
     *
     * @complexity O(1)
     */
    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }
}