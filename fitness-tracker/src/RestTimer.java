import javax.swing.*;
import java.awt.*;

/** Timer descrescator: numara pauza dintre seturi pana la 0, apoi anunta urmatorul set. */
public class RestTimer {

    private final JLabel label;
    private final Runnable onFinished;
    private final Timer timer;
    private int seconds;

    public RestTimer(JLabel label, Runnable onFinished) {
        this.label = label;
        this.onFinished = onFinished;
        this.timer = new Timer(1000, e -> tick());
        label.setText("Pauza: --");
    }

    public void start(int duration) {
        seconds = duration;
        update();
        timer.restart();
    }

    private void tick() {
        seconds--;
        update();
        if (seconds == 0) {
            timer.stop();
            Toolkit.getDefaultToolkit().beep();
            label.setText("Pauza s-a terminat! Incepe setul urmator.");
            onFinished.run();
        }
    }

    private void update() {
        label.setText(String.format("Pauza: %02d:%02d", seconds / 60, seconds % 60));
    }
}
