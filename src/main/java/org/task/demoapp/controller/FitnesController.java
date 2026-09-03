package org.task.demoapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.Timer;
import java.util.TimerTask;

public class FitnesController {
    @FXML
    Label fitnessTimerLabel;
    @FXML
    Label lapCountLabel;
    @FXML
    ListView<String> lapsListView;

    private Timer timer;
    private int elapsedMilliseconds = 0;
    private int lapNumber = 0;

    @FXML
    void start() {
        if (timer != null) {
            return;
        }

        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int minutes = elapsedMilliseconds / 60000;
                int seconds = (elapsedMilliseconds % 60000) / 1000;
                int milliseconds = elapsedMilliseconds % 1000;

                Platform.runLater(() ->
                        fitnessTimerLabel.setText(
                                String.format("%02d:%02d:%03d",
                                        minutes,
                                        seconds,
                                        milliseconds)
                        )
                );

                elapsedMilliseconds += 10;
            }
        };

        timer.scheduleAtFixedRate(task, 0, 10);
    }

    @FXML
    void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @FXML
    void lap() {
        int minutes = elapsedMilliseconds / 60000;
        int seconds = (elapsedMilliseconds % 60000) / 1000;
        int milliseconds = elapsedMilliseconds % 1000;

        lapNumber++;
        lapsListView.getItems().add(
                "Lap " + lapNumber + " - " +
                        String.format("%02d:%02d:%03d",
                                minutes,
                                seconds,
                                milliseconds)
        );
        lapCountLabel.setText(lapNumber + " laps");
    }
}
