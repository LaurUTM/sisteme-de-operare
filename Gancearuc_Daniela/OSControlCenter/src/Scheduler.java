import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LAB 4 - Algoritmii de planificare a proceselor.
 *
 * Aici este doar LOGICA (calculele), fără interfață grafică.
 * Toate procesele sosesc în momentul 0 (ca în îndrumar).
 *
 * Fiecare algoritm produce o listă de "segmente" (bucăți din diagrama Gantt):
 * segmentul spune ce proces a rulat, de la ce moment până la ce moment
 * și cât timp i-a mai rămas după acel segment.
 */
public class Scheduler {

    /** Un proces: nume, timp de execuție, prioritate (opțional), utilizator (opțional). */
    public static class Proc {
        public final String name;
        public final int burst;      // timpul de execuție
        public final int priority;   // folosit doar la planificarea prioritară
        public Proc(String name, int burst, int priority) {
            this.name = name;
            this.burst = burst;
            this.priority = priority;
        }
        public Proc(String name, int burst) { this(name, burst, 0); }
    }

    /** Un utilizator pentru planificarea echitabilă: procentul lui și procesele lui. */
    public static class User {
        public final String name;
        public final int percent;
        public final List<Proc> procs;
        public User(String name, int percent, List<Proc> procs) {
            this.name = name;
            this.percent = percent;
            this.procs = procs;
        }
    }

