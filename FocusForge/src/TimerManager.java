import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

public class TimerManager {
    public void startAfterDelay(Runnable action, long delayMillis) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                action.run();
            }
        }, delayMillis);
    }

    public void scheduleAt(Runnable action, Date time) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                action.run();
            }
        }, time);
    }

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