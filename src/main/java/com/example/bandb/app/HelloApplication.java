
package com.example.bandb.app;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {

        stage.setTitle("Blush & Bloom");
        stage.setMinWidth(950);
        stage.setMinHeight(680);

        SceneManager.setMainStage(stage);

        try {
            // Open customer login screen
            SceneManager.switchScene("login.fxml");

            // Maximize application window
            stage.setMaximized(true);
            stage.show();

        } catch (RuntimeException exception) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Blush & Bloom");
            alert.setHeaderText("Unable to start application");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
