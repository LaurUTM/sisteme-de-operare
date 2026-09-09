package md.utm.smarttimermanager.timer;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduledAlarmTimer extends BaseTimer {

    private int hour;
    private int minute;
    private int second;

    private Runnable action;


    // Constructor
    public ScheduledAlarmTimer(Runnable action) {
        this.action = action;
    }


    // Setăm ora completă
    public void setTime(int hour, int minute, int second) {

        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }


    // SUPRAÎNCĂRCARE
    // Dacă utilizatorul introduce doar ora și minutul,
    // secundele vor fi automat 0.
    public void setTime(int hour, int minute) {

        setTime(hour, minute, 0);
    }


    // SUPRASCRIERE
    // start() există în BaseTimer,
    // dar aici îi spunem ce face Timerul 2.
    @Override
    public void start() {

        // Dacă exista deja o alarmă, o oprim.
        stopTimer();

        Calendar currentTime = Calendar.getInstance();

        Calendar alarmTime = Calendar.getInstance();

        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, second);
        alarmTime.set(Calendar.MILLISECOND, 0);


        // Dacă ora a trecut deja azi,
        // alarma va fi pentru ziua următoare.
        if (alarmTime.before(currentTime)) {

            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }


        Date date = alarmTime.getTime();


        timer = new Timer(true);

        running = true;


        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                // Executăm acțiunea
                action.run();

                running = false;

                stopTimer();
            }
        };


        // Executăm task-ul la ora exactă.
        timer.schedule(task, date);
    }


    // Butonul Cancel
    public void cancelAlarm() {

        stopTimer();
    }
}