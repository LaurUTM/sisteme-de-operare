package md.utm.smarttimermanager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import md.utm.smarttimermanager.timer.CountdownTimer;
import md.utm.smarttimermanager.timer.FocusCycleTimer;
import md.utm.smarttimermanager.timer.PersistentStopwatch;
import md.utm.smarttimermanager.timer.ScheduledAlarmTimer;

public class HelloController {

    // TIMER 1

    @FXML
    private TextField countdownHoursField;

    @FXML
    private TextField countdownMinutesField;

    @FXML
    private TextField countdownSecondsField;

    @FXML
    private Label countdownTimeLabel;

    @FXML
    private Label countdownStatusLabel;

    private CountdownTimer countdownTimer;


    // TIMER 2

    @FXML
    private TextField exactHourField;

    @FXML
    private TextField exactMinuteField;

    @FXML
    private TextField exactSecondField;

    @FXML
    private TextField exactTaskField;

    @FXML
    private ComboBox<String> exactActionComboBox;

    @FXML
    private Label exactTimeLabel;

    @FXML
    private Label exactStatusLabel;

    private ScheduledAlarmTimer scheduledAlarmTimer;


    // TIMER 3 - AL TAU

    @FXML
    private TextField focusMinutesField;

    @FXML
    private TextField breakMinutesField;

    @FXML
    private TextField focusCyclesField;

    @FXML
    private CheckBox autoStartCheckBox;

    @FXML
    private Label focusTimeLabel;

    @FXML
    private Label focusPhaseLabel;

    @FXML
    private Label focusCycleLabel;

    @FXML
    private Label focusStatusLabel;

    @FXML
    private ProgressBar focusProgressBar;

    private FocusCycleTimer timerFocus;


    // TIMER 4 - AL TAU

    @FXML
    private Label persistentTimeLabel;

    @FXML
    private TextField reminderMinutesField;

    @FXML
    private Label reminderCountdownLabel;

    @FXML
    private Label reminderStatusLabel;

    private PersistentStopwatch timerReminder;


    @FXML
    public void initialize() {

        // TIMER 1

        countdownTimer = new CountdownTimer(
                () -> Platform.runLater(this::updateCountdown)
        );


        // TIMER 2

        scheduledAlarmTimer = new ScheduledAlarmTimer(
                () -> Platform.runLater(this::alarmFinished)
        );

        exactActionComboBox.getItems().add("Show message");
        exactActionComboBox.getItems().add("Console message");


        // TIMER 3

        timerFocus = new FocusCycleTimer(

                () -> Platform.runLater(
                        this::actualizeazaFocus
                ),

                () -> Platform.runLater(() -> {

                    focusStatusLabel.setText("● Finished");

                    focusProgressBar.setProgress(1);
                })
        );


        // TIMER 4

        timerReminder = new PersistentStopwatch(

                () -> Platform.runLater(
                        this::actualizeazaCeas
                ),

                () -> Platform.runLater(
                        this::afiseazaReminder
                )
        );

        timerReminder.start();
    }


    // =====================================================
    // TIMER 1
    // =====================================================

    @FXML
    private void startCountdown() {

        if (countdownTimer.isPaused()) {

            countdownTimer.start();

            countdownStatusLabel.setText("● Running");

            return;
        }

        try {

            int hours =
                    Integer.parseInt(
                            countdownHoursField.getText().trim()
                    );

            int minutes =
                    Integer.parseInt(
                            countdownMinutesField.getText().trim()
                    );

            int seconds =
                    Integer.parseInt(
                            countdownSecondsField.getText().trim()
                    );


            if (hours < 0 ||
                    minutes < 0 ||
                    seconds < 0) {

                countdownStatusLabel.setText(
                        "● Invalid time"
                );

                return;
            }


            countdownTimer.setTime(
                    hours,
                    minutes,
                    seconds
            );

            countdownTimer.start();

            countdownStatusLabel.setText(
                    "● Running"
            );

        } catch (NumberFormatException e) {

            countdownStatusLabel.setText(
                    "● Invalid time"
            );
        }
    }


