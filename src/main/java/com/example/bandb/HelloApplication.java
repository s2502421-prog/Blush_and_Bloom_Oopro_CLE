
package com.example.bandb;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        stage.setTitle("Blush & Bloom");

        SceneManager.setMainStage(stage);
        SceneManager.switchScene("login.fxml");

        stage.setMaximized(true);
        stage.show();

        Platform.runLater(() -> {
            stage.setFullScreen(true);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
