import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * TIMERUL 2 - reacționează LA UN ANUMIT MOMENT de timp (oră exactă).
 *
 * Tema: o listă de mementouri (reminder-e). Utilizatorul alege ora și textul,
 * iar la ora respectivă apare o notificare.
 *
 * Particularități:
 *   - UN SINGUR obiect Timer pentru TOATE mementourile (un singur fir de execuție);
 *   - fiecare memento este o SUBCLASĂ a lui TimerTask (clasa Memento);
 *   - planificarea se face cu schedule(task, Date time);
 *   - un memento poate fi anulat individual cu task.cancel(), apoi timer.purge()
 *     scoate din coada timerului acțiunile anulate.
 */
public class MementoTimer extends JPanel {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");

    // Un singur timer pentru toate mementourile
    private final Timer timer = new Timer("mementouri", true);

    private final JSpinner spinnerOra;
    private final JTextField campText = new JTextField("Pauză de cafea", 18);
    private final DefaultListModel<Memento> model = new DefaultListModel<>();
    private final JList<Memento> lista = new JList<>(model);
    private final Consumer<String> stare;

    /** Un memento = o acțiune planificată (subclasă a lui TimerTask). */
    private class Memento extends TimerTask {
        final Date moment;
        final String text;
        volatile String status = "în așteptare";

        Memento(Date moment, String text) {
            this.moment = moment;
            this.text = text;
        }

        @Override
        public void run() {
            status = "îndeplinit";
            Toolkit.getDefaultToolkit().beep();
            stare.accept("Memento: " + text);
            SwingUtilities.invokeLater(() -> {
                actualizeazaInLista(this);
                JOptionPane.showMessageDialog(MementoTimer.this,
                        "Este ora " + FORMAT.format(moment) + "\n" + text,
                        "Memento", JOptionPane.WARNING_MESSAGE);
            });
        }

        @Override
        public String toString() {
            return FORMAT.format(moment) + "  –  " + text + "   [" + status + "]";
        }
    }

    public MementoTimer(Consumer<String> stare) {
        this.stare = stare;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Spinner pentru oră, cu valoarea implicită = acum + 1 minut
        Calendar implicit = Calendar.getInstance();
        implicit.add(Calendar.MINUTE, 1);
        spinnerOra = new JSpinner(new SpinnerDateModel(implicit.getTime(), null, null, Calendar.SECOND));
        spinnerOra.setEditor(new JSpinner.DateEditor(spinnerOra, "HH:mm:ss"));

        JPanel sus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sus.add(new JLabel("Ora:"));
        sus.add(spinnerOra);
        sus.add(new JLabel("Text:"));
        sus.add(campText);
        JButton adauga = new JButton("Adaugă memento");
        adauga.addActionListener(e -> adauga());
        sus.add(adauga);
        add(sus, BorderLayout.NORTH);

        lista.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createTitledBorder("Mementouri planificate"));
        add(scroll, BorderLayout.CENTER);

        JPanel jos = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton anuleaza = new JButton("Anulează mementoul selectat");
        anuleaza.addActionListener(e -> anuleazaSelectat());
        jos.add(anuleaza);
        add(jos, BorderLayout.SOUTH);
    }

    private void adauga() {
        String text = campText.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Scrie textul mementoului.");
            return;
        }

        // Din spinner luăm doar ora/minutul/secunda și construim momentul pentru azi
        Calendar ales = Calendar.getInstance();
        ales.setTime((Date) spinnerOra.getValue());
        Calendar moment = Calendar.getInstance();
        moment.set(Calendar.HOUR_OF_DAY, ales.get(Calendar.HOUR_OF_DAY));
        moment.set(Calendar.MINUTE, ales.get(Calendar.MINUTE));
        moment.set(Calendar.SECOND, ales.get(Calendar.SECOND));
        moment.set(Calendar.MILLISECOND, 0);

        // Dacă ora a trecut deja azi, mementoul se planifică pentru mâine
        if (moment.getTime().before(new Date())) {
            moment.add(Calendar.DAY_OF_MONTH, 1);
        }

        Memento m = new Memento(moment.getTime(), text);
        timer.schedule(m, m.moment);          // <-- planificare la un moment exact
        model.addElement(m);
        stare.accept("Memento adăugat pentru " + moment.getTime());
    }

    private void anuleazaSelectat() {
        Memento m = lista.getSelectedValue();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Selectează un memento din listă.");
            return;
        }
        // cancel() întoarce true doar dacă acțiunea nu s-a executat încă
        if (m.cancel()) {
            m.status = "anulat";
            int scoase = timer.purge();       // curăță coada timerului
            actualizeazaInLista(m);
            stare.accept("Memento anulat: " + m.text + " (scoase din coadă: " + scoase + ")");
        } else {
            JOptionPane.showMessageDialog(this, "Acest memento a fost deja executat sau anulat.");
        }
    }

    /** Re-desenează rândul din listă după schimbarea stării (se apelează pe EDT). */
    private void actualizeazaInLista(Memento m) {
        int i = model.indexOf(m);
        if (i >= 0) {
            model.set(i, m);
        }
    }
}
