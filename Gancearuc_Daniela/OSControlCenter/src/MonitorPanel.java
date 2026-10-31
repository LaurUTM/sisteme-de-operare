import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * LAB 5 - Funcții de nivel înalt ale sistemului de operare.
 *
 *  - încărcarea procesorului și memoria RAM la fiecare N secunde (OperatingSystemMXBean)
 *    -> citirea se face cu un java.util.Timer, adică exact mecanismul din LAB 1
 *  - jurnal într-un fișier text
 *  - lansarea unui editor de text și așteptarea codului de ieșire (ProcessBuilder + waitFor)
 *  - rularea comenzilor de sistem (ping, ls, ip a ...) și citirea rezultatului
 *  - adăugarea programului la pornirea automată (Linux: ~/.config/autostart, Windows: registru)
 *  - repornirea sistemului
 */
public class MonitorPanel extends JPanel {

    private static final File HOME = new File(System.getProperty("user.home"));
    private static final File LOG_FILE = new File(HOME, "system_monitor_log.txt");

    private final OperatingSystemMXBean os =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private final JProgressBar cpuBar = bar();
    private final JProgressBar ramBar = bar();
    private final JLabel cpuText = new JLabel("-");
    private final JLabel ramText = new JLabel("-");
    private final JSpinner interval = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
    private final JCheckBox saveLog = new JCheckBox("Salvează în jurnal: " + LOG_FILE.getPath());
    private final HistoryChart history = new HistoryChart();
    private final JTextArea output = Theme.logArea();
    private final JLabel autostartStatus = new JLabel();

    private Timer monitorTimer;

