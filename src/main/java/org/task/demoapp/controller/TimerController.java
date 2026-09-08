package org.task.demoapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;

import java.util.Timer;
import java.util.TimerTask;

public class TimerController {

    @FXML
    Spinner<Integer> hoursSpinner;

    @FXML
    Spinner<Integer> minutesSpinner;

    @FXML
    Spinner<Integer> secondsSpinner;

    @FXML
    Label timerDisplayLabel;

    @FXML
    Label timerStateLabel;

    @FXML
    ProgressBar timerProgressBar;

    @FXML
    Button timerStartButton;

    @FXML
    Button timerPauseButton;

    @FXML
    Button timerResetButton;

    private int totalSeconds;
    private int remainingSeconds;

    private Timer timer;
    private TimerTask timerTask;

    private boolean isPaused = false;

    @FXML
    void startTimer() {

        if (!isPaused) {

            totalSeconds = (hoursSpinner.getValue() * 3600)
                    + (minutesSpinner.getValue() * 60)
                    + secondsSpinner.getValue();

            if (totalSeconds == 0) {
                timerStateLabel.setText(
                        ""
                );
                return;
            }

            remainingSeconds = totalSeconds;
        }

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {

                if (remainingSeconds <= 0) {

                    timerTask.cancel();
                    timer.cancel();

                    Platform.runLater(() -> {
                        timerDisplayLabel.setText("00:00:00");
                        timerStateLabel.setText("Finished");
                        timerProgressBar.setProgress(1.0);
                    });

                    return;
                }

                int hours = remainingSeconds / 3600;

                int rest = remainingSeconds % 3600;

                int minutes = rest / 60;

                int seconds = rest % 60;

                double progress =
                        (double) (totalSeconds - remainingSeconds)
                                / totalSeconds;

                Platform.runLater(() -> {

                    timerDisplayLabel.setText(
                            String.format(
                                    "%02d:%02d:%02d",
                                    hours,
                                    minutes,
                                    seconds
                            )
                    );

                    timerStateLabel.setText("Running");

                    timerProgressBar.setProgress(progress);
                });

                remainingSeconds--;
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);

        isPaused = false;
    }

    @FXML
    void pauseTimer() {

        if (timerTask == null) {
            return;
        }

        timerTask.cancel();

        if (timer != null) {
            timer.cancel();
        }

        isPaused = true;

        timerStateLabel.setText("Paused");
    }

    @FXML
    void resetTimer() {

        if (timerTask != null) {
            timerTask.cancel();
        }

        if (timer != null) {
            timer.cancel();
        }

        totalSeconds = 0;
        remainingSeconds = 0;

        timerDisplayLabel.setText("00:00:00");

        hoursSpinner.getValueFactory().setValue(0);
        minutesSpinner.getValueFactory().setValue(0);
        secondsSpinner.getValueFactory().setValue(0);

        timerStateLabel.setText("Ready");

        timerProgressBar.setProgress(0.0);

        isPaused = false;
    }
}
