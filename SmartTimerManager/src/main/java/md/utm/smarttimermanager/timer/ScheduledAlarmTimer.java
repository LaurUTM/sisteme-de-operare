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

    public ScheduledAlarmTimer(Runnable action) {
        this.action = action;
    }

    public void setTime(int hour, int minute, int second) {

        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public void setTime(int hour, int minute) {

        setTime(hour, minute, 0);
    }

    @Override
    public void start() {

        stopTimer();

        Calendar currentTime = Calendar.getInstance();

        Calendar alarmTime = Calendar.getInstance();

        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, second);
        alarmTime.set(Calendar.MILLISECOND, 0);

        if (alarmTime.before(currentTime)) {

            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        Date date = alarmTime.getTime();

        timer = new Timer(true);

        running = true;

        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                action.run();

                running = false;

                stopTimer();
            }
        };

        timer.schedule(task, date);
    }

    public void cancelAlarm() {

        stopTimer();
    }
}