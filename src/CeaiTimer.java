import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * TIMERUL 1 - reacționează DUPĂ UN ANUMIT INTERVAL de timp.
 *
 * Tema: un "ceainic" - alegi tipul de ceai, iar după timpul de infuzare
 * aplicația te anunță că ceaiul e gata.
 *
 * Pe ACELAȘI obiect Timer sunt planificate DOUĂ acțiuni (TimerTask):
 *   - alarma   : schedule(alarma, delay)          -> rulează o singură dată, după interval
 *   - progresul: schedule(progres, 0, 200)        -> actualizează bara de progres
 * Când alarma se declanșează, apelează timer.cancel(), care oprește ambele acțiuni.
 */
public class CeaiTimer extends JPanel {

    // Tipurile de ceai și timpul de infuzare (în secunde)
    private static final String[] CEAIURI = {
            "Test rapid (10 secunde)",
            "Ceai verde (2 minute)",
            "Ceai negru (4 minute)",
            "Ceai din plante (6 minute)",
            "Personalizat"
    };
    private static final int[] SECUNDE = {10, 120, 240, 360, -1};

    private final JComboBox<String> comboCeai = new JComboBox<>(CEAIURI);
    private final JSpinner spinnerSecunde = new JSpinner(new SpinnerNumberModel(30, 1, 3600, 1));
    private final JProgressBar bara = new JProgressBar(0, 1000);
    private final JLabel ramas = new JLabel("Alege ceaiul și apasă „Pune la infuzat”", SwingConstants.CENTER);
    private final JButton butonStart = new JButton("Pune la infuzat");
    private final JButton butonAnuleaza = new JButton("Anulează");
    private final Consumer<String> stare;

    private Timer timer;   // timerul activ; null când nu rulează nimic

    public CeaiTimer(Consumer<String> stare) {
        this.stare = stare;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel sus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sus.add(new JLabel("Ceai:"));
        sus.add(comboCeai);
        sus.add(new JLabel("Secunde:"));
        sus.add(spinnerSecunde);
        spinnerSecunde.setEnabled(false);
        // Câmpul de secunde e activ doar pentru varianta "Personalizat"
        comboCeai.addActionListener(e ->
                spinnerSecunde.setEnabled(SECUNDE[comboCeai.getSelectedIndex()] < 0));
        add(sus, BorderLayout.NORTH);

        JPanel centru = new JPanel(new BorderLayout(10, 10));
        ramas.setFont(ramas.getFont().deriveFont(Font.BOLD, 26f));
        bara.setStringPainted(true);
        bara.setString("");
        bara.setPreferredSize(new Dimension(400, 30));
        centru.add(ramas, BorderLayout.CENTER);
        centru.add(bara, BorderLayout.SOUTH);
        add(centru, BorderLayout.CENTER);

        JPanel jos = new JPanel(new FlowLayout(FlowLayout.CENTER));
        butonAnuleaza.setEnabled(false);
        butonStart.addActionListener(e -> porneste());
        butonAnuleaza.addActionListener(e -> anuleaza());
        jos.add(butonStart);
        jos.add(butonAnuleaza);
        add(jos, BorderLayout.SOUTH);
    }

    private void porneste() {
        int index = comboCeai.getSelectedIndex();
        int secunde = SECUNDE[index] > 0 ? SECUNDE[index] : (Integer) spinnerSecunde.getValue();
        String ceai = index == CEAIURI.length - 1 ? "Ceaiul" : CEAIURI[index].replaceAll(" \\(.*\\)", "");

        final long durata = secunde * 1000L;
        final long sfarsit = System.currentTimeMillis() + durata;

        // Pasul 1: creăm firul de execuție al timerului (daemon = nu blochează închiderea aplicației)
        timer = new Timer("ceainic", true);

        // Pasul 2a: acțiunea principală - se execută O SINGURĂ DATĂ, după interval
        TimerTask alarma = new TimerTask() {
            @Override
            public void run() {
                Toolkit.getDefaultToolkit().beep();
                stare.accept(ceai + " este gata!");
                SwingUtilities.invokeLater(() -> {
                    bara.setValue(1000);
                    bara.setString("100%");
                    ramas.setText(ceai + " este gata!");
                    seteazaButoane(false);
                    JOptionPane.showMessageDialog(CeaiTimer.this,
                            ceai + " este gata! Poftă bună!", "Ceainic",
                            JOptionPane.INFORMATION_MESSAGE);
                });
                opresteTimerul();  // oprește și acțiunea de progres
            }
        };

        // Pasul 2b: acțiune auxiliară - actualizează bara de progres la fiecare 200 ms
        TimerTask progres = new TimerTask() {
            @Override
            public void run() {
                long ramasMs = Math.max(0, sfarsit - System.currentTimeMillis());
                int valoare = (int) (1000 * (durata - ramasMs) / durata);
                long sec = (ramasMs + 999) / 1000;
                String text = String.format("%02d:%02d", sec / 60, sec % 60);
                SwingUtilities.invokeLater(() -> {
                    bara.setValue(valoare);
                    bara.setString(valoare / 10 + "%");
                    ramas.setText("Mai rămân " + text);
                });
            }
        };

        // Pasul 3: planificarea acțiunilor pe același timer
        timer.schedule(alarma, durata);      // după interval, o singură dată
        timer.schedule(progres, 0, 200);     // imediat, apoi la fiecare 200 ms

        seteazaButoane(true);
        stare.accept(ceai + " pus la infuzat pentru " + secunde + " s");
    }

    private void anuleaza() {
        opresteTimerul();
        bara.setValue(0);
        bara.setString("");
        ramas.setText("Infuzarea a fost anulată");
        seteazaButoane(false);
        stare.accept("Ceainic: infuzarea a fost anulată");
    }

    /** Oprire forțată: anulează toate acțiunile timerului și îi oprește firul. */
    private synchronized void opresteTimerul() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void seteazaButoane(boolean ruleaza) {
        butonStart.setEnabled(!ruleaza);
        butonAnuleaza.setEnabled(ruleaza);
        comboCeai.setEnabled(!ruleaza);
    }
}
