import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.sound.sampled.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class StudyPage extends JFrame {

    private Timer readingTimer;
    private int readingSeconds = 0;
    private int readingInitialSeconds = 0;
    private boolean readingSessionInitialized = false;
    private boolean thirtySecondWarningShown = false;

    private JLabel readingLabel;
    private JTextField readingNameField;
    private JTextField readingMinutesField;

    private Timer reminderTimer;
    private JTextField reminderNameField;
    private JTextField reminderMinutesField;
    private JLabel reminderStatusLabel;

    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final Color ACCENT_COLOR = new Color(255, 87, 34);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color MUTED_TEXT_COLOR = new Color(117, 117, 117);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font TIMER_FONT = new Font("Segoe UI", Font.BOLD, 48);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font STATUS_FONT = new Font("Segoe UI", Font.ITALIC, 13);

    public StudyPage() {
        setTitle("Study Timer");
        setSize(640, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        createInterface();
    }

    private void createInterface() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel headerLabel = new JLabel("Study Timer");
        headerLabel.setFont(TITLE_FONT);
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(headerLabel);

        mainPanel.add(createReadingPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(createReminderPanel());

        add(mainPanel);
    }

    private JPanel createReadingPanel() {
        JPanel readingPanel = new JPanel();
        readingPanel.setLayout(new GridBagLayout());
        readingPanel.setBackground(CARD_COLOR);
        readingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel readingTitle = new JLabel("Reading Timer");
        readingTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        readingTitle.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        readingPanel.add(readingTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        readingPanel.add(createStyledLabel("Session name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        readingNameField = createStyledTextField();
        readingPanel.add(readingNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        readingPanel.add(createStyledLabel("Duration (minutes):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        readingMinutesField = createStyledTextField();
        readingPanel.add(readingMinutesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        readingLabel = new JLabel("00:00", SwingConstants.CENTER);
        readingLabel.setFont(TIMER_FONT);
        readingLabel.setForeground(PRIMARY_COLOR);
        readingLabel.setBorder(new EmptyBorder(15, 0, 15, 0));
        readingPanel.add(readingLabel, gbc);

        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 1;

        JButton startReadingButton = createStyledButton("Start", SUCCESS_COLOR);
        startReadingButton.addActionListener(e -> startReadingTimer());
        gbc.gridx = 0;
        readingPanel.add(startReadingButton, gbc);

        JButton pauseReadingButton = createStyledButton("Pause", ACCENT_COLOR);
        pauseReadingButton.addActionListener(e -> pauseReadingTimer());
        gbc.gridx = 1;
        readingPanel.add(pauseReadingButton, gbc);

        JButton resetReadingButton = createStyledButton("Reset", new Color(96, 125, 139));
        resetReadingButton.addActionListener(e -> resetReadingTimer());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        readingPanel.add(resetReadingButton, gbc);

        return readingPanel;
    }

    private JPanel createReminderPanel() {
        JPanel reminderPanel = new JPanel();
        reminderPanel.setLayout(new GridBagLayout());
        reminderPanel.setBackground(CARD_COLOR);
        reminderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel reminderTitle = new JLabel("Study Reminder");
        reminderTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        reminderTitle.setForeground(ACCENT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        reminderPanel.add(reminderTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        reminderPanel.add(createStyledLabel("Reminder name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        reminderNameField = createStyledTextField();
        reminderPanel.add(reminderNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        reminderPanel.add(createStyledLabel("Remind me after (minutes):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        reminderMinutesField = createStyledTextField();
        reminderPanel.add(reminderMinutesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        reminderPanel.add(createStyledLabel("Status:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        reminderStatusLabel = new JLabel("No reminder set");
        reminderStatusLabel.setFont(STATUS_FONT);
        reminderStatusLabel.setForeground(MUTED_TEXT_COLOR);
        reminderPanel.add(reminderStatusLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 1;

        JButton setReminderButton = createStyledButton("Set Reminder", PRIMARY_COLOR);
        setReminderButton.addActionListener(e -> startReminderTimer());
        reminderPanel.add(setReminderButton, gbc);

        JButton cancelReminderButton = createStyledButton("Cancel", new Color(96, 125, 139));
        cancelReminderButton.addActionListener(e -> cancelReminderTimer());
        gbc.gridx = 1;
        reminderPanel.add(cancelReminderButton, gbc);

        return reminderPanel;
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
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });

        return button;
    }

    private void playAlertSound() {
        new Thread(() -> {
            try {
                byte[] buf = new byte[16000 * 2];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (16000.0 / 800.0) * 2.0 * Math.PI;
                    buf[i] = (byte) (Math.sin(angle) * 127);
                }
                AudioFormat af = new AudioFormat(16000, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        }).start();
    }

    private void startReadingTimer() {
        if (readingTimer != null) {
            return;
        }

        if (!readingSessionInitialized || readingSeconds <= 0) {
            String name = readingNameField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a session name.");
                return;
            }

            int minutes;
            try {
                minutes = Integer.parseInt(readingMinutesField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number of minutes.");
                return;
            }

            if (minutes <= 0) {
                JOptionPane.showMessageDialog(this, "Duration must be greater than 0.");
                return;
            }

            readingSeconds = minutes * 60;
            readingInitialSeconds = readingSeconds;
            readingSessionInitialized = true;
            thirtySecondWarningShown = false;
            updateReadingLabel();
        }

        readingTimer = new Timer();
        readingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                readingSeconds--;
                SwingUtilities.invokeLater(() -> {
                    updateReadingLabel();

                    if (readingSeconds == 30 && !thirtySecondWarningShown) {
                        thirtySecondWarningShown = true;
                        playAlertSound();
                        JOptionPane.showMessageDialog(
                                StudyPage.this,
                                "30 de secunde rămase din sesiune!"
                        );
                    }

                    if (readingSeconds <= 0) {
                        readingTimer.cancel();
                        readingTimer = null;
                        readingSessionInitialized = false;
                        thirtySecondWarningShown = false;
                        playAlertSound();
                        String sessionName = readingNameField.getText().trim();
                        JOptionPane.showMessageDialog(
                                StudyPage.this,
                                "Reading session finished!\n\n" + sessionName
                        );
                    }
                });
            }
        }, 1000, 1000);
    }

    private void pauseReadingTimer() {
        if (readingTimer != null) {
            readingTimer.cancel();
            readingTimer = null;
            JOptionPane.showMessageDialog(this, "Reading timer paused.");
        }
    }

    private void resetReadingTimer() {
        if (readingTimer != null) {
            readingTimer.cancel();
            readingTimer = null;
        }

        String minutesText = readingMinutesField.getText().trim();
        int minutes;

        try {
            minutes = Integer.parseInt(minutesText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number of minutes.");
            return;
        }

        if (minutes <= 0) {
            JOptionPane.showMessageDialog(this, "Duration must be greater than 0.");
            return;
        }

        readingInitialSeconds = minutes * 60;
        readingSeconds = readingInitialSeconds;
        readingSessionInitialized = true;
        thirtySecondWarningShown = false;
        updateReadingLabel();
    }

    private void updateReadingLabel() {
        readingLabel.setText(formatTime(readingSeconds));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void startReminderTimer() {
        if (reminderTimer != null) {
            reminderTimer.cancel();
            reminderTimer = null;
        }

        String reminderName = reminderNameField.getText().trim();

        if (reminderName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a reminder name.");
            return;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(reminderMinutesField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number of minutes.");
            return;
        }

        if (minutes <= 0) {
            JOptionPane.showMessageDialog(this, "Reminder time must be greater than 0.");
            return;
        }

        long delay = minutes * 60L * 1000L;
        reminderTimer = new Timer();

        reminderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    playAlertSound();
                    JOptionPane.showMessageDialog(
                            StudyPage.this,
                            "Study Reminder!\n\n" + reminderName
                    );
                    reminderStatusLabel.setText("Reminder completed");
                    reminderStatusLabel.setForeground(SUCCESS_COLOR);
                    reminderTimer = null;
                });
            }
        }, delay);

        reminderStatusLabel.setText("Reminder set for " + minutes + " minute(s)");
        reminderStatusLabel.setForeground(PRIMARY_COLOR);
    }

    private void cancelReminderTimer() {
        if (reminderTimer != null) {
            reminderTimer.cancel();
            reminderTimer = null;
            reminderStatusLabel.setText("Reminder cancelled");
            reminderStatusLabel.setForeground(ACCENT_COLOR);
        } else {
            reminderStatusLabel.setText("No active reminder");
            reminderStatusLabel.setForeground(MUTED_TEXT_COLOR);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudyPage page = new StudyPage();
            page.setVisible(true);
        });
    }
}