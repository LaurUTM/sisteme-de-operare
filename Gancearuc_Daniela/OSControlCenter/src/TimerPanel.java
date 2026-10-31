import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * LAB 1 - Planificarea activității cu Timer și TimerTask.
 *
 * Cele 3 cerințe din lucrare:
 *   1. să reacționeze după un anumit interval de timp  -> schedule(task, delay)
 *   2. să reacționeze la un anumit moment (oră exactă)  -> schedule(task, Date)
 *   3. să reacționeze periodic                          -> schedule / scheduleAtFixedRate(task, delay, period)
 */
public class TimerPanel extends JPanel {

    private final JTextArea log = Theme.logArea();
    private final List<Timer> activeTimers = new ArrayList<>(); // ca să le putem opri pe toate
    private Timer periodicTimer;                                // timerul de la punctul 3
    private int periodicCount = 0;

    public TimerPanel() {
        super(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel left = new FitWidthPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(delayCard());
        left.add(Box.createVerticalStrut(10));
        left.add(exactTimeCard());
        left.add(Box.createVerticalStrut(10));
        left.add(periodicCard());
        left.add(Box.createVerticalGlue());

        JButton stopAll = new JButton("Oprește toate timerele (cancel)");
        stopAll.addActionListener(e -> stopAll());
        JButton clear = new JButton("Golește jurnalul");
        clear.addActionListener(e -> log.setText(""));
        JPanel logButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logButtons.setOpaque(false);
        logButtons.add(stopAll);
        logButtons.add(clear);

        JPanel logPanel = new JPanel(new BorderLayout(0, 8));
        logPanel.add(new JScrollPane(log), BorderLayout.CENTER);
        logPanel.add(logButtons, BorderLayout.SOUTH);

        JScrollPane leftScroll = new JScrollPane(left);
        leftScroll.setBorder(null);
        leftScroll.getViewport().setBackground(Theme.BG);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftScroll.setPreferredSize(new Dimension(430, 10));

        add(leftScroll, BorderLayout.WEST);
        add(Theme.card("Jurnalul timerelor", logPanel), BorderLayout.CENTER);

        write("Fiecare timer este un fir de execuție separat (new Timer()).");
    }

    // ---------------- 1. După un interval ----------------
    private JComponent delayCard() {
        JSpinner seconds = new JSpinner(new SpinnerNumberModel(5, 1, 3600, 1));
        JTextField message = new JTextField("Au trecut câteva secunde!", 16);
        JButton start = new JButton("Pornește");

        start.addActionListener(e -> {
            int s = (Integer) seconds.getValue();
            String msg = message.getText();
            Timer t = newTimer();
            // schedule(task, delay): rulează O SINGURĂ DATĂ după "delay" milisecunde
            t.schedule(new TimerTask() {
                @Override public void run() {
                    Toolkit.getDefaultToolkit().beep();
                    write("[interval] " + msg + "  (după " + s + " s)");
                    t.cancel(); // și-a făcut treaba, oprim firul
                }
            }, s * 1000L);
            write("Timer 1 pornit: va reacționa peste " + s + " secunde.");
        });

        JPanel form = form(
            new String[]{"Peste (secunde):", "Mesaj:"},
            new JComponent[]{seconds, message});
        return section("1. Reacție după un interval",
            "Metoda schedule(task, delay) - acțiunea rulează o singură dată, după întârziere.",
            form, start);
    }

    // ---------------- 2. La o oră exactă ----------------
    private JComponent exactTimeCard() {
        Calendar inOneMinute = Calendar.getInstance();
        inOneMinute.add(Calendar.MINUTE, 1);
        inOneMinute.set(Calendar.SECOND, 0);

        JSpinner time = new JSpinner(new SpinnerDateModel(inOneMinute.getTime(), null, null, Calendar.MINUTE));
        time.setEditor(new JSpinner.DateEditor(time, "HH:mm:ss"));
        JTextField message = new JTextField("Ora mesei!", 16);
        JButton start = new JButton("Programează");

        start.addActionListener(e -> {
            // luăm ora/minutul/secunda alese și le punem pe data de AZI
            Calendar chosen = Calendar.getInstance();
            chosen.setTime((Date) time.getValue());
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, chosen.get(Calendar.HOUR_OF_DAY));
            target.set(Calendar.MINUTE, chosen.get(Calendar.MINUTE));
            target.set(Calendar.SECOND, chosen.get(Calendar.SECOND));
            target.set(Calendar.MILLISECOND, 0);
            if (target.getTimeInMillis() < System.currentTimeMillis()) {
                target.add(Calendar.DAY_OF_MONTH, 1); // ora a trecut -> mâine
            }
            Date when = target.getTime();
            String msg = message.getText();

            Timer t = newTimer();
            // schedule(task, Date): rulează o singură dată, la momentul exact
            t.schedule(new TimerTask() {
                @Override public void run() {
                    Toolkit.getDefaultToolkit().beep();
                    write("[ora exactă] " + msg);
                    t.cancel();
                }
            }, when);
            write("Timer 2 programat pentru " + new java.text.SimpleDateFormat("dd.MM HH:mm:ss").format(when));
        });

        JPanel form = form(
            new String[]{"La ora:", "Mesaj:"},
            new JComponent[]{time, message});
        return section("2. Reacție la un anumit moment",
            "Metoda schedule(task, Date) - folosim Calendar ca să construim momentul exact.",
            form, start);
    }

