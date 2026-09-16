import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class TimerExamen extends JPanel {

    // Elementele interfetei
    JLabel timeLabel;
    JLabel statusLabel;
    JTextField durataField;
    JButton startButton;
    JButton stopButton;
    JTextArea log;

    // Timerele examenului
    Timer timerAfisaj;
    Timer timerAvert80;
    Timer timerAvert90;
    Timer timerFinal;

    // Secunde scurse de la pornire
    int scurs = 0;

    public TimerExamen() {

        setLayout(new BorderLayout());

        // Titlu
        JLabel titleLabel = new JLabel("TIMER EXAMEN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));

        // Timpul scurs / ramas
        timeLabel = new JLabel("Scurs: 0s | Ramas: 0s", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 30));

        // Status
        statusLabel = new JLabel("Gata de pornire", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Campul pentru durata examenului
        durataField = new JTextField("20", 4);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Durata (secunde):"));
        inputPanel.add(durataField);

        // Butoane
        startButton = new JButton("START");
        stopButton = new JButton("STOP");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Zona de log (inlocuieste System.out)
        log = new JTextArea(8, 30);
        log.setEditable(false);
        JScrollPane scroll = new JScrollPane(log);

        // Panel de sus cu titlu, timp, status, input, butoane
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(Box.createVerticalStrut(20));
        top.add(titleLabel);

        top.add(Box.createVerticalStrut(20));
        top.add(timeLabel);

        top.add(Box.createVerticalStrut(10));
        top.add(statusLabel);

        top.add(Box.createVerticalStrut(20));
        top.add(inputPanel);

        top.add(Box.createVerticalStrut(20));
        top.add(buttonPanel);

        top.add(Box.createVerticalStrut(20));

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Butonul START
        startButton.addActionListener(e -> startExamen());

        // Butonul STOP
        stopButton.addActionListener(e -> stopExamen());
    }


    // Porneste examenul
    void startExamen() {

        // Daca examenul deja merge, nu pornim altul
        if (timerAfisaj != null) {
            return;
        }

        final int D;

        try {

            D = Integer.parseInt(durataField.getText());

            if (D <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Introdu o durata mai mare decat 0."
                );
                return;
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Introdu doar numere."
            );

            return;
        }

        scurs = 0;
        log.setText("");

        final long delay80 = Math.round(0.80 * D) * 1_000L;
        final long delay90 = Math.round(0.90 * D) * 1_000L;
        final long delayFinal = (long) D * 1_000L;

        final long ramas80 = D - delay80 / 1_000L; // secunde ramase la 80%
        final long ramas90 = D - delay90 / 1_000L; // secunde ramase la 90%

        adaugaLog("Examen pornit. Durata = " + D + "s");
        adaugaLog("Avertisment la 80% (" + (delay80 / 1000) + "s) si 90% ("
                + (delay90 / 1000) + "s).");
        statusLabel.setText("Examen in desfasurare...");

        // Afisajul secunda cu secunda
        timerAfisaj = new Timer();
        timerAfisaj.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                int ramas = D - scurs;

                if (ramas >= 0) {

                    final int s = scurs;
                    final int r = ramas;

                    SwingUtilities.invokeLater(() ->
                            timeLabel.setText("Scurs: " + s + "s | Ramas: " + r + "s"));
                }

                scurs++;
            }
        }, 0, 1_000);

        // Avertisment la 80%
        timerAvert80 = new Timer();
        timerAvert80.schedule(new TimerTask() {
            @Override
            public void run() {
                adaugaLog(">>> ATENTIE: au mai ramas " + ramas80 + " secunde!");
                timerAvert80.cancel();
            }
        }, delay80);

        // Avertisment la 90%
        timerAvert90 = new Timer();
        timerAvert90.schedule(new TimerTask() {
            @Override
            public void run() {
                adaugaLog(">>> ATENTIE: au mai ramas " + ramas90 + " secunde!");
                timerAvert90.cancel();
            }
        }, delay90);

        // Sfarsitul examenului
        timerFinal = new Timer();
        timerFinal.schedule(new TimerTask() {
            @Override
            public void run() {

                adaugaLog("=== TIMP EXPIRAT! Predati lucrarile. ===");

                opresteTimere();

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Examen terminat!");
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }, delayFinal);
    }


    // Opreste examenul manual
    void stopExamen() {

        if (timerAfisaj != null) {

            opresteTimere();

            statusLabel.setText("Examen oprit.");
            adaugaLog("Examen oprit manual.");
        }
    }


    // Anuleaza toate timerele
    private void opresteTimere() {

        if (timerAfisaj != null)  { timerAfisaj.cancel();  timerAfisaj = null;  }
        if (timerAvert80 != null) { timerAvert80.cancel(); timerAvert80 = null; }
        if (timerAvert90 != null) { timerAvert90.cancel(); timerAvert90 = null; }
        if (timerFinal != null)   { timerFinal.cancel();   timerFinal = null;   }
    }


    // Adauga o linie in zona de log (pe EDT)
    private void adaugaLog(String mesaj) {
        SwingUtilities.invokeLater(() -> log.append(mesaj + "\n"));
    }
}
