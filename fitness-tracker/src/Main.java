import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createWindow);
    }

    private static void createWindow() {
        JFrame frame = new JFrame("Fitness Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(0, 1, 5, 5));

        // Timer 1 - crescator
        JLabel workoutLabel = new JLabel("", SwingConstants.CENTER);
        WorkoutTimer workoutTimer = new WorkoutTimer(workoutLabel);
        JButton start = new JButton("Start");
        JButton stop = new JButton("Stop");
        JButton reset = new JButton("Reset");
        start.addActionListener(e -> workoutTimer.start());
        stop.addActionListener(e -> workoutTimer.stop());
        reset.addActionListener(e -> workoutTimer.reset());

        JPanel workoutButtons = new JPanel();
        workoutButtons.add(start);
        workoutButtons.add(stop);
        workoutButtons.add(reset);
        frame.add(workoutLabel);
        frame.add(workoutButtons);

        // Timer 2 - descrescator
        JLabel restLabel = new JLabel("", SwingConstants.CENTER);
        RestTimer restTimer = new RestTimer(restLabel);
        JButton rest = new JButton("Pauza 60s");
        rest.addActionListener(e -> restTimer.start(60));

        JPanel restButtons = new JPanel();
        restButtons.add(rest);
        frame.add(restLabel);
        frame.add(restButtons);

        frame.setSize(400, 220);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