    public MonitorPanel() {
        super(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(monitorCard(), BorderLayout.NORTH);
        add(processCard(), BorderLayout.CENTER);

        interval.addChangeListener(e -> startMonitoring());
        startMonitoring();
        updateAutostartStatus();
    }

    // =====================================================================
    // Monitorizarea CPU / RAM
    // =====================================================================

    private JComponent monitorCard() {
        JPanel bars = new JPanel(new GridBagLayout());
        bars.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 4, 10);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; bars.add(bold("Procesor (CPU)"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; bars.add(cpuBar, c);
        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE; bars.add(cpuText, c);
        c.gridx = 0; c.gridy = 1; bars.add(bold("Memorie RAM"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; bars.add(ramBar, c);
        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE; bars.add(ramText, c);

        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        options.setOpaque(false);
        options.add(new JLabel("Actualizare la fiecare"));
        options.add(interval);
        options.add(new JLabel("secunde"));
        saveLog.setOpaque(false);
        options.add(saveLog);

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.add(bars, BorderLayout.NORTH);
        left.add(options, BorderLayout.SOUTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.add(left);
        body.add(history);
        return Theme.card("Monitor sistem în timp real (Lab 5)", body);
    }

    /** Pornește (sau repornește) timerul de citire, cu perioada din căsuță. */
    private void startMonitoring() {
        if (monitorTimer != null) monitorTimer.cancel();
        long periodMs = (Integer) interval.getValue() * 1000L;
        monitorTimer = new Timer("monitor", true);
        monitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { readResources(); }
        }, 0, periodMs);
    }

    @SuppressWarnings("deprecation") // metodele vechi merg și pe Java 11, și pe Java 17/21
    private void readResources() {
        double cpu = os.getSystemCpuLoad() * 100;         // 0..100 %
        if (cpu < 0 || Double.isNaN(cpu)) cpu = 0;          // prima citire poate fi -1

        long totalMb = meminfoMb("MemTotal:", os.getTotalPhysicalMemorySize() / (1024 * 1024));
        long freeMb = meminfoMb("MemAvailable:", os.getFreePhysicalMemorySize() / (1024 * 1024));
        long usedMb = Math.max(0, totalMb - freeMb);
        double ramPercent = totalMb > 0 ? usedMb * 100.0 / totalMb : 0;

        final double cpuF = cpu;
        SwingUtilities.invokeLater(() -> {
            cpuBar.setValue((int) Math.round(cpuF));
            cpuBar.setString(String.format("%.1f %%", cpuF));
            cpuBar.setForeground(cpuF > 80 ? Theme.WARN : Theme.ACCENT);
            cpuText.setText(Runtime.getRuntime().availableProcessors() + " nuclee");
            ramBar.setValue((int) Math.round(ramPercent));
            ramBar.setString(String.format("%.1f %%", ramPercent));
            ramText.setText(Util.formatMb(usedMb) + " folosită / " + Util.formatMb(freeMb) + " liberă");
            history.add(cpuF, ramPercent);
        });

        if (saveLog.isSelected()) {
            String entry = String.format("%s - CPU: %.2f%% | Memorie: %d MB folosită / %d MB total",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), cpu, usedMb, totalMb);
            try (FileWriter w = new FileWriter(LOG_FILE, true)) { // true = adaugă la sfârșit
                w.write(entry + "\n");
            } catch (Exception e) {
                print("Nu pot scrie în jurnal: " + e.getMessage());
            }
        }
    }

    /**
     * În Linux, memoria "liberă" nu include cache-ul, care se eliberează oricând.
     * De aceea citim MemTotal și MemAvailable din /proc/meminfo - aceleași valori pe care le arată comanda free.
     * În Windows folosim valorile date de OperatingSystemMXBean.
     */
    private long meminfoMb(String key, long fallback) {
        if (!Util.isLinux()) return fallback;
        try {
            for (String line : Files.readAllLines(Paths.get("/proc/meminfo"))) {
                if (line.startsWith(key)) {
                    return Long.parseLong(line.replaceAll("\\D+", "")) / 1024; // kB -> MB
                }
            }
        } catch (Exception ignored) { }
        return fallback;
    }

    // =====================================================================
    // Procese și comenzi de sistem
    // =====================================================================

    private JComponent processCard() {
        // --- editor de text ---
        JButton editor = new JButton("Deschide editorul de text");
        editor.addActionListener(e -> openEditor());

        // --- comenzi ---
        JTextField command = new JTextField(Util.isWindows() ? "ping -n 4 8.8.8.8" : "ping -c 4 8.8.8.8", 26);
        command.setFont(Theme.MONO.deriveFont(13f));
        JCheckBox saveOutput = new JCheckBox("salvează rezultatul în fișier");
        saveOutput.setOpaque(false);
        JButton run = new JButton("Rulează");
        run.addActionListener(e -> runCommand(command.getText(), saveOutput.isSelected()));
        command.addActionListener(e -> run.doClick());

        JComboBox<String> examples = new JComboBox<>(Util.isWindows()
            ? new String[]{"Exemple...", "ping -n 4 8.8.8.8", "tracert -d -h 8 8.8.8.8", "ipconfig", "tasklist", "systeminfo"}
            : new String[]{"Exemple...", "ping -c 4 8.8.8.8", "traceroute -m 8 8.8.8.8", "ip a", "free -h", "df -h", "ps aux --sort=-%cpu", "uptime"});
        examples.addActionListener(e -> {
            if (examples.getSelectedIndex() > 0) command.setText((String) examples.getSelectedItem());
        });

        // --- autostart ---
        JButton addAuto = new JButton("Adaugă la pornire automată");
        addAuto.addActionListener(e -> setAutostart(true));
        JButton removeAuto = new JButton("Elimină");
        removeAuto.addActionListener(e -> setAutostart(false));

        // --- repornire ---
        JButton reboot = new JButton("Repornește sistemul");
        reboot.setForeground(new Color(0xB3261E));
        reboot.addActionListener(e -> reboot());

        JButton clear = new JButton("Golește");
        clear.addActionListener(e -> output.setText(""));

        JPanel r1 = flow(new JLabel("Comandă:"), command, examples, saveOutput, run);
        JPanel r2 = flow(editor, new JLabel("   "), addAuto, removeAuto, autostartStatus);
        JPanel r3 = flow(reboot, clear);

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(r1);
        controls.add(Box.createVerticalStrut(6));
        controls.add(r2);
        controls.add(Box.createVerticalStrut(6));
        controls.add(r3);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.add(controls, BorderLayout.NORTH);
        body.add(new JScrollPane(output), BorderLayout.CENTER);
        return Theme.card("Procese și comenzi de sistem (ProcessBuilder)", body);
    }

    /** Sarcina 1 din Lab 5: lansează editorul, așteaptă să fie închis, afișează codul de ieșire. */
    private void openEditor() {
        File notes = new File(HOME, "notite_lab.txt");
        String[] candidates = Util.isWindows()
            ? new String[]{"notepad.exe"}
            : new String[]{"gnome-text-editor", "gedit", "mousepad", "kate", "xed", "pluma", "leafpad"};
        String editor = null;
        for (String c : candidates) {
            if (Util.isWindows() || Util.commandExists(c)) { editor = c; break; }
        }
        if (editor == null) {
            print("Nu am găsit niciun editor grafic. Instalează unul: sudo apt install gedit");
            return;
        }
        final String chosen = editor;
        // pe un fir separat, ca fereastra să nu "înghețe" cât timp așteptăm
        new Thread(() -> {
            try {
                if (!notes.exists()) notes.createNewFile();
                print("Pornesc " + chosen + " cu fișierul " + notes.getPath() + " ...");
                Process p = new ProcessBuilder(chosen, notes.getPath()).start();
                print("Proces pornit (PID " + p.pid() + "). Aștept să închizi editorul...");
                int code = p.waitFor();
                print("Editorul s-a închis. Codul de terminare al procesului: " + code
                    + (code == 0 ? " (succes)" : " (eroare)"));
            } catch (Exception ex) {
                print("Eroare: " + ex.getMessage());
            }
        }).start();
    }

    /** Sarcina 6 din Lab 5: rulează o comandă și afișează (și salvează) rezultatul. */
    private void runCommand(String text, boolean saveToFile) {
        List<String> parts = Util.splitCommand(text.trim());
        if (parts.isEmpty()) return;
        new Thread(() -> {
            StringBuilder all = new StringBuilder();
            try {
                print("$ " + text);
                ProcessBuilder pb = new ProcessBuilder(parts);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        appendRaw(line);       // afișăm rând cu rând, pe măsură ce vin
                        all.append(line).append('\n');
                    }
                }
                int code = p.waitFor();
                print("Comanda s-a terminat cu codul " + code);
                if (saveToFile) {
                    String name = parts.get(0).replaceAll("[^A-Za-z0-9]", "") + "_"
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                    File f = new File(HOME, name);
                    Files.write(f.toPath(), all.toString().getBytes());
                    print("Rezultat salvat în " + f.getPath());
                }
            } catch (Exception ex) {
                print("Nu pot rula comanda: " + ex.getMessage());
            }
        }).start();
    }

