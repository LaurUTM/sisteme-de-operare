import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

public class TimerManager {

    // 1. Reactie dupa un interval de timp (delay).
    // Returneaza Timer-ul ca sa poata fi anulat manual (ex: userul opreste task-ul mai devreme).
    public Timer startAfterDelay(Runnable action, long delayMillis) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                action.run();
            }
        }, delayMillis);
        return timer;
    }

    // 2. Reactie la un anumit moment de timp.
    // Returneaza Timer-ul ca sa poata fi anulata o programare inainte sa se declanseze.
    public Timer scheduleAt(Runnable action, Date time) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                action.run();
            }
        }, time);
        return timer;
    }

    // 3. Reactie cu o perioada indicata.
    public Timer startPeriodic(Runnable action, long delayMillis, long periodMillis) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                action.run();
            }
        }, delayMillis, periodMillis);
        return timer;
    }
}