    @FXML
    private void pauseCountdown() {

        countdownTimer.pause();

        countdownStatusLabel.setText(
                "● Paused"
        );
    }


    @FXML
    private void cancelCountdown() {

        countdownTimer.cancel();

        updateCountdown();

        countdownStatusLabel.setText(
                "● Ready"
        );
    }


    private void updateCountdown() {

        int seconds =
                countdownTimer.getRemainingSeconds();

        countdownTimeLabel.setText(
                CountdownTimer.formatTime(seconds)
        );

        if (seconds <= 0 &&
                !countdownTimer.isRunning()) {

            countdownStatusLabel.setText(
                    "● Finished"
            );
        }
    }


    // =====================================================
    // TIMER 2
    // =====================================================

    @FXML
    private void scheduleExact() {

        try {

            int hour =
                    Integer.parseInt(
                            exactHourField.getText().trim()
                    );

            int minute =
                    Integer.parseInt(
                            exactMinuteField.getText().trim()
                    );

            int second =
                    Integer.parseInt(
                            exactSecondField.getText().trim()
                    );


            if (hour < 0 || hour > 23 ||
                    minute < 0 || minute > 59 ||
                    second < 0 || second > 59) {

                exactStatusLabel.setText(
                        "● Invalid time"
                );

                return;
            }


            scheduledAlarmTimer.setTime(
                    hour,
                    minute,
                    second
            );


            exactTimeLabel.setText(
                    String.format(
                            "%02d:%02d:%02d",
                            hour,
                            minute,
                            second
                    )
            );


            scheduledAlarmTimer.start();

            exactStatusLabel.setText(
                    "● Waiting"
            );

        } catch (NumberFormatException e) {

            exactStatusLabel.setText(
                    "● Invalid time"
            );
        }
    }


    @FXML
    private void cancelExact() {

        scheduledAlarmTimer.cancelAlarm();

        exactStatusLabel.setText(
                "● Cancelled"
        );
    }


    private void alarmFinished() {

        exactStatusLabel.setText(
                "● Finished"
        );


        String task =
                exactTaskField.getText();

        String action =
                exactActionComboBox.getValue();


        if ("Show message".equals(action)) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle("Alarm");

            alert.setHeaderText(
                    "Time is up!"
            );

            alert.setContentText(
                    task
            );

            alert.show();
        }


