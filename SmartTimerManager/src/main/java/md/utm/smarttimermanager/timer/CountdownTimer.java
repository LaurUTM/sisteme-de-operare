package md.utm.smarttimermanager.timer;

import javafx.application.Platform;

public class CountdownTimer {

    private volatile int secundeRamase;
    private int secundeInitiale;

    private volatile boolean pauzat = false;
    private volatile boolean running = false;

    private Thread firTimer;

    private Runnable actiuneActualizare;


    public CountdownTimer(Runnable actiuneActualizare) {
        this.actiuneActualizare = actiuneActualizare;
    }


    public void setTime(int ore, int minute, int secunde) {//se face initiakuzarea

        secundeInitiale =
                ore * 3600 +
                        minute * 60 +
                        secunde;

        secundeRamase = secundeInitiale;
    }


    public void start() {

        if (running) {
            return;
        }

        if (secundeRamase <= 0) {
            return;
        }

        running = true;
        pauzat = false;


        firTimer = new Thread(() -> {//creeam un fioer seoparat de executie

            while (running && secundeRamase > 0) {//repet codul atat timp cat timerul ruleaza si mai exista

                try {

                    Thread.sleep(1000);

                } catch (InterruptedException e) {

                    return;
                }


                if (!running) {
                    return;
                }


                secundeRamase--;//scade cu 1


                if (secundeRamase <= 0) {

                    secundeRamase = 0;

                    running = false;
                    pauzat = false;

                    Platform.runLater(
                            actiuneActualizare
                    );

                    return;
                }


                Platform.runLater(
                        actiuneActualizare
                );
            }
        });


        firTimer.setDaemon(true);//nu tine timerul deschis

        firTimer.start();
    }


    public void pause() {

        if (!running) {
            return;
        }

        running = false;
        pauzat = true;


        if (firTimer != null) {
            firTimer.interrupt();
        }
    }


    public void cancel() {

        running = false;
        pauzat = false;


        if (firTimer != null) {
            firTimer.interrupt();
        }


        secundeRamase = secundeInitiale;


        Platform.runLater(
                actiuneActualizare
        );
    }


    public boolean isPaused() {
        return pauzat;
    }


    public boolean isRunning() {
        return running;
    }


    public int getRemainingSeconds() {
        return secundeRamase;
    }


    public static String formatTime(int secunde) {

        int ore = secunde / 3600;

        int minute =
                (secunde % 3600) / 60;

        int sec =
                secunde % 60;


        return String.format(
                "%02d:%02d:%02d",
                ore,
                minute,
                sec
        );
    }
}