package com.example.bandb.ui;

import com.example.bandb.app.SceneManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import java.io.IOException;

/** Reusable customer-facing FXML confirmation dialog. */
public final class BeautyDialogs {
    private BeautyDialogs() {}

    public static boolean confirm(Window owner, String title, String message,
                                  String confirmText) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.resource("confirm-dialog.fxml"));
            Parent root = loader.load();
            Label heading = (Label) loader.getNamespace().get("dialogTitle");
            Label description = (Label) loader.getNamespace().get("dialogMessage");
            Button yes = (Button) loader.getNamespace().get("confirmButton");
            Button no = (Button) loader.getNamespace().get("cancelButton");
            heading.setText(title);
            description.setText(message);
            yes.setText(confirmText);
            Stage dialog = new Stage(StageStyle.DECORATED);
            dialog.setTitle("Blush & Bloom");
            if (owner != null) {
                dialog.initOwner(owner);
                dialog.initModality(Modality.WINDOW_MODAL);
            } else {
                dialog.initModality(Modality.APPLICATION_MODAL);
            }
            final boolean[] approved = {false};
            yes.setOnAction(e -> { approved[0] = true; dialog.close(); });
            no.setOnAction(e -> dialog.close());
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
            return approved[0];
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load confirm-dialog.fxml", exception);
        }
    }
}