        if ("Console message".equals(action)) {

            System.out.println(
                    "Task: " + task
            );
        }
    }


    // =====================================================
    // TIMER 3 - FOCUS CYCLE
    // =====================================================

    @FXML
    private void startFocus() {

        if (timerFocus.isPaused()) {

            timerFocus.start();

            focusStatusLabel.setText(
                    "● Running"
            );

            return;
        }


        try {

            int minuteFocus =
                    Integer.parseInt(
                            focusMinutesField.getText().trim()
                    );

            int minutePauza =
                    Integer.parseInt(
                            breakMinutesField.getText().trim()
                    );

            int cicluri =
                    Integer.parseInt(
                            focusCyclesField.getText().trim()
                    );


            if (minuteFocus <= 0 ||
                    minutePauza <= 0 ||
                    cicluri <= 0) {

                focusStatusLabel.setText(
                        "● Invalid value"
                );

                return;
            }


            boolean pornireAutomata =
                    autoStartCheckBox.isSelected();


            timerFocus.setSettings(
                    minuteFocus,
                    minutePauza,
                    cicluri,
                    pornireAutomata
            );


            timerFocus.start();


            focusStatusLabel.setText(
                    "● Running"
            );


            actualizeazaFocus();


        } catch (NumberFormatException e) {

            focusStatusLabel.setText(
                    "● Invalid value"
            );
        }
    }


    @FXML
    private void pauseFocus() {

        timerFocus.pause();

        focusStatusLabel.setText(
                "● Paused"
        );
    }


    @FXML
    private void skipFocus() {

        timerFocus.skip();

        actualizeazaFocus();
    }


    @FXML
    private void stopFocus() {

        timerFocus.stop();

        focusStatusLabel.setText(
                "● Ready"
        );

        focusProgressBar.setProgress(0);

        actualizeazaFocus();
    }


    private void actualizeazaFocus() {

        int secunde =
                timerFocus.getRemainingSeconds();


        focusTimeLabel.setText(
                FocusCycleTimer.formatTime(secunde)
        );


        focusPhaseLabel.setText(
                timerFocus.getPhaseText()
        );


        focusCycleLabel.setText(
                "Cycle "
                        + timerFocus.getCurrentCycle()
                        + " of "
                        + timerFocus.getTotalCycles()
        );


        actualizeazaProgres();
    }


    private void actualizeazaProgres() {

        try {

            int totalSecunde;


            if (timerFocus.isFocusPhase()) {

                totalSecunde =
                        Integer.parseInt(
                                focusMinutesField.getText().trim()
                        ) * 60;

            } else {

                totalSecunde =
                        Integer.parseInt(
                                breakMinutesField.getText().trim()
                        ) * 60;
            }


            if (totalSecunde <= 0) {

                focusProgressBar.setProgress(0);

                return;
            }


            int secundeRamase =
                    timerFocus.getRemainingSeconds();


            double progres =
                    1.0 -
                            ((double) secundeRamase
                                    / totalSecunde);


            focusProgressBar.setProgress(
                    progres
            );


        } catch (NumberFormatException e) {

            focusProgressBar.setProgress(0);
        }
    }


    // =====================================================
    // TIMER 4 - REMINDER
    // =====================================================

    @FXML
    private void startPersistent() {

        if ("● Paused".equals(
                reminderStatusLabel.getText()
        )) {

            timerReminder.startReminder();

            reminderStatusLabel.setText(
                    "● Running"
            );

            return;
        }


        try {

            int minute =
                    Integer.parseInt(
                            reminderMinutesField.getText().trim()
                    );


            if (minute <= 0) {

                reminderStatusLabel.setText(
                        "● Invalid value"
                );

                return;
            }


            timerReminder.setReminderMinutes(
                    minute
            );


            timerReminder.startReminder();


            reminderStatusLabel.setText(
                    "● Running"
            );


            actualizeazaCeas();


        } catch (NumberFormatException e) {

            reminderStatusLabel.setText(
                    "● Invalid value"
            );
        }
    }


    @FXML
    private void pausePersistent() {

        timerReminder.pauseReminder();

        reminderStatusLabel.setText(
                "● Paused"
        );
    }


    @FXML
    private void resetPersistent() {

        try {

            int minute =
                    Integer.parseInt(
                            reminderMinutesField.getText().trim()
                    );


            if (minute <= 0) {

                reminderStatusLabel.setText(
                        "● Invalid value"
                );

                return;
            }


            timerReminder.setReminderMinutes(
                    minute
            );


            timerReminder.resetReminder();


            reminderStatusLabel.setText(
                    "● Ready"
            );


            actualizeazaCeas();


        } catch (NumberFormatException e) {

            reminderStatusLabel.setText(
                    "● Invalid value"
            );
        }
    }


    private void actualizeazaCeas() {

        persistentTimeLabel.setText(
                timerReminder.getCurrentTime()
        );


        reminderCountdownLabel.setText(
                timerReminder.getFormattedRemainingTime()
        );
    }


    private void afiseazaReminder() {

        Alert alerta =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alerta.setTitle(
                "Reminder"
        );


        alerta.setHeaderText(
                "Time for a break!"
        );


        alerta.setContentText(
                "Your reminder interval has finished."
        );


        alerta.show();
    }
}