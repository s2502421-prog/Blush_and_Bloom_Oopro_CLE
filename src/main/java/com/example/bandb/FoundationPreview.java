
package com.example.bandb;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;

public class FoundationPreview extends StackPane {

    private final Rectangle liquid;

    public FoundationPreview() {

        Pane bottle = new Pane();
        bottle.setPrefSize(190, 220);
        bottle.setMinSize(190, 220);
        bottle.setMaxSize(190, 220);

        Rectangle cap = new Rectangle(65, 10, 60, 35);
        cap.setArcWidth(10);
        cap.setArcHeight(10);
        cap.setFill(Color.web("#D7A1B7"));

        Rectangle neck = new Rectangle(78, 40, 34, 18);
        neck.setFill(Color.web("#E9D4DC"));

        Rectangle glass = new Rectangle(35, 55, 120, 150);
        glass.setArcWidth(22);
        glass.setArcHeight(22);
        glass.setFill(Color.web("#FFF0F5"));
        glass.setStroke(Color.web("#C995AB"));
        glass.setStrokeWidth(2);

        liquid = new Rectangle(43, 85, 104, 112);
        liquid.setArcWidth(16);
        liquid.setArcHeight(16);
        liquid.setFill(Color.web("#F4D9C9"));

        Rectangle label = new Rectangle(54, 125, 82, 43);
        label.setArcWidth(8);
        label.setArcHeight(8);
        label.setFill(Color.web("#FFF8FB", 0.90));

        Line shine = new Line(49, 70, 49, 108);
        shine.setStroke(Color.WHITE);
        shine.setStrokeWidth(4);
        shine.setOpacity(0.7);

        bottle.getChildren().addAll(
                cap, neck, glass, liquid, label, shine
        );

        getChildren().add(bottle);
    }

    public void setShadeColor(String hexColor) {
        liquid.setFill(Color.web(hexColor));
    }
}
