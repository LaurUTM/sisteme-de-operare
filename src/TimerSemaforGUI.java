import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class TimerSemaforGUI extends JPanel {

    enum Stare {
        VERDE(5), GALBEN(2), ROSU(5);

        final int durata; // secunde

        Stare(int durata) {
            this.durata = durata;
        }

        Stare urmatoarea() {
            switch (this) {
                case VERDE:  return GALBEN;
                case GALBEN: return ROSU;
                default:     return VERDE;
            }
        }
    }

    private Stare stare = Stare.ROSU;      // starea curenta a semaforului
    private int ramase = Stare.ROSU.durata; // secunde ramase din starea curenta

    private final PanouSemafor panou = new PanouSemafor();
    private final JLabel eticheta = new JLabel("", SwingConstants.CENTER);
    private final JButton butonStartStop = new JButton("Stop");
    private final Timer timer;

    public TimerSemaforGUI() {
        setLayout(new BorderLayout());

        add(panou, BorderLayout.CENTER);
        add(eticheta, BorderLayout.NORTH);
        add(butonStartStop, BorderLayout.SOUTH);

        timer = new Timer(1_000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });

        butonStartStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer.isRunning()) {
                    timer.stop();
                    butonStartStop.setText("Start");
                } else {
                    timer.start();
                    butonStartStop.setText("Sfarsit");
                }
            }
        });

        actualizeazaEticheta();

        timer.start(); // pornim semaforul
    }

    private void tick() {
        ramase--;
        if (ramase <= 0) {
            stare = stare.urmatoarea();
            ramase = stare.durata;
        }
        actualizeazaEticheta();
        panou.repaint();
    }

    private void actualizeazaEticheta() {
        eticheta.setText(stare + " — mai sunt " + ramase + "s");
    }

    private class PanouSemafor extends JPanel {

        PanouSemafor() {
            setPreferredSize(new Dimension(200, 340));
            setBackground(Color.DARK_GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int d = 90;             // diametrul unui bec
            int x = (getWidth() - d) / 2;
            int gap = 20;           // spatiu intre becuri
            int y0 = 15;            // rosu (sus)
            int y1 = y0 + d + gap;  // galben (mijloc)
            int y2 = y1 + d + gap;  // verde (jos)

            deseneazaBec(g, x, y0, d, Color.RED,    stare == Stare.ROSU);
            deseneazaBec(g, x, y1, d, Color.YELLOW, stare == Stare.GALBEN);
            deseneazaBec(g, x, y2, d, Color.GREEN,  stare == Stare.VERDE);
        }

        private void deseneazaBec(Graphics g, int x, int y, int d,
                                  Color culoare, boolean aprins) {
            g.setColor(aprins ? culoare : culoare.darker().darker().darker());
            g.fillOval(x, y, d, d);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, d, d);
        }
    }
}
