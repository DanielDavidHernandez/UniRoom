import structures.Queue;

import javax.swing.*;
import java.awt.*;

/**
 * Graphical interface for the university study room reservation system.
 *
 * This interface demonstrates the use of a custom Queue<T> together
 * with concurrent reservation requests.
 */
public class ReservationGUI extends JFrame {

    private final Queue<String> waitingQueue = new Queue<>();

    private boolean roomAvailable = true;
    private String currentStudent = "None";

    private final JLabel roomStatusLabel;
    private final JLabel currentStudentLabel;
    private final JLabel waitingCountLabel;

    private final DefaultListModel<String> queueModel;
    private final JTextArea historyArea;

    private final JButton simulateButton;
    private final JButton releaseButton;
    private final JButton clearButton;

    /**
     * Creates the reservation system graphical interface.
     *
     * @complexity O(1)
     */
    public ReservationGUI() {

        setTitle("University Study Room Reservations");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // =========================
        // TITLE
        // =========================

        JLabel title = new JLabel(
                "UNIVERSITY STUDY ROOM RESERVATIONS",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 24));

        mainPanel.add(title, BorderLayout.NORTH);

        // =========================
        // CENTER
        // =========================

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));

        // Room information
        JPanel roomPanel = new JPanel();
        roomPanel.setBorder(
                BorderFactory.createTitledBorder("Study Room 101")
        );

        roomPanel.setLayout(new BoxLayout(roomPanel, BoxLayout.Y_AXIS));

        roomStatusLabel = new JLabel("STATUS: AVAILABLE");
        roomStatusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        roomStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        currentStudentLabel = new JLabel("Current student: None");
        currentStudentLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        currentStudentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        waitingCountLabel = new JLabel("Students waiting: 0");
        waitingCountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        waitingCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        roomPanel.add(Box.createVerticalStrut(30));
        roomPanel.add(roomStatusLabel);
        roomPanel.add(Box.createVerticalStrut(20));
        roomPanel.add(currentStudentLabel);
        roomPanel.add(Box.createVerticalStrut(15));
        roomPanel.add(waitingCountLabel);
        roomPanel.add(Box.createVerticalStrut(30));

        // Queue panel
        JPanel queuePanel = new JPanel(new BorderLayout());

        queuePanel.setBorder(
                BorderFactory.createTitledBorder("Waiting Queue - FIFO")
        );

        queueModel = new DefaultListModel<>();

        JList<String> queueList = new JList<>(queueModel);
        queueList.setFont(new Font("Arial", Font.PLAIN, 16));

        queuePanel.add(new JScrollPane(queueList), BorderLayout.CENTER);

        centerPanel.add(roomPanel);
        centerPanel.add(queuePanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // =========================
        // HISTORY
        // =========================

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        JScrollPane historyScroll = new JScrollPane(historyArea);

        historyScroll.setBorder(
                BorderFactory.createTitledBorder("System History")
        );

        historyScroll.setPreferredSize(new Dimension(0, 150));

        mainPanel.add(historyScroll, BorderLayout.SOUTH);

        // =========================
        // BUTTONS
        // =========================

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 10)
        );

        simulateButton = new JButton("Simulate Reservations");
        releaseButton = new JButton("Release Room");
        clearButton = new JButton("Clear");

        simulateButton.setFont(new Font("Arial", Font.BOLD, 14));
        releaseButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));

        buttonPanel.add(simulateButton);
        buttonPanel.add(releaseButton);
        buttonPanel.add(clearButton);

        mainPanel.add(buttonPanel, BorderLayout.PAGE_END);

        setContentPane(mainPanel);

        // =========================
        // EVENTS
        // =========================

        simulateButton.addActionListener(e -> simulateReservations());

        releaseButton.addActionListener(e -> releaseRoom());

        clearButton.addActionListener(e -> clearSystem());

        updateInterface();
    }

    /**
     * Simulates several students requesting the same study room.
     *
     * @complexity O(n), where n is the number of students
     */
    private void simulateReservations() {

        if (!roomAvailable) {
            addHistory("Room is currently occupied.");
            addHistory("New students will be added to the waiting queue.");
        }

        String[] students = {
                "Student 101",
                "Student 205",
                "Student 318",
                "Student 427"
        };

        for (String student : students) {

            Thread reservationThread = new Thread(
                    () -> requestReservation(student)
            );

            reservationThread.start();
        }
    }

    /**
     * Handles a reservation request.
     *
     * @param student the student requesting the room
     * @complexity O(1)
     */
    private synchronized void requestReservation(String student) {

        if (roomAvailable) {

            roomAvailable = false;
            currentStudent = student;

            SwingUtilities.invokeLater(() -> {

                addHistory(
                        "✓ " + student + " reserved Study Room 101."
                );

                updateInterface();
            });

        } else {

            waitingQueue.enqueue(student);

            SwingUtilities.invokeLater(() -> {

                addHistory(
                        "→ " + student + " entered the waiting queue."
                );

                updateInterface();
            });
        }
    }

    /**
     * Releases the room and assigns it to the first student
     * in the waiting queue.
     *
     * @complexity O(1)
     */
    private synchronized void releaseRoom() {

        if (roomAvailable) {

            addHistory("The room is already available.");
            return;
        }

        addHistory(
                "Room released by " + currentStudent + "."
        );

        roomAvailable = true;
        currentStudent = "None";

        if (!waitingQueue.isEmpty()) {

            String nextStudent = waitingQueue.dequeue();

            roomAvailable = false;
            currentStudent = nextStudent;

            addHistory(
                    "✓ Reservation assigned to " + nextStudent + "."
            );
        }

        updateInterface();
    }

    /**
     * Clears the entire simulation.
     *
     * @complexity O(1)
     */
    private synchronized void clearSystem() {

        waitingQueue.clear();

        roomAvailable = true;
        currentStudent = "None";

        queueModel.clear();
        historyArea.setText("");

        addHistory("System reset.");
        updateInterface();
    }

    /**
     * Updates all graphical components.
     *
     * @complexity O(n), where n is the number of students in the queue
     */
    private void updateInterface() {

        if (roomAvailable) {
            roomStatusLabel.setText("STATUS: AVAILABLE");
        } else {
            roomStatusLabel.setText("STATUS: OCCUPIED");
        }

        currentStudentLabel.setText(
                "Current student: " + currentStudent
        );

        waitingCountLabel.setText(
                "Students waiting: " + waitingQueue.size()
        );

        queueModel.clear();

        Queue<String> temporaryQueue = new Queue<>();

        while (!waitingQueue.isEmpty()) {

            String student = waitingQueue.dequeue();

            queueModel.addElement(student);

            temporaryQueue.enqueue(student);
        }

        while (!temporaryQueue.isEmpty()) {

            waitingQueue.enqueue(
                    temporaryQueue.dequeue()
            );
        }
    }

    /**
     * Adds a message to the system history.
     *
     * @param message message to display
     * @complexity O(1)
     */
    private void addHistory(String message) {

        historyArea.append(message + "\n");
    }

    /**
     * Starts the graphical application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            ReservationGUI window = new ReservationGUI();

            window.setVisible(true);
        });
    }
}