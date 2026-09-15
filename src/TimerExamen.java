import java.util.Timer;
import java.util.TimerTask;

public class TimerExamen {

    static class Afisaj extends TimerTask {
        private final int durataTotala; // secunde
        private int scurs = 0;

        Afisaj(int durataTotala) {
            this.durataTotala = durataTotala;
        }

        @Override
        public void run() {
            int ramas = durataTotala - scurs;
            if (ramas >= 0) {
                System.out.println("Scurs: " + scurs + "s | Ramas: " + ramas + "s");
            }
            scurs++;
        }
    }

    public static void main(String[] args) {
        final int D = 20;

        final long delay80 = Math.round(0.80 * D) * 1_000L;
        final long delay90 = Math.round(0.90 * D) * 1_000L;
        final long delayFinal = (long) D * 1_000L;

        final long ramas80 = D - delay80 / 1_000L; // secunde ramase la 80%
        final long ramas90 = D - delay90 / 1_000L; // secunde ramase la 90%

        System.out.println("Examen pornit. Durata = " + D + "s");
        System.out.println("Avertisment la 80% (" + (delay80 / 1000) + "s) si 90% ("
                + (delay90 / 1000) + "s).");

        final Timer timerAfisaj = new Timer();
        timerAfisaj.scheduleAtFixedRate(new Afisaj(D), 0, 1_000);

        final Timer timerAvert80 = new Timer();
        timerAvert80.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(">>> ATENTIE: au mai ramas " + ramas80 + " secunde!");
                timerAvert80.cancel();
            }
        }, delay80);

        final Timer timerAvert90 = new Timer();
        timerAvert90.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(">>> ATENTIE: au mai ramas " + ramas90 + " secunde!");
                timerAvert90.cancel();
            }
        }, delay90);

        final Timer timerFinal = new Timer();
        timerFinal.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("=== TIMP EXPIRAT! Predati lucrarile. ===");
                timerAfisaj.cancel();
                timerAvert80.cancel();
                timerAvert90.cancel();
                timerFinal.cancel();
                System.exit(0);
            }
        }, delayFinal);
    }
}