    // ---------------- pornire automată ----------------

    private File autostartFile() {
        return new File(HOME, ".config/autostart/os-control-center.desktop");
    }

    /** Comanda cu care se pornește programul acesta (din .jar sau din folderul cu .class). */
    private String launchCommand(boolean windows) {
        try {
            File where = new File(OSControlCenter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String java = windows ? "javaw" : "java";
            if (where.isFile()) return java + " -jar \"" + where.getAbsolutePath() + "\"";
            return java + " -cp \"" + where.getAbsolutePath() + "\" OSControlCenter";
        } catch (Exception e) {
            return "java -jar OSControlCenter.jar";
        }
    }

    private void setAutostart(boolean enable) {
        try {
            if (Util.isWindows()) {
                // Windows: o valoare în registru, în cheia Run a utilizatorului curent
                String key = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
                if (enable) {
                    Util.runCommand("reg", "add", key, "/v", "OSControlCenter", "/t", "REG_SZ", "/d", launchCommand(true), "/f");
                    print("Adăugat în registru: " + key + "\\OSControlCenter");
                } else {
                    Util.runCommand("reg", "delete", key, "/v", "OSControlCenter", "/f");
                    print("Șters din registru.");
                }
            } else {
                // Linux: un fișier .desktop în ~/.config/autostart
                File f = autostartFile();
                if (enable) {
                    f.getParentFile().mkdirs();
                    String desktop = "[Desktop Entry]\n"
                        + "Type=Application\n"
                        + "Name=OS Control Center\n"
                        + "Comment=Proiect laborator Sisteme de operare\n"
                        + "Exec=" + launchCommand(false) + "\n"
                        + "Hidden=false\n"
                        + "X-GNOME-Autostart-enabled=true\n";
                    Files.write(f.toPath(), desktop.getBytes());
                    print("Creat fișierul " + f.getPath() + "\nLa următoarea autentificare programul pornește singur.");
                } else {
                    print(f.delete() ? "Șters " + f.getPath() : "Programul nu era în pornirea automată.");
                }
            }
        } catch (Exception ex) {
            print("Eroare la pornirea automată: " + ex.getMessage());
        }
        updateAutostartStatus();
    }

    private void updateAutostartStatus() {
        boolean on;
        if (Util.isWindows()) {
            on = Util.runCommand("reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run", "/v", "OSControlCenter")
                     .contains("OSControlCenter");
        } else {
            on = autostartFile().exists();
        }
        autostartStatus.setText(on ? "  ● activată" : "  ○ dezactivată");
        autostartStatus.setForeground(on ? Theme.OK : Theme.MUTED);
    }

    // ---------------- repornire ----------------

    private void reboot() {
        int answer = JOptionPane.showConfirmDialog(this,
            "Sigur vrei să repornești sistemul?\nSalvează tot ce ai deschis.\n(Recomandat doar în mașina virtuală.)",
            "Repornire", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        // înainte de repornire scriem un mesaj într-un fișier (sarcina 3 din Lab 5)
        File desktop = new File(HOME, "Desktop");
        File note = new File(desktop.isDirectory() ? desktop : HOME, "repornire.txt");
        try (FileWriter w = new FileWriter(note, true)) {
            w.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " - sistemul a fost repornit din OS Control Center\n");
        } catch (Exception ignored) { }

        String result;
        if (Util.isWindows()) {
            result = Util.runCommand("shutdown", "/r", "/t", "5");
        } else {
            result = Util.runCommand("systemctl", "reboot"); // pe Debian merge fără sudo pentru utilizatorul logat
        }
        print("Comanda de repornire a fost trimisă. " + result
            + "\nDacă nu se întâmplă nimic, rulează din terminal: sudo reboot");
    }

    // ---------------- ajutătoare ----------------

    private void print(String text) {
        SwingUtilities.invokeLater(() -> {
            output.append(Util.now() + "  " + text + "\n");
            output.setCaretPosition(output.getDocument().getLength());
        });
    }

    private void appendRaw(String text) {
        SwingUtilities.invokeLater(() -> {
            output.append("          " + text + "\n");
            output.setCaretPosition(output.getDocument().getLength());
        });
    }

    private static JProgressBar bar() {
        JProgressBar b = new JProgressBar(0, 100);
        b.setStringPainted(true);
        b.setPreferredSize(new Dimension(220, 22));
        return b;
    }

    private static JLabel bold(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.BOLD);
        return l;
    }

