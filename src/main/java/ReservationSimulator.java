import structures.Queue;

/**
 * Simulates concurrent study room reservations.
 *
 * The simulation represents several students trying to reserve
 * the same study room at the same time.
 *
 * Students who cannot obtain the room immediately are placed
 * in a FIFO waiting queue.
 */
public class ReservationSimulator {

    private static boolean roomAvailable = true;

    public static void main(String[] args) {

        Queue<String> waitingQueue = new Queue<>();

        System.out.println("==========================================");
        System.out.println("   UNIVERSITY STUDY ROOM RESERVATIONS");
        System.out.println("==========================================");
        System.out.println();

        Thread student1 = new Thread(() ->
                requestReservation("Student 101", waitingQueue));

        Thread student2 = new Thread(() ->
                requestReservation("Student 205", waitingQueue));

        Thread student3 = new Thread(() ->
                requestReservation("Student 318", waitingQueue));

        Thread student4 = new Thread(() ->
                requestReservation("Student 427", waitingQueue));

        System.out.println("Students are requesting the same study room...");
        System.out.println();

        student1.start();
        student2.start();
        student3.start();
        student4.start();

        try {
            student1.join();
            student2.join();
            student3.join();
            student4.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println();
        System.out.println("------------------------------------------");
        System.out.println("RESERVATION STATUS");
        System.out.println("------------------------------------------");

        System.out.println("Students waiting: " + waitingQueue.size());

        if (!waitingQueue.isEmpty()) {
            System.out.println(
                    "First student waiting: " + waitingQueue.peek()
            );
        }

        System.out.println();
        System.out.println("==========================================");
        System.out.println("        RELEASING STUDY ROOM");
        System.out.println("==========================================");

        roomAvailable = true;

        System.out.println("The study room has been released.");

        if (!waitingQueue.isEmpty()) {

            String nextStudent = waitingQueue.dequeue();

            System.out.println(
                    "Reservation assigned to: " + nextStudent
            );
        }

        System.out.println(
                "Students still waiting: " + waitingQueue.size()
        );

        System.out.println();
        System.out.println("Simulation completed successfully.");
    }

    /**
     * Simulates a student requesting a study room.
     *
     * @param student the student requesting the room
     * @param waitingQueue the queue of students waiting for the room
     * @complexity O(1)
     */
    private static synchronized void requestReservation(
            String student,
            Queue<String> waitingQueue) {

        if (roomAvailable) {

            roomAvailable = false;

            System.out.println(
                    student + " successfully reserved the study room."
            );

        } else {

            waitingQueue.enqueue(student);

            System.out.println(
                    student + " entered the waiting queue."
            );
        }
    }
}