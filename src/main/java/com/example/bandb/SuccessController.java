
package com.example.bandb;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

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
        try {
            Stage stage = (Stage)
                    successCard.getScene().getWindow();
            SceneManager.switchScene("login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
