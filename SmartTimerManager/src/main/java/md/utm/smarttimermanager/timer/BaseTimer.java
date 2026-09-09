package md.utm.smarttimermanager.timer;

import java.util.Timer;

public abstract class BaseTimer {

    protected Timer timer;
    protected boolean running = false;

    public void stopTimer() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public abstract void start();
}