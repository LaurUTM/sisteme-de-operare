import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer extends JPanel {

    private Timer countdownTimer;
    private int countdownSeconds = 0;

    private JLabel countdownLabel;
    private JTextField minutesField;
    private JTextField secondsField;

    // Aceleași culori ca în StudyPage
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final Color ACCENT_COLOR = new Color(255, 87, 34);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color TEXT_COLOR = new Color(33, 33, 33);

    private static final Font TITLE_FONT =
            new Font("Segoe UI", Font.BOLD, 18);

    private static final Font LABEL_FONT =
            new Font("Segoe UI", Font.PLAIN, 14);

    private static final Font FIELD_FONT =
            new Font("Segoe UI", Font.PLAIN, 14);

    private static final Font TIMER_FONT =
            new Font("Segoe UI", Font.BOLD, 48);

    private static final Font BUTTON_FONT =
            new Font("Segoe UI", Font.BOLD, 13);

    public CountdownTimer() {
        createInterface();
    }

    private void createInterface() {

        setLayout(new GridBagLayout());
        setBackground(CARD_COLOR);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(224, 224, 224),
                        1,
                        true
                ),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // TITLE
        JLabel title =
                new JLabel("Countdown Timer");

        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        add(title, gbc);

        gbc.gridwidth = 1;

        // MINUTES
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;

        add(
                createStyledLabel("Minutes:"),
                gbc
        );

        minutesField =
                createStyledTextField();

        gbc.gridx = 1;
        gbc.weightx = 1;

        add(minutesField, gbc);

        // SECONDS
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;

        add(
                createStyledLabel("Seconds:"),
                gbc
        );

        secondsField =
                createStyledTextField();

        gbc.gridx = 1;
        gbc.weightx = 1;

        add(secondsField, gbc);

        // TIMER
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        countdownLabel =
                new JLabel(
                        "00:00",
                        SwingConstants.CENTER
                );

        countdownLabel.setFont(TIMER_FONT);
        countdownLabel.setForeground(PRIMARY_COLOR);

        countdownLabel.setBorder(
                new EmptyBorder(
                        15, 0, 15, 0
                )
        );

        add(countdownLabel, gbc);

        // START
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1;

        JButton startButton =
                createStyledButton(
                        "Start",
                        SUCCESS_COLOR
                );

        startButton.addActionListener(
                e -> startCountdown()
        );

        add(startButton, gbc);

        // PAUSE
        gbc.gridx = 1;

        JButton pauseButton =
                createStyledButton(
                        "Pause",
                        ACCENT_COLOR
                );

        pauseButton.addActionListener(
                e -> pauseCountdown()
        );

        add(pauseButton, gbc);

        // RESET
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;

        JButton resetButton =
                createStyledButton(
                        "Reset",
                        new Color(96, 125, 139)
                );

        resetButton.addActionListener(
                e -> resetCountdown()
        );

        add(resetButton, gbc);
    }

    private JLabel createStyledLabel(String text) {

        JLabel label =
                new JLabel(text);

        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);

        return label;
    }

    private JTextField createStyledTextField() {

        JTextField field =
                new JTextField();

        field.setFont(FIELD_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBackground(
                new Color(250, 250, 250)
        );

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(200, 200, 200),
                                1,
                                true
                        ),
                        new EmptyBorder(
                                6, 10, 6, 10
                        )
                )
        );

        return field;
    }

    private JButton createStyledButton(
            String text,
            Color baseColor
    ) {

        JButton button =
                new JButton(text);

        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new EmptyBorder(
                        10, 18, 10, 18
                )
        );

        button.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    public void mouseEntered(
                            java.awt.event.MouseEvent evt
                    ) {
                        button.setBackground(
                                baseColor.darker()
                        );
                    }

                    public void mouseExited(
                            java.awt.event.MouseEvent evt
                    ) {
                        button.setBackground(
                                baseColor
                        );
                    }
                }
        );

        return button;
    }

    private void startCountdown() {

        if (countdownTimer != null) {
            return;
        }

        if (countdownSeconds <= 0) {

            int minutes;
            int seconds;

            try {

                minutes =
                        Integer.parseInt(
                                minutesField
                                        .getText()
                                        .trim()
                        );

                seconds =
                        Integer.parseInt(
                                secondsField
                                        .getText()
                                        .trim()
                        );

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter valid minutes and seconds."
                );

                return;
            }

            if (minutes < 0 ||
                    seconds < 0 ||
                    seconds > 59 ||
                    (minutes == 0 &&
                            seconds == 0)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid time."
                );

                return;
            }

            countdownSeconds =
                    minutes * 60 + seconds;

            updateCountdownLabel();
        }

        countdownTimer = new Timer();

        countdownTimer.scheduleAtFixedRate(
                new TimerTask() {

                    @Override
                    public void run() {

                        countdownSeconds--;

                        SwingUtilities.invokeLater(() -> {

                            updateCountdownLabel();

                            if (countdownSeconds <= 0) {

                                countdownTimer.cancel();
                                countdownTimer = null;

                                Toolkit.getDefaultToolkit()
                                        .beep();

                                JOptionPane.showMessageDialog(
                                        CountdownTimer.this,
                                        "Countdown finished!",
                                        "Countdown Timer",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            }
                        });
                    }
                },
                1000,
                1000
        );
    }

    private void pauseCountdown() {

        if (countdownTimer != null) {

            countdownTimer.cancel();
            countdownTimer = null;

            JOptionPane.showMessageDialog(
                    this,
                    "Countdown timer paused."
            );
        }
    }

    private void resetCountdown() {

        if (countdownTimer != null) {

            countdownTimer.cancel();
            countdownTimer = null;
        }

        countdownSeconds = 0;

        countdownLabel.setText(
                "00:00"
        );
    }

    private void updateCountdownLabel() {

        int minutes =
                countdownSeconds / 60;

        int seconds =
                countdownSeconds % 60;

        countdownLabel.setText(
                String.format(
                        "%02d:%02d",
                        minutes,
                        seconds
                )
        );
    }
}