
package com.example.bandb;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginErrorLabel;

    @FXML
    public void initialize() {

        ShoppingCart.logout();

        if (loginErrorLabel != null) {

            loginErrorLabel.setVisible(false);
            loginErrorLabel.setManaged(false);
            loginErrorLabel.setWrapText(true);

            loginErrorLabel.setMinHeight(
                    Region.USE_PREF_SIZE
            );

            loginErrorLabel.setPrefHeight(
                    Region.USE_COMPUTED_SIZE
            );

            loginErrorLabel.setMaxWidth(
                    Double.MAX_VALUE
            );

            loginErrorLabel.setStyle(
                    "-fx-text-fill: #B72F60;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;"
            );
        }

        usernameField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideLoginError()
        );

        passwordField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideLoginError()
        );
    }

    @FXML
    private void handleLogin() {

        String username =
                usernameField.getText().trim();

        String password =
                passwordField.getText();

        if (username.isEmpty() && password.isEmpty()) {

            showLoginError(
                    "Enter username and password."
            );

            return;
        }

        if (username.isEmpty()) {

            showLoginError(
                    "Please enter your username."
            );

            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {

            showLoginError(
                    "Please enter your password."
            );

            passwordField.requestFocus();
            return;
        }

        try {

            Customer customer = CustomerData.login(
                    username,
                    password
            );

            if (customer == null) {

                showLoginError(
                        "Invalid username or password."
                );

                return;
            }

            ShoppingCart.activateAccount(
                    customer.getUsername()
            );

            hideLoginError();

            try {

                SceneManager.switchScene(
                        "skintone.fxml"
                );

            } catch (IOException exception) {

                ShoppingCart.logout();

                showAlert(
                        "Navigation Error",
                        "Unable to open the skin tone page."
                );
            }

        } catch (RuntimeException exception) {

            showAlert(
                    "Login Error",
                    exception.getMessage() == null
                            ? "Unable to load your account."
                            : exception.getMessage()
            );
        }
    }

    private void showLoginError(String message) {

        if (loginErrorLabel == null) {

            showAlert(
                    "Login Error",
                    message
            );

            return;
        }

        loginErrorLabel.setText(message);
        loginErrorLabel.setWrapText(true);

        loginErrorLabel.setMinHeight(
                Region.USE_PREF_SIZE
        );

        loginErrorLabel.setVisible(true);
        loginErrorLabel.setManaged(true);
        loginErrorLabel.setOpacity(0);

        FadeTransition fade = new FadeTransition(
                Duration.millis(250),
                loginErrorLabel
        );

        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        TranslateTransition shake =
                new TranslateTransition(
                        Duration.millis(65),
                        loginErrorLabel
                );

        shake.setFromX(0);
        shake.setToX(5);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);

        shake.setOnFinished(
                event -> loginErrorLabel.setTranslateX(0)
        );

        shake.play();
    }

    private void hideLoginError() {

        if (loginErrorLabel == null) {
            return;
        }

        loginErrorLabel.setVisible(false);
        loginErrorLabel.setManaged(false);
        loginErrorLabel.setOpacity(1);
        loginErrorLabel.setTranslateX(0);
    }

    @FXML
    private void handleRegisterPage() {

        try {

            SceneManager.switchScene(
                    "register.fxml"
            );

        } catch (IOException exception) {

            showAlert(
                    "Navigation Error",
                    "Unable to open the registration page."
            );
        }
    }

    private void showAlert(
            String title,
            String message
    ) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        if (usernameField != null
                && usernameField.getScene() != null) {

            alert.initOwner(
                    usernameField.getScene().getWindow()
            );
        }

        alert.setTitle("Blush & Bloom");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
