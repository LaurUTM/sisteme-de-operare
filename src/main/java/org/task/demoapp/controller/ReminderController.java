package org.task.demoapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderController {

    @FXML
    private DatePicker reminderDatePicker;

    @FXML
    private Spinner<Integer> reminderHourSpinner;

    @FXML
    private Spinner<Integer> reminderMinuteSpinner;

    @FXML
    private TextField reminderCommentField;

    @FXML
    private Label reminderStatusLabel;

    @FXML
    private Label reminderTimeLabel;

    @FXML
    private Button reminderSetButton;

    @FXML
    private Button reminderCancelButton;

    private Timer timer;
    private TimerTask timerTask;

    @FXML
    public void initialize() {

        reminderHourSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, 23, 12
                )
        );

        reminderMinuteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, 59, 0
                )
        );

        reminderDatePicker.setValue(LocalDate.now());

        reminderStatusLabel.setText("Ready");
        reminderTimeLabel.setText("--/--/---- --:--");
    }

    @FXML
    void setReminder() {

        if (timerTask != null) {
            timerTask.cancel();
        }

        if (timer != null) {
            timer.cancel();
        }

        LocalDate date = reminderDatePicker.getValue();

        if (date == null) {
            reminderStatusLabel.setText(
                    "Selectează o dată."
            );
            return;
        }

        int hour = reminderHourSpinner.getValue();
        int minute = reminderMinuteSpinner.getValue();

        String comment = reminderCommentField.getText().trim();

        if (comment.isEmpty()) {
            reminderStatusLabel.setText(
                    "Introdu un comentariu."
            );
            return;
        }

        LocalTime time = LocalTime.of(hour, minute);

        LocalDateTime reminderDateTime =
                LocalDateTime.of(date, time);

        if (!reminderDateTime.isAfter(LocalDateTime.now())) {

            reminderStatusLabel.setText(
                    "Alege o dată și o oră din viitor."
            );

            return;
        }

        Date reminderDate = Date.from(
                reminderDateTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        timer = new Timer();

        timerTask = new TimerTask() {

            @Override
            public void run() {

                Platform.runLater(() -> {

                    Alert alert = new Alert(
                            Alert.AlertType.INFORMATION
                    );

                    alert.setTitle("Reminder");
                    alert.setHeaderText("Reminder declanșat!");
                    alert.setContentText(comment);

                    alert.showAndWait();

                    reminderStatusLabel.setText("Triggered");

                    reminderTimeLabel.setText(
                            "Reminder declanșat"
                    );
                });

                cancel();
            }
        };

        timer.schedule(timerTask, reminderDate);

        reminderStatusLabel.setText("Scheduled");

        reminderTimeLabel.setText(
                String.format(
                        "%02d/%02d/%04d %02d:%02d",
                        date.getDayOfMonth(),
                        date.getMonthValue(),
                        date.getYear(),
                        hour,
                        minute
                )
        );
    }

    @FXML
    void cancelReminder() {

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        reminderStatusLabel.setText("Ready");

        reminderTimeLabel.setText(
                "--/--/---- --:--"
        );

        reminderCommentField.clear();

        reminderDatePicker.setValue(
                LocalDate.now()
        );

        reminderHourSpinner
                .getValueFactory()
                .setValue(12);

        reminderMinuteSpinner
                .getValueFactory()
                .setValue(0);
    }
}