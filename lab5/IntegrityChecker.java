import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class IntegrityChecker {

    static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    static final Path BACKUP = Paths.get(System.getProperty("user.home"), "integrity_backup");
    static final Path LOG = BACKUP.resolve("jurnal.txt");
    static List<Path> files = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        // Fisierele critice monitorizate (sau toate fisierele dintr-un folder dat ca argument, pentru test)
        if (args.length > 0) {
            Files.list(Paths.get(args[0])).filter(Files::isRegularFile).forEach(files::add);
        } else if (WINDOWS) {
            files.add(Paths.get("C:\\Windows\\System32\\drivers\\etc\\hosts"));
            files.add(Paths.get("C:\\Windows\\win.ini"));
        } else {
            files.add(Paths.get("/etc/passwd"));
            files.add(Paths.get("/etc/group"));
            files.add(Paths.get("/etc/hosts"));
            files.add(Paths.get("/etc/fstab"));
        }

        Files.createDirectories(BACKUP);
        if (!Files.exists(BACKUP.resolve(files.get(0).getFileName()))) {
            backup(); // backup automat la prima pornire
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n1 - Backup manual");
            System.out.println("2 - Verifica integritatea");
            System.out.println("3 - Verificare periodica (la fiecare 10 s)");
            System.out.println("4 - " + (WINDOWS ? "sfc /scannow" : "fsck"));
            System.out.println("0 - Iesire");
            System.out.print("Optiune: ");
            switch (sc.nextLine().trim()) {
                case "1": backup(); break;
                case "2": check(); break;
                case "3": periodic(sc); break;
                case "4": systemCheck(); break;
                case "0": return;
            }
        }
    }

    // Copiaza fisierele critice in folderul de backup
    static void backup() throws IOException {
        for (Path f : files) {
            Files.copy(f, BACKUP.resolve(f.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Backup facut in " + BACKUP);
    }

    // Compara fiecare fisier cu copia din backup; daca e modificat sau sters il restaureaza
    static void check() throws IOException {
        boolean ok = true;
        for (Path f : files) {
            Path copy = BACKUP.resolve(f.getFileName());
            String problem = null;
            if (!Files.exists(f)) problem = "STERS";
            else if (Files.mismatch(f, copy) != -1) problem = "MODIFICAT";

            if (problem != null) {
                ok = false;
                restore(copy, f);
                String msg = LocalDateTime.now().withNano(0) + " - " + problem + ": " + f + " (restaurat)";
                System.out.println(msg);
                Files.writeString(LOG, msg + System.lineSeparator(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        }
        if (ok) System.out.println(LocalDateTime.now().withNano(0) + " - toate fisierele sunt intacte");
    }

    // Restaurarea fisierelor de sistem cere drepturi de administrator
    static void restore(Path copy, Path f) {
        try {
            Files.copy(copy, f, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            if (WINDOWS) run("cmd", "/c", "copy", "/Y", copy.toString(), f.toString());
            else run("sudo", "cp", copy.toString(), f.toString());
        }
    }

    // Verifica la fiecare 10 secunde, pana se apasa Enter
    static void periodic(Scanner sc) throws Exception {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    check();
                    Thread.sleep(10000);
                }
            } catch (Exception e) { }
        });
        t.start();
        System.out.println("Verificare periodica pornita. Apasa Enter pentru oprire.");
        sc.nextLine();
        t.interrupt();
    }

    static void systemCheck() throws Exception {
        if (WINDOWS) {
            run("sfc", "/scannow"); // cmd ca administrator
        } else {
            // fsck -n doar verifica partitia root, fara modificari
            Process p = new ProcessBuilder("findmnt", "-n", "-o", "SOURCE", "/").start();
            String disk = new String(p.getInputStream().readAllBytes()).trim();
            run("sudo", "fsck", "-n", disk);
        }
    }

    // Executa o comanda de sistem si afiseaza rezultatul
    static void run(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).inheritIO().start();
            System.out.println("Cod de terminare: " + p.waitFor());
        } catch (Exception e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }
}
