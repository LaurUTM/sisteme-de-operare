import java.io.*;
import java.net.*;
import java.util.*;

public class FirewallManager {

    private static final int PORT = 8080;
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS.contains("win");
    private static final boolean IS_LINUX = OS.contains("linux");

    public static void main(String[] args) {
        if (!IS_WINDOWS && !IS_LINUX) {
            System.out.println("Sistem de operare nesuportat: " + OS);
            return;
        }
        Scanner sc = new Scanner(System.in);
        System.out.println("Sistem detectat: " + System.getProperty("os.name"));

        while (true) {
            System.out.println("\n===== MENIU =====");
            System.out.println("1 - Adauga regula firewall (port " + PORT + ")");
            System.out.println("2 - Verifica regula firewall");
            System.out.println("3 - Sterge regula firewall");
            System.out.println("4 - Afiseaza configuratia IP (ipconfig / ip addr)");
            System.out.println("5 - Ping catre o adresa");
            System.out.println("6 - Traseu catre o adresa (tracert / traceroute)");
            System.out.println("7 - Testeaza portul " + PORT + " (porneste server + conectare)");
            System.out.println("0 - Iesire");
            System.out.print("Optiune: ");
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1": addRule(); break;
                case "2": checkRule(); break;
                case "3": deleteRule(); break;
                case "4": showIpConfig(); break;
                case "5": ping(askHost(sc)); break;
                case "6": trace(askHost(sc)); break;
                case "7": testPort(); break;
                case "0": System.out.println("Iesire."); return;
                default: System.out.println("Optiune invalida.");
            }
        }
    }

    // ---------------- FIREWALL ----------------

    private static void addRule() {
        if (IS_WINDOWS) {
            // netsh: regula pentru conexiuni de intrare TCP pe portul 8080
            run("netsh", "advfirewall", "firewall", "add", "rule",
                    "name=Allow_Port_" + PORT, "dir=in", "action=allow",
                    "protocol=TCP", "localport=" + PORT);
        } else {
            // iptables: adauga (-A) in lantul INPUT regula de acceptare TCP pe portul 8080
            run("sudo", "iptables", "-A", "INPUT", "-p", "tcp",
                    "--dport", String.valueOf(PORT), "-j", "ACCEPT");
        }
    }

    private static void checkRule() {
        if (IS_WINDOWS) {
            run("netsh", "advfirewall", "firewall", "show", "rule", "name=Allow_Port_" + PORT);
        } else {
            // -C verifica daca regula exista (cod 0 = exista)
            int code = run("sudo", "iptables", "-C", "INPUT", "-p", "tcp",
                    "--dport", String.valueOf(PORT), "-j", "ACCEPT");
            System.out.println(code == 0 ? "Regula EXISTA." : "Regula NU exista.");
            run("sudo", "iptables", "-L", "INPUT", "-n", "--line-numbers");
        }
    }

    private static void deleteRule() {
        if (IS_WINDOWS) {
            run("netsh", "advfirewall", "firewall", "delete", "rule", "name=Allow_Port_" + PORT);
        } else {
            run("sudo", "iptables", "-D", "INPUT", "-p", "tcp",
                    "--dport", String.valueOf(PORT), "-j", "ACCEPT");
        }
    }

    // ---------------- RETEA ----------------

    private static void showIpConfig() {
        if (IS_WINDOWS) {
            run("ipconfig");
        } else {
            // pe Linux ipconfig nu exista; echivalentul este "ip addr" (sau ifconfig)
            if (run("ip", "addr") != 0) run("ifconfig");
        }
    }

    private static void ping(String host) {
        if (IS_WINDOWS) {
            run("ping", "-n", "4", host);   // -n = numar de pachete pe Windows
        } else {
            run("ping", "-c", "4", host);   // -c = numar de pachete pe Linux
        }
    }

    private static void trace(String host) {
        if (IS_WINDOWS) {
            run("tracert", "-h", "15", host);
        } else {
            // pe Linux tracert se numeste traceroute (sudo apt install traceroute)
            if (run("traceroute", "-m", "15", host) != 0) run("tracepath", host);
        }
    }

    // Porneste un server pe 8080, se conecteaza la el si arata portul in lista de porturi ascultate
    private static void testPort() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server pornit pe portul " + PORT);

            if (IS_WINDOWS) {
                run("cmd", "/c", "netstat -ano | findstr :" + PORT);
            } else {
                run("sh", "-c", "ss -tln | grep :" + PORT);
            }

            Thread t = new Thread(() -> {
                try (Socket s = server.accept()) {
                    System.out.println("Conexiune acceptata de la: " + s.getInetAddress());
                } catch (IOException ignored) {
                }
            });
            t.start();

            String ip = getLocalIp();
            try (Socket client = new Socket()) {
                client.connect(new InetSocketAddress(ip, PORT), 3000);
                System.out.println("Conectare reusita la " + ip + ":" + PORT + " -> portul este DESCHIS.");
            } catch (IOException e) {
                System.out.println("Conectare esuata la " + ip + ":" + PORT + " -> " + e.getMessage());
            }
            t.join(3000);
        } catch (IOException | InterruptedException e) {
            System.out.println("Nu s-a putut porni serverul pe portul " + PORT + ": " + e.getMessage());
        }
    }

    // ---------------- UTILITARE ----------------

    private static String getLocalIp() {
        try (DatagramSocket s = new DatagramSocket()) {
            s.connect(InetAddress.getByName("8.8.8.8"), 80);
            return s.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            return "127.0.0.1";
        }
    }

    private static String askHost(Scanner sc) {
        System.out.print("Adresa (Enter = 8.8.8.8): ");
        String h = sc.nextLine().trim();
        return h.isEmpty() ? "8.8.8.8" : h;
    }

    // Executa o comanda prin ProcessBuilder, afiseaza iesirea si intoarce codul de terminare
    private static int run(String... cmd) {
        System.out.println("\n> " + String.join(" ", cmd));
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            if (cmd[0].equals("sudo")) pb.redirectInput(ProcessBuilder.Redirect.INHERIT); // parola sudo
            Process p = pb.start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) System.out.println(line);
            }
            int code = p.waitFor();
            System.out.println("Cod de terminare: " + code);
            return code;
        } catch (IOException | InterruptedException e) {
            System.out.println("Eroare: " + e.getMessage());
            return -1;
        }
    }
}