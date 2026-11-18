
package com.example.bandb;

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

import java.io.IOException;

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

            if (CustomerData.usernameExists(username)) {

                showAlert(
                        "Account Already Exists",
                        "This username is already registered. Please log in."
                );
                return;
            }

            if (CustomerData.emailExists(email)) {

                showAlert(
                        "Email Already Registered",
                        "An account with this email already exists. Please log in."
                );
                return;
            }

            Customer customer = new Customer(
                    fullName,
                    username,
                    email,
                    password
            );

            boolean registered = CustomerData.register(customer);

            if (!registered) {

                showAlert(
                        "Account Already Exists",
                        "This username or email is already registered. Please log in."
                );
                return;
            }

            try {

                SceneManager.switchScene("success.fxml");

            } catch (IOException exception) {

                showAlert(
                        "Account Saved",
                        "Your account was saved successfully, but the success page could not open. Please return to Login."
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
                    "Your account could not be saved. " +
                            "Please try again. " +
                            exception.getMessage()
            );
        }
    }

    @FXML
    private void handleBack() {

        try {

            SceneManager.switchScene("login.fxml");

        } catch (IOException exception) {

            showAlert(
                    "Navigation Error",
                    "Unable to return to the login page."
            );
        }
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

        Label heading = new Label(title);

        heading.setWrapText(true);
        heading.setMaxWidth(Double.MAX_VALUE);

        heading.setStyle(
                "-fx-text-fill: #A65378;" +
                        "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;"
        );

        Label description = new Label(message);

        description.setWrapText(true);
        description.setMaxWidth(350);
        description.setMinHeight(Region.USE_PREF_SIZE);

        description.setStyle(
                "-fx-text-fill: #825B6D;" +
                        "-fx-font-size: 13px;"
        );

        javafx.scene.layout.VBox content =
                new javafx.scene.layout.VBox(
                        12,
                        heading,
                        description
                );

        content.setPadding(
                new javafx.geometry.Insets(16, 12, 16, 12)
        );

        content.setStyle(
                "-fx-background-color: #FFF8FB;"
        );

        pane.setContent(content);

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
