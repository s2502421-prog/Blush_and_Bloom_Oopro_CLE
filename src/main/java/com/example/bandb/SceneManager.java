
package com.example.bandb;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void setStage(Stage stage) {
        setMainStage(stage);
    }

    public static void switchScene(String fxmlFile)
            throws IOException {

        if (mainStage == null) {
            throw new IllegalStateException(
                    "Main stage has not been initialized."
            );
        }

        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource(fxmlFile)
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        mainStage.setScene(scene);
        mainStage.setMaximized(true);
        mainStage.setFullScreenExitHint("");

        if (!mainStage.isShowing()) {
            mainStage.show();
        }

        Platform.runLater(() -> {
            mainStage.setFullScreen(true);
        });
    }
}
