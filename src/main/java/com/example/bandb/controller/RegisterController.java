package com.example.bandb.controller;

import com.example.bandb.app.SceneManager;
import com.example.bandb.model.Customer;
import com.example.bandb.service.CustomerData;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Modality;

public class RegisterController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister() {

        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (fullName.isEmpty()
                || username.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirm.isEmpty()) {

            showAlert(
                    "Incomplete Information",
                    "Please fill in all fields."
            );
            return;
        }

        if (!username.matches("[A-Za-z0-9_]{3,30}")) {

            showAlert(
                    "Invalid Username",
                    "Username must contain 3 to 30 letters, numbers, or underscores."
            );
            return;
        }

        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {

            showAlert(
                    "Invalid Email",
                    "Please enter a valid email address."
            );
            return;
        }

        if (password.length() < 6) {

            showAlert(
                    "Invalid Password",
                    "Password must contain at least 6 characters."
            );
            return;
        }

        if (!password.equals(confirm)) {

            showAlert(
                    "Password Mismatch",
                    "Passwords do not match."
            );
            return;
        }

        try {

            Customer customer = new Customer(
                    fullName,
                    username,
                    email
            );

            boolean registered = CustomerData.register(
                    customer,
                    password
            );

            if (!registered) {

                showAlert(
                        "Account Already Exists",
                        "This username or email is already registered. Please log in."
                );
                return;
            }

            if (!SceneManager.switchScene("success.fxml")) {

                showAlert(
                        "Account Saved",
                        "Your account was saved successfully. Please return to Login."
                );
            }

        } catch (IllegalArgumentException exception) {

            showAlert(
                    "Registration Error",
                    exception.getMessage()
            );

        } catch (RuntimeException exception) {

            showAlert(
                    "Unable to Register",
                    "Your account could not be saved. Please try again."
            );
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("login.fxml");
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.NONE);

        if (fullNameField != null
                && fullNameField.getScene() != null) {

            alert.initOwner(
                    fullNameField.getScene().getWindow()
            );
        }

        alert.initModality(Modality.WINDOW_MODAL);

        alert.setTitle("Blush & Bloom");
        alert.setHeaderText(null);
        alert.setGraphic(null);

        ButtonType okayType = new ButtonType(
                "Okay",
                ButtonBar.ButtonData.OK_DONE
        );

        alert.getButtonTypes().setAll(okayType);

        DialogPane pane = alert.getDialogPane();

        pane.setStyle(
                "-fx-background-color: #FFF8FB;" +
                        "-fx-border-color: #F2D4E1;" +
                        "-fx-border-width: 1;"
        );

        pane.setPrefWidth(420);
        pane.setMinWidth(360);

        try {
            javafx.fxml.FXMLLoader messageLoader = new javafx.fxml.FXMLLoader(
                    com.example.bandb.app.SceneManager.resource("register-message.fxml"));
            javafx.scene.layout.VBox content = messageLoader.load();
            ((Label) messageLoader.getNamespace().get("messageTitle")).setText(title);
            ((Label) messageLoader.getNamespace().get("messageDescription")).setText(message);
            pane.setContent(content);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Unable to load register-message.fxml", ex);
        }

        Button okayButton =
                (Button) pane.lookupButton(okayType);

        okayButton.setDefaultButton(true);

        okayButton.setStyle(
                "-fx-background-color: #D97DA5;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 28;" +
                        "-fx-cursor: hand;"
        );

        alert.setResizable(false);
        alert.showAndWait();
    }
}
