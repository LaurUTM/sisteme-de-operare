import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class NetworkMonitor extends JFrame {

    private static final String DEFAULT_TARGET = "8.8.8.8";
    private static final int DEFAULT_INTERVAL_SEC = 30;
    private static final int FAILURES_BEFORE_RESTART = 2;
    private static final String LOG_FILE = "network_monitor.log";
    private static final String FIRST_RUN_MARKER = ".network_monitor_installed";

    private static NetworkMonitor instance;

    private final JTextField targetField = new JTextField(DEFAULT_TARGET, 12);
    private final JSpinner intervalSpinner =
            new JSpinner(new SpinnerNumberModel(DEFAULT_INTERVAL_SEC, 5, 3600, 5));
    private final JButton startBtn = new JButton("Start");
    private final JButton stopBtn = new JButton("Stop");
    private final JButton restartBtn = new JButton("Repornește rețeaua acum");
    private final JButton clearBtn = new JButton("Șterge afișarea");
    private final JLabel statusLabel = new JLabel("OPRIT", SwingConstants.CENTER);
    private final JLabel failuresLabel = new JLabel("Eșecuri consecutive: 0 / " + FAILURES_BEFORE_RESTART);
    private final JLabel lastCheckLabel = new JLabel("Ultima verificare: -");
    private final JTextArea logArea = new JTextArea();

    private volatile Thread worker;
    private int consecutiveFailures = 0;

    public NetworkMonitor() {
        super("Network Monitor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        statusLabel.setOpaque(true);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 22f));
        statusLabel.setPreferredSize(new Dimension(0, 50));
        setStatus("OPRIT", Color.GRAY);

        JPanel settings = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settings.add(new JLabel("Adresă test:"));
        settings.add(targetField);
        settings.add(new JLabel("Interval (s):"));
        settings.add(intervalSpinner);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(startBtn);
        buttons.add(stopBtn);
        buttons.add(restartBtn);
        buttons.add(clearBtn);

        JPanel info = new JPanel(new GridLayout(0, 1));
        info.add(failuresLabel);
        info.add(lastCheckLabel);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(statusLabel);
        top.add(settings);
        top.add(buttons);
        top.add(info);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        stopBtn.setEnabled(false);
        startBtn.addActionListener(e -> startMonitoring());
        stopBtn.addActionListener(e -> stopMonitoring());
        restartBtn.addActionListener(e -> new Thread(NetworkMonitor::restartNetwork).start());
        clearBtn.addActionListener(e -> logArea.setText(""));

        setSize(720, 480);
        setLocationRelativeTo(null);
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setBackground(color);
        statusLabel.setForeground(Color.WHITE);
    }

    private void startMonitoring() {
        if (worker != null) {
            return;
        }
        final String target = targetField.getText().trim();
        final int interval = (Integer) intervalSpinner.getValue();
        if (target.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduceți o adresă de test.");
            return;
        }
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        targetField.setEnabled(false);
        intervalSpinner.setEnabled(false);
        setStatus("SE VERIFICĂ...", Color.GRAY);

        worker = new Thread(() -> monitorLoop(target, interval), "monitor");
        worker.setDaemon(true);
        worker.start();
    }

    private void stopMonitoring() {
        Thread t = worker;
        if (t != null) {
            t.interrupt();
        }
    }

    private void monitorLoop(String target, int interval) {
        log("Monitor pornit. Target=" + target + " interval=" + interval + "s");
        consecutiveFailures = 0;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                boolean online = pingHost(target);
                final String time = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                if (online) {
                    if (consecutiveFailures > 0) {
                        log("Conexiune restabilită după " + consecutiveFailures + " eșec(uri).");
                    }
                    consecutiveFailures = 0;
                    updateUi("ONLINE", new Color(0, 140, 60), time);
                } else {
                    consecutiveFailures++;
                    log("Ping eșuat (" + consecutiveFailures + "/"
                            + FAILURES_BEFORE_RESTART + ") către " + target);
                    updateUi("OFFLINE", new Color(190, 30, 30), time);

                    if (consecutiveFailures >= FAILURES_BEFORE_RESTART) {
                        SwingUtilities.invokeLater(() -> setStatus("SE REPORNEȘTE...", new Color(210, 130, 0)));
                        restartNetwork();
                        // consecutiveFailures = 0;
                        TimeUnit.SECONDS.sleep(15);
                    }
                }
                TimeUnit.SECONDS.sleep(interval);
            }
        } catch (InterruptedException e) {
            // oprire solicitată de utilizator
        }
        log("Monitor oprit.");
        worker = null;
        SwingUtilities.invokeLater(() -> {
            setStatus("OPRIT", Color.GRAY);
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            targetField.setEnabled(true);
            intervalSpinner.setEnabled(true);
        });
    }

    private void updateUi(String status, Color color, String time) {
        final int failures = consecutiveFailures;
        SwingUtilities.invokeLater(() -> {
            setStatus(status, color);
            failuresLabel.setText("Eșecuri consecutive: " + failures + " / " + FAILURES_BEFORE_RESTART);
            lastCheckLabel.setText("Ultima verificare: " + time);
        });
    }

    private static boolean pingHost(String host) {
        try {
            Process p = new ProcessBuilder("ping", "-c", "1", "-W", "2", host)
                    .redirectErrorStream(true)
                    .start();

            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;

        } catch (IOException | InterruptedException e) {
            log("Eroare la ping: " + e.getMessage());
            return false;
        }
    }

    private static void restartNetwork() {
        log(">>> Repornire interfață rețea (nmcli networking off/on)...");
        try {
            int off = runCommand("nmcli", "networking", "off");
            log("nmcli networking off -> exit " + off);

            TimeUnit.SECONDS.sleep(3);

            int on = runCommand("nmcli", "networking", "on");
            log("nmcli networking on  -> exit " + on);

        } catch (IOException | InterruptedException e) {
            log("Eroare la restart rețea: " + e.getMessage());
        }
    }

    private static int runCommand(String... cmd) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();

        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                log("  [" + cmd[0] + "] " + line);
            }
        }
        return p.waitFor();
    }

    private static void installAutostartIfNeeded() {
        File marker = new File(System.getProperty("user.home"), FIRST_RUN_MARKER);
        if (marker.exists()) {
            return;
        }

        try {
            String userHome = System.getProperty("user.home");

            File autostartDir = new File(userHome + "/.config/autostart");
            if (!autostartDir.exists() && !autostartDir.mkdirs()) {
                log("Nu pot crea directorul autostart: " + autostartDir);
                return;
            }

            String jarPath = new File(NetworkMonitor.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath()).getAbsolutePath();

            String entry = "[Desktop Entry]\n"
                    + "Type=Application\n"
                    + "Name=NetworkMonitor\n"
                    + "Exec=java -cp " + jarPath + " NetworkMonitor\n"
                    + "Terminal=false\n"
                    + "X-GNOME-Autostart-enabled=true\n";

            try (FileWriter w = new FileWriter(
                    new File(autostartDir, "NetworkMonitor.desktop"))) {
                w.write(entry);
            }

            marker.createNewFile();
            log("Adăugat la autostart: " + autostartDir + "/NetworkMonitor.desktop");

        } catch (IOException e) {
            log("Eroare la instalarea autostart: " + e.getMessage());
        }
    }

    private static synchronized void log(String msg) {
        String line = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " | " + msg;

        System.out.println(line);

        try (FileWriter w = new FileWriter(LOG_FILE, true)) {
            w.write(line + "\n");
        } catch (IOException ignored) {
        }

        final NetworkMonitor gui = instance;
        if (gui != null) {
            SwingUtilities.invokeLater(() -> {
                gui.logArea.append(line + "\n");
                gui.logArea.setCaretPosition(gui.logArea.getDocument().getLength());
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            instance = new NetworkMonitor();
            instance.setVisible(true);
            installAutostartIfNeeded();
            instance.startMonitoring();
        });
    }
}