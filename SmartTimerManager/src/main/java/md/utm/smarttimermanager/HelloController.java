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

    // =====================================================
    // TIMER 1 - COUNTDOWN TIMER
    // =====================================================

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


    // =====================================================
    // TIMER 2 - SCHEDULED TASK ALARM
    // =====================================================

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


    // =====================================================
    // TIMER 3 - SMART FOCUS CYCLE
    // =====================================================

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

    private FocusCycleTimer focusCycleTimer;


    // =====================================================
    // TIMER 4 - PERIODIC REMINDER CLOCK
    // =====================================================

    @FXML
    private Label persistentTimeLabel;

    @FXML
    private TextField reminderMinutesField;

    @FXML
    private Label reminderCountdownLabel;

    @FXML
    private Label reminderStatusLabel;

    private PersistentStopwatch persistentStopwatch;


    // =====================================================
    // INITIALIZE
    // =====================================================

    @FXML
    public void initialize() {

        // -------------------------------------------------
        // TIMER 1
        // -------------------------------------------------

        countdownTimer = new CountdownTimer(
                () -> Platform.runLater(this::updateCountdown)
        );


        // -------------------------------------------------
        // TIMER 2
        // -------------------------------------------------

        scheduledAlarmTimer = new ScheduledAlarmTimer(
                () -> Platform.runLater(this::alarmFinished)
        );

        exactActionComboBox.getItems().add("Show message");
        exactActionComboBox.getItems().add("Console message");


        // -------------------------------------------------
        // TIMER 3
        // -------------------------------------------------

        focusCycleTimer = new FocusCycleTimer(

                () -> Platform.runLater(
                        this::updateFocus
                ),

                () -> Platform.runLater(() -> {

                    focusStatusLabel.setText(
                            "● Finished"
                    );

                    focusProgressBar.setProgress(1);
                })
        );


        // -------------------------------------------------
        // TIMER 4
        // -------------------------------------------------

        persistentStopwatch = new PersistentStopwatch(

                // actualizează ora și countdown-ul
                () -> Platform.runLater(
                        this::updatePersistentClock
                ),

                // afișează reminderul
                () -> Platform.runLater(
                        this::showClockReminder
                )
        );


        // Ora reală pornește automat
        persistentStopwatch.start();

        // Afișare inițială
        updatePersistentClock();
    }


    // =====================================================
    // TIMER 1 - START
    // =====================================================

    @FXML
    private void startCountdown() {

        // Dacă era pe pauză, continuă
        if (countdownTimer.isPaused()) {

            countdownTimer.start();

            countdownStatusLabel.setText(
                    "● Running"
            );

            return;
        }


        try {

            int hours =
                    Integer.parseInt(
                            countdownHoursField
                                    .getText()
                                    .trim()
                    );

            int minutes =
                    Integer.parseInt(
                            countdownMinutesField
                                    .getText()
                                    .trim()
                    );

            int seconds =
                    Integer.parseInt(
                            countdownSecondsField
                                    .getText()
                                    .trim()
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


    // =====================================================
    // TIMER 1 - PAUSE
    // =====================================================

    @FXML
    private void pauseCountdown() {

        countdownTimer.pause();

        countdownStatusLabel.setText(
                "● Paused"
        );
    }


    // =====================================================
    // TIMER 1 - CANCEL
    // =====================================================

    @FXML
    private void cancelCountdown() {

        countdownTimer.cancel();

        updateCountdown();

        countdownStatusLabel.setText(
                "● Ready"
        );
    }


    // =====================================================
    // TIMER 1 - UPDATE
    // =====================================================

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
    // TIMER 2 - SCHEDULE
    // =====================================================

    @FXML
    private void scheduleExact() {

        try {

            int hour =
                    Integer.parseInt(
                            exactHourField
                                    .getText()
                                    .trim()
                    );

            int minute =
                    Integer.parseInt(
                            exactMinuteField
                                    .getText()
                                    .trim()
                    );

            int second =
                    Integer.parseInt(
                            exactSecondField
                                    .getText()
                                    .trim()
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


    // =====================================================
    // TIMER 2 - CANCEL
    // =====================================================

    @FXML
    private void cancelExact() {

        scheduledAlarmTimer.cancelAlarm();

        exactStatusLabel.setText(
                "● Cancelled"
        );
    }


    // =====================================================
    // TIMER 2 - ALARM
    // =====================================================

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

            alert.setTitle(
                    "Alarm"
            );

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
    // TIMER 3 - START
    // =====================================================

    @FXML
    private void startFocus() {

        // Dacă este pe pauză, continuă
        if (focusCycleTimer.isPaused()) {

            focusCycleTimer.start();

            focusStatusLabel.setText(
                    "● Running"
            );

            return;
        }


        try {

            int focusMinutes =
                    Integer.parseInt(
                            focusMinutesField
                                    .getText()
                                    .trim()
                    );

            int breakMinutes =
                    Integer.parseInt(
                            breakMinutesField
                                    .getText()
                                    .trim()
                    );

            int cycles =
                    Integer.parseInt(
                            focusCyclesField
                                    .getText()
                                    .trim()
                    );


            if (focusMinutes <= 0 ||
                    breakMinutes <= 0 ||
                    cycles <= 0) {

                focusStatusLabel.setText(
                        "● Invalid value"
                );

                return;
            }


            boolean autoStart =
                    autoStartCheckBox.isSelected();


            focusCycleTimer.setSettings(
                    focusMinutes,
                    breakMinutes,
                    cycles,
                    autoStart
            );


            updateFocus();


            focusCycleTimer.start();


            focusStatusLabel.setText(
                    "● Running"
            );


        } catch (NumberFormatException e) {

            focusStatusLabel.setText(
                    "● Invalid value"
            );
        }
    }


    // =====================================================
    // TIMER 3 - PAUSE
    // =====================================================

    @FXML
    private void pauseFocus() {

        focusCycleTimer.pause();

        focusStatusLabel.setText(
                "● Paused"
        );
    }


    // =====================================================
    // TIMER 3 - SKIP
    // =====================================================

    @FXML
    private void skipFocus() {

        focusCycleTimer.skip();

        updateFocus();
    }


    // =====================================================
    // TIMER 3 - STOP
    // =====================================================

    @FXML
    private void stopFocus() {

        focusCycleTimer.stop();

        updateFocus();

        focusProgressBar.setProgress(0);

        focusStatusLabel.setText(
                "● Ready"
        );
    }


    // =====================================================
    // TIMER 3 - UPDATE
    // =====================================================

    private void updateFocus() {

        int seconds =
                focusCycleTimer.getRemainingSeconds();


        focusTimeLabel.setText(
                FocusCycleTimer.formatTime(seconds)
        );


        focusPhaseLabel.setText(
                focusCycleTimer.getPhaseText()
        );


        focusCycleLabel.setText(
                "Cycle "
                        + focusCycleTimer.getCurrentCycle()
                        + " of "
                        + focusCycleTimer.getTotalCycles()
        );


        updateProgress();
    }


    // =====================================================
    // TIMER 3 - PROGRESS BAR
    // =====================================================

    private void updateProgress() {

        try {

            int totalSeconds;


            if (focusCycleTimer.isFocusPhase()) {

                totalSeconds =
                        Integer.parseInt(
                                focusMinutesField
                                        .getText()
                                        .trim()
                        ) * 60;

            } else {

                totalSeconds =
                        Integer.parseInt(
                                breakMinutesField
                                        .getText()
                                        .trim()
                        ) * 60;
            }


            if (totalSeconds <= 0) {

                focusProgressBar.setProgress(0);

                return;
            }


            int remaining =
                    focusCycleTimer.getRemainingSeconds();


            double progress =
                    1.0 -
                            ((double) remaining
                                    / totalSeconds);


            focusProgressBar.setProgress(
                    progress
            );


        } catch (NumberFormatException e) {

            focusProgressBar.setProgress(0);
        }
    }


    // =====================================================
    // TIMER 4 - START REMINDER
    // =====================================================

    @FXML
    private void startPersistent() {

        try {

            int minutes =
                    Integer.parseInt(
                            reminderMinutesField
                                    .getText()
                                    .trim()
                    );


            if (minutes <= 0) {

                reminderStatusLabel.setText(
                        "● Invalid value"
                );

                return;
            }


            // Setăm intervalul introdus
            persistentStopwatch.setReminderMinutes(
                    minutes
            );


            // Pornim reminderul
            persistentStopwatch.startReminder();


            reminderStatusLabel.setText(
                    "● Running"
            );


            updatePersistentClock();


        } catch (NumberFormatException e) {

            reminderStatusLabel.setText(
                    "● Invalid value"
            );
        }
    }


    // =====================================================
    // TIMER 4 - PAUSE REMINDER
    // =====================================================

    @FXML
    private void pausePersistent() {

        persistentStopwatch.pauseReminder();


        reminderStatusLabel.setText(
                "● Paused"
        );


        updatePersistentClock();
    }


    // =====================================================
    // TIMER 4 - RESET REMINDER
    // =====================================================

    @FXML
    private void resetPersistent() {

        try {

            int minutes =
                    Integer.parseInt(
                            reminderMinutesField
                                    .getText()
                                    .trim()
                    );


            if (minutes <= 0) {

                reminderStatusLabel.setText(
                        "● Invalid value"
                );

                return;
            }


            // Luăm valoarea actuală din TextField
            persistentStopwatch.setReminderMinutes(
                    minutes
            );


            // Resetăm countdown-ul
            persistentStopwatch.resetReminder();


            reminderStatusLabel.setText(
                    "● Ready"
            );


            updatePersistentClock();


        } catch (NumberFormatException e) {

            reminderStatusLabel.setText(
                    "● Invalid value"
            );
        }
    }


    // =====================================================
    // TIMER 4 - UPDATE
    // =====================================================

    private void updatePersistentClock() {

        // Ora reală
        persistentTimeLabel.setText(
                persistentStopwatch.getCurrentTime()
        );


        // Countdown până la reminder
        reminderCountdownLabel.setText(
                persistentStopwatch
                        .getFormattedRemainingTime()
        );
    }


    // =====================================================
    // TIMER 4 - REMINDER
    // =====================================================

    private void showClockReminder() {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "Reminder"
        );


        alert.setHeaderText(
                "Time for a break!"
        );


        alert.setContentText(
                "Your reminder interval has finished."
        );


        alert.show();
    }
}