package md.utm.smarttimermanager.timer;

import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer extends BaseTimer {

    private int remainingSeconds;
    private int initialSeconds;

    private boolean paused = false;

    private Runnable updateAction;


    public CountdownTimer(Runnable updateAction) {
        this.updateAction = updateAction;
    }


    public void setTime(int hours, int minutes, int seconds) {

        initialSeconds =
                hours * 3600 +
                        minutes * 60 +
                        seconds;

        remainingSeconds = initialSeconds;
    }


    @Override
    public void start() {

        if (running) {
            return;
        }

        if (remainingSeconds <= 0) {
            return;
        }

        timer = new Timer(true);

        running = true;
        paused = false;


        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                remainingSeconds--;

                updateAction.run();

                if (remainingSeconds <= 0) {

                    remainingSeconds = 0;

                    stopTimer();
                }
            }
        };


        timer.scheduleAtFixedRate(
                task,
                1000,
                1000
        );
    }


    public void pause() {

        stopTimer();

        paused = true;
    }


    public void cancel() {

        stopTimer();

        paused = false;

        remainingSeconds = initialSeconds;

        updateAction.run();
    }


    public boolean isPaused() {
        return paused;
    }


    public int getRemainingSeconds() {
        return remainingSeconds;
    }


    public static String formatTime(int seconds) {

        int hours = seconds / 3600;

        int minutes =
                (seconds % 3600) / 60;

        int sec =
                seconds % 60;


        return String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                sec
        );
    }
}