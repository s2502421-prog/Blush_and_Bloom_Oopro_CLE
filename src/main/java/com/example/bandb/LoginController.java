package com.example.bandb;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginErrorLabel;


    private void showLoginError(String message) {

        loginErrorLabel.setText("✕  " + message);
        loginErrorLabel.setVisible(true);
        loginErrorLabel.setManaged(true);

        loginErrorLabel.setOpacity(0);

        FadeTransition fade = new FadeTransition(
                Duration.millis(350), loginErrorLabel
        );

        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition shake = new TranslateTransition(
                Duration.millis(70), loginErrorLabel
        );

        shake.setFromX(-5);
        shake.setToX(5);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);

        fade.play();
        shake.play();
    }


    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Please enter your username and password.");
            return;
        }

        Customer customer = CustomerData.login(username, password);

        if (customer != null) {

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("skintone.fxml")
                );

                Stage stage = (Stage)
                        usernameField.getScene().getWindow();

                Scene scene = new Scene(loader.load());
                SceneManager.switchScene("skintone.fxml");

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Unable to open skin tone selection.");
            }
        } else {
            showLoginError("Invalid username or password.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Blush & Bloom");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    private void handleRegisterPage() {
        try {
            Stage stage = (Stage)
                    usernameField.getScene().getWindow();

            SceneManager.switchScene("register.fxml");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Unable to open registration page.");
        }
    }

}