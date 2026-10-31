import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LAB 4 - Diagrama Gantt: o bandă orizontală împărțită în segmente.
 * Lățimea fiecărui segment este proporțională cu timpul cât a rulat procesul.
 */
public class GanttChart extends JPanel {

    private List<Scheduler.Segment> segments = new ArrayList<>();

    public GanttChart() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 118));
    }

    /** Fiecare proces primește o culoare proprie, în ordinea în care apare. */
    private final java.util.Map<String, Color> colors = new java.util.HashMap<>();

    /** În timpul rulării în timp real: momentul curent (-1 = nu rulează). */
    private double progress = -1;

    public void setProgress(double t) {
        progress = t;
        repaint();
    }

    public Color colorOf(String name) {
        return colors.getOrDefault(name, Theme.ACCENT);
    }

    public void show(Scheduler.Result r) {
        segments = r.segments;
        progress = -1;
        colors.clear();
        for (String name : r.burst.keySet()) {
            colors.put(name, Theme.PALETTE[colors.size() % Theme.PALETTE.length]);
        }
        if (!segments.isEmpty()) setToolTipText(r.order());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (segments.isEmpty()) {
            g.setColor(Theme.MUTED);
            g.setFont(Theme.BASE);
            g.drawString("Apasă „Calculează” ca să vezi diagrama Gantt.", 16, 50);
            return;
        }

        int left = 16, right = getWidth() - 16;
        int top = 26, barHeight = 44;
        int total = segments.get(segments.size() - 1).end;
        double scale = (right - left) / (double) total;

        // segmentele
        for (Scheduler.Segment s : segments) {
            int x1 = left + (int) Math.round(s.start * scale);
            int x2 = left + (int) Math.round(s.end * scale);
            Color c = colors.getOrDefault(s.name, Theme.ACCENT);
            if (progress >= 0) {
                // în timp real: partea deja executată e plină, restul e palidă
                int xp = left + (int) Math.round(Math.max(s.start, Math.min(progress, s.end)) * scale);
                g.setColor(fade(c));
                g.fillRect(x1, top, x2 - x1, barHeight);
                g.setColor(c);
                g.fillRect(x1, top, xp - x1, barHeight);
            } else {
                g.setColor(c);
                g.fillRect(x1, top, x2 - x1, barHeight);
            }
            g.setColor(Color.WHITE);
            g.drawLine(x2, top, x2, top + barHeight); // separator între segmente
            // textul e alb pe culoare plină, închis la culoare pe partea încă neexecutată
            Color textColor = (progress >= 0 && progress < (s.start + s.end) / 2.0) ? Theme.TEXT : Color.WHITE;

            // eticheta: B(7) dacă încape, altfel doar B
            g.setFont(Theme.BOLD);
            FontMetrics fm = g.getFontMetrics();
            String label = fm.stringWidth(s.label()) + 6 < x2 - x1 ? s.label() : s.name;
            if (fm.stringWidth(label) + 4 < x2 - x1) {
                g.setColor(textColor);
                g.drawString(label, x1 + (x2 - x1 - fm.stringWidth(label)) / 2, top + barHeight / 2 + fm.getAscent() / 2 - 2);
            }
        }

        // axa timpului: 0 și momentul de sfârșit al fiecărui segment
        g.setFont(Theme.BASE.deriveFont(11f));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Theme.MUTED);
        int axisY = top + barHeight;
        int lastLabelEnd = -100;
        List<Integer> ticks = new ArrayList<>();
        ticks.add(0);
        for (Scheduler.Segment s : segments) ticks.add(s.end);
        for (int t : ticks) {
            int x = left + (int) Math.round(t * scale);
            g.drawLine(x, axisY, x, axisY + 5);
            String txt = String.valueOf(t);
            int tx = x - fm.stringWidth(txt) / 2;
            if (tx > lastLabelEnd + 3) { // nu suprapunem numerele
                g.drawString(txt, tx, axisY + 18);
                lastLabelEnd = tx + fm.stringWidth(txt);
            }
        }

        // linia "acum" în timpul rulării în timp real
        if (progress >= 0) {
            int x = left + (int) Math.round(Math.min(progress, total) * scale);
            g.setColor(Theme.TEXT);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(x, top - 6, x, top + barHeight + 4);
            g.setFont(Theme.BOLD.deriveFont(11f));
            String now = "t = " + (int) progress;
            int nw = g.getFontMetrics().stringWidth(now);
            g.drawString(now, Math.min(Math.max(left, x - nw / 2), right - nw), 14);
        }
    }

    private static Color fade(Color c) {
        return new Color((c.getRed() + 3 * 255) / 4, (c.getGreen() + 3 * 255) / 4, (c.getBlue() + 3 * 255) / 4);
    }
}
