package structures;

/**
 * Represents a node used by the Queue data structure.
 *
 * @param <T> the type of data stored in the node
 */
public class Node<T> {

    private T data;
    private Node<T> next;

    /**
     * Creates a node containing the specified data.
     *
     * @param data the data to store in the node
     * @complexity O(1)
     */
    public Node(T data) {
        this.data = data;
        this.next = null;
    }

    /**
     * Returns the data stored in this node.
     *
     * @return the stored data
     * @complexity O(1)
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the next node.
     *
     * @return the next node, or null if this is the last node
     * @complexity O(1)
     */
    public Node<T> getNext() {
        return next;
    }

    /**
     * Sets the next node.
     *
     * @param next the node to link after this node
     * @complexity O(1)
     */
    public void setNext(Node<T> next) {
        this.next = next;
    }
}