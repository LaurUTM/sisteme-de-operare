import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudyPage page = new StudyPage();
            page.setVisible(true);
        });
    }
}