    // ---------------- 3. Periodic ----------------
    private JComponent periodicCard() {
        JSpinner period = new JSpinner(new SpinnerNumberModel(2, 1, 3600, 1));
        JComboBox<String> method = new JComboBox<>(new String[]{"scheduleAtFixedRate", "schedule"});
        JCheckBox beep = new JCheckBox("cu sunet (beep)", true);
        JSpinner stopAfter = new JSpinner(new SpinnerNumberModel(10, 0, 3600, 1));
        JButton start = new JButton("Pornește");
        JButton stop = new JButton("Oprește");

        start.addActionListener(e -> {
            if (periodicTimer != null) periodicTimer.cancel();
            int p = (Integer) period.getValue();
            int limit = (Integer) stopAfter.getValue();
            boolean fixedRate = method.getSelectedIndex() == 0;
            periodicCount = 0;

            periodicTimer = newTimer();
            TimerTask task = new TimerTask() {
                @Override public void run() {
                    periodicCount++;
                    if (beep.isSelected()) Toolkit.getDefaultToolkit().beep();
                    write("[periodic] execuția nr. " + periodicCount);
                }
            };
            if (fixedRate) periodicTimer.scheduleAtFixedRate(task, 0, p * 1000L);
            else periodicTimer.schedule(task, 0, p * 1000L);
            write("Timer 3 pornit: la fiecare " + p + " s, metoda " + (fixedRate ? "scheduleAtFixedRate" : "schedule"));

            // un al doilea timer care îl oprește pe primul (ca în exemplul din îndrumar)
            if (limit > 0) {
                Timer stopper = newTimer();
                final Timer target = periodicTimer;
                stopper.schedule(new TimerTask() {
                    @Override public void run() {
                        target.cancel();
                        write("Au trecut " + limit + " secunde -> timerul periodic a fost oprit cu cancel().");
                        stopper.cancel();
                    }
                }, limit * 1000L);
            }
        });

        stop.addActionListener(e -> {
            if (periodicTimer != null) {
                periodicTimer.cancel();
                write("Timer 3 oprit manual (cancel). După cancel() nu mai poate fi refolosit.");
                periodicTimer = null;
            }
        });

        JPanel form = form(
            new String[]{"Perioada (secunde):", "Metoda:", "Oprește după (s, 0 = niciodată):", ""},
            new JComponent[]{period, method, stopAfter, beep});
        return section("3. Reacție cu o perioadă indicată",
            "schedule() = întârziere fixă: dacă o execuție întârzie, le împinge și pe următoarele. "
          + "scheduleAtFixedRate() = rată fixă: recuperează întârzierea, ca numărul de execuții să rămână același.",
            form, start, stop);
    }

    // ---------------- ajutătoare ----------------

    private Timer newTimer() {
        Timer t = new Timer(true); // daemon: se oprește odată cu programul
        activeTimers.add(t);
        return t;
    }

    private void stopAll() {
        for (Timer t : activeTimers) t.cancel();
        activeTimers.clear();
        periodicTimer = null;
        write("Toate timerele au fost oprite.");
    }

    /** Timerele rulează pe alt fir, așa că scriem în interfață prin invokeLater. */
    private void write(String text) {
        SwingUtilities.invokeLater(() -> {
            log.append(Util.now() + "  " + text + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    private JPanel form(String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            p.add(new JLabel(labels[i]), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            p.add(fields[i], c);
        }
        return p;
    }

    /** Panou care se potrivește mereu pe lățimea ferestrei (fără bară orizontală). */
    private static class FitWidthPanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 120; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private JComponent section(String title, String explanation, JPanel form, JButton... buttons) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JTextArea note = Theme.note(explanation);
        note.setAlignmentX(LEFT_ALIGNMENT);
        form.setAlignmentX(LEFT_ALIGNMENT);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        btns.setAlignmentX(LEFT_ALIGNMENT);
        for (JButton b : buttons) btns.add(b);
        body.add(note);
        body.add(Box.createVerticalStrut(8));
        body.add(form);
        body.add(Box.createVerticalStrut(8));
        body.add(btns);
        JPanel card = Theme.card(title, body);
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }
}
