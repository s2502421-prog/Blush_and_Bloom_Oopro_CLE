package com.example.bandb.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Switches between screens. Every controller button that changes page
 * goes through here, so a screen that fails to load is reported to the
 * user instead of silently doing nothing.
 */
public final class SceneManager {

    private static final String FXML_FOLDER = "/com/example/bandb/fxml/";

    private static Stage mainStage;

    private SceneManager() {
    }

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    /** Finds a file in the fxml resource folder (fxml, css, images). */
    public static URL resource(String fileName) {
        URL url = SceneManager.class.getResource(FXML_FOLDER + fileName);

        if (url == null) {
            throw new IllegalStateException(
                    "Missing resource: " + fileName);
        }

        return url;
    }

    /** @return true when the screen was opened */
    public static boolean switchScene(String fxmlFile) {

        if (mainStage == null) {
            throw new IllegalStateException(
                    "Main stage has not been initialized.");
        }

        try {
            Parent root = FXMLLoader.load(resource(fxmlFile));

            Scene scene = mainStage.getScene();

            if (scene == null) {
                mainStage.setScene(new Scene(root));
            } else {
                scene.setRoot(root);
            }

            if (!mainStage.isShowing()) {
                mainStage.show();
            }

            return true;

        } catch (Exception exception) {
            exception.printStackTrace();

            Throwable cause = exception;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(mainStage);
            alert.setTitle("Blush & Bloom");
            alert.setHeaderText("Unable to open " + fxmlFile);
            alert.setContentText(String.valueOf(cause.getMessage()));
            alert.showAndWait();

            return false;
        }
    }
}
