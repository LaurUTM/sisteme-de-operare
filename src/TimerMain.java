import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class TimerMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame fereastra = new JFrame("Timere - LL1 SO");

                JTabbedPane taburi = new JTabbedPane();
                taburi.addTab("Semafor", new TimerSemaforGUI());
                taburi.addTab("Study Timer", new StudyTimer());
                taburi.addTab("Alarma", new Alarm());
                taburi.addTab("Examen", new TimerExamen());
                // Timerele adăugate de mine (mesajele lor se afișează în consolă)
                taburi.addTab("Ceainic", new CeaiTimer(mesaj -> System.out.println(mesaj)));
                taburi.addTab("Mementouri", new MementoTimer(mesaj -> System.out.println(mesaj)));
                taburi.addTab("Zar automat", new ZarTimer(mesaj -> System.out.println(mesaj)));

                fereastra.add(taburi);
                fereastra.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                fereastra.setSize(700, 500);
                fereastra.setLocationRelativeTo(null); // centreaza fereastra
                fereastra.setVisible(true);
            }
        });
    }
}
