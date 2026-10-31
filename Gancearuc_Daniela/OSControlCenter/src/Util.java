import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Funcții ajutătoare folosite de mai multe file. */
public class Util {

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    /** Ora curentă, pentru jurnale: 14:05:33 */
    public static String now() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * Rulează o comandă de sistem și întoarce tot ce a afișat (LAB 5 - ProcessBuilder).
     * Dacă ceva nu merge, întoarce un text gol în loc să oprească programul.
     */
    public static String runCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // erorile vin în același flux cu rezultatul
            Process p = pb.start();
            StringBuilder out = new StringBuilder();
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = r.readLine()) != null) out.append(line).append('\n');
            }
            p.waitFor();
            return out.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** Verifică dacă un program există în sistem (ex. "gedit"). */
    public static boolean commandExists(String name) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        for (String dir : path.split(File.pathSeparator)) {
            if (new File(dir, name).canExecute()) return true;
        }
        return false;
    }

    /** Citește prima linie dintr-un fișier (ex. /sys/class/dmi/id/product_name). */
    public static String readFirstLine(String file) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(file));
            return lines.isEmpty() ? "" : lines.get(0).trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** Desparte o comandă scrisă de utilizator în bucăți: "ping -c 4 8.8.8.8" */
    public static List<String> splitCommand(String text) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (char c : text.toCharArray()) {
            if (c == '"') { inQuotes = !inQuotes; continue; }
            if (Character.isWhitespace(c) && !inQuotes) {
                if (cur.length() > 0) { parts.add(cur.toString()); cur.setLength(0); }
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) parts.add(cur.toString());
        return parts;
    }

    /** Afișează un număr de MB frumos: 2048 MB -> "2,0 GB" */
    public static String formatMb(long mb) {
        if (mb >= 1024) return String.format("%.1f GB", mb / 1024.0);
        return mb + " MB";
    }
}
