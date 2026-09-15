package md.utm.smarttimermanager.timer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class PersistentStopwatch extends BaseTimer {

    private String oraCurenta;

    private int timpRamas = 300;
    private int interval = 300;

    private boolean reminderPornit = false;

    private Runnable actualizare;
    private Runnable reminder;

    private long momentStart;

    public PersistentStopwatch(Runnable actualizare,
                               Runnable reminder) {

        this.actualizare = actualizare;
        this.reminder = reminder;
    }

    @Override
    public void start() {

        if (running) {
            return;
        }

        timer = new Timer();
        running = true;

        momentStart = System.currentTimeMillis();

        TimerTask sarcina = new TimerTask() {

            @Override
            public void run() {

                LocalTime ora = LocalTime.now();

                oraCurenta = ora.format(
                        DateTimeFormatter.ofPattern("HH:mm:ss")
                );

                if (reminderPornit) {

                    long timpTrecut =
                            (System.currentTimeMillis() - momentStart) / 1000;

                    timpRamas =
                            interval - (int) timpTrecut;

                    if (timpRamas <= 0) {

                        reminder.run();

                        momentStart = System.currentTimeMillis();

                        timpRamas = interval;
                    }
                }

                actualizare.run();
            }
        };

        timer.scheduleAtFixedRate(
                sarcina,
                0,
                1000
        );
    }

    public void setReminderMinutes(int minute) {

        interval = minute * 60;

        timpRamas = interval;

        momentStart = System.currentTimeMillis();
    }

    public void startReminder() {

        momentStart =
                System.currentTimeMillis()
                        - ((long) (interval - timpRamas) * 1000);

        reminderPornit = true;
    }

    public void pauseReminder() {

        if (reminderPornit) {

            long timpTrecut =
                    (System.currentTimeMillis() - momentStart) / 1000;

            timpRamas =
                    interval - (int) timpTrecut;

            if (timpRamas < 0) {
                timpRamas = 0;
            }
        }

        reminderPornit = false;
    }

    public void resetReminder() {

        reminderPornit = false;

        timpRamas = interval;

        momentStart = System.currentTimeMillis();
    }

    public String getCurrentTime() {

        return oraCurenta;
    }

    public String getFormattedRemainingTime() {

        int minute = timpRamas / 60;

        int secunde = timpRamas % 60;

        return String.format(
                "%02d:%02d",
                minute,
                secunde
        );
    }
}
//-_-