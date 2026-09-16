import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.IntConsumer;

public class ReadingTimer {

    private Timer timer;
    private int remainingSeconds = 0;
    private boolean sessionInitialized = false;
    private boolean thirtySecondWarningShown = false;

    private final IntConsumer onTick;
    private final Runnable onThirtySecondWarning;
    private final Runnable onFinished;

    public ReadingTimer(IntConsumer onTick,
                        Runnable onThirtySecondWarning,
                        Runnable onFinished) {
        this.onTick = onTick;
        this.onThirtySecondWarning = onThirtySecondWarning;
        this.onFinished = onFinished;
    }

    public boolean isRunning() {
        return timer != null;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void start(int minutes) {
        if (timer != null) {
            return;
        }

        if (!sessionInitialized || remainingSeconds <= 0) {
            remainingSeconds = minutes * 60;
            sessionInitialized = true;
            thirtySecondWarningShown = false;
            SwingUtilities.invokeLater(() -> onTick.accept(remainingSeconds));
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                remainingSeconds--;
                SwingUtilities.invokeLater(() -> {
                    onTick.accept(remainingSeconds);

                    if (remainingSeconds == 30 && !thirtySecondWarningShown) {
                        thirtySecondWarningShown = true;
                        SoundPlayer.playAlert();
                        onThirtySecondWarning.run();
                    }

                    if (remainingSeconds <= 0) {
                        stopInternal();
                        sessionInitialized = false;
                        thirtySecondWarningShown = false;
                        SoundPlayer.playAlert();
                        onFinished.run();
                    }
                });
            }
        }, 1000, 1000);
    }

    public void pause() {
        stopInternal();
    }

    public void reset(int minutes) {
        stopInternal();
        remainingSeconds = minutes * 60;
        sessionInitialized = true;
        thirtySecondWarningShown = false;
        SwingUtilities.invokeLater(() -> onTick.accept(remainingSeconds));
    }

    private void stopInternal() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}