import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ExerciseTimer extends JPanel {

    private Timer exerciseTimer;
    private int exerciseSeconds = 0;

    private JLabel exerciseLabel;
    private JTextField exerciseNameField;
    private JTextField exerciseMinutesField;

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

    public ExerciseTimer() {
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
        JLabel title = new JLabel("Exercise Timer");

        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        add(title, gbc);

        gbc.gridwidth = 1;

        // EXERCISE NAME
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;

        add(createStyledLabel("Exercise name:"), gbc);

        exerciseNameField = createStyledTextField();

        gbc.gridx = 1;
        gbc.weightx = 1;

        add(exerciseNameField, gbc);

        // DURATION
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;

        add(createStyledLabel("Duration (minutes):"), gbc);

        exerciseMinutesField = createStyledTextField();

        gbc.gridx = 1;
        gbc.weightx = 1;

        add(exerciseMinutesField, gbc);

        // TIMER
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        exerciseLabel = new JLabel(
                "00:00",
                SwingConstants.CENTER
        );

        exerciseLabel.setFont(TIMER_FONT);
        exerciseLabel.setForeground(PRIMARY_COLOR);
        exerciseLabel.setBorder(
                new EmptyBorder(15, 0, 15, 0)
        );

        add(exerciseLabel, gbc);

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
                e -> startExerciseTimer()
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
                e -> pauseExerciseTimer()
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
                e -> resetExerciseTimer()
        );

        add(resetButton, gbc);
    }

    private JLabel createStyledLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);

        return label;
    }

    private JTextField createStyledTextField() {

        JTextField field = new JTextField();

        field.setFont(FIELD_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBackground(new Color(250, 250, 250));

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

        JButton button = new JButton(text);

        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
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

    private void startExerciseTimer() {

        if (exerciseTimer != null) {
            return;
        }

        if (exerciseSeconds <= 0) {

            String name =
                    exerciseNameField
                            .getText()
                            .trim();

            if (name.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter an exercise name."
                );

                return;
            }

            int minutes;

            try {

                minutes = Integer.parseInt(
                        exerciseMinutesField
                                .getText()
                                .trim()
                );

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid number of minutes."
                );

                return;
            }

            if (minutes <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Duration must be greater than 0."
                );

                return;
            }

            exerciseSeconds = minutes * 60;

            updateExerciseLabel();
        }

        exerciseTimer = new Timer();

        exerciseTimer.scheduleAtFixedRate(
                new TimerTask() {

                    @Override
                    public void run() {

                        exerciseSeconds--;

                        SwingUtilities.invokeLater(() -> {

                            updateExerciseLabel();

                            if (exerciseSeconds <= 0) {

                                exerciseTimer.cancel();
                                exerciseTimer = null;

                                Toolkit.getDefaultToolkit()
                                        .beep();

                                JOptionPane.showMessageDialog(
                                        ExerciseTimer.this,
                                        "Exercise finished!",
                                        "Exercise Timer",
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

    private void pauseExerciseTimer() {

        if (exerciseTimer != null) {

            exerciseTimer.cancel();
            exerciseTimer = null;

            JOptionPane.showMessageDialog(
                    this,
                    "Exercise timer paused."
            );
        }
    }

    private void resetExerciseTimer() {

        if (exerciseTimer != null) {

            exerciseTimer.cancel();
            exerciseTimer = null;
        }

        String minutesText =
                exerciseMinutesField
                        .getText()
                        .trim();

        int minutes;

        try {

            minutes = Integer.parseInt(
                    minutesText
            );

        } catch (NumberFormatException e) {

            exerciseSeconds = 0;
            exerciseLabel.setText("00:00");

            return;
        }

        if (minutes <= 0) {

            exerciseSeconds = 0;
            exerciseLabel.setText("00:00");

            return;
        }

        exerciseSeconds = minutes * 60;

        updateExerciseLabel();
    }

    private void updateExerciseLabel() {

        int minutes =
                exerciseSeconds / 60;

        int seconds =
                exerciseSeconds % 60;

        exerciseLabel.setText(
                String.format(
                        "%02d:%02d",
                        minutes,
                        seconds
                )
        );
    }
}