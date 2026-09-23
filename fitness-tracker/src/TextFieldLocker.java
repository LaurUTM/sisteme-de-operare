import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TextFieldLocker {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void blocheaza30Secunde(JTextField field) {
        field.setEnabled(false);
        field.setText("Asteapta 30s...");

        executor.schedule(() -> {
            SwingUtilities.invokeLater(() -> {
                field.setEnabled(true);
                field.setText("");
                field.requestFocus();
            });
        }, 30, TimeUnit.SECONDS);
    }
}