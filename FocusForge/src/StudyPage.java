import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StudyPage extends JFrame {

    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final Color ACCENT_COLOR = new Color(255, 87, 34);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color NEUTRAL_COLOR = new Color(96, 125, 139);
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color MUTED_TEXT_COLOR = new Color(117, 117, 117);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font TIMER_FONT = new Font("Segoe UI", Font.BOLD, 48);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font STATUS_FONT = new Font("Segoe UI", Font.ITALIC, 13);

    private JTextField readingNameField;
    private JTextField readingMinutesField;
    private JLabel readingLabel;

    private JTextField reminderNameField;
    private JTextField reminderMinutesField;
    private JLabel reminderStatusLabel;

    private final ReadingTimer readingTimer;
    private final ReminderTimer reminderTimer = new ReminderTimer();

    public StudyPage() {
        setTitle("Study Timer");
        setSize(640, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        readingTimer = new ReadingTimer(
                seconds -> readingLabel.setText(ReadingTimer.formatTime(seconds)),
                () -> JOptionPane.showMessageDialog(this,
                        "30 de secunde rămase din sesiune!"),
                () -> {
                    String sessionName = readingNameField.getText().trim();
                    JOptionPane.showMessageDialog(this,
                            "Reading session finished!\n\n" + sessionName);
                }
        );

        createInterface();
    }

    private void createInterface() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel header = new JLabel("Study Timer");
        header.setFont(TITLE_FONT);
        header.setForeground(TEXT_COLOR);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(header);

        mainPanel.add(createReadingPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(createReminderPanel());

        add(mainPanel);
    }

    private JPanel createReadingPanel() {
        JPanel panel = createCardPanel();
        GridBagConstraints gbc = createGbc();

        JLabel title = new JLabel("Reading Timer");
        title.setFont(SECTION_FONT);
        title.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(createLabel("Session name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        readingNameField = createTextField();
        panel.add(readingNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(createLabel("Duration (minutes):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        readingMinutesField = createTextField();
        panel.add(readingMinutesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        readingLabel = new JLabel("00:00", SwingConstants.CENTER);
        readingLabel.setFont(TIMER_FONT);
        readingLabel.setForeground(PRIMARY_COLOR);
        readingLabel.setBorder(new EmptyBorder(15, 0, 15, 0));
        panel.add(readingLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 4;
        gbc.weightx = 1;

        JButton startBtn = createButton("Start", SUCCESS_COLOR);
        startBtn.addActionListener(e -> onStartReading());
        gbc.gridx = 0;
        panel.add(startBtn, gbc);

        JButton pauseBtn = createButton("Pause", ACCENT_COLOR);
        pauseBtn.addActionListener(e -> onPauseReading());
        gbc.gridx = 1;
        panel.add(pauseBtn, gbc);

        JButton resetBtn = createButton("Reset", NEUTRAL_COLOR);
        resetBtn.addActionListener(e -> onResetReading());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(resetBtn, gbc);

        return panel;
    }

    private JPanel createReminderPanel() {
        JPanel panel = createCardPanel();
        GridBagConstraints gbc = createGbc();

        JLabel title = new JLabel("Study Reminder");
        title.setFont(SECTION_FONT);
        title.setForeground(ACCENT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(createLabel("Reminder name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        reminderNameField = createTextField();
        panel.add(reminderNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(createLabel("Remind me after (minutes):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        reminderMinutesField = createTextField();
        panel.add(reminderMinutesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(createLabel("Status:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        reminderStatusLabel = new JLabel("No reminder set");
        reminderStatusLabel.setFont(STATUS_FONT);
        reminderStatusLabel.setForeground(MUTED_TEXT_COLOR);
        panel.add(reminderStatusLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1;

        JButton setBtn = createButton("Set Reminder", PRIMARY_COLOR);
        setBtn.addActionListener(e -> onSetReminder());
        panel.add(setBtn, gbc);

        JButton cancelBtn = createButton("Cancel", NEUTRAL_COLOR);
        cancelBtn.addActionListener(e -> onCancelReminder());
        gbc.gridx = 1;
        panel.add(cancelBtn, gbc);

        return panel;
    }

    private void onStartReading() {
        if (readingTimer.isRunning()) {
            return;
        }

        if (readingTimer.getRemainingSeconds() <= 0) {
            if (readingNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a session name.");
                return;
            }
            Integer minutes = readMinutes(readingMinutesField);
            if (minutes == null) {
                return;
            }
            readingTimer.start(minutes);
        } else {
            readingTimer.start(0);
        }
    }

    private void onPauseReading() {
        if (readingTimer.isRunning()) {
            readingTimer.pause();
            JOptionPane.showMessageDialog(this, "Reading timer paused.");
        }
    }

    private void onResetReading() {
        Integer minutes = readMinutes(readingMinutesField);
        if (minutes == null) {
            return;
        }
        readingTimer.reset(minutes);
    }

    private void onSetReminder() {
        String name = reminderNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a reminder name.");
            return;
        }

        Integer minutes = readMinutes(reminderMinutesField);
        if (minutes == null) {
            return;
        }

        reminderTimer.start(name, minutes, reminderName -> {
            JOptionPane.showMessageDialog(this,
                    "Study Reminder!\n\n" + reminderName);
            reminderStatusLabel.setText("Reminder completed");
            reminderStatusLabel.setForeground(SUCCESS_COLOR);
        });

        reminderStatusLabel.setText("Reminder set for " + minutes + " minute(s)");
        reminderStatusLabel.setForeground(PRIMARY_COLOR);
    }

    private void onCancelReminder() {
        if (reminderTimer.isRunning()) {
            reminderTimer.cancel();
            reminderStatusLabel.setText("Reminder cancelled");
            reminderStatusLabel.setForeground(ACCENT_COLOR);
        } else {
            reminderStatusLabel.setText("No active reminder");
            reminderStatusLabel.setForeground(MUTED_TEXT_COLOR);
        }
    }

    private Integer readMinutes(JTextField field) {
        try {
            int minutes = Integer.parseInt(field.getText().trim());
            if (minutes <= 0) {
                JOptionPane.showMessageDialog(this, "Duration must be greater than 0.");
                return null;
            }
            return minutes;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number of minutes.");
            return null;
        }
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        return panel;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JTextField createTextField() {
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

    private JButton createButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });

        return button;
    }
}