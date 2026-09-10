import org.junit.jupiter.api.Test;
import structures.Queue;

import static org.junit.jupiter.api.Assertions.*;

public class QueueTest {

    @Test
    void testQueueStartsEmpty() {
        Queue<Integer> queue = new Queue<>();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void testEnqueue() {
        Queue<Integer> queue = new Queue<>();

        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);

        assertEquals(3, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    void testPeek() {
        Queue<String> queue = new Queue<>();

        queue.enqueue("A");
        queue.enqueue("B");

        assertEquals("A", queue.peek());
        assertEquals(2, queue.size());
    }

    @Test
    void testDequeueFIFO() {
        Queue<Integer> queue = new Queue<>();

        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);

        assertEquals(10, queue.dequeue());
        assertEquals(20, queue.dequeue());
        assertEquals(30, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void testDequeueEmptyQueue() {
        Queue<Integer> queue = new Queue<>();

        assertThrows(
            IllegalStateException.class,
            () -> queue.dequeue()
        );
    }

    @Test
    void testClear() {
        Queue<Integer> queue = new Queue<>();

        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);

        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        assertThrows(
            IllegalStateException.class,
            () -> queue.peek()
        );
    }
}