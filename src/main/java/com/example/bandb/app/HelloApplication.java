package com.example.bandb.app;

import com.example.bandb.service.CustomerData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {

        stage.setTitle("Blush & Bloom");
        stage.setMinWidth(950);
        stage.setMinHeight(680);

        SceneManager.setMainStage(stage);

        String[] newAdmin = null;
        String startupProblem = null;

        try {
            // First run only: creates the shop admin account.
            newAdmin = CustomerData.ensureAdminAccount();
        } catch (RuntimeException exception) {
            startupProblem = exception.getMessage();
        }

        SceneManager.switchScene("login.fxml");

        // Maximized (not full screen) so pop-up windows such as the
        // cart and alerts always appear on top of the main window.
        stage.setMaximized(true);
        stage.show();

        final String[] admin = newAdmin;
        final String problem = startupProblem;

        Platform.runLater(() -> {
            if (problem != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Blush & Bloom");
                alert.setHeaderText("Customer data could not be loaded");
                alert.setContentText(problem);
                alert.showAndWait();
            }

            if (admin != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initOwner(stage);
                alert.setTitle("Blush & Bloom");
                alert.setHeaderText("Shop administrator account created");

                TextArea text = new TextArea(
                        "Username: " + admin[0] + "\n"
                                + "Password: " + admin[1] + "\n\n"
                                + "Write this password down now. It is "
                                + "shown only once and is stored only as "
                                + "a secure hash.");
                text.setEditable(false);
                text.setWrapText(true);
                text.setPrefRowCount(6);

                alert.getDialogPane().setContent(text);
                alert.showAndWait();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
