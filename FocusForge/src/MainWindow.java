import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class MainWindow extends JFrame {

    private TimerManager timerManager;
    private Task task;
    private FocusSession focusSession;

    private JTextArea log;
    private Timer periodicTimerHandle;

    private final Color BACKGROUND = new Color(248, 245, 247);
    private final Color WHITE = new Color(255, 253, 254);

    private final Color PINK = new Color(232, 202, 215);
    private final Color PINK_DARK = new Color(181, 128, 153);

    private final Color LILAC = new Color(216, 207, 231);
    private final Color LILAC_DARK = new Color(137, 122, 161);

    private final Color BLUE = new Color(207, 222, 232);
    private final Color BLUE_DARK = new Color(112, 143, 163);

    private final Color TEXT = new Color(75, 68, 75);
    private final Color SECONDARY_TEXT = new Color(130, 120, 130);

    public MainWindow() {
        super("Focus Timer");

        timerManager = new TimerManager();
        task = new Task("Planificarea activităților cu Timer", 25);
        focusSession = new FocusSession(task, 0, 25);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 600);
        setLocationRelativeTo(null);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());


        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BACKGROUND);
        header.setBorder(new EmptyBorder(25, 30, 15, 30));

        JLabel title = new JLabel("Focus Timer");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Organizează-ți timpul și rămâi concentrat ");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(SECONDARY_TEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);


        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(5, 25, 10, 25));

        content.add(buildDelayPanel());
        content.add(Box.createVerticalStrut(12));

        content.add(buildAtTimePanel());
        content.add(Box.createVerticalStrut(12));

        content.add(buildPeriodicPanel());

        add(content, BorderLayout.CENTER);


        log = new JTextArea();
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));
        log.setForeground(TEXT);
        log.setBackground(WHITE);
        log.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(log);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(WHITE);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(WHITE);
        logPanel.setBorder(new EmptyBorder(0, 25, 20, 25));

        JLabel logTitle = new JLabel("  Activitate");
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        logTitle.setForeground(TEXT);
        logTitle.setBorder(new EmptyBorder(8, 0, 8, 0));

        logPanel.add(logTitle, BorderLayout.NORTH);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        add(logPanel, BorderLayout.SOUTH);

        appendLog("Sesiune creată pentru task: " + task.getTitlu());
    }


    private JPanel buildDelayPanel() {

        JPanel panel = createCard(PINK);

        JLabel title = createTitle("⏱  Reacție după un interval");
        JLabel description = createDescription(
                "Pornește sesiunea și oprește-o automat după timpul ales."
        );

        JTextField secField = createTextField("5");
        JButton start = createButton("Start", PINK_DARK);

        start.addActionListener(e -> {

            long secunde;

            try {
                secunde = Long.parseLong(secField.getText().trim());

                if (secunde < 0) {
                    appendLog("Intervalul nu poate fi negativ.");
                    return;
                }

            } catch (NumberFormatException ex) {
                appendLog("Interval invalid.");
                return;
            }

            focusSession.start();

            appendLog(
                    "Programat: sesiunea se oprește peste "
                            + secunde + " secunde."
            );

            timerManager.startAfterDelay(() -> {

                SwingUtilities.invokeLater(() -> {

                    focusSession.stop();

                    appendLog(
                            "[DELAY] Timp expirat → sesiune oprită automat."
                    );
                });

            }, secunde * 1000L);
        });

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setBackground(WHITE);

        JLabel secLabel = new JLabel("Secunde:");
        secLabel.setForeground(TEXT);

        controls.add(secLabel);
        controls.add(secField);
        controls.add(start);

        panel.add(title);
        panel.add(description);
        panel.add(Box.createVerticalStrut(10));
        panel.add(controls);

        return panel;
    }


    private JPanel buildAtTimePanel() {

        JPanel panel = createCard(LILAC);

        JLabel title = createTitle("◷  Reacție la o oră exactă");
        JLabel description = createDescription(
                "Programează o notificare pentru un anumit moment."
        );

        JTextField secField = createTextField("10");
        JButton schedule = createButton("Programează", LILAC_DARK);

        schedule.addActionListener(e -> {

            long secunde;

            try {
                secunde = Long.parseLong(secField.getText().trim());

                if (secunde < 0) {
                    appendLog("Valoarea nu poate fi negativă.");
                    return;
                }

            } catch (NumberFormatException ex) {
                appendLog("Valoare invalidă.");
                return;
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, (int) secunde);

            Date targetTime = cal.getTime();

            SimpleDateFormat sdf =
                    new SimpleDateFormat("HH:mm:ss");

            appendLog(
                    "Programat pentru ora: "
                            + sdf.format(targetTime)
            );

            timerManager.scheduleAt(() -> {

                SwingUtilities.invokeLater(() ->
                        appendLog(
                                "[LA ORA] "
                                        + sdf.format(new Date())
                                        + " → Notificare declanșată!"
                        )
                );

            }, targetTime);
        });

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setBackground(WHITE);

        JLabel secLabel = new JLabel("Peste (sec):");
        secLabel.setForeground(TEXT);

        controls.add(secLabel);
        controls.add(secField);
        controls.add(schedule);

        panel.add(title);
        panel.add(description);
        panel.add(Box.createVerticalStrut(10));
        panel.add(controls);

        return panel;
    }


    private JPanel buildPeriodicPanel() {

        JPanel panel = createCard(BLUE);

        JLabel title = createTitle("↻  Reacție periodică");
        JLabel description = createDescription(
                "Execută automat o acțiune la fiecare X secunde."
        );

        JTextField periodField = createTextField("2");

        JButton startPeriodic =
                createButton("Start", BLUE_DARK);

        JButton stopPeriodic =
                createButton("Stop", SECONDARY_TEXT);

        startPeriodic.addActionListener(e -> {

            long periodSec;

            try {
                periodSec =
                        Long.parseLong(periodField.getText().trim());

                if (periodSec <= 0) {
                    appendLog(
                            "Perioada trebuie să fie mai mare decât 0."
                    );
                    return;
                }

            } catch (NumberFormatException ex) {
                appendLog("Perioada invalidă.");
                return;
            }

            if (periodicTimerHandle != null) {

                appendLog(
                        "Un timer periodic rulează deja. "
                                + "Opriți-l întâi."
                );

                return;
            }

            periodicTimerHandle =
                    timerManager.startPeriodic(
                            () -> SwingUtilities.invokeLater(() ->
                                    appendLog(
                                            "[PERIODIC] Tick la fiecare "
                                                    + periodField.getText()
                                                    + " sec."
                                    )
                            ),
                            0L,
                            periodSec * 1000L
                    );

            appendLog(
                    "Timer periodic pornit (perioada "
                            + periodSec + " sec)."
            );
        });

        stopPeriodic.addActionListener(e -> {

            if (periodicTimerHandle != null) {

                periodicTimerHandle.cancel();
                periodicTimerHandle = null;

                appendLog("Timer periodic oprit.");

            } else {

                appendLog(
                        "Nu există niciun timer periodic activ."
                );
            }
        });

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );

        controls.setBackground(WHITE);

        JLabel periodLabel =
                new JLabel("Perioada (sec):");

        periodLabel.setForeground(TEXT);

        controls.add(periodLabel);
        controls.add(periodField);
        controls.add(startPeriodic);
        controls.add(stopPeriodic);

        panel.add(title);
        panel.add(description);
        panel.add(Box.createVerticalStrut(10));
        panel.add(controls);

        return panel;
    }


    private JPanel createCard(Color accent) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                4, 0, 0, 0, accent
                        ),
                        new EmptyBorder(
                                12, 15, 14, 15
                        )
                )
        );

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    private JLabel createTitle(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        16
                )
        );

        label.setForeground(TEXT);

        return label;
    }

    private JLabel createDescription(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        label.setForeground(SECONDARY_TEXT);

        return label;
    }

    private JTextField createTextField(String text) {

        JTextField field = new JTextField(text, 5);

        field.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        field.setForeground(TEXT);
        field.setBackground(new Color(250, 248, 250));

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 218, 225)
                        ),
                        new EmptyBorder(
                                5, 7, 5, 7
                        )
                )
        );

        return field;
    }

    private JButton createButton(
            String text,
            Color background
    ) {

        JButton button = new JButton(text);

        button.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );

        button.setForeground(TEXT);
        button.setBackground(background);

        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setBorder(
                new EmptyBorder(
                        7, 14, 7, 14
                )
        );

        return button;
    }

    private void appendLog(String mesaj) {

        if (log != null) {

            log.append(mesaj + "\n");

            log.setCaretPosition(
                    log.getDocument().getLength()
            );
        }
    }
}