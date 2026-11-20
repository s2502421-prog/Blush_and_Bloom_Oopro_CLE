package com.example.bandb.ui;

import com.example.bandb.app.SceneManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import java.io.IOException;

/** The illustration layout is defined in blush-preview.fxml (Scene Builder editable). */
public class BlushPreview extends StackPane {
    private final Shape shadeShape;
    public BlushPreview() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.resource("blush-preview.fxml"));
            StackPane illustration = loader.load();
            shadeShape = (Shape) loader.getNamespace().get("shadeShape");
            if (shadeShape == null) throw new IllegalStateException("Missing shadeShape in blush-preview.fxml");
            getChildren().add(illustration);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load blush-preview.fxml", ex);
        }
    }
    public void setShadeColor(String hexColor) { shadeShape.setFill(Color.web(hexColor)); }
}
