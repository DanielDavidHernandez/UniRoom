import structures.Queue;

/**
 * Main class for the University Study Room Reservation System.
 */
public class Main {

    public static void main(String[] args) {

        Queue<String> waitingQueue = new Queue<>();

        System.out.println("=== UNIVERSITY STUDY ROOM RESERVATION SYSTEM ===");
        System.out.println();

        System.out.println("Room: Study Room 101");
        System.out.println("Time: 10:00 - 11:00");
        System.out.println("Status: Currently occupied");
        System.out.println();

        System.out.println("Adding students to the waiting queue...");

        waitingQueue.enqueue("Student 101");
        waitingQueue.enqueue("Student 205");
        waitingQueue.enqueue("Student 318");

        System.out.println();
        System.out.println("Students waiting: " + waitingQueue.size());
        System.out.println("First student in queue: " + waitingQueue.peek());

        System.out.println();
        System.out.println("Room 101 has been released.");

        String nextStudent = waitingQueue.dequeue();

        System.out.println("Next student notified: " + nextStudent);
        System.out.println("Students remaining in queue: "
                + waitingQueue.size());

        System.out.println();
        System.out.println("Next student in queue: "
                + waitingQueue.peek());
    }
}