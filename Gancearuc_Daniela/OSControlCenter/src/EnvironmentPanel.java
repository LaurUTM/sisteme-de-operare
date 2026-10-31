import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * LAB 2 - Mașina virtuală și BIOS/UEFI.
 *
 * Fila arată pe ce mediu rulează programul: sistemul de operare, resursele
 * alocate mașinii virtuale (CPU, RAM, disc), dacă suntem într-o mașină
 * virtuală și dacă sistemul a pornit prin BIOS clasic sau prin UEFI.
 */
public class EnvironmentPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Parametru", "Valoare"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public EnvironmentPanel() {
        super(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(Theme.BASE);
        table.getTableHeader().setFont(Theme.BOLD);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(480);

        JButton refresh = new JButton("Reîmprospătează");
        refresh.addActionListener(e -> load());
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Theme.note("Datele sunt citite în timp real din sistem. Rulează programul pe calculatorul tău "
            + "și apoi în mașina virtuală Debian ca să vezi diferența de resurse."), BorderLayout.CENTER);
        top.add(refresh, BorderLayout.EAST);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.add(top, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(Theme.card("Mediul de execuție (Lab 2)", tablePanel), BorderLayout.CENTER);
        add(checklist(), BorderLayout.EAST);
        load();
    }

    private void load() {
        model.setRowCount(0);
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        row("Sistem de operare", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        row("Distribuție Linux", linuxDistro());
        row("Arhitectură", System.getProperty("os.arch"));
        row("Nume calculator (hostname)", hostname());
        row("Utilizator curent", System.getProperty("user.name"));
        row("Mașină virtuală?", virtualization());
        row("Mod de pornire (firmware)", firmware());
        row("Procesoare (nuclee) alocate", String.valueOf(Runtime.getRuntime().availableProcessors()));
        row("Memorie RAM totală", Util.formatMb(totalRamMb(os)));
        row("Memorie swap (memorie virtuală)", Util.formatMb(swapMb(os)));
        File root = Util.isWindows() ? new File("C:\\") : new File("/");
        row("Disc (" + root.getPath() + ") total / liber",
            Util.formatMb(root.getTotalSpace() / (1024 * 1024)) + " / " + Util.formatMb(root.getUsableSpace() / (1024 * 1024)));
        row("Data și ora sistemului", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
            + "  (" + ZoneId.systemDefault() + ")");
        row("Timp de la pornire (uptime)", uptime());
        row("Versiune Java", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
    }

    private void row(String key, String value) {
        model.addRow(new Object[]{key, value == null || value.isEmpty() ? "necunoscut" : value});
    }

    // -------- detectarea mediului --------

    private String linuxDistro() {
        if (!Util.isLinux()) return "-";
        // /etc/os-release conține PRETTY_NAME="Debian GNU/Linux 12 (bookworm)"
        try {
            for (String line : java.nio.file.Files.readAllLines(java.nio.file.Paths.get("/etc/os-release"))) {
                if (line.startsWith("PRETTY_NAME=")) return line.substring(12).replace("\"", "");
            }
        } catch (Exception ignored) { }
        return "";
    }

    private String hostname() {
        try { return InetAddress.getLocalHost().getHostName(); } catch (Exception e) { return ""; }
    }

    /** Ne uităm la producătorul "plăcii de bază". Într-un VM e ceva de genul "VirtualBox". */
    private String virtualization() {
        String product, vendor;
        if (Util.isLinux()) {
            product = Util.readFirstLine("/sys/class/dmi/id/product_name");
            vendor = Util.readFirstLine("/sys/class/dmi/id/sys_vendor");
            if (product.isEmpty() && Util.commandExists("systemd-detect-virt")) {
                String v = Util.runCommand("systemd-detect-virt");
                if (!v.isEmpty() && !v.equals("none")) return "DA - " + v;
            }
        } else if (Util.isWindows()) {
            product = Util.runCommand("powershell", "-NoProfile", "-Command", "(Get-CimInstance Win32_ComputerSystem).Model");
            vendor = Util.runCommand("powershell", "-NoProfile", "-Command", "(Get-CimInstance Win32_ComputerSystem).Manufacturer");
        } else {
            return "";
        }
        String all = (product + " " + vendor).toLowerCase();
        String[] hypervisors = {"virtualbox", "vmware", "kvm", "qemu", "hyper-v", "virtual machine", "xen", "parallels", "bochs"};
        for (String h : hypervisors) {
            if (all.contains(h)) return "DA - " + (product + " / " + vendor).trim();
        }
        return "NU (calculator fizic: " + (product + " / " + vendor).trim() + ")";
    }

    /** UEFI sau BIOS clasic. În Linux există folderul /sys/firmware/efi doar la pornire prin UEFI. */
    private String firmware() {
        if (Util.isLinux()) {
            return new File("/sys/firmware/efi").exists()
                ? "UEFI"
                : "BIOS clasic (Legacy) - în VirtualBox: Settings > System > Enable EFI";
        }
        if (Util.isWindows()) {
            String t = System.getenv("firmware_type");
            return t != null ? t : "vezi msinfo32 > BIOS Mode";
        }
        return "";
    }

    private String uptime() {
        if (Util.isLinux()) {
            String up = Util.readFirstLine("/proc/uptime");
            if (!up.isEmpty()) {
                long sec = (long) Double.parseDouble(up.split(" ")[0]);
                return String.format("%d h %02d min", sec / 3600, (sec % 3600) / 60);
            }
        }
        long ms = ManagementFactory.getRuntimeMXBean().getUptime();
        return "programul rulează de " + ms / 1000 + " s";
    }

    @SuppressWarnings("deprecation")
    private long totalRamMb(OperatingSystemMXBean os) {
        if (Util.isLinux()) { // aceeași valoare ca în comanda free
            try {
                for (String line : java.nio.file.Files.readAllLines(java.nio.file.Paths.get("/proc/meminfo"))) {
                    if (line.startsWith("MemTotal:")) return Long.parseLong(line.replaceAll("\\D+", "")) / 1024;
                }
            } catch (Exception ignored) { }
        }
        return os.getTotalPhysicalMemorySize() / (1024 * 1024);
    }

    @SuppressWarnings("deprecation")
    private long swapMb(OperatingSystemMXBean os) {
        return os.getTotalSwapSpaceSize() / (1024 * 1024);
    }

    // -------- lista de sarcini din Lab 2 (o bifezi în timpul prezentării) --------

    private JComponent checklist() {
        String[] steps = {
            "Am lansat VirtualBox",
            "Am creat mașina virtuală și i-am dat nume",
            "Am ales tipul și versiunea SO (Debian)",
            "Am alocat CPU, RAM și disc",
            "Am montat imaginea ISO",
            "Am configurat BIOS/UEFI (dată, oră, ordine boot)",
            "Am instalat sistemul de operare",
            "Am instalat Java și am rulat Lab 1",
            "Am configurat rețeaua și rezoluția",
            "Am schimbat ordinea de boot",
            "Am dezactivat un dispozitiv (ex. audio)",
            "Am verificat efectul după repornire",
        };
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (String s : steps) {
            JCheckBox cb = new JCheckBox(s);
            cb.setOpaque(false);
            cb.setFont(Theme.BASE);
            list.add(cb);
        }
        JPanel card = Theme.card("Pașii din Lab 2", list);
        card.setPreferredSize(new Dimension(330, 10));
        return card;
    }
}
