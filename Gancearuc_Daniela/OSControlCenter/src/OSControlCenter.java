import javax.swing.*;
import java.awt.*;

/**
 * OS Control Center - proiect comun pentru lucrările de laborator la Sisteme de operare.
 *
 * Fereastra principală: un antet și câte o filă pentru fiecare lucrare.
 *   Lab 1 -> TimerPanel        (Timer, TimerTask)
 *   Lab 2 -> EnvironmentPanel  (mașina virtuală, BIOS/UEFI)
 *   Lab 4 -> SchedulerPanel    (algoritmi de planificare) + Scheduler + GanttChart
 *   Lab 5 -> MonitorPanel      (CPU/RAM, procese, comenzi, pornire automată)
 *
 * Compilare și rulare:  vezi README.md (pe scurt: ./run.sh)
 */
public class OSControlCenter {

    // ======= COMPLETEAZĂ CU DATELE TALE =======
    public static final String STUDENT = "Daniela Gancearuc";
    public static final String GROUP   = "Grupa ____";
    // ==========================================

    public static void main(String[] args) {
        // interfața Swing se construiește pe firul ei special (Event Dispatch Thread)
        SwingUtilities.invokeLater(OSControlCenter::createWindow);
    }

    private static void createWindow() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        } catch (Exception ignored) { }
        UIManager.put("defaultFont", Theme.BASE);

        JFrame frame = new JFrame("OS Control Center - Sisteme de operare");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BOLD);
        tabs.addTab("Proiect", overview());
        tabs.addTab("Lab 1 · Timer", new TimerPanel());
        tabs.addTab("Lab 2 · Mediu VM", new EnvironmentPanel());
        tabs.addTab("Lab 3 · Administrare", new AdminPanel());
        MonitorPanel monitor = new MonitorPanel();                 // Lab 5
        SchedulerPanel scheduler = new SchedulerPanel(monitor);   // Lab 4 îi trimite monitorului ce proces rulează
        tabs.addTab("Lab 4 · Planificator", scheduler);
        tabs.addTab("Lab 5 · Monitor sistem", monitor);

        // la închidere oprim procesele pornite de rularea în timp real
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));

        frame.getContentPane().setBackground(Theme.BG);
        frame.add(header(), BorderLayout.NORTH);
        frame.add(tabs, BorderLayout.CENTER);
        frame.setSize(1180, 780);
        frame.setMinimumSize(new Dimension(980, 640));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JComponent header() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Theme.HEADER_BG);
        h.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("OS Control Center");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Proiect comun · Sisteme de operare · " + STUDENT + ", " + GROUP);
        sub.setForeground(new Color(0xC9D3E8));
        sub.setFont(Theme.BASE);
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);
        JLabel os = new JLabel(System.getProperty("os.name") + " · Java " + System.getProperty("java.version"));
        os.setForeground(new Color(0xC9D3E8));
        os.setFont(Theme.BASE);
        h.add(left, BorderLayout.WEST);
        h.add(os, BorderLayout.EAST);
        return h;
    }

    /** Prima filă: ce face proiectul și cum se leagă lucrările. */
    private static JComponent overview() {
        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextArea intro = Theme.note(
            "Un singur program care adună lucrările de laborator. Toate sunt despre același lucru: "
          + "cum gestionează sistemul de operare timpul procesorului, procesele și resursele. "
          + "Programul rulează în mașina virtuală Debian creată la Lab 2.");
        intro.setFont(Theme.BASE.deriveFont(14f));
        intro.setForeground(Theme.TEXT);

        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.add(labCard("Lab 1 · Timer și Git",
            "Planificarea acțiunilor cu Timer și TimerTask: după un interval, la o oră exactă și periodic. "
          + "Același mecanism de Timer pornește și monitorizarea din Lab 5. Codul este pe GitHub, câte o ramură pe lucrare."));
        grid.add(labCard("Lab 2 · Mașina virtuală și BIOS",
            "Programul afișează mediul în care rulează: sistemul de operare, resursele alocate VM-ului (CPU, RAM, disc), "
          + "dacă rulează în VirtualBox și dacă a pornit prin BIOS sau UEFI."));
        grid.add(labCard("Lab 3 · Administrare",
            "Utilizatori, grupuri, politica parolelor, ultimele logări, fișierul de pagină (swap) și pornirea automată. "
          + "Pe Windows: net user, net localgroup, reg. Pe Debian: echivalentele lor (useradd, groupadd, chage)."));
        grid.add(labCard("Lab 4 · Planificarea proceselor",
            "Simulator pentru FCFS, SJF, SRTF, Round Robin, priorități, „cel mai scurt proces urmează” și planificarea echitabilă. "
          + "Desenează diagrama Gantt, calculează timpii medii și poate executa planul în timp real, cu procese reale."));
        grid.add(labCard("Lab 5 · Funcții de nivel înalt",
            "Monitorizează CPU și RAM la fiecare 5 secunde, scrie un jurnal, lansează procese (editor, ping, traceroute), "
          + "adaugă programul la pornirea automată și poate reporni sistemul."));
        grid.add(labCard("Toate împreună",
            "„Rulează în timp real” (Lab 4): planificatorul decide, Timer-ul (Lab 1) execută, procese reale pornesc "
          + "în VM (Lab 2, Lab 5), iar memoria și swap-ul se verifică din Lab 3."));

        JTextArea lab3 = Theme.note("Programul merge și pe Windows, și pe Linux: Lab 3 folosește pe fiecare sistem comenzile lui "
            + "(net user / useradd, registrul / ~/.config/autostart, pagefile.sys / swap).");

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(intro, BorderLayout.NORTH);
        top.add(grid, BorderLayout.CENTER);

        p.add(top, BorderLayout.NORTH);
        JPanel linkBody = new JPanel(new BorderLayout(0, 6));
        linkBody.add(Theme.note("Butonul „Rulează în timp real” din Lab 4 le folosește pe toate odată: planificatorul calculează ordinea, "
            + "un Timer o execută secundă cu secundă, pentru fiecare proces pornește un proces real în Debian, "
            + "iar monitorul arată cum procesorul mașinii virtuale trece de la un proces la altul."), BorderLayout.NORTH);
        linkBody.add(new LinkDiagram(), BorderLayout.CENTER);
        p.add(Theme.card("Cum se leagă lucrările", linkBody), BorderLayout.CENTER);
        p.add(lab3, BorderLayout.SOUTH);
        return p;
    }

    /** O schemă simplă: unde rulează programul și ce module are. */
    private static class LinkDiagram extends JPanel {
        LinkDiagram() { setOpaque(false); setPreferredSize(new Dimension(800, 150)); }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int bw = Math.min(200, (w - 80) / 4), bh = 46;

            // rândul de sus: GitHub -> mașina virtuală -> programul
            int y1 = 10;
            Rectangle git = new Rectangle(20, y1, bw, bh);
            Rectangle vm  = new Rectangle(w / 2 - bw / 2, y1, bw, bh);
            Rectangle app = new Rectangle(w - bw - 20, y1, bw, bh);
            box(g, git, "GitHub (Lab 1)", "codul, câte o ramură", Theme.MUTED);
            box(g, vm,  "VM Debian (Lab 2)", "VirtualBox, BIOS/UEFI", Theme.MUTED);
            box(g, app, "OS Control Center", "programul Java", Theme.HEADER_BG);
            arrow(g, git.x + git.width, y1 + bh / 2, vm.x, y1 + bh / 2, "git clone");
            arrow(g, vm.x + vm.width, y1 + bh / 2, app.x, y1 + bh / 2, "rulează în");

            // rândul de jos: modulele programului
            int y2 = Math.max(y1 + bh + 50, h - bh - 6);
            int gap = (w - 40 - 4 * bw) / 3;
            Rectangle timer = new Rectangle(20, y2, bw, bh);
            Rectangle admin = new Rectangle(20 + bw + gap, y2, bw, bh);
            Rectangle sched = new Rectangle(20 + 2 * (bw + gap), y2, bw, bh);
            Rectangle mon   = new Rectangle(20 + 3 * (bw + gap), y2, bw, bh);
            box(g, timer, "Timer (Lab 1)", "schedule, scheduleAtFixedRate", Theme.ACCENT);
            box(g, admin, "Administrare (Lab 3)", "utilizatori, grupuri, swap", Theme.ACCENT);
            box(g, sched, "Planificator (Lab 4)", "FCFS, SJF, RR, ...", Theme.ACCENT);
            box(g, mon,   "Monitor (Lab 5)", "CPU, RAM, procese", Theme.ACCENT);

            int ax = app.x + app.width / 2, ay = app.y + bh;
            for (Rectangle r : new Rectangle[]{timer, admin, sched, mon}) {
                arrow(g, ax, ay, r.x + r.width / 2, r.y, null);
            }

        }

        private void box(Graphics2D g, Rectangle r, String title, String sub, Color c) {
            g.setColor(c);
            g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(Theme.BOLD);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(title, r.x + (r.width - fm.stringWidth(title)) / 2, r.y + 20);
            g.setFont(Theme.BASE.deriveFont(11f));
            fm = g.getFontMetrics();
            g.drawString(sub, r.x + (r.width - fm.stringWidth(sub)) / 2, r.y + 36);
        }

        private void arrow(Graphics2D g, int x1, int y1, int x2, int y2, String label) {
            g.setColor(Theme.MUTED);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(x1, y1, x2, y2);
            double a = Math.atan2(y2 - y1, x2 - x1);
            int s = 8;
            int[] xs = {x2, (int) (x2 - s * Math.cos(a - 0.4)), (int) (x2 - s * Math.cos(a + 0.4))};
            int[] ys = {y2, (int) (y2 - s * Math.sin(a - 0.4)), (int) (y2 - s * Math.sin(a + 0.4))};
            g.fillPolygon(xs, ys, 3);
            if (label != null) {
                g.setFont(Theme.BASE.deriveFont(11f));
                int lw = g.getFontMetrics().stringWidth(label);
                g.drawString(label, (x1 + x2) / 2 - lw / 2, y1 - 6);
            }
        }
    }

    private static JComponent labCard(String title, String text) {
        JTextArea t = Theme.note(text);
        t.setFont(Theme.BASE.deriveFont(13.5f));
        return Theme.card(title, t);
    }
}
