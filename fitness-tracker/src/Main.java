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
        RestTimer restTimer = new RestTimer(restLabel, workoutTimer::start);
        JButton rest = new JButton("Pauza 60s");
        rest.addActionListener(e -> {
            workoutTimer.stop();
            restTimer.start(60);
        });

        JPanel restButtons = new JPanel();
        restButtons.add(rest);
        frame.add(restLabel);
        frame.add(restButtons);

        // Timer 3 - ScheduledExecutorService: text field blocat 30s
        JLabel lockLabel = new JLabel("Nota antrenament (blocat 30s la pornire):", SwingConstants.CENTER);
        JTextField notaField = new JTextField(15);
        TextFieldLocker locker = new TextFieldLocker();
        locker.blocheaza30Secunde(notaField);

        JPanel lockPanel = new JPanel();
        lockPanel.add(notaField);
        frame.add(lockLabel);
        frame.add(lockPanel);

        // Timer 4 - java.util.Timer + TimerTask: alarma cu popup dupa un timp setat
        JLabel alarmLabel = new JLabel("Seteaza o alarma (secunde):", SwingConstants.CENTER);
        JTextField secundeField = new JTextField(5);
        JButton setAlarma = new JButton("Seteaza alarma");
        AlarmTimer alarmTimer = new AlarmTimer();

        setAlarma.addActionListener(e -> {
            try {
                int secunde = Integer.parseInt(secundeField.getText().trim());
                if (secunde <= 0) {
                    JOptionPane.showMessageDialog(frame, "Introdu un numar de secunde mai mare ca 0.");
                    return;
                }
                alarmTimer.seteazaAlarma(secunde, frame);
                JOptionPane.showMessageDialog(frame, "Alarma setata pentru " + secunde + " secunde.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Introdu un numar valid de secunde.");
            }
        });

        JPanel alarmPanel = new JPanel();
        alarmPanel.add(secundeField);
        alarmPanel.add(setAlarma);
        frame.add(alarmLabel);
        frame.add(alarmPanel);

        frame.setSize(420, 420);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}