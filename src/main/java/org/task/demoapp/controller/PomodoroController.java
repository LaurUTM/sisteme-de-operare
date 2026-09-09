package org.task.demoapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.util.Timer;
import java.util.TimerTask;

public class PomodoroController {
    @FXML
    Label pomodoroTimerLabel;
    @FXML
    Slider workRestSlider;
    @FXML
    Label workSectionsLabel;
    @FXML
    Label restSectionsLabel;
    @FXML
    Label pomodoroStateLabel;

    private Timer timer;

    @FXML
    void setSections() {
        workSectionsLabel.setText(getWorkMinutes() + " minutes");
        restSectionsLabel.setText(getRestMinutes() + " minutes");

        if (timer == null) {
            pomodoroTimerLabel.setText(String.format("%02d:00", getWorkMinutes()));
        }
    }

    @FXML
    void startTimer() {
        startWorkTimer();
    }

    private void startWorkTimer() {
        startSession("Work", getWorkMinutes(), true);
    }

    private void startRestTimer() {
        startSession("Rest", getRestMinutes(), false);
    }

    private void startSession(String sessionName, int startMinutes, boolean workSession) {
        if (timer != null) {
            return;
        }

        pomodoroStateLabel.setText(sessionName);
        Timer currentTimer = new Timer();
        timer = currentTimer;

        TimerTask task = new TimerTask() {
            int remainingSeconds = startMinutes * 60;

            @Override
            public void run() {
                int minutes = remainingSeconds / 60;
                int seconds = remainingSeconds % 60;

                Platform.runLater(() ->
                        pomodoroTimerLabel.setText(
                                String.format("%02d:%02d", minutes, seconds)
                        )
                );

                if (remainingSeconds == 0) {
                    currentTimer.cancel();
                    currentTimer.purge();

                    Platform.runLater(() -> {
                        if (timer == currentTimer) {
                            timer = null;
                            showNextSessionWindow(workSession);
                        }
                    });
                    return;
                }

                remainingSeconds--;
            }
        };
        currentTimer.scheduleAtFixedRate(task, 0, 1000);
    }

    @FXML
    void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        pomodoroStateLabel.setText("Stopped");
        pomodoroTimerLabel.setText(String.format("%02d:00", getWorkMinutes()));
    }

    private void showNextSessionWindow(boolean workSession) {
        String buttonText;
        String message;

        if (workSession) {
            buttonText = "Incepe odihna!!";
            message = "Work time finished";
        } else {
            buttonText = "START LUCRU";
            message = "Rest time finished";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, new ButtonType(buttonText));
        alert.setHeaderText(null);
        alert.setTitle("Pomodoro");
        if (alert.showAndWait().isEmpty()) {
            stopTimer();
            return;
        }

        if (workSession) {
            startRestTimer();
        } else {
            startWorkTimer();
        }
    }

    private int getWorkMinutes() {
        return 60 * (int) workRestSlider.getValue() / 12;
    }

    private int getRestMinutes() {
        return 60 * (12 - (int) workRestSlider.getValue()) / 12;
    }
}
