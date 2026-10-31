import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LAB 4 - Simulatorul planificatorului de procese (interfața).
 * Calculele sunt în Scheduler.java, aici doar citim datele și afișăm rezultatul.
 */
public class SchedulerPanel extends JPanel {

    // ordinea este aceeași ca în lucrare (sarcinile 1-7)
    private static final String[] ALGORITHMS = {
        "1. FCFS - Primul venit, primul servit",
        "2. SJF - Cea mai scurtă sarcină prima",
        "3. SRTF - Cel mai scurt timp rămas",
        "4. Round Robin",
        "5. Planificare prioritară",
        "6. Cel mai scurt proces este următorul",
        "7. Planificare echitabilă",
    };

    private static final String[] DESCRIPTIONS = {
        "Procesele rulează în ordinea în care au venit, fiecare până la capăt.",
        "Se alege mereu procesul cu cel mai mic timp de execuție. Fără întreruperi.",
        "La fiecare cuant K se alege procesul cu cel mai mic timp RĂMAS.",
        "Fiecare proces primește pe rând un cuant K; dacă nu a terminat, trece la sfârșitul cozii.",
        "Rulează mai întâi procesul cu prioritatea cea mai mare. Format: A=5:1 (timp:prioritate).",
        "Ca SRTF: după fiecare cuant K rulează procesul cel mai scurt rămas.",
        "Fiecare rând = un utilizator: „procent: procese”. Cuantele se împart după procente.",
    };

    /** Exemplele din îndrumar și sarcinile 1-5 din lucrare, pentru fiecare algoritm. */
    private static final String[][][] PRESETS = {
        { // FCFS  {eticheta, date, K}
            {"Exemplul din îndrumar", "A=4, B=20, C=12, D=8", "0"},
            {"Sarcina 1.1", "A=5, B=9, C=4, D=7", "0"},
            {"Sarcina 1.2", "P1=6, P2=3, P3=8, P4=2", "0"},
            {"Sarcina 1.3", "X=2, Y=6, Z=3, W=4", "0"},
            {"Sarcina 1.4", "T1=10, T2=2, T3=4, T4=6", "0"},
            {"Sarcina 1.5", "M=7, N=5, O=8, P=3", "0"},
        },
        { // SJF
            {"Exemplul din îndrumar", "A=4, B=20, C=12, D=8", "0"},
            {"Sarcina 2.1", "A=7, B=2, C=4, D=1", "0"},
            {"Sarcina 2.2", "P1=5, P2=3, P3=1, P4=2", "0"},
            {"Sarcina 2.3", "X=6, Y=4, Z=3, W=2", "0"},
            {"Sarcina 2.4", "T1=8, T2=3, T3=6, T4=1", "0"},
            {"Sarcina 2.5", "M=9, N=5, O=2, P=4", "0"},
        },
        { // SRTF
            {"Exemplul din îndrumar", "A=4, B=20, C=12, D=8", "10"},
            {"Sarcina 3.1", "A=8, B=4, C=3, D=7", "5"},
            {"Sarcina 3.2", "P1=10, P2=5, P3=6, P4=8", "4"},
            {"Sarcina 3.3", "X=7, Y=9, Z=5, W=3", "3"},
            {"Sarcina 3.4", "T1=11, T2=6, T3=4, T4=8", "6"},
            {"Sarcina 3.5", "M=9, N=2, O=8, P=5", "4"},
        },
        { // Round Robin
            {"Exemplul din îndrumar", "A=4, B=20, C=12, D=8", "13"},
            {"Sarcina 4.1", "A=10, B=15, C=20, D=25", "10"},
            {"Sarcina 4.2", "P1=12, P2=8, P3=16, P4=6", "7"},
            {"Sarcina 4.3", "X=9, Y=14, Z=11, W=13", "5"},
            {"Sarcina 4.4", "T1=20, T2=12, T3=9, T4=16", "8"},
            {"Sarcina 4.5", "M=18, N=22, O=13, P=17", "9"},
        },
        { // Prioritate
            {"Exemplul din îndrumar", "A=4:3, B=20:8, C=12:5, D=8:4", "0"},
            {"Sarcina 5.1", "A=5:1, B=9:3, C=6:2, D=8:4", "0"},
            {"Sarcina 5.2", "P1=10:2, P2=4:1, P3=8:3, P4=6:4", "0"},
            {"Sarcina 5.3", "X=12:2, Y=7:1, Z=5:3, W=9:4", "0"},
            {"Sarcina 5.4", "T1=11:3, T2=6:1, T3=9:2, T4=8:4", "0"},
            {"Sarcina 5.5", "M=13:4, N=10:3, O=7:2, P=5:1", "0"},
        },
        { // Cel mai scurt proces este următorul
            {"Exemplul din îndrumar", "A=4, B=20, C=12, D=8", "8"},
            {"Sarcina 6.1", "A=5, B=12, C=7, D=9", "8"},
            {"Sarcina 6.2", "A=15, B=3, C=8, D=20, E=4", "8"},
            {"Sarcina 6.3", "A=10, B=8, C=6, D=2", "8"},
            {"Sarcina 6.4", "A=6, B=16, C=8", "8"},
            {"Sarcina 6.5", "A=3, B=9, C=14, D=6", "8"},
        },
        { // Echitabilă
            {"Exemplul din îndrumar (50/25/25)", "50: A=10, B=12, C=7\n25: X=6, Y=10\n25: Z=26", "8"},
            {"Exemplul din îndrumar (33/33/33)", "33: A=10, B=12, C=7\n33: X=6, Y=10\n33: Z=26", "8"},
            {"Sarcina 7.1", "33: A=12, B=8\n33: X=10, Y=6\n33: Z=16", "8"},
            {"Sarcina 7.2", "50: A=10, B=5\n25: X=7, Y=3\n25: Z=12", "8"},
            {"Sarcina 7.3", "33: A=8, B=6, C=5\n33: X=12, Y=10\n33: Z=20", "8"},
            {"Sarcina 7.4", "40: A=15, B=10, C=8\n20: X=6, Y=5\n20: Z=18\n20: Q=14", "8"},
            {"Sarcina 7.5", "50: A=10, B=7, C=6\n25: X=10\n25: Z=14", "8"},
        },
    };

