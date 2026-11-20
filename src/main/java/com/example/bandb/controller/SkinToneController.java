package com.example.bandb.controller;

import com.example.bandb.app.SceneManager;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.Duration;

public class SkinToneController {

    @FXML private ToggleButton fairButton;
    @FXML private ToggleButton lightButton;
    @FXML private ToggleButton mediumButton;
    @FXML private ToggleButton tanButton;
    @FXML private ToggleButton deepButton;
    @FXML private ToggleButton richDeepButton;
    @FXML private Label selectedLabel;

    private ToggleGroup toneGroup;

    private static String selectedSkinTone;

    @FXML
    public void initialize() {

        toneGroup = new ToggleGroup();

        fairButton.setToggleGroup(toneGroup);
        lightButton.setToggleGroup(toneGroup);
        mediumButton.setToggleGroup(toneGroup);
        tanButton.setToggleGroup(toneGroup);
        deepButton.setToggleGroup(toneGroup);
        richDeepButton.setToggleGroup(toneGroup);

        // Clicking the chosen tone again must not leave nothing selected.
        toneGroup.selectedToggleProperty().addListener(
                (observable, oldToggle, newToggle) -> {
                    if (newToggle == null && oldToggle != null) {
                        toneGroup.selectToggle(oldToggle);
                    }
                }
        );
    }

    private void selectTone(
            String tone,
            ToggleButton button) {

        selectedSkinTone = tone;

        selectedLabel.setText("Selected: " + tone);

        ScaleTransition animation =
                new ScaleTransition(
                        Duration.millis(180),
                        button
                );

        animation.setFromX(0.95);
        animation.setFromY(0.95);
        animation.setToX(1);
        animation.setToY(1);

        animation.play();
    }

    @FXML
    private void handleFair() {
        selectTone("Fair", fairButton);
    }

    @FXML
    private void handleLight() {
        selectTone("Light", lightButton);
    }

    @FXML
    private void handleMedium() {
        selectTone("Medium", mediumButton);
    }

    @FXML
    private void handleTan() {
        selectTone("Tan", tanButton);
    }

    @FXML
    private void handleDeep() {
        selectTone("Deep", deepButton);
    }

    @FXML
    private void handleRichDeep() {
        selectTone("Rich Deep", richDeepButton);
    }

    @FXML
    private void handleContinue() {

        if (toneGroup.getSelectedToggle() == null) {
            showAlert("Please select your skin tone.");
            return;
        }

        ToggleButton selectedButton =
                (ToggleButton) toneGroup.getSelectedToggle();

        selectedSkinTone = selectedButton.getText();

        SceneManager.switchScene("home.fxml");
    }

    public static String getSelectedSkinTone() {
        return selectedSkinTone;
    }

    private void showAlert(String message) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle("Blush & Bloom");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
