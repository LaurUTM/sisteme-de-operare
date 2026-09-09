import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class MainWindow extends JFrame {

    private TimerManager timerManager;

    private JPanel taskListContainer;
    private JLabel emptyStateLabel;
    private JTextArea log;

    private final List<TaskEntry> taskEntries = new ArrayList<>();

    private final Color BACKGROUND = new Color(248, 245, 247);
    private final Color WHITE = new Color(255, 253, 254);

    private final Color PINK = new Color(232, 202, 215);
    private final Color PINK_DARK = new Color(181, 128, 153);

    private final Color LILAC = new Color(216, 207, 231);
    private final Color LILAC_DARK = new Color(137, 122, 161);

    private final Color BLUE_DARK = new Color(112, 143, 163);

    private final Color TEXT = new Color(75, 68, 75);
    private final Color SECONDARY_TEXT = new Color(130, 120, 130);

    /**
     * Stare interna asociata unui task adaugat in lista: task-ul propriu-zis,
     * sesiunea de focus si "manerele" (handles) catre timerele Java active,
     * ca sa poata fi anulate daca userul se razgandeste.
     */
    private static class TaskEntry {
        Task task;
        FocusSession session;
        Timer autoStopHandle;   // Timer pornit din startAfterDelay
        Timer scheduleHandle;   // Timer pornit din scheduleAt
        Timer reminderHandle;   // Timer pornit din startPeriodic
        JLabel statusLabel;
        JPanel cardPanel;
    }

    public MainWindow() {
        super("Focus Timer");

        timerManager = new TimerManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 760);
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

        JLabel subtitle = new JLabel("Adaugă task-uri și controlează-le exact cum vrei.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(SECONDARY_TEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(15));
        header.add(buildAddTaskPanel());

        add(header, BorderLayout.NORTH);

        taskListContainer = new JPanel();
        taskListContainer.setLayout(new BoxLayout(taskListContainer, BoxLayout.Y_AXIS));
        taskListContainer.setBackground(BACKGROUND);
        taskListContainer.setBorder(new EmptyBorder(5, 25, 10, 25));

        emptyStateLabel = new JLabel("Nicio sarcină încă. Adaugă un task mai sus ca să începi.");
        emptyStateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        emptyStateLabel.setForeground(SECONDARY_TEXT);
        emptyStateLabel.setBorder(new EmptyBorder(10, 5, 10, 5));
        emptyStateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        taskListContainer.add(emptyStateLabel);

        JScrollPane taskScroll = new JScrollPane(taskListContainer);
        taskScroll.setBorder(BorderFactory.createEmptyBorder());
        taskScroll.getVerticalScrollBar().setUnitIncrement(16);
        taskScroll.setBackground(BACKGROUND);

        add(taskScroll, BorderLayout.CENTER);

        log = new JTextArea();
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));
        log.setForeground(TEXT);
        log.setBackground(WHITE);
        log.setBorder(new EmptyBorder(10, 10, 10, 10));
        log.setRows(8);

        JScrollPane logScroll = new JScrollPane(log);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        logScroll.setBackground(WHITE);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(WHITE);
        logPanel.setBorder(new EmptyBorder(0, 25, 20, 25));
        logPanel.setPreferredSize(new Dimension(0, 180));

        JLabel logTitle = new JLabel("  Activitate");
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        logTitle.setForeground(TEXT);
        logTitle.setBorder(new EmptyBorder(8, 0, 8, 0));

        logPanel.add(logTitle, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        add(logPanel, BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------
    // Formular: adaugarea unui task nou in lista (nimic pornit automat).
    // ---------------------------------------------------------------
    private JPanel buildAddTaskPanel() {

        JPanel panel = createCard(PINK_DARK);

        JLabel titleLbl = createTitle("Adaugă un task nou");
        JLabel description = createDescription(
                "Task-ul se adaugă în listă, dar nu pornește automat — tu alegi când."
        );

        JTextField titluField = new JTextField(16);
        styleazaTextField(titluField);

        JSpinner oreSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(25, 0, 59, 1));
        oreSpinner.setPreferredSize(new Dimension(55, 28));
        minuteSpinner.setPreferredSize(new Dimension(55, 28));

        JButton adauga = createButton("Adaugă în listă", PINK_DARK);
        adauga.addActionListener(e -> {
            String titlu = titluField.getText().trim();
            int ore = (int) oreSpinner.getValue();
            int minute = (int) minuteSpinner.getValue();

            if (titlu.isEmpty()) {
                appendLog("Titlul task-ului nu poate fi gol.");
                return;
            }
            if (ore == 0 && minute == 0) {
                appendLog("Durata task-ului trebuie să fie mai mare decât 0.");
                return;
            }

            adaugaTaskInLista(titlu, ore, minute);
            titluField.setText("");
        });

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setBackground(WHITE);

        JLabel titluLbl = new JLabel("Titlu:");
        titluLbl.setForeground(TEXT);
        JLabel oreLbl = new JLabel("Ore:");
        oreLbl.setForeground(TEXT);
        JLabel minLbl = new JLabel("Minute:");
        minLbl.setForeground(TEXT);

        controls.add(titluLbl);
        controls.add(titluField);
        controls.add(oreLbl);
        controls.add(oreSpinner);
        controls.add(minLbl);
        controls.add(minuteSpinner);
        controls.add(adauga);

        panel.add(titleLbl);
        panel.add(description);
        panel.add(Box.createVerticalStrut(10));
        panel.add(controls);

        return panel;
    }

    private void adaugaTaskInLista(String titlu, int ore, int minute) {
        int durataTotalaMinute = ore * 60 + minute;
        Task task = new Task(titlu, durataTotalaMinute);
        FocusSession session = new FocusSession(task, ore, minute);

        TaskEntry entry = new TaskEntry();
        entry.task = task;
        entry.session = session;

        JPanel card = buildTaskCard(entry);
        entry.cardPanel = card;

        if (taskEntries.isEmpty()) {
            taskListContainer.remove(emptyStateLabel);
        }

        taskEntries.add(entry);
        taskListContainer.add(card);
        taskListContainer.add(Box.createVerticalStrut(12));
        taskListContainer.revalidate();
        taskListContainer.repaint();

        appendLog("Task adăugat: \"" + titlu + "\" (" + durataText(session) + ").");
    }

    // ---------------------------------------------------------------
    // Cardul unui task din lista: acopera cele 3 cerinte ale laboratorului,
    // aplicate exact acestui task, plus controale de baza (start/pauza/finalizare/stergere).
    // ---------------------------------------------------------------
    private JPanel buildTaskCard(TaskEntry entry) {

        JPanel panel = createCard(LILAC);

        JLabel titleLbl = createTitle(entry.task.getTitlu());
        JLabel durataLbl = createDescription("Durată: " + durataText(entry.session));

        entry.statusLabel = createDescription("Stare: De pornit");
        entry.statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        // ----- Rand 1: control de baza -----
        JButton start = createButton("Start acum", PINK_DARK);
        JButton pauza = createButton("Pauză", SECONDARY_TEXT);
        JButton finalizeaza = createButton("Finalizează", LILAC_DARK);
        JButton sterge = createButton("Șterge", new Color(200, 90, 90));

        start.addActionListener(e -> {
            if (entry.task.isCompleted()) {
                appendLog("\"" + entry.task.getTitlu() + "\" este deja finalizat.");
                return;
            }
            if (entry.session.isActiv()) {
                appendLog("\"" + entry.task.getTitlu() + "\" este deja activ.");
                return;
            }
            porneesteAcum(entry);
        });

        pauza.addActionListener(e -> {
            if (!entry.session.isActiv()) {
                appendLog("\"" + entry.task.getTitlu() + "\" nu este activ momentan.");
                return;
            }
            entry.session.pause();
            entry.statusLabel.setText("Stare: Pauzat");
            appendLog("\"" + entry.task.getTitlu() + "\" a fost pus pe pauză.");
        });

        finalizeaza.addActionListener(e -> {
            if (entry.task.isCompleted()) {
                appendLog("\"" + entry.task.getTitlu() + "\" este deja finalizat.");
                return;
            }
            anuleazaTimer(entry.autoStopHandle);
            entry.autoStopHandle = null;
            anuleazaTimer(entry.scheduleHandle);
            entry.scheduleHandle = null;

            entry.session.stop(); // marcheaza task-ul completed
            entry.statusLabel.setText("Stare: Finalizat (manual)");
            appendLog("\"" + entry.task.getTitlu() + "\" a fost finalizat manual.");
        });

        sterge.addActionListener(e -> {
            anuleazaTimer(entry.autoStopHandle);
            anuleazaTimer(entry.scheduleHandle);
            anuleazaTimer(entry.reminderHandle);

            taskListContainer.remove(entry.cardPanel);
            taskEntries.remove(entry);

            if (taskEntries.isEmpty()) {
                taskListContainer.add(emptyStateLabel);
            }

            taskListContainer.revalidate();
            taskListContainer.repaint();
            appendLog("Task șters: \"" + entry.task.getTitlu() + "\".");
        });

        JPanel randControale = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        randControale.setBackground(WHITE);
        randControale.add(start);
        randControale.add(pauza);
        randControale.add(finalizeaza);
        randControale.add(sterge);

        // ----- Rand 2: programare la o data si ora exacta (zile/saptamani/luni in viitor) -----
        JPanel randProgramare = buildProgramarePanel(entry);

        // ----- Rand 3: amintiri periodice cu mesaj personalizat -----
        JPanel randAmintiri = buildAmintiriPanel(entry);

        panel.add(titleLbl);
        panel.add(durataLbl);
        panel.add(entry.statusLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(randControale);
        panel.add(Box.createVerticalStrut(6));
        panel.add(randProgramare);
        panel.add(Box.createVerticalStrut(6));
        panel.add(randAmintiri);

        return panel;
    }

    // 1. REACTIE DUPA UN INTERVAL DE TIMP (Timer.schedule cu delay)
    private void porneesteAcum(TaskEntry entry) {
        entry.session.start();
        long delayMillis = entry.session.getTotalMillis();

        entry.statusLabel.setText("Stare: Activ (se termină automat în " + durataText(entry.session) + ")");
        appendLog("\"" + entry.task.getTitlu() + "\" pornit acum.");

        entry.autoStopHandle = timerManager.startAfterDelay(() -> {
            SwingUtilities.invokeLater(() -> {
                entry.session.stop();
                entry.autoStopHandle = null;
                entry.statusLabel.setText("Stare: Finalizat (timp expirat)");
                appendLog("[INTERVAL] \"" + entry.task.getTitlu() + "\" finalizat automat.");
            });
        }, delayMillis);
    }

    // 2. REACTIE LA UN ANUMIT TIMP (Timer.schedule cu Date, oricat de departe in viitor)
    private JPanel buildProgramarePanel(TaskEntry entry) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(WHITE);

        JLabel label = new JLabel("Programează pornirea:");
        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));

        Calendar implicit = Calendar.getInstance();
        implicit.add(Calendar.HOUR_OF_DAY, 1);

        SpinnerDateModel dataModel = new SpinnerDateModel(implicit.getTime(), null, null, Calendar.MINUTE);
        JSpinner dataSpinner = new JSpinner(dataModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dataSpinner, "dd.MM.yyyy HH:mm");
        dataSpinner.setEditor(editor);
        dataSpinner.setPreferredSize(new Dimension(150, 28));

        JButton programeaza = createButton("Programează", LILAC_DARK);
        JButton anuleaza = createButton("Anulează programarea", SECONDARY_TEXT);

        programeaza.addActionListener(e -> {
            if (entry.scheduleHandle != null) {
                appendLog("Există deja o programare activă pentru \"" + entry.task.getTitlu()
                        + "\". Anulați-o întâi.");
                return;
            }
            if (entry.task.isCompleted()) {
                appendLog("\"" + entry.task.getTitlu() + "\" este deja finalizat.");
                return;
            }

            Date target = (Date) dataSpinner.getValue();
            if (target.before(new Date())) {
                appendLog("Data aleasă trebuie să fie în viitor.");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            appendLog("\"" + entry.task.getTitlu() + "\" programat să pornească la "
                    + sdf.format(target) + ".");
            entry.statusLabel.setText("Stare: Programat pentru " + sdf.format(target));

            entry.scheduleHandle = timerManager.scheduleAt(() -> {
                SwingUtilities.invokeLater(() -> {
                    entry.scheduleHandle = null;
                    if (entry.session.isActiv() || entry.task.isCompleted()) {
                        appendLog("[LA ORA] \"" + entry.task.getTitlu()
                                + "\" nu a pornit (deja activ sau finalizat).");
                        return;
                    }
                    porneesteAcum(entry);
                    appendLog("[LA ORA] \"" + entry.task.getTitlu() + "\" pornit conform programării.");
                });
            }, target);
        });

        anuleaza.addActionListener(e -> {
            if (entry.scheduleHandle == null) {
                appendLog("Nu există nicio programare activă pentru \"" + entry.task.getTitlu() + "\".");
                return;
            }
            anuleazaTimer(entry.scheduleHandle);
            entry.scheduleHandle = null;
            entry.statusLabel.setText("Stare: De pornit");
            appendLog("Programarea pentru \"" + entry.task.getTitlu() + "\" a fost anulată.");
        });

        row.add(label);
        row.add(dataSpinner);
        row.add(programeaza);
        row.add(anuleaza);
        return row;
    }

    // 3. REACTIE CU O PERIOADA INDICATA (Timer.scheduleAtFixedRate), mesaj ales de utilizator
    private JPanel buildAmintiriPanel(TaskEntry entry) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(WHITE);

        JLabel label = new JLabel("Amintire:");
        label.setForeground(TEXT);

        JTextField mesajField = new JTextField(14);
        styleazaTextField(mesajField);
        mesajField.setToolTipText("Ce vrei sa iti reamintesti? Ex: \"Bea apa\", \"Verifica postura\"");

        JLabel laFiecareLbl = new JLabel("la fiecare (min):");
        laFiecareLbl.setForeground(TEXT);

        JSpinner perioadaSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 120, 1));
        perioadaSpinner.setPreferredSize(new Dimension(55, 28));

        JButton startAmintiri = createButton("Start", BLUE_DARK);
        JButton stopAmintiri = createButton("Stop", SECONDARY_TEXT);

        startAmintiri.addActionListener(e -> {
            if (entry.reminderHandle != null) {
                appendLog("Amintirile pentru \"" + entry.task.getTitlu() + "\" rulează deja.");
                return;
            }

            String mesaj = mesajField.getText().trim();
            if (mesaj.isEmpty()) {
                appendLog("Scrie mai întâi mesajul amintirii pentru \"" + entry.task.getTitlu() + "\".");
                return;
            }

            int minute = (int) perioadaSpinner.getValue();
            long periodMillis = minute * 60L * 1000L;

            entry.reminderHandle = timerManager.startPeriodic(() -> {
                SwingUtilities.invokeLater(() -> appendLog(
                        "[AMINTIRE - " + entry.task.getTitlu() + "] " + mesaj));
            }, periodMillis, periodMillis);

            appendLog("Amintiri pornite pentru \"" + entry.task.getTitlu()
                    + "\", la fiecare " + minute + " minute: \"" + mesaj + "\".");
        });

        stopAmintiri.addActionListener(e -> {
            if (entry.reminderHandle == null) {
                appendLog("Nu există nicio amintire activă pentru \"" + entry.task.getTitlu() + "\".");
                return;
            }
            anuleazaTimer(entry.reminderHandle);
            entry.reminderHandle = null;
            appendLog("Amintiri oprite pentru \"" + entry.task.getTitlu() + "\".");
        });

        row.add(label);
        row.add(mesajField);
        row.add(laFiecareLbl);
        row.add(perioadaSpinner);
        row.add(startAmintiri);
        row.add(stopAmintiri);
        return row;
    }

    private void anuleazaTimer(Timer timer) {
        if (timer != null) {
            timer.cancel();
        }
    }

    private String durataText(FocusSession session) {
        long totalMinute = session.getTotalMillis() / 60000;
        long ore = totalMinute / 60;
        long minute = totalMinute % 60;
        return ore + " h " + minute + " min";
    }

    private JPanel createCard(Color accent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(WHITE);
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(4, 0, 0, 0, accent),
                        new EmptyBorder(12, 15, 14, 15)
                )
        );
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(TEXT);
        return label;
    }

    private JLabel createDescription(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(SECONDARY_TEXT);
        return label;
    }

    private void styleazaTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(new Color(250, 248, 250));
        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(225, 218, 225)),
                        new EmptyBorder(5, 7, 5, 7)
                )
        );
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(7, 14, 7, 14));
        return button;
    }

    private void appendLog(String mesaj) {
        if (log != null) {
            log.append(mesaj + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
}