    private final JComboBox<String> algorithm = new JComboBox<>(ALGORITHMS);
    private final JComboBox<String> preset = new JComboBox<>();
    private final JTextArea input = new JTextArea(3, 30);
    private final JSpinner quantum = new JSpinner(new SpinnerNumberModel(8, 1, 1000, 1));
    private final JCheckBox biggerIsHigher = new JCheckBox("număr mai mare = prioritate mai mare", true);
    private final JTextArea description = Theme.note("");
    private final GanttChart gantt = new GanttChart();
    private final JTextArea summary = Theme.logArea();
    private JTabbedPane resultTabs;
    private final DefaultTableModel table = new DefaultTableModel(
        new Object[]{"Proces", "Timp execuție", "Se termină la", "Timp așteptare"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // ---- rularea în timp real (legătura cu Lab 1, 2 și 5) ----
    private final MonitorPanel monitor;
    private final JSpinner unitMs = new JSpinner(new SpinnerNumberModel(500, 100, 3000, 100));
    private final JButton liveButton = new JButton("▶ Rulează în timp real");
    private final JButton stopButton = new JButton("■ Oprește");
    private final JLabel liveStatus = new JLabel(" ");
    private final JTextArea liveLog = Theme.logArea();
    private LiveRunner live;

    public SchedulerPanel(MonitorPanel monitor) {
        super(new BorderLayout(12, 12));
        this.monitor = monitor;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        algorithm.setPrototypeDisplayValue("6. Cel mai scurt proces este următorul    ");
        preset.setPrototypeDisplayValue("Exemplul din îndrumar (33/33/33)    ");
        input.setFont(Theme.MONO.deriveFont(14f));
        input.setLineWrap(true);
        input.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        add(inputCard(), BorderLayout.NORTH);
        add(resultCard(), BorderLayout.CENTER);

        algorithm.addActionListener(e -> onAlgorithmChanged());
        preset.addActionListener(e -> loadPreset());
        onAlgorithmChanged();
        calculate();
    }

    private JComponent inputCard() {
        JButton calc = new JButton("Calculează");
        calc.setFont(Theme.BOLD);
        calc.addActionListener(e -> calculate());
        JButton compare = new JButton("Compară toți algoritmii");
        compare.addActionListener(e -> compareAll());

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.setOpaque(false);
        row1.add(new JLabel("Algoritm:"));
        row1.add(algorithm);
        row1.add(new JLabel("   Date din lucrare:"));
        row1.add(preset);

        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        options.setOpaque(false);
        options.add(new JLabel("Cuantul K:"));
        options.add(quantum);
        options.add(biggerIsHigher);
        options.add(calc);
        options.add(compare);

        liveButton.setFont(Theme.BOLD);
        liveButton.setForeground(Theme.OK);
        liveButton.addActionListener(e -> startLive());
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> { if (live != null) live.stop(); });
        liveStatus.setFont(Theme.BOLD);
        JPanel liveRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        liveRow.setOpaque(false);
        liveRow.add(new JLabel("Timp real:  1 unitate ="));
        liveRow.add(unitMs);
        liveRow.add(new JLabel("ms"));
        liveRow.add(liveButton);
        liveRow.add(stopButton);
        liveRow.add(liveStatus);

        JPanel south = new JPanel(new GridLayout(2, 1, 0, 6));
        south.setOpaque(false);
        south.add(options);
        south.add(liveRow);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.add(row1, BorderLayout.NORTH);
        JScrollPane inScroll = new JScrollPane(input);
        inScroll.setPreferredSize(new Dimension(500, 72));
        JPanel middle = new JPanel(new BorderLayout(0, 4));
        middle.setOpaque(false);
        middle.add(description, BorderLayout.NORTH);
        middle.add(inScroll, BorderLayout.CENTER);
        body.add(middle, BorderLayout.CENTER);
        body.add(south, BorderLayout.SOUTH);
        return Theme.card("Date de intrare (Lab 4)", body);
    }

    private JComponent resultCard() {
        JTable t = new JTable(table);
        t.setRowHeight(24);
        t.setFont(Theme.BASE);
        t.getTableHeader().setFont(Theme.BOLD);
        JScrollPane tableScroll = new JScrollPane(t);
        tableScroll.setPreferredSize(new Dimension(420, 160));

        summary.setFont(Theme.MONO.deriveFont(13f));
        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0));
        bottom.setOpaque(false);
        bottom.add(tableScroll);
        JTabbedPane right = new JTabbedPane();
        right.addTab("Calcule", new JScrollPane(summary));
        right.addTab("Jurnal timp real", new JScrollPane(liveLog));
        resultTabs = right;
        bottom.add(right);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.add(gantt, BorderLayout.NORTH);
        body.add(bottom, BorderLayout.CENTER);
        return Theme.card("Rezultat: diagrama Gantt și timpii", body);
    }

    private void onAlgorithmChanged() {
        int a = algorithm.getSelectedIndex();
        description.setText(DESCRIPTIONS[a]);
        quantum.setEnabled(a == 2 || a == 3 || a == 5 || a == 6);
        biggerIsHigher.setVisible(a == 4);
        preset.removeAllItems();
        for (String[] p : PRESETS[a]) preset.addItem(p[0]);
        preset.setSelectedIndex(0);
        loadPreset();
    }

    private void loadPreset() {
        int a = algorithm.getSelectedIndex(), p = preset.getSelectedIndex();
        if (p < 0) return;
        input.setText(PRESETS[a][p][1]);
        int k = Integer.parseInt(PRESETS[a][p][2]);
        if (k > 0) quantum.setValue(k);
    }

    /** Rulează algoritmul ales pe datele din căsuță. */
    private Scheduler.Result runSelected() {
        int a = algorithm.getSelectedIndex();
        int k = (Integer) quantum.getValue();
        String text = input.getText();
        switch (a) {
            case 0: return Scheduler.fcfs(Scheduler.parseProcs(text));
            case 1: return Scheduler.sjf(Scheduler.parseProcs(text));
            case 2: return Scheduler.shortestRemaining(Scheduler.parseProcs(text), k, "SRTF - Cel mai scurt timp rămas");
            case 3: return Scheduler.roundRobin(Scheduler.parseProcs(text), k);
            case 4: return Scheduler.priority(Scheduler.parseProcs(text), biggerIsHigher.isSelected());
            case 5: return Scheduler.shortestRemaining(Scheduler.parseProcs(text), k, "Cel mai scurt proces este următorul");
            default: return Scheduler.fairShare(Scheduler.parseUsers(text), k);
        }
    }

    private void calculate() {
        Scheduler.Result r;
        try {
            r = runSelected();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Date greșite: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }
        gantt.show(r);

        table.setRowCount(0);
        for (String p : r.completion.keySet()) {
            int b = r.burst.get(p), c = r.completion.get(p);
            table.addRow(new Object[]{p, b, c, c - b});
        }

        StringBuilder s = new StringBuilder();
        s.append(r.algorithm).append("\n\n");
        s.append("Ordinea de execuție:\n  ").append(r.order()).append("\n\n");
        s.append(String.format("Timp mediu de execuție (pe procese):%n  %.2f unități%n", r.avgCompletion()));
        s.append(String.format("Timp mediu de așteptare:%n  %.2f unități%n%n", r.avgWaiting()));
        s.append("Timp mediu - metoda din îndrumar\n(media sfârșiturilor tuturor segmentelor):\n  ")
         .append(r.segmentFormula())
         .append(String.format(" = %.2f", r.avgSegmentEnd()));
        summary.setText(s.toString());
        summary.setCaretPosition(0);
    }

    // =====================================================================
    // RULAREA ÎN TIMP REAL - aici se leagă toate lucrările:
    //   Lab 4 calculează planul, Lab 1 (Timer) îl execută,
    //   Lab 5 (ProcessBuilder + monitor) pornește procese reale și arată CPU,
    //   Lab 2: totul rulează pe nucleele mașinii virtuale.
    // =====================================================================

    private void startLive() {
        if (live != null && live.isActive()) return;
        Scheduler.Result plan;
        try {
            plan = runSelected();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Date greșite: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }
        calculate();                 // afișăm planul (diagrama, tabelul)
        gantt.setProgress(0);
        liveLog.setText("");
        resultTabs.setSelectedIndex(1);

        int cores = Runtime.getRuntime().availableProcessors();
        int ms = (Integer) unitMs.getValue();
        int total = plan.segments.get(plan.segments.size() - 1).end;
        String mode = LiveRunner.usesRealProcesses()
            ? "procese reale (sh), oprite cu kill -STOP și pornite cu kill -CONT"
            : "fire de execuție (Thread)";
        logLive(String.format("%s: %d unități x %d ms = %.1f secunde", plan.algorithm, total, ms, total * ms / 1000.0));
        logLive("Mod: " + mode);
        logLive(String.format("VM are %d nuclee -> un proces ocupă ~1 nucleu, adică ~%.0f %% CPU", cores, 100.0 / cores));
        monitor.liveStarted("pornesc „" + plan.algorithm + "” în timp real (" + total + " unități)");

        liveButton.setEnabled(false);
        stopButton.setEnabled(true);
        algorithm.setEnabled(false);

        live = new LiveRunner(plan, ms, new LiveRunner.Listener() {
            @Override public void onTick(int time, Scheduler.Segment current) {
                double cpu = monitor.lastCpu();
                SwingUtilities.invokeLater(() -> {
                    gantt.setProgress(time + 1);
                    liveStatus.setText(String.format("t = %d / %d   rulează: %s   CPU: %.0f %%",
                        time + 1, total, current != null ? current.name : "-", cpu));
                });
            }
            @Override public void onSwitch(int time, String from, String to, String id) {
                monitor.liveRunning(to, gantt.colorOf(to));
                String text = from == null
                    ? String.format("t=%-3d pornește %s (%s)", time, to, id)
                    : String.format("t=%-3d comutare de context: %s -> %s (%s)", time, from, to, id);
                logLive(text);
                monitor.liveMessage(text);
            }
            @Override public void onFinish(int time, String process) {
                String text = String.format("t=%-3d procesul %s s-a terminat", time, process);
                logLive(text);
                monitor.liveMessage(text);
            }
            @Override public void onEnd(boolean completed) {
                String text = completed ? "Gata: toate procesele s-au terminat." : "Oprit manual.";
                logLive(text);
                monitor.liveStopped(text);
                SwingUtilities.invokeLater(() -> {
                    liveButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    algorithm.setEnabled(true);
                    liveStatus.setText(completed ? "Gata ✔" : "Oprit");
                    if (!completed) gantt.setProgress(-1);
                });
            }
        });
        live.start();
    }

    private void logLive(String text) {
        SwingUtilities.invokeLater(() -> {
            liveLog.append(Util.now() + "  " + text + "\n");
            liveLog.setCaretPosition(liveLog.getDocument().getLength());
        });
    }

    /** Apelat la închiderea programului, ca să nu rămână procese pornite. */
    public void shutdown() {
        if (live != null) live.stop();
    }

    /** Rulează aceleași procese prin toți algoritmii și arată un tabel de comparație. */
    private void compareAll() {
        List<Scheduler.Proc> procs;
        try {
            if (algorithm.getSelectedIndex() == 6) {
                procs = new ArrayList<>();
                for (Scheduler.User u : Scheduler.parseUsers(input.getText())) procs.addAll(u.procs);
            } else {
                procs = Scheduler.parseProcs(input.getText());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Date greșite: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int k = (Integer) quantum.getValue();
        List<Scheduler.Result> results = new ArrayList<>();
        results.add(Scheduler.fcfs(procs));
        results.add(Scheduler.sjf(procs));
        results.add(Scheduler.shortestRemaining(procs, k, "SRTF"));
        results.add(Scheduler.roundRobin(procs, k));
        results.add(Scheduler.shortestRemaining(procs, k, "Cel mai scurt proces urm."));

        DefaultTableModel m = new DefaultTableModel(
            new Object[]{"Algoritm", "Ordinea", "Timp mediu execuție", "Timp mediu așteptare", "Media (îndrumar)"}, 0);
        for (Scheduler.Result r : results) {
            m.addRow(new Object[]{r.algorithm, r.order(),
                String.format("%.2f", r.avgCompletion()),
                String.format("%.2f", r.avgWaiting()),
                String.format("%.2f", r.avgSegmentEnd())});
        }
        JTable t = new JTable(m);
        t.setRowHeight(24);
        t.getColumnModel().getColumn(0).setPreferredWidth(200);
        t.getColumnModel().getColumn(1).setPreferredWidth(260);
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(860, 170));

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.add(sp, BorderLayout.CENTER);
        p.add(Theme.note("Același set de procese, K = " + k + ". Cel mai mic timp mediu de așteptare îl dă de obicei SJF; "
            + "Round Robin este mai „corect” pentru sisteme interactive, dar are timp mediu mai mare."), BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, p, "Comparația algoritmilor", JOptionPane.PLAIN_MESSAGE);
    }
}
