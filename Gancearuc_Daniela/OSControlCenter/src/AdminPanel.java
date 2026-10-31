import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LAB 3 - Administrarea utilizatorilor, grupurilor, parolelor și a memoriei virtuale.
 *
 * Fila merge pe ambele sisteme:
 *   - pe Windows rulează comenzile din lucrare: net user, net localgroup, net accounts, reg, pagefile
 *   - pe Linux (Debian) rulează echivalentele: getent, useradd, groupadd, usermod, chage, swap
 *
 * Operațiile care schimbă sistemul cer drept de administrator:
 *   - Linux: prin pkexec (apare o fereastră care cere parola)
 *   - Windows: programul trebuie pornit "Run as administrator"
 *
 * Fiecare buton afișează și comanda exactă pe care a rulat-o, ca să o poți explica.
 */
public class AdminPanel extends JPanel {

    private final JTextField user = new JTextField("student1", 12);
    private final JTextField fullName = new JTextField("Student Unu", 12);
    private final JPasswordField password = new JPasswordField("Parola123!", 12);
    private final JTextField group = new JTextField("laborator", 12);
    private final JTextArea output = Theme.logArea();
    private final boolean win = Util.isWindows();
    /** Un singur fir de lucru: comenzile rulează una după alta, iar rezultatele nu se amestecă. */
    private final java.util.concurrent.ExecutorService worker =
        java.util.concurrent.Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "lab3"); t.setDaemon(true); return t; });

    public AdminPanel() {
        super(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(dataCard());
        left.add(Box.createVerticalStrut(10));
        left.add(viewCard());
        left.add(Box.createVerticalStrut(10));
        left.add(adminCard());
        left.add(Box.createVerticalStrut(10));
        left.add(memoryCard());
        left.add(Box.createVerticalGlue());

        JScrollPane leftScroll = new JScrollPane(left);
        leftScroll.setBorder(null);
        leftScroll.getViewport().setBackground(Theme.BG);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftScroll.setPreferredSize(new Dimension(450, 10));

        output.setFont(Theme.MONO.deriveFont(12.5f));
        JButton clear = new JButton("Golește");
        clear.addActionListener(e -> output.setText(""));
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.add(Theme.note(win
            ? "Rulezi pe Windows: se folosesc comenzile din Lab 3 (net user, net localgroup, net accounts, reg). "
              + "Pentru creare/ștergere pornește programul cu „Run as administrator”."
            : "Rulezi pe Linux: se folosesc echivalentele comenzilor din Lab 3. "
              + "La operațiile de administrare apare o fereastră care cere parola (pkexec)."), BorderLayout.NORTH);
        right.add(new JScrollPane(output), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(clear);
        right.add(bottom, BorderLayout.SOUTH);

        add(leftScroll, BorderLayout.WEST);
        add(Theme.card("Rezultat (comanda rulată și răspunsul sistemului)", right), BorderLayout.CENTER);

        print("Windows (Lab 3)            ->  Linux (Debian)");
        print("net user                   ->  getent passwd / useradd / userdel");
        print("net localgroup             ->  getent group / groupadd / usermod -aG");
        print("net accounts (parole)      ->  chage, /etc/login.defs");
        print("registrul (Run)            ->  ~/.config/autostart");
        print("pagefile.sys               ->  swap (swapon --show)");
        print("Event Viewer (logări)      ->  last / journalctl\n");
    }

    // =====================================================================
    // Cardurile cu butoane
    // =====================================================================

    private JComponent dataCard() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        String[] labels = {"Utilizator:", "Nume complet:", "Parolă:", "Grup:"};
        JComponent[] fields = {user, fullName, password, group};
        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            form.add(new JLabel(labels[i]), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            form.add(fields[i], c);
        }
        return card("Datele folosite de butoane", form);
    }

    private JComponent viewCard() {
        return card("Vizualizare (fără drepturi de administrator)", buttons(
            button("Utilizatori", this::listUsers),
            button("Grupuri", this::listGroups),
            button("Detalii utilizator", this::userDetails),
            button("Membrii grupului", this::groupMembers),
            button("Politica parolelor", this::passwordPolicy),
            button("Ultimele logări", this::lastLogins)
        ));
    }

    private JComponent adminCard() {
        return card("Administrare (cere parola de administrator)", buttons(
            button("1. Creează utilizator", this::createUser),
            button("2. Fă-l administrator", this::makeAdmin),
            button("3. Expirare parolă 30 zile", this::passwordExpiry),
            button("4. Creează grup", this::createGroup),
            button("4. Adaugă în grup", this::addToGroup),
            button("Șterge utilizatorul", this::deleteUser)
        ));
    }

    private JComponent memoryCard() {
        return card("Memorie virtuală și pornire automată", buttons(
            button("Fișier de pagină / swap", this::virtualMemory),
            button("Pornire automată (registru)", this::autostart)
        ));
    }

    // =====================================================================
    // Vizualizare
    // =====================================================================

    private void listUsers() {
        if (win) { run("net", "user"); return; }
        // în Linux, utilizatorii "reali" au UID >= 1000 (cei mai mici sunt ai sistemului)
        String all = Util.runCommand("getent", "passwd");
        print("$ getent passwd   (afișez doar utilizatorii cu UID >= 1000)");
        print(String.format("  %-16s %-6s %-22s %s", "NUME", "UID", "NUME COMPLET", "DIRECTOR"));
        for (String line : all.split("\n")) {
            String[] f = line.split(":", -1);
            if (f.length < 7) continue;
            int uid = parseInt(f[2]);
            if (uid >= 1000 && uid < 65534) {
                print(String.format("  %-16s %-6s %-22s %s", f[0], f[2], f[4].split(",")[0], f[5]));
            }
        }
        print("");
    }

    private void listGroups() {
        if (win) { run("net", "localgroup"); return; }
        String all = Util.runCommand("getent", "group");
        print("$ getent group   (grupurile create de utilizatori, GID >= 1000, și grupul sudo)");
        for (String line : all.split("\n")) {
            String[] f = line.split(":", -1);
            if (f.length < 4) continue;
            int gid = parseInt(f[2]);
            if ((gid >= 1000 && gid < 65534) || f[0].equals("sudo")) {
                print(String.format("  %-16s GID %-6s membri: %s", f[0], f[2], f[3].isEmpty() ? "-" : f[3]));
            }
        }
        print("");
    }

    private void userDetails() {
        String u = user.getText().trim();
        if (!validName(u)) return;
        if (win) { run("net", "user", u); return; }
        run("id", u);
        run("chage", "-l", u); // merge fără sudo doar pentru propriul cont
    }

    private void groupMembers() {
        String g = group.getText().trim();
        if (!validName(g)) return;
        if (win) run("net", "localgroup", g);
        else run("getent", "group", g);
    }

    private void passwordPolicy() {
        if (win) { run("net", "accounts"); return; }
        print("$ grep PASS_ /etc/login.defs   (politica implicită pentru conturile noi)");
        try {
            for (String line : Files.readAllLines(Paths.get("/etc/login.defs"))) {
                if (line.startsWith("PASS_")) print("  " + line);
            }
        } catch (Exception e) {
            print("  nu pot citi /etc/login.defs");
        }
        print("  PASS_MAX_DAYS = valabilitatea maximă a parolei (ca „Maximum password age” din Windows)\n");
    }

    private void lastLogins() {
        if (win) {
            // evenimentul 4624 = logare reușită (necesită administrator)
            run("wevtutil", "qe", "Security", "/q:*[System[(EventID=4624)]]", "/c:5", "/rd:true", "/f:text");
            return;
        }
        if (Util.commandExists("last")) run("last", "-n", "10");
        else run("journalctl", "-n", "15", "--no-pager", "_COMM=systemd-logind");
        run("who");
    }

    // =====================================================================
    // Administrare (Lab 3, sarcinile 1-4)
    // =====================================================================

    /** Sarcina 1: utilizator nou, cu parolă și schimbarea parolei obligatorie la prima logare. */
    private void createUser() {
        String u = user.getText().trim();
        String name = fullName.getText().trim();
        String pass = new String(password.getPassword());
        if (!validName(u)) return;
        if (pass.isEmpty()) { print("Scrie o parolă.\n"); return; }

        if (win) {
            run("net", "user", u, pass, "/add", "/fullname:" + name, "/logonpasswordchg:yes");
            return;
        }
        // un singur pkexec (o singură cerere de parolă) care face 3 lucruri:
        //   useradd -> creează contul;  chpasswd -> setează parola (citită din stdin);
        //   chage -d 0 -> parola trebuie schimbată la prima logare
        String script = "useradd -m -s /bin/bash -c \"$2\" \"$1\" && chpasswd && chage -d 0 \"$1\"";
        print("$ pkexec useradd -m -s /bin/bash -c \"" + name + "\" " + u);
        print("$ pkexec chpasswd            (parola se trimite ascunsă)");
        print("$ pkexec chage -d 0 " + u + "     (schimbarea parolei la prima logare)");
        runWithInput(u + ":" + pass + "\n", "pkexec", "sh", "-c", script, "sh", u, name);
    }

    /** Sarcina 2: din utilizator standard -> administrator. */
    private void makeAdmin() {
        String u = user.getText().trim();
        if (!validName(u)) return;
        if (win) run("net", "localgroup", "Administrators", u, "/add");
        else run("pkexec", "usermod", "-aG", "sudo", u);
    }

    /** Sarcina 3: parola expiră la 30 de zile. */
    private void passwordExpiry() {
        String u = user.getText().trim();
        if (!validName(u)) return;
        if (win) run("net", "accounts", "/maxpwage:30");
        else run("pkexec", "chage", "-M", "30", u);
    }

    /** Sarcina 4: grup nou. */
    private void createGroup() {
        String g = group.getText().trim();
        if (!validName(g)) return;
        if (win) run("net", "localgroup", g, "/add");
        else run("pkexec", "groupadd", g);
    }

    /** Sarcina 4: utilizatorul intră în grup. */
    private void addToGroup() {
        String u = user.getText().trim(), g = group.getText().trim();
        if (!validName(u) || !validName(g)) return;
        if (win) run("net", "localgroup", g, u, "/add");
        else run("pkexec", "usermod", "-aG", g, u);
    }

    /** Sarcina 2: ștergerea unui utilizator (cu confirmare). */
    private void deleteUser() {
        String u = user.getText().trim();
        if (!validName(u)) return;
        if (u.equals(System.getProperty("user.name"))) {
            print("Nu poți șterge contul cu care ești conectată acum.\n");
            return;
        }
        int[] answer = new int[1];
        try {
            SwingUtilities.invokeAndWait(() -> answer[0] = JOptionPane.showConfirmDialog(this,
            "Sigur ștergi utilizatorul „" + u + "”" + (win ? "?" : " și directorul lui personal?"),
            "Ștergere utilizator", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE));
        } catch (Exception e) { return; }
        if (answer[0] != JOptionPane.YES_OPTION) return;
        if (win) run("net", "user", u, "/delete");
        else run("pkexec", "userdel", "-r", u);
    }

    // =====================================================================
    // Memorie virtuală (Lab 3, partea 3) și pornire automată
    // =====================================================================

    @SuppressWarnings("deprecation")
    private void virtualMemory() {
        com.sun.management.OperatingSystemMXBean os =
            (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        long ramMb = os.getTotalPhysicalMemorySize() / (1024 * 1024);
        if (!win) { // aceeași valoare ca în comanda free
            try {
                for (String line : Files.readAllLines(Paths.get("/proc/meminfo"))) {
                    if (line.startsWith("MemTotal:")) ramMb = Long.parseLong(line.replaceAll("\\D+", "")) / 1024;
                }
            } catch (Exception ignored) { }
        }

        if (win) {
            run("powershell", "-NoProfile", "-Command",
                "Get-CimInstance Win32_PageFileUsage | Format-List Name,AllocatedBaseSize,CurrentUsage,PeakUsage");
        } else {
            print("În Linux, rolul fișierului pagefile.sys îl are memoria SWAP.");
            run("swapon", "--show");
            run("free", "-h");
        }
        // Lab 3, sarcina 12: dimensiunea recomandată = 1,5 x RAM ... 3 x RAM
        print("Dimensiunea recomandată pentru fișierul de pagină / swap (sarcina 12):");
        print(String.format("  RAM instalat:      %s", Util.formatMb(ramMb)));
        print(String.format("  minim  (1,5 x RAM): %s", Util.formatMb(Math.round(ramMb * 1.5))));
        print(String.format("  maxim  (3 x RAM):   %s", Util.formatMb(ramMb * 3)));
        print("  Când RAM-ul nu ajunge, SO mută pagini de memorie pe disc (pagefile / swap).");
        print("  Încearcă „Rulează în timp real” din Lab 4 și urmărește aici cum se schimbă memoria.\n");
    }

    /** Legătura cu Lab 5: intrarea de pornire automată creată din fila Monitor. */
    private void autostart() {
        if (win) {
            run("reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run");
        } else {
            File dir = new File(System.getProperty("user.home"), ".config/autostart");
            print("$ ls ~/.config/autostart   (echivalentul cheii Run din registrul Windows)");
            String[] files = dir.list();
            if (files == null || files.length == 0) {
                print("  (gol) - apasă „Adaugă la pornire automată” în fila Lab 5 și verifică din nou");
            } else {
                Arrays.sort(files);
                for (String f : files) print("  " + f);
                File ours = new File(dir, "os-control-center.desktop");
                if (ours.exists()) {
                    print("\n$ cat ~/.config/autostart/os-control-center.desktop");
                    try {
                        for (String line : Files.readAllLines(ours.toPath())) print("  " + line);
                    } catch (Exception ignored) { }
                }
            }
            print("");
        }
    }

    // =====================================================================
    // Ajutătoare
    // =====================================================================

    /** Rulează o comandă pe un fir separat și afișează comanda + rezultatul + codul de ieșire. */
    private void run(String... command) {
        runWithInput(null, command);
    }

    private void runWithInput(String input, String... command) {
        List<String> cmd = new ArrayList<>(Arrays.asList(command));
        boolean showCommand = input == null; // la crearea contului comanda e deja afișată, fără parolă
        {
            if (showCommand) print("$ " + String.join(" ", cmd));
            if (cmd.get(0).equals("pkexec") && !Util.commandExists("pkexec")) {
                print("  ✖ lipsește pkexec. Instalează-l o dată în Terminal:  sudo apt install pkexec\n");
                return;
            }
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                if (input != null) {
                    try (OutputStream os = p.getOutputStream()) { os.write(input.getBytes()); }
                } else {
                    p.getOutputStream().close();
                }
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) print("  " + line);
                }
                int code = p.waitFor();
                if (code == 0) print("  ✔ gata (cod 0)\n");
                else if ((code == 126 || code == 127) && cmd.get(0).equals("pkexec"))
                    print("  ✖ anulat sau parolă greșită (cod " + code + ")\n");
                else print("  ✖ eroare (cod " + code + ")" + (win ? " - ai pornit programul ca administrator?" : "") + "\n");
            } catch (Exception e) {
                print("  ✖ nu pot rula comanda: " + e.getMessage() + "\n");
            }
        }
    }

    /** Numele de utilizator/grup: litere mici, cifre, _ și - (regula din Linux). */
    private boolean validName(String s) {
        if (s.matches("[A-Za-z_][A-Za-z0-9_-]{0,31}")) return true;
        print("Nume invalid: „" + s + "”. Folosește litere, cifre, _ sau -, fără spații.\n");
        return false;
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return -1; }
    }

    private void print(String text) {
        SwingUtilities.invokeLater(() -> {
            output.append(text + "\n");
            output.setCaretPosition(output.getDocument().getLength());
        });
    }

    /** Butonul trimite acțiunea pe firul de lucru, ca fereastra să nu înghețe. */
    private JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> worker.submit(action));
        return b;
    }

    private static JPanel buttons(JButton... bs) {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        for (JButton b : bs) p.add(b);
        return p;
    }

    private static JComponent card(String title, JComponent content) {
        JPanel c = Theme.card(title, content);
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }
}
