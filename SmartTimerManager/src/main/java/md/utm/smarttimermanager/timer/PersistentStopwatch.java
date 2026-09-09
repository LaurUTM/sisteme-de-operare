package md.utm.smarttimermanager.timer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class PersistentStopwatch extends BaseTimer {

    private String currentTime;

    private int intervalSeconds = 300;
    private int remainingSeconds = 300;
    private int configuredMinutes = 5;

    private boolean reminderRunning = false;

    private Runnable updateAction;
    private Runnable reminderAction;


    public PersistentStopwatch(
            Runnable updateAction,
            Runnable reminderAction
    ) {

        this.updateAction = updateAction;
        this.reminderAction = reminderAction;

        updateCurrentTime();
    }


    // PORNEȘTE CEASUL REAL
    @Override
    public void start() {

        if (running) {
            return;
        }

        timer = new Timer(true);
        running = true;


        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                // actualizăm ora
                updateCurrentTime();


                // reminderul scade doar dacă a fost apăsat Start
                if (reminderRunning) {

                    remainingSeconds--;


                    // dacă a ajuns la 0
                    if (remainingSeconds <= 0) {

                        if (reminderAction != null) {
                            reminderAction.run();
                        }


                        // se reprogramează automat
                        remainingSeconds = intervalSeconds;
                    }
                }


                // actualizăm interfața
                if (updateAction != null) {
                    updateAction.run();
                }
            }
        };


        timer.scheduleAtFixedRate(
                task,
                0,
                1000
        );
    }


    // setează numărul de minute
    public void setReminderMinutes(int minutes) {

        if (minutes <= 0) {
            return;
        }


        // resetăm timpul doar dacă utilizatorul
        // a schimbat numărul de minute
        if (minutes != configuredMinutes) {

            configuredMinutes = minutes;

            intervalSeconds =
                    minutes * 60;

            remainingSeconds =
                    intervalSeconds;
        }
    }


    // START REMINDER
    public void startReminder() {

        reminderRunning = true;
    }


    // PAUSE REMINDER
    public void pauseReminder() {

        reminderRunning = false;
    }


    // RESET REMINDER
    public void resetReminder() {

        reminderRunning = false;

        remainingSeconds =
                intervalSeconds;
    }


    private void updateCurrentTime() {

        LocalTime time =
                LocalTime.now();

        DateTimeFormatter format =
                DateTimeFormatter.ofPattern(
                        "HH:mm:ss"
                );

        currentTime =
                time.format(format);
    }


    public String getCurrentTime() {

        return currentTime;
    }


    public int getRemainingSeconds() {

        return remainingSeconds;
    }


    public boolean isReminderRunning() {

        return reminderRunning;
    }


    public String getFormattedRemainingTime() {

        int minutes =
                remainingSeconds / 60;

        int seconds =
                remainingSeconds % 60;

        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
    }
}