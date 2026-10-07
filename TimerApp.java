import java.awt.GridLayout;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TimerApp extends JFrame {

    private JTextField intervalField;
    private JTextField oraField;
    private JTextField perioadaField;

    private JLabel statusLabel;
    private JLabel cronometruLabel;

    private Timer timerPeriodic;
    private Timer timerCronometru;

    private int secundeCronometru = 0;
    private boolean cronometruPornit = false;

    public TimerApp() {

        setTitle("Planificarea activitatii proceselor");
        setSize(550, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(12, 2, 10, 10));

        // timer 1
        panel.add(new JLabel("Interval (secunde):"));

        intervalField = new JTextField("5");
        panel.add(intervalField);

        JButton butonInterval = new JButton("Executa dupa interval");
        panel.add(butonInterval);

        panel.add(new JLabel(""));

        // timer 2
        panel.add(new JLabel("Alt Text (HH:mm):"));

        oraField = new JTextField("12:00");
        panel.add(oraField);

        JButton butonOra = new JButton("Executa la ora");
        panel.add(butonOra);

        panel.add(new JLabel(""));

        // timer 3
        panel.add(new JLabel("Iara text (secunde):"));

        perioadaField = new JTextField("3");
        panel.add(perioadaField);

        JButton butonPeriodic = new JButton("Porneste periodic");
        panel.add(butonPeriodic);

        JButton butonStop = new JButton("Opreste periodic");
        panel.add(butonStop);

        // cronometru
        panel.add(new JLabel("Cronometrul:"));

        cronometruLabel = new JLabel("00:00:00");
        panel.add(cronometruLabel);

        JButton butonStart = new JButton("Start");
        panel.add(butonStart);

        JButton butonPauza = new JButton("Pauza");
        panel.add(butonPauza);

        JButton butonReset = new JButton("Reset");
        panel.add(butonReset);

        panel.add(new JLabel(""));

        statusLabel = new JLabel("Asteptare...");
        panel.add(statusLabel);

        add(panel);

        // timer dupa interval
        butonInterval.addActionListener(e -> executaDupaInterval());

        // timer la ora
        butonOra.addActionListener(e -> executaLaOra());

        // timer periodic
        butonPeriodic.addActionListener(e -> pornesteTimerPeriodic());

        // oprire timer periodic
        butonStop.addActionListener(e -> opresteTimerPeriodic());

        // cronometru
        butonStart.addActionListener(e -> pornesteCronometru());

        butonPauza.addActionListener(e -> pauzaCronometru());

        butonReset.addActionListener(e -> resetCronometru());
    }

    // 1. executare dupa un anumit interval
    private void executaDupaInterval() {

        try {

            int secunde = Integer.parseInt(intervalField.getText());

            Timer timer = new Timer();

            statusLabel.setText("Timer pornit...");

            timer.schedule(new TimerTask() {

                @Override
                public void run() {

                    SwingUtilities.invokeLater(() -> {

                        statusLabel.setText(
                            "Executat dupa " + secunde + " secunde"
                        );

                        JOptionPane.showMessageDialog(
                            TimerApp.this,
                            "Au trecut " + secunde + " secunde!"
                        );
                    });

                    timer.cancel();
                }

            }, secunde * 1000L);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                this,
                "Introdu un numar valid!"
            );
        }
    }

    // 2. executare la o anumita ora
    private void executaLaOra() {

        try {

            String textOra = oraField.getText();

            String[] parti = textOra.split(":");

            int ora = Integer.parseInt(parti[0]);
            int minute = Integer.parseInt(parti[1]);

            if (ora < 0 || ora > 23 || minute < 0 || minute > 59) {
                throw new Exception();
            }

            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, ora);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Date timp = calendar.getTime();

            if (timp.before(new Date())) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                timp = calendar.getTime();
            }

            Timer timer = new Timer();

            statusLabel.setText(
                "Programat pentru " + textOra
            );

            timer.schedule(new TimerTask() {

                @Override
                public void run() {

                    SwingUtilities.invokeLater(() -> {

                        statusLabel.setText(
                            "Executat la " + textOra
                        );

                        JOptionPane.showMessageDialog(
                            TimerApp.this,
                            "Timer executat la ora " + textOra
                        );
                    });

                    timer.cancel();
                }

            }, timp);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                this,
                "Alt text HH:MM"
            );
        }
    }

    // 3. executare repetata
    private void pornesteTimerPeriodic() {

        try {

            int secunde = Integer.parseInt(
                perioadaField.getText()
            );

            if (secunde <= 0) {
                throw new NumberFormatException();
            }

            if (timerPeriodic != null) {
                timerPeriodic.cancel();
            }

            timerPeriodic = new Timer();

            statusLabel.setText(
                "Timer periodic: fiecare "
                + secunde
                + " secunde"
            );

            timerPeriodic.scheduleAtFixedRate(
                new TimerTask() {

                    int numarExecutari = 0;

                    @Override
                    public void run() {

                        numarExecutari++;

                        int numar = numarExecutari;

                        SwingUtilities.invokeLater(() -> {

                            statusLabel.setText(
                                "Executare periodica: "
                                + numar
                            );
                        });

                        System.out.println(
                            "Executare periodica: "
                            + numar
                        );
                    }
                },

                0,
                secunde * 1000L
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                this,
                "Introdu o perioada valida!"
            );
        }
    }

    private void opresteTimerPeriodic() {

        if (timerPeriodic != null) {

            timerPeriodic.cancel();
            timerPeriodic = null;

            statusLabel.setText(
                "Timer periodic oprit"
            );
        }
    }

    // cronometru
    private void pornesteCronometru() {

        if (cronometruPornit) {
            return;
        }

        cronometruPornit = true;

        timerCronometru = new Timer();

        timerCronometru.scheduleAtFixedRate(
            new TimerTask() {

                @Override
                public void run() {

                    secundeCronometru++;

                    SwingUtilities.invokeLater(() -> {
                        actualizeazaCronometru();
                    });
                }
            },

            1000,
            1000
        );
    }

    private void pauzaCronometru() {

        cronometruPornit = false;

        if (timerCronometru != null) {

            timerCronometru.cancel();
            timerCronometru = null;
        }
    }

    private void resetCronometru() {

        pauzaCronometru();

        secundeCronometru = 0;

        actualizeazaCronometru();
    }

    private void actualizeazaCronometru() {

        int ore = secundeCronometru / 3600;

        int minute = (secundeCronometru % 3600) / 60;

        int secunde = secundeCronometru % 60;

        cronometruLabel.setText(
            String.format(
                "%02d:%02d:%02d",
                ore,
                minute,
                secunde
            )
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            TimerApp aplicatie = new TimerApp();

            aplicatie.setVisible(true);
        });
    }
}