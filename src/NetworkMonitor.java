import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class NetworkMonitor {

    private static final String PING_TARGET = "8.8.8.8";
    private static final String LOG_FILE = "network_monitor.log";
    private static final int CHECK_INTERVAL_SEC = 30;
    private static final int FAILURES_BEFORE_RESTART = 2;
    private static final String FIRST_RUN_MARKER = ".network_monitor_installed";

    private static int consecutiveFailures = 0;

    public static void main(String[] args) {
        installAutostartIfNeeded();
        log("Monitor pornit. Target=" + PING_TARGET
                + " interval=" + CHECK_INTERVAL_SEC + "s");

        while (true) {
            try {
                boolean online = pingHost(PING_TARGET);

                if (online) {
                    if (consecutiveFailures > 0) {
                        log("Conexiune restabilită după "
                                + consecutiveFailures + " eșec(uri).");
                    }
                    consecutiveFailures = 0;
                } else {
                    consecutiveFailures++;
                    log("Ping eșuat (" + consecutiveFailures + "/"
                            + FAILURES_BEFORE_RESTART + ") către " + PING_TARGET);

                    if (consecutiveFailures >= FAILURES_BEFORE_RESTART) {
                        restartNetwork();
                        consecutiveFailures = 0;
                        TimeUnit.SECONDS.sleep(15);
                    }
                }

                TimeUnit.SECONDS.sleep(CHECK_INTERVAL_SEC);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Monitor oprit.");
                return;
            }
        }
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
    }
}