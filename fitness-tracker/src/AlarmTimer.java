import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmTimer {

    public void seteazaAlarma(int secunde, Component parinte) {
        Timer timer = new Timer(true);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(parinte, "Timpul a trecut!"));
            }
        }, secunde * 1000L);
    }
}