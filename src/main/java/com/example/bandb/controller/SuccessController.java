package com.example.bandb.controller;

import com.example.bandb.app.SceneManager;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SuccessController {

    @FXML
    private VBox successCard;

    @FXML
    private Label checkIcon;

    @FXML
    public void initialize() {

        successCard.setOpacity(0);
        successCard.setScaleX(0.8);
        successCard.setScaleY(0.8);

        FadeTransition fade = new FadeTransition(
                Duration.millis(800), successCard
        );

        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition zoom = new ScaleTransition(
                Duration.millis(800), successCard
        );

        zoom.setFromX(0.8);
        zoom.setFromY(0.8);
        zoom.setToX(1);
        zoom.setToY(1);

        ParallelTransition animation =
                new ParallelTransition(fade, zoom);

        animation.play();
    }


    @FXML
    private void handleContinue() {
        SceneManager.switchScene("login.fxml");
    }

}
