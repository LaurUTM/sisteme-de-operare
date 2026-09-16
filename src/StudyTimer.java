import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class StudyTimer extends JPanel {

  // Elementele interfetei
  JLabel timeLabel;
  JLabel statusLabel;

  JTextField minutesField;
  JTextField secondsField;

  JButton startButton;
  JButton pauseButton;
  JButton resetButton;

  // Timer-ul
  Timer timer;

  // Timpul ramas in secunde
  int remainingSeconds = 0;


  public StudyTimer() {

    setLayout(new BorderLayout());

    // Titlu
    JLabel titleLabel = new JLabel("STUDY TIMER", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 26));

    // Timp
    timeLabel = new JLabel("00:00", SwingConstants.CENTER);
    timeLabel.setFont(new Font("Arial", Font.BOLD, 60));

    // Status
    statusLabel = new JLabel("Gata de pornire", SwingConstants.CENTER);
    statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));

    // Campurile pentru minute si secunde
    minutesField = new JTextField("1", 4);
    secondsField = new JTextField("00", 4);

    JPanel inputPanel = new JPanel();

    inputPanel.add(new JLabel("Minute:"));
    inputPanel.add(minutesField);

    inputPanel.add(new JLabel("Secunde:"));
    inputPanel.add(secondsField);


    // Butoane
    startButton = new JButton("START");
    pauseButton = new JButton("PAUZA");
    resetButton = new JButton("RESET");

    JPanel buttonPanel = new JPanel();

    buttonPanel.add(startButton);
    buttonPanel.add(pauseButton);
    buttonPanel.add(resetButton);


    // Panel principal
    JPanel panel = new JPanel();

    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    panel.add(Box.createVerticalStrut(20));
    panel.add(titleLabel);

    panel.add(Box.createVerticalStrut(20));
    panel.add(timeLabel);

    panel.add(Box.createVerticalStrut(10));
    panel.add(statusLabel);

    panel.add(Box.createVerticalStrut(20));
    panel.add(inputPanel);

    panel.add(Box.createVerticalStrut(20));
    panel.add(buttonPanel);

    add(panel, BorderLayout.CENTER);


    // Butonul START
    startButton.addActionListener(e -> startTimer());

    // Butonul PAUZA
    pauseButton.addActionListener(e -> pauseTimer());

    // Butonul RESET
    resetButton.addActionListener(e -> resetTimer());
  }


  // Porneste timer-ul
  void startTimer() {

    // Daca timer-ul deja merge, nu mai pornim unul nou
    if (timer != null) {
      return;
    }

    // Daca nu exista timp ramas, luam timpul din campuri
    if (remainingSeconds == 0) {

      try {

        int minutes = Integer.parseInt(minutesField.getText());
        int seconds = Integer.parseInt(secondsField.getText());

        if (minutes < 0 || seconds < 0 || seconds > 59) {
          JOptionPane.showMessageDialog(
                  this,
                  "Introduceti timpuk corect."
          );
          return;
        }

        remainingSeconds = minutes * 60 + seconds;

        if (remainingSeconds == 0) {
          JOptionPane.showMessageDialog(
                  this,
                  "Introdu un timp mai mare decat 0."
          );
          return;
        }

      } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Introdu doar numere."
        );

        return;
      }
    }


    statusLabel.setText("Perechea se desfasoara...");

    // Cream timer-ul
    timer = new Timer();

    // TimerTask se executa la fiecare secunda
    timer.scheduleAtFixedRate(new TimerTask() {

      public void run() {

        remainingSeconds--;

        // Actualizam timpul
        SwingUtilities.invokeLater(() -> updateTime());

        // Verificam daca timpul a ajuns la 0
        if (remainingSeconds <= 0) {

          timer.cancel();
          timer = null;

          SwingUtilities.invokeLater(() -> {

            timeLabel.setText("00:00");

            statusLabel.setText("Perechea s-a terminat!");

            // Sunet
            Toolkit.getDefaultToolkit().beep();

            JOptionPane.showMessageDialog(
                    StudyTimer.this,
                    "Sesiunea de studiu s-a terminat!"
            );
          });
        }
      }

    }, 1000, 1000);
  }


  // Pune timer-ul pe pauza
  void pauseTimer() {

    if (timer != null) {

      timer.cancel();
      timer = null;

      statusLabel.setText("Timer pus pe pauza.");
    }
  }


  // Reseteaza timer-ul
  void resetTimer() {

    if (timer != null) {

      timer.cancel();
      timer = null;
    }

    try {

      int minutes = Integer.parseInt(minutesField.getText());
      int seconds = Integer.parseInt(secondsField.getText());

      if (minutes < 0 || seconds < 0 || seconds > 59) {
        JOptionPane.showMessageDialog(
                this,
                "Introdu un timp corect."
        );
        return;
      }

      remainingSeconds = minutes * 60 + seconds;

      updateTime();

      statusLabel.setText("Gata de pornire.");

    } catch (NumberFormatException e) {

      JOptionPane.showMessageDialog(
              this,
              "Introdu doar numere."
      );
    }
  }


  // Afiseaza timpul in format MM:SS
  void updateTime() {

    int minutes = remainingSeconds / 60;
    int seconds = remainingSeconds % 60;

    timeLabel.setText(
            String.format("%02d:%02d", minutes, seconds)
    );
  }
}

