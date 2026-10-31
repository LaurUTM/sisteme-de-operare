import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/** Culorile și fonturile comune, ca toate filele să arate la fel. */
public class Theme {
    public static final Color BG        = new Color(0xF5F6F8);
    public static final Color CARD      = Color.WHITE;
    public static final Color BORDER    = new Color(0xDADDE3);
    public static final Color TEXT      = new Color(0x1F2430);
    public static final Color MUTED     = new Color(0x5F6675);
    public static final Color ACCENT    = new Color(0x2F6FDB);
    public static final Color HEADER_BG = new Color(0x1F2A44);
    public static final Color OK        = new Color(0x2E8B57);
    public static final Color WARN      = new Color(0xD9822B);

    /** Culori pentru procese în diagrama Gantt (aceeași literă = aceeași culoare). */
    public static final Color[] PALETTE = {
        new Color(0x4C78A8), new Color(0xF58518), new Color(0x54A24B), new Color(0xE45756),
        new Color(0x72B7B2), new Color(0xB279A2), new Color(0x5B5F97), new Color(0x9D755D),
    };

    public static final Font BASE  = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font BOLD  = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    public static final Font MONO  = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    /** Un "card" alb cu titlu, în care punem conținutul unei secțiuni. */
    public static JPanel card(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD);
        Border line = BorderFactory.createLineBorder(BORDER);
        p.setBorder(BorderFactory.createCompoundBorder(line, BorderFactory.createEmptyBorder(10, 12, 12, 12)));
        if (title != null) {
            JLabel t = new JLabel(title);
            t.setFont(TITLE);
            t.setForeground(TEXT);
            p.add(t, BorderLayout.NORTH);
        }
        content.setOpaque(false);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    /** Un text explicativ gri, care se rupe singur pe rânduri. */
    public static JTextArea note(String text) {
        JTextArea a = new JTextArea(text);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setFont(BASE);
        a.setForeground(MUTED);
        a.setBorder(null);
        return a;
    }

    /** O zonă de jurnal (log) cu font monospațiat. */
    public static JTextArea logArea() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setFont(MONO);
        a.setLineWrap(true);
        a.setBackground(new Color(0xFBFBFC));
        a.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return a;
    }

    public static Color colorFor(String processName) {
        return PALETTE[Math.abs(processName.hashCode()) % PALETTE.length];
    }
}
