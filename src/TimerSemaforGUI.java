/*
 * ============================================================================
 *  IMPLEMENTARE — "Semafor (versiune GUI Swing)"
 * ============================================================================
 *
 *  IDEEA DE IMPLEMENTARE (se citește înainte de cod):
 *
 *  Scop: aceeași logică de semafor (modul PERIOADĂ INDICATĂ), dar cu interfață
 *        grafică Swing, în stilul Exemplului 2 din laborator (javax.swing.Timer).
 *
 *  De ce javax.swing.Timer si NU java.util.Timer:
 *        actualizarea componentelor Swing trebuie facuta pe Event Dispatch
 *        Thread (EDT). javax.swing.Timer declanseaza actionPerformed() chiar pe
 *        EDT, deci putem apela in siguranta repaint()/setText(). Cu
 *        java.util.Timer ar fi trebuit sa impachetam totul in
 *        SwingUtilities.invokeLater(...).
 *
 *  Pași de proiectare:
 *    1. Reutilizam ideea de enum Stare { VERDE, GALBEN, ROSU } cu durata (s) si
 *       tranzitia catre culoarea urmatoare.
 *    2. PanouSemafor extends JPanel: in paintComponent deseneaza 3 becuri
 *       (cercuri). Becul starii curente e "aprins" (culoare vie), restul sunt
 *       "stinse" (culoare inchisa).
 *    3. Un javax.swing.Timer cu perioada 1000 ms: la fiecare tick scade
 *       secundele ramase; cand ajung la 0 trece la culoarea urmatoare, apoi
 *       actualizeaza eticheta si cheama repaint().
 *    4. Fereastra JFrame contine panoul si o eticheta de stare jos.
 *    5. Buton Start/Stop optional pentru a porni/opri timerul.
 *
 *  Clase Java folosite: javax.swing.*, java.awt.*, javax.swing.Timer.
 * ============================================================================
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class TimerSemaforGUI extends JFrame {

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
        super("Semafor");

        getContentPane().add(panou, BorderLayout.CENTER);
        getContentPane().add(eticheta, BorderLayout.NORTH);
        getContentPane().add(butonStartStop, BorderLayout.SOUTH);

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
                    butonStartStop.setText("Stop");
                }
            }
        });

        actualizeazaEticheta();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(220, 420);
        setLocationRelativeTo(null); // centreaza fereastra
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

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TimerSemaforGUI fereastra = new TimerSemaforGUI();
                fereastra.setVisible(true);
                fereastra.timer.start(); // pornim semaforul
            }
        });
    }
}