    private static JPanel flow(JComponent... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        for (JComponent c : items) p.add(c);
        return p;
    }

    /** Grafic mic cu ultimele 60 de citiri (CPU albastru, RAM portocaliu). */
    private static class HistoryChart extends JPanel {
        private final List<double[]> points = new ArrayList<>();

        HistoryChart() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(300, 110));
            setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        }

        void add(double cpu, double ram) {
            points.add(new double[]{cpu, ram});
            if (points.size() > 60) points.remove(0);
            repaint();
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = 8, top = 22;
            g.setFont(Theme.BASE.deriveFont(11f));
            g.setColor(Theme.ACCENT);  g.drawString("● CPU %", pad, 15);
            g.setColor(Theme.PALETTE[1]); g.drawString("● RAM %", pad + 64, 15);
            g.setColor(Theme.MUTED);   g.drawString("ultimele 60 de citiri", w - 130, 15);
            g.setColor(new Color(0xEEEEEE));
            for (int i = 0; i <= 4; i++) {
                int y = top + (h - top - pad) * i / 4;
                g.drawLine(pad, y, w - pad, y);
            }
            if (points.size() < 2) return;
            g.setStroke(new BasicStroke(2f));
            for (int series = 0; series < 2; series++) {
                g.setColor(series == 0 ? Theme.ACCENT : Theme.PALETTE[1]);
                int prevX = -1, prevY = -1;
                for (int i = 0; i < points.size(); i++) {
                    int x = pad + (w - 2 * pad) * i / 59;
                    int y = top + (int) ((h - top - pad) * (1 - points.get(i)[series] / 100.0));
                    if (prevX >= 0) g.drawLine(prevX, prevY, x, y);
                    prevX = x; prevY = y;
                }
            }
        }
    }
}
