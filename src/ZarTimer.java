import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * TIMERUL 3 - reacționează CU O PERIOADĂ indicată (execuții repetate).
 *
 * Tema: un zar automat. La fiecare perioadă, timerul "aruncă" zarul
 * (generează un număr aleator 1..6) și desenează fața obținută.
 *
 * Planificarea: scheduleAtFixedRate(task, 0, perioada)
 *   - prima aruncare imediat (delay = 0);
 *   - apoi câte o aruncare la fiecare 'perioada' milisecunde,
 *     până la apăsarea butonului "Oprește".
 */
public class ZarTimer extends JPanel {

    private final JSpinner spinnerSecunde = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final JButton butonStart = new JButton("Pornește");
    private final JButton butonStop = new JButton("Oprește");
    private final JLabel lblInfo = new JLabel("Aruncări: 0   |   Suma: 0", SwingConstants.CENTER);
    private final Zar zar = new Zar();
    private final Random random = new Random();
    private final Consumer<String> stare;

    private Timer timer;
    private int aruncari = 0;
    private int suma = 0;

    public ZarTimer(Consumer<String> stare) {
        this.stare = stare;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel sus = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sus.add(new JLabel("Aruncă zarul la fiecare (secunde):"));
        sus.add(spinnerSecunde);
        sus.add(butonStart);
        sus.add(butonStop);
        butonStop.setEnabled(false);
        butonStart.addActionListener(e -> porneste());
        butonStop.addActionListener(e -> opreste());
        add(sus, BorderLayout.NORTH);

        add(zar, BorderLayout.CENTER);

        lblInfo.setFont(lblInfo.getFont().deriveFont(Font.BOLD, 16f));
        add(lblInfo, BorderLayout.SOUTH);
    }

    private void porneste() {
        int secunde = (Integer) spinnerSecunde.getValue();
        aruncari = 0;
        suma = 0;

        // Pasul 1: creăm timerul (fir daemon)
        timer = new Timer("zar", true);

        // Pasul 2: acțiunea care se repetă - aruncarea zarului
        TimerTask arunca = new TimerTask() {
            @Override
            public void run() {
                int valoare = random.nextInt(6) + 1;   // număr aleator între 1 și 6
                aruncari++;
                suma += valoare;
                int n = aruncari, s = suma;
                stare.accept("Zar: aruncarea #" + n + " = " + valoare);
                SwingUtilities.invokeLater(() -> {
                    zar.arata(valoare);
                    lblInfo.setText("Aruncări: " + n + "   |   Ultima: " + valoare + "   |   Suma: " + s);
                });
            }
        };

        // Pasul 3: planificare periodică - imediat, apoi la fiecare 'secunde'
        timer.scheduleAtFixedRate(arunca, 0, secunde * 1000L);

        butonStart.setEnabled(false);
        butonStop.setEnabled(true);
        spinnerSecunde.setEnabled(false);
        stare.accept("Zar pornit, perioada = " + secunde + " s");
    }

    private void opreste() {
        if (timer != null) {
            timer.cancel();     // oprire forțată a timerului
            timer = null;
        }
        butonStart.setEnabled(true);
        butonStop.setEnabled(false);
        spinnerSecunde.setEnabled(true);
        stare.accept("Zar oprit după " + aruncari + " aruncări");
    }

    /** Panou care desenează fața zarului (pătrat alb cu puncte). */
    private static class Zar extends JPanel {
        private int valoare = 0;   // 0 = zarul nu a fost încă aruncat

        void arata(int v) {
            valoare = v;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int l = Math.min(Math.min(getWidth(), getHeight()) - 20, 200);   // latura zarului
            int x = (getWidth() - l) / 2, y = (getHeight() - l) / 2;

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, l, l, l / 5, l / 5);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(x, y, l, l, l / 5, l / 5);

            // Pozițiile posibile ale punctelor: 1/4, 1/2, 3/4 din latură
            int a = x + l / 4, b = x + l / 2, c = x + 3 * l / 4;
            int p = y + l / 4, q = y + l / 2, r = y + 3 * l / 4;
            int d = l / 7;   // diametrul unui punct

            switch (valoare) {
                case 1: punct(g2, b, q, d); break;
                case 2: punct(g2, a, p, d); punct(g2, c, r, d); break;
                case 3: punct(g2, a, p, d); punct(g2, b, q, d); punct(g2, c, r, d); break;
                case 4: punct(g2, a, p, d); punct(g2, c, p, d); punct(g2, a, r, d); punct(g2, c, r, d); break;
                case 5: punct(g2, a, p, d); punct(g2, c, p, d); punct(g2, b, q, d);
                        punct(g2, a, r, d); punct(g2, c, r, d); break;
                case 6: punct(g2, a, p, d); punct(g2, c, p, d); punct(g2, a, q, d);
                        punct(g2, c, q, d); punct(g2, a, r, d); punct(g2, c, r, d); break;
                default:
                    g2.setFont(g2.getFont().deriveFont(Font.BOLD, l / 2f));
                    g2.drawString("?", b - l / 8, q + l / 6);
            }
        }

        private static void punct(Graphics2D g2, int cx, int cy, int d) {
            g2.fillOval(cx - d / 2, cy - d / 2, d, d);
        }
    }
}
