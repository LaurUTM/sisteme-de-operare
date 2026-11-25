import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

// Lab 1 (timere) refacut in Java, rulat in container Docker.
// Cele 3 cerinte din lab 1:
//   1) reactie dupa un interval de timp   -> timer.schedule(task, intarziere)
//   2) reactie la un anumit moment         -> timer.schedule(task, Date)
//   3) reactie cu o perioada indicata      -> timer.scheduleAtFixedRate(task, 0, perioada)
public class TimerApp {

    static final DateTimeFormatter F = DateTimeFormatter.ofPattern("HH:mm:ss");

    static void log(String msg) {
        System.out.println("[" + LocalTime.now().format(F) + "] " + msg);
    }

    // citeste o valoare din variabilele de mediu (setate in docker-compose.yml)
    static int env(String name, int def) {
        String v = System.getenv(name);
        return v == null ? def : Integer.parseInt(v);
    }

    public static void main(String[] args) throws InterruptedException {
        int delaySec = env("DELAY_SEC", 3);    // dupa cate secunde vine mesajul intarziat
        int atSec = env("AT_SEC", 6);          // momentul = acum + atSec secunde
        int periodSec = env("PERIOD_SEC", 2);  // perioada reamintirii
        int repeat = env("REPEAT", 5);         // de cate ori se repeta reamintirea

        Timer timer = new Timer("TimerLab");
        // latch = numarator; programul se termina cand toate sarcinile au terminat
        CountDownLatch done = new CountDownLatch(3);

        log("Pornire. Intarziere=" + delaySec + "s, moment=+" + atSec + "s, perioada=" + periodSec + "s x" + repeat);

        // 1) o singura executie, dupa un interval
        timer.schedule(new TimerTask() {
            public void run() {
                log("1) INTERVAL: au trecut " + delaySec + " secunde.");
                done.countDown();
            }
        }, delaySec * 1000L);

        // 2) o singura executie, la un moment exact (Date)
        Date moment = new Date(System.currentTimeMillis() + atSec * 1000L);
        log("2) MOMENT programat pentru ora " + new java.text.SimpleDateFormat("HH:mm:ss").format(moment));
        timer.schedule(new TimerTask() {
            public void run() {
                log("2) MOMENT: este ora programata.");
                done.countDown();
            }
        }, moment);

        // 3) executie repetata, cu perioada fixa
        timer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            public void run() {
                count++;
                log("3) PERIODIC: reamintirea nr. " + count + " din " + repeat);
                if (count == repeat) {
                    cancel();          // opreste doar aceasta sarcina
                    done.countDown();
                }
            }
        }, 0, periodSec * 1000L);

        done.await();      // asteapta toate cele 3 sarcini
        timer.cancel();    // opreste firul timer-ului
        log("Toate timerele s-au terminat.");
    }
}
