import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class ReminderTimer {

    private Timer timer;

    public boolean isRunning() {
        return timer != null;
    }

    public void start(String reminderName, int minutes, Consumer<String> onFired) {
        cancel();

        long delay = minutes * 60L * 1000L;
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    SoundPlayer.playAlert();
                    onFired.accept(reminderName);
                    timer = null;
                });
            }
        }, delay);
    }

    public void cancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}