    /** O bucată din diagrama Gantt. */
    public static class Segment {
        public final String name;
        public final int start, end;
        public final int remaining; // cât i-a mai rămas procesului după acest segment
        public Segment(String name, int start, int end, int remaining) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.remaining = remaining;
        }
        /** Notația din îndrumar: B(7) = B a rulat și i-au mai rămas 7 unități. */
        public String label() {
            return remaining > 0 ? name + "(" + remaining + ")" : name;
        }
    }

    /** Rezultatul unui algoritm. */
    public static class Result {
        public final String algorithm;
        public final List<Segment> segments = new ArrayList<>();
        public final Map<String, Integer> burst = new LinkedHashMap<>();
        public final Map<String, Integer> completion = new LinkedHashMap<>();

        public Result(String algorithm) { this.algorithm = algorithm; }

        /** Ordinea de execuție, scrisă ca în îndrumar: A D C(2) C B(10) B */
        public String order() {
            StringBuilder sb = new StringBuilder();
            for (Segment s : segments) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(s.label());
            }
            return sb.toString();
        }

        /** Timpul mediu de execuție (până la terminare) pe procese. */
        public double avgCompletion() {
            double sum = 0;
            for (int c : completion.values()) sum += c;
            return completion.isEmpty() ? 0 : sum / completion.size();
        }

        /** Timpul mediu de așteptare = terminare - timp de execuție. */
        public double avgWaiting() {
            double sum = 0;
            for (String p : completion.keySet()) sum += completion.get(p) - burst.get(p);
            return completion.isEmpty() ? 0 : sum / completion.size();
        }

        /**
         * Metoda folosită în îndrumar: media momentelor în care se termină
         * FIECARE segment din diagrama Gantt.
         * Ex. Round Robin: (4 + 17 + 29 + 37 + 44) / 5 = 26,2
         */
        public double avgSegmentEnd() {
            double sum = 0;
            for (Segment s : segments) sum += s.end;
            return segments.isEmpty() ? 0 : sum / segments.size();
        }

        /** Formula scrisă desfășurat, ca s-o poți arăta la susținere. */
        public String segmentFormula() {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < segments.size(); i++) {
                if (i > 0) sb.append(" + ");
                sb.append(segments.get(i).end);
            }
            sb.append(") / ").append(segments.size());
            return sb.toString();
        }

        // folosit intern de algoritmi
        void run(Proc p, int start, int duration, int remainingAfter) {
            segments.add(new Segment(p.name, start, start + duration, remainingAfter));
            if (remainingAfter == 0) completion.put(p.name, start + duration);
        }
    }

    // ---------------------------------------------------------------------
    // 1. FCFS - Primul venit, primul servit
    // Procesele rulează în ordinea în care au fost date, fiecare până la capăt.
    // ---------------------------------------------------------------------
    public static Result fcfs(List<Proc> procs) {
        return runInOrder("FCFS - Primul venit, primul servit", procs);
    }

    // ---------------------------------------------------------------------
    // 2. SJF - Cea mai scurtă sarcină prima
    // Sortăm după timpul de execuție (crescător), apoi rulăm ca la FCFS.
    // ---------------------------------------------------------------------
    public static Result sjf(List<Proc> procs) {
        List<Proc> sorted = new ArrayList<>(procs);
        sorted.sort(Comparator.comparingInt(p -> p.burst)); // sortare stabilă
        return runInOrder("SJF - Cea mai scurtă sarcină prima", sorted);
    }

    // ---------------------------------------------------------------------
    // 5. Planificare pe bază de prioritate
    // Sortăm după prioritate, apoi rulăm ca la FCFS.
    // În îndrumar: număr mai mare = prioritate mai mare (B-8 rulează primul).
    // ---------------------------------------------------------------------
    public static Result priority(List<Proc> procs, boolean biggerIsHigher) {
        List<Proc> sorted = new ArrayList<>(procs);
        Comparator<Proc> c = Comparator.comparingInt(p -> p.priority);
        sorted.sort(biggerIsHigher ? c.reversed() : c);
        return runInOrder("Planificare prioritară", sorted);
    }

    /** Rulează procesele unul după altul, fiecare complet (fără întreruperi). */
    private static Result runInOrder(String title, List<Proc> order) {
        Result r = new Result(title);
        int time = 0;
        for (Proc p : order) {
            r.burst.put(p.name, p.burst);
            r.run(p, time, p.burst, 0);
            time += p.burst;
        }
        return r;
    }

    // ---------------------------------------------------------------------
    // 4. Round Robin - fiecare proces primește pe rând un cuant K.
    // Dacă nu a terminat, trece la sfârșitul cozii.
    // ---------------------------------------------------------------------
    public static Result roundRobin(List<Proc> procs, int k) {
        Result r = new Result("Round Robin (K=" + k + ")");
        Deque<Proc> queue = new ArrayDeque<>();
        Map<String, Integer> rem = new LinkedHashMap<>();
        for (Proc p : procs) {
            queue.add(p);
            rem.put(p.name, p.burst);
            r.burst.put(p.name, p.burst);
        }
        int time = 0;
        while (!queue.isEmpty()) {
            Proc p = queue.poll();
            int run = Math.min(k, rem.get(p.name));
            int left = rem.get(p.name) - run;
            r.run(p, time, run, left);
            time += run;
            rem.put(p.name, left);
            if (left > 0) queue.add(p); // nu a terminat -> la coada
        }
        return r;
    }

    // ---------------------------------------------------------------------
    // 3. SRTF - Cel mai scurt timp rămas  (și 6. "Cel mai scurt proces e următorul")
    // La fiecare pas alegem procesul cu cel mai MIC timp rămas
    // și îl lăsăm să ruleze cel mult un cuant K. Apoi alegem din nou.
    // Ex. din îndrumar (K=10): A D C(2) C B(10) B
    // ---------------------------------------------------------------------
    public static Result shortestRemaining(List<Proc> procs, int k, String title) {
        Result r = new Result(title + " (K=" + k + ")");
        Map<String, Integer> rem = new LinkedHashMap<>();
        for (Proc p : procs) {
            rem.put(p.name, p.burst);
            r.burst.put(p.name, p.burst);
        }
        int time = 0;
        while (true) {
            Proc best = null;
            for (Proc p : procs) { // la egalitate câștigă cel dat primul
                int left = rem.get(p.name);
                if (left > 0 && (best == null || left < rem.get(best.name))) best = p;
            }
            if (best == null) break; // toate au terminat
            int run = Math.min(k, rem.get(best.name));
            int left = rem.get(best.name) - run;
            r.run(best, time, run, left);
            time += run;
            rem.put(best.name, left);
        }
        return r;
    }

    // ---------------------------------------------------------------------
    // 7. Planificare echitabilă (fair share)
    // Fiecare utilizator primește cuante proporțional cu procentul lui.
    // 50% / 25% / 25%  ->  tiparul U1 U1 U2 U3, repetat.
    // În interiorul unui utilizator, procesele lui merg pe rând (Round Robin).
    // Ex. din îndrumar: A(2) B(4) X Z(18) C A Y(2) Z(10) B Y Z(2) Z
    // ---------------------------------------------------------------------
    public static Result fairShare(List<User> users, int k) {
        Result r = new Result("Planificare echitabilă (K=" + k + ")");

        // 1) Transformăm procentele în număr de cuante: 50/25/25 -> 2/1/1
        int g = 0;
        for (User u : users) g = gcd(g, u.percent);
        if (g == 0) g = 1;

        // 2) Construim tiparul: [U1, U1, U2, U3]
        List<Integer> pattern = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            int slots = Math.max(1, users.get(i).percent / g);
            for (int s = 0; s < slots; s++) pattern.add(i);
        }

        // 3) Fiecare utilizator are coada lui de procese
        List<Deque<Proc>> queues = new ArrayList<>();
        Map<String, Integer> rem = new LinkedHashMap<>();
        for (User u : users) {
            Deque<Proc> q = new ArrayDeque<>(u.procs);
            queues.add(q);
            for (Proc p : u.procs) {
                rem.put(p.name, p.burst);
                r.burst.put(p.name, p.burst);
            }
        }

        // 4) Parcurgem tiparul la nesfârșit, până se golesc toate cozile.
        //    Un utilizator fără procese este sărit.
        int time = 0;
        while (true) {
            boolean anyLeft = false;
            for (Deque<Proc> q : queues) if (!q.isEmpty()) anyLeft = true;
            if (!anyLeft) break;

            for (int userIndex : pattern) {
                Deque<Proc> q = queues.get(userIndex);
                if (q.isEmpty()) continue;
                Proc p = q.poll();
                int run = Math.min(k, rem.get(p.name));
                int left = rem.get(p.name) - run;
                r.run(p, time, run, left);
                time += run;
                rem.put(p.name, left);
                if (left > 0) q.add(p);
            }
        }
        return r;
    }

    private static int gcd(int a, int b) {
        while (b != 0) { int t = a % b; a = b; b = t; }
        return Math.abs(a);
    }

    // ---------------------------------------------------------------------
    // Citirea datelor din text, ca să le poți scrie ușor în interfață.
    // ---------------------------------------------------------------------

    /** "A=5, B=9, C=4"  sau cu prioritate:  "A=5:1, B=9:3" */
    public static List<Proc> parseProcs(String text) {
        List<Proc> list = new ArrayList<>();
        for (String part : text.split("[,;\\n]")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            String[] nv = part.split("=");
            if (nv.length != 2) throw new IllegalArgumentException("Format greșit: \"" + part + "\" (corect: A=5)");
            String name = nv[0].trim();
            String[] tp = nv[1].split(":");
            int burst = Integer.parseInt(tp[0].trim());
            int prio = tp.length > 1 ? Integer.parseInt(tp[1].trim()) : 0;
            if (burst <= 0) throw new IllegalArgumentException("Timpul trebuie să fie > 0: " + part);
            list.add(new Proc(name, burst, prio));
        }
        if (list.isEmpty()) throw new IllegalArgumentException("Nu ai introdus niciun proces.");
        return list;
    }

    /**
     * Fiecare rând = un utilizator:  "50: A=10, B=12, C=7"
     */
    public static List<User> parseUsers(String text) {
        List<User> users = new ArrayList<>();
        int n = 1;
        for (String line : text.split("\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            int colon = line.indexOf(':');
            if (colon < 0) throw new IllegalArgumentException("Rând greșit: \"" + line + "\" (corect: 50: A=10, B=12)");
            int percent = Integer.parseInt(line.substring(0, colon).replace("%", "").trim());
            List<Proc> procs = parseProcs(line.substring(colon + 1));
            users.add(new User("Utilizator " + n++, percent, procs));
        }
        if (users.isEmpty()) throw new IllegalArgumentException("Nu ai introdus niciun utilizator.");
        return users;
    }
}
