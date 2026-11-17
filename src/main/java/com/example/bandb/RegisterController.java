package com.example.bandb;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister() {

        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (fullName.isEmpty() || username.isEmpty()
                || email.isEmpty() || password.isEmpty()
                || confirm.isEmpty()) {

            showAlert("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            showAlert("Passwords do not match.");
            return;
        }

        Customer customer = new Customer(
                fullName,
                username,
                email,
                password
        );

        boolean registered = CustomerData.register(customer);


        if (registered) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("success.fxml")
                );

                Stage stage = (Stage) fullNameField.getScene().getWindow();
                SceneManager.switchScene("success.fxml");

                Scene scene = new Scene(loader.load());

                stage.setScene(scene);
                stage.setMaximized(true);

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Unable to open success page.");
            }
        } else {
            showAlert("Username already exists. Try another.");
        }

    }



    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage)
                    fullNameField.getScene().getWindow();

            SceneManager.switchScene("login.fxml");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Blush & Bloom");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}