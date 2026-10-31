import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * LEGĂTURA DINTRE LUCRĂRI - execută planul calculat de planificator (Lab 4) în timp real.
 *
 *  - Lab 4: Scheduler calculează ordinea (segmentele din diagrama Gantt)
 *  - Lab 1: un java.util.Timer "bate" o dată pe unitate de timp și decide ce proces rulează acum
 *  - Lab 5: pentru fiecare proces pornim un PROCES REAL în sistem (ProcessBuilder),
 *           care ține procesorul ocupat; monitorul arată efectul pe grafic
 *  - Lab 2: procesele chiar rulează pe nucleele alocate mașinii virtuale
 *
 * În Linux facem exact ce face și planificatorul sistemului: procesul care nu are voie
 * să ruleze este OPRIT (kill -STOP, starea "T" în ps), iar cel ales este PORNIT (kill -CONT, starea "R").
 * Pe alte sisteme (ex. Windows) folosim fire de execuție (Thread) în locul proceselor.
 */
public class LiveRunner {

    /** Ce află interfața în timpul rulării. */
    public interface Listener {
        void onTick(int time, Scheduler.Segment current);                // la fiecare unitate de timp
        void onSwitch(int time, String from, String to, String id);      // comutare de context
        void onFinish(int time, String process);                         // un proces s-a terminat
        void onEnd(boolean completed);                                   // gata (sau oprit manual)
    }

    /** Un "proces" pe care îl putem porni, opri temporar și termina. */
    private interface Worker {
        void go();
        void hold();
        void kill();
        String id();
    }

    private final Scheduler.Result plan;
    private final int unitMs;
    private final Listener listener;
    private final Map<String, Worker> workers = new LinkedHashMap<>();
    private Timer timer;
    private int time = 0;
    private String running = null;
    private volatile boolean active = false;

    public LiveRunner(Scheduler.Result plan, int unitMs, Listener listener) {
        this.plan = plan;
        this.unitMs = unitMs;
        this.listener = listener;
    }

    public boolean isActive() { return active; }

    /** Spune dacă se folosesc procese reale (Linux) sau fire de execuție. */
    public static boolean usesRealProcesses() { return Util.isLinux() && Util.commandExists("kill"); }

    public void start() {
        active = true;
        // 1) creăm câte un proces pentru fiecare proces din plan; toate pornesc OPRITE
        for (String name : plan.burst.keySet()) {
            Worker w = usesRealProcesses() ? new RealProcess(name) : new BusyThread(name);
            w.hold();
            workers.put(name, w);
        }
        // 2) Timer-ul (Lab 1) execută planul: un pas la fiecare unitate de timp
        timer = new Timer("planificator-timp-real", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { tick(); }
        }, 0, unitMs);
    }

    /** Un pas al planificatorului: cine trebuie să ruleze în momentul "time"? */
    private synchronized void tick() {
        if (!active) return;

        // procesele care s-au terminat exact acum
        for (Map.Entry<String, Integer> e : plan.completion.entrySet()) {
            if (e.getValue() == time && workers.containsKey(e.getKey())) {
                workers.remove(e.getKey()).kill();
                if (e.getKey().equals(running)) running = null;
                listener.onFinish(time, e.getKey());
            }
        }

        int total = plan.segments.get(plan.segments.size() - 1).end;
        if (time >= total) {
            finish(true);
            return;
        }

        // segmentul din diagrama Gantt care conține momentul curent
        Scheduler.Segment current = null;
        for (Scheduler.Segment s : plan.segments) {
            if (s.start <= time && time < s.end) { current = s; break; }
        }
        if (current != null && !current.name.equals(running)) {
            // COMUTARE DE CONTEXT: oprim procesul vechi, pornim procesul nou
            if (running != null && workers.containsKey(running)) workers.get(running).hold();
            Worker next = workers.get(current.name);
            if (next != null) next.go();
            listener.onSwitch(time, running, current.name, next != null ? next.id() : "");
            running = current.name;
        }
        listener.onTick(time, current);
        time++;
    }

    /** Oprire manuală. */
    public synchronized void stop() {
        if (active) finish(false);
    }

    private void finish(boolean completed) {
        active = false;
        if (timer != null) timer.cancel();
        for (Worker w : workers.values()) w.kill();
        workers.clear();
        listener.onEnd(completed);
    }

    // ---------------------------------------------------------------------
    // Proces real în Linux: un mic script care ține procesorul ocupat.
    // Îl vezi în terminal cu:  ps -o pid,stat,args -C sh | grep proces_
    // ---------------------------------------------------------------------
    private static class RealProcess implements Worker {
        private final Process process;
        private final String pid;

        RealProcess(String name) {
            Process p = null;
            try {
                // "proces_A" devine numele scriptului ($0), ca să-l recunoști în ps
                p = new ProcessBuilder("sh", "-c", "while :; do :; done", "proces_" + name).start();
            } catch (Exception e) {
                System.err.println("Nu pot porni procesul " + name + ": " + e.getMessage());
            }
            process = p;
            pid = p != null ? String.valueOf(p.pid()) : "?";
        }
        public void go() { if (process != null) Util.runCommand("kill", "-CONT", pid); }
        public void hold()  { if (process != null) Util.runCommand("kill", "-STOP", pid); }
        public void kill()   { if (process != null) process.destroyForcibly(); } // SIGKILL merge și pe un proces oprit
        public String id()   { return "PID " + pid; }
    }

    // ---------------------------------------------------------------------
    // Pe Windows: un fir de execuție care face calcule cât timp are voie.
    // ---------------------------------------------------------------------
    private static class BusyThread extends Thread implements Worker {
        private volatile boolean allowed = false;
        private volatile boolean alive = true;

        BusyThread(String name) {
            super("proces_" + name);
            setDaemon(true);
            start();
        }
        @Override public void run() {
            double x = 0;
            while (alive) {
                if (allowed) {
                    for (int i = 0; i < 200_000; i++) x += Math.sqrt(i); // muncă pentru procesor
                } else {
                    try { Thread.sleep(5); } catch (InterruptedException e) { return; }
                }
            }
            if (x < 0) System.out.println(x); // ca să nu fie "optimizat" calculul
        }
        public void go() { allowed = true; }
        public void hold()  { allowed = false; }
        public void kill()   { alive = false; }
        public String id()   { return "fir " + getName(); }
    }
}
