package md.utm.smarttimermanager.timer;

import java.util.Timer;
import java.util.TimerTask;

public class FocusCycleTimer extends BaseTimer {

    private int focusSeconds;
    private int breakSeconds;

    private int totalCycles;
    private int currentCycle;

    private int remainingSeconds;

    private boolean focusPhase;
    private boolean paused;

    private boolean autoStart;

    private Runnable onUpdate;
    private Runnable onFinish;


    // Constructor
    public FocusCycleTimer(Runnable onUpdate,
                           Runnable onFinish) {

        this.onUpdate = onUpdate;
        this.onFinish = onFinish;
    }


    // Setările complete
    public void setSettings(
            int focusMinutes,
            int breakMinutes,
            int cycles,
            boolean autoStart
    ) {

        focusSeconds = focusMinutes * 60;
        breakSeconds = breakMinutes * 60;

        totalCycles = cycles;

        this.autoStart = autoStart;

        currentCycle = 1;

        focusPhase = true;

        paused = false;

        remainingSeconds = focusSeconds;

        update();
    }


    // SUPRAÎNCĂRCARE
    // Dacă nu trimitem autoStart,
    // acesta va fi true.
    public void setSettings(
            int focusMinutes,
            int breakMinutes,
            int cycles
    ) {

        setSettings(
                focusMinutes,
                breakMinutes,
                cycles,
                true
        );
    }


    // SUPRASCRIERE
    @Override
    public void start() {

        if (running) {
            return;
        }


        timer = new Timer(true);

        running = true;
        paused = false;


        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                remainingSeconds--;

                update();


                // Sesiunea s-a terminat
                if (remainingSeconds <= 0) {

                    nextSession();
                }
            }
        };


        // Task-ul se execută la fiecare secundă.
        timer.scheduleAtFixedRate(
                task,
                1000,
                1000
        );
    }


    // PAUSE
    public void pause() {

        if (!running) {
            return;
        }

        stopTimer();

        paused = true;

        update();
    }


    // SKIP
    public void skip() {

        boolean wasRunning = running;

        stopTimer();

        nextSession();


        // Dacă timerul mergea înainte de Skip,
        // pornim sesiunea următoare.
        if (wasRunning) {
            start();
        }
    }


    // STOP
    public void stop() {

        stopTimer();

        paused = false;

        currentCycle = 1;

        focusPhase = true;

        remainingSeconds = focusSeconds;

        update();
    }


    // Trecem la următoarea sesiune.
    private void nextSession() {

        // Dacă eram în Focus,
        // trecem în Break.
        if (focusPhase) {

            focusPhase = false;

            remainingSeconds = breakSeconds;

        } else {

            // Dacă am terminat toate ciclurile
            if (currentCycle >= totalCycles) {

                finish();

                return;
            }


            // Trecem la ciclul următor.
            currentCycle++;

            focusPhase = true;

            remainingSeconds = focusSeconds;
        }


        update();


        // Dacă Auto-start nu este bifat,
        // ne oprim între sesiuni.
        if (!autoStart) {

            stopTimer();

            paused = true;
        }
    }


    // Final
    private void finish() {

        stopTimer();

        paused = false;

        remainingSeconds = 0;

        update();


        if (onFinish != null) {
            onFinish.run();
        }
    }


    // Actualizăm interfața.
    private void update() {

        if (onUpdate != null) {
            onUpdate.run();
        }
    }


    // GETTERS

    public int getRemainingSeconds() {
        return remainingSeconds;
    }


    public int getCurrentCycle() {
        return currentCycle;
    }


    public int getTotalCycles() {
        return totalCycles;
    }


    public boolean isFocusPhase() {
        return focusPhase;
    }


    public boolean isPaused() {
        return paused;
    }


    public String getPhaseText() {

        if (focusPhase) {
            return "FOCUS TIME";
        } else {
            return "BREAK TIME";
        }
    }


    // Transformă secundele:
    // 1500 secunde -> 25:00
    public static String formatTime(int seconds) {

        int minutes = seconds / 60;

        int sec = seconds % 60;

        return String.format(
                "%02d:%02d",
                minutes,
                sec
        );
    }
}