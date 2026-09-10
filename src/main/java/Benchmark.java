import structures.Queue;

/**
 * Benchmark for the Queue data structure.
 */
public class Benchmark {

    /**
     * Runs the benchmark with different input sizes.
     *
     * @param args command-line arguments
     * @complexity O(n) for each benchmark
     */
    public static void main(String[] args) {

        int[] sizes = {1000, 10000, 100000};

        System.out.println("=== QUEUE BENCHMARK ===");

        for (int n : sizes) {

            Queue<Integer> queue = new Queue<>();

            long startEnqueue = System.nanoTime();

            for (int i = 0; i < n; i++) {
                queue.enqueue(i);
            }

            long endEnqueue = System.nanoTime();

            long startDequeue = System.nanoTime();

            while (!queue.isEmpty()) {
                queue.dequeue();
            }

            long endDequeue = System.nanoTime();

            long enqueueTime = endEnqueue - startEnqueue;
            long dequeueTime = endDequeue - startDequeue;

            System.out.println();
            System.out.println("Elements: " + n);
            System.out.println("Enqueue time: " + enqueueTime + " ns");
            System.out.println("Dequeue time: " + dequeueTime + " ns");
        }
    }
}