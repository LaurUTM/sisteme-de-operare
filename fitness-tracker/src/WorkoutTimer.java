import javax.swing.*;

/** Timer crescator: masoara durata antrenamentului si estimeaza caloriile arse. */
public class WorkoutTimer {

    private static final double KCAL_PER_SECOND = 0.1; // ~6 kcal/min, antrenament moderat

    private final JLabel label;
    private final Timer timer;
    private int seconds;

    public WorkoutTimer(JLabel label) {
        this.label = label;
        this.timer = new Timer(1000, e -> {
            seconds++;
            update();
        });
        update();
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void reset() {
        timer.stop();
        seconds = 0;
        update();
    }

    private void update() {
        label.setText(String.format("Antrenament: %02d:%02d  |  Calorii: %.1f kcal",
                seconds / 60, seconds % 60, seconds * KCAL_PER_SECOND));
    }
}
