import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalTime;

public class Alarm extends JPanel {

    JLabel timeLabel;
    JLabel statusLabel;

    JTextField hourField;
    JTextField minuteField;

    JButton startButton;
    JButton stopButton;

    Timer timer;


    public Alarm() {

        setLayout(new BorderLayout());

        // Titlu
        JLabel titleLabel = new JLabel("ALARMA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));

        // Ora curenta
        timeLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 50));

        // Status
        statusLabel = new JLabel("Gata de pornire", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Campuri pentru ora
        hourField = new JTextField("07", 4);
        minuteField = new JTextField("30", 4);

        JPanel inputPanel = new JPanel();

        inputPanel.add(new JLabel("Ora:"));
        inputPanel.add(hourField);

        inputPanel.add(new JLabel("Minute:"));
        inputPanel.add(minuteField);


        // Butoane
        startButton = new JButton("STAET");
        stopButton = new JButton("STOP");

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);


        // Panel principal
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(20));
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(20));
        panel.add(timeLabel);

        panel.add(Box.createVerticalStrut(10));
        panel.add(statusLabel);

        panel.add(Box.createVerticalStrut(20));
        panel.add(inputPanel);

        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonPanel);

        add(panel, BorderLayout.CENTER);


        // Pornirea alarmei
        startButton.addActionListener(e -> startAlarm());

        // Oprirea alarmei
        stopButton.addActionListener(e -> stopAlarm());
    }


    // Porneste alarma
    void startAlarm() {

        if (timer != null) {
            return;
        }

        try {

            int hour = Integer.parseInt(hourField.getText());
            int minute = Integer.parseInt(minuteField.getText());

            if (hour < 0 || hour > 23 ||
                    minute < 0 || minute > 59) {

                JOptionPane.showMessageDialog(
                        this,
                        "Introdu o ora corecta."
                );

                return;
            }

            statusLabel.setText("Alarma este activa.");

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Introdu doar numere."
            );

            return;
        }


        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {

                LocalTime now = LocalTime.now();

                String currentTime = String.format(
                        "%02d:%02d:%02d",
                        now.getHour(),
                        now.getMinute(),
                        now.getSecond()
                );

                SwingUtilities.invokeLater(() -> {
                    timeLabel.setText(currentTime);
                });


                int hour = Integer.parseInt(hourField.getText());
                int minute = Integer.parseInt(minuteField.getText());

                if (now.getHour() == hour &&
                        now.getMinute() == minute &&
                        now.getSecond() == 0) {

                    timer.cancel();
                    timer = null;

                    SwingUtilities.invokeLater(() -> {

                        statusLabel.setText("Alarma a sunat!");

                        Toolkit.getDefaultToolkit().beep();

                        JOptionPane.showMessageDialog(
                                Alarm.this,
                                "ALARMA!\nEste timpul!"
                        );
                    });
                }
            }

        }, 0, 1000);
    }


    // Opreste alarma
    void stopAlarm() {

        if (timer != null) {

            timer.cancel();
            timer = null;

            statusLabel.setText("Alarma oprita.");
        }
    }
}