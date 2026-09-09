package md.utm.smarttimermanager.timer;

import java.util.Timer;
import java.util.TimerTask;

public class FocusCycleTimer extends BaseTimer {

    private int secundeFocus;
    private int secundePauza;
    private int secundeRamase;

    private int totalCicluri;
    private int ciclulCurent;

    private boolean esteFocus = true;
    private boolean estePauzat = false;
    private boolean pornireAutomata;

    private Runnable actiuneActualizare;
    private Runnable actiuneFinalizare;


    public FocusCycleTimer(Runnable actiuneActualizare,
                           Runnable actiuneFinalizare) {

        this.actiuneActualizare = actiuneActualizare;
        this.actiuneFinalizare = actiuneFinalizare;
    }


    public void setSettings(int minuteFocus,
                            int minutePauza,
                            int cicluri,
                            boolean pornireAutomata) {

        secundeFocus = minuteFocus * 60;
        secundePauza = minutePauza * 60;

        totalCicluri = cicluri;
        ciclulCurent = 1;

        esteFocus = true;
        estePauzat = false;

        this.pornireAutomata = pornireAutomata;

        secundeRamase = secundeFocus;

        actiuneActualizare.run();
    }


    @Override
    public void start() {

        if (running) {
            return;
        }

        timer = new Timer();

        running = true;
        estePauzat = false;


        TimerTask sarcina = new TimerTask() {

            @Override
            public void run() {

                secundeRamase--;

                if (secundeRamase <= 0) {
                    urmatoareaSesiune();
                }

                actiuneActualizare.run();
            }
        };


        timer.scheduleAtFixedRate(
                sarcina,
                1000,
                1000
        );
    }


    public void pause() {

        stopTimer();

        estePauzat = true;
    }


    public void skip() {

        urmatoareaSesiune();

        actiuneActualizare.run();
    }


    public void stop() {

        stopTimer();

        ciclulCurent = 1;

        esteFocus = true;
        estePauzat = false;

        secundeRamase = secundeFocus;

        actiuneActualizare.run();
    }


    private void urmatoareaSesiune() {

        if (esteFocus) {

            esteFocus = false;

            secundeRamase = secundePauza;

        } else {

            ciclulCurent++;

            if (ciclulCurent > totalCicluri) {

                stopTimer();

                secundeRamase = 0;

                actiuneFinalizare.run();

                return;
            }

            esteFocus = true;

            secundeRamase = secundeFocus;
        }


        if (!pornireAutomata) {

            stopTimer();

            estePauzat = true;
        }
    }


    public int getRemainingSeconds() {

        return secundeRamase;
    }


    public int getCurrentCycle() {

        return ciclulCurent;
    }


    public int getTotalCycles() {

        return totalCicluri;
    }


    public boolean isFocusPhase() {

        return esteFocus;
    }


    public boolean isPaused() {

        return estePauzat;
    }


    public String getPhaseText() {

        if (esteFocus) {
            return "FOCUS TIME";
        }

        return "BREAK TIME";
    }


    public static String formatTime(int secunde) {

        int minute = secunde / 60;
        int sec = secunde % 60;

        return String.format(
                "%02d:%02d",
                minute,
                sec
        );
    }
}