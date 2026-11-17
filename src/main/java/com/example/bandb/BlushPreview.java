
package com.example.bandb;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class BlushPreview extends StackPane {

    private final Ellipse powder;

    public BlushPreview() {

        Pane canvas = new Pane();
        canvas.setPrefSize(190, 220);
        canvas.setMinSize(190, 220);
        canvas.setMaxSize(190, 220);

        // Soft shadow
        Ellipse shadow = new Ellipse(95, 191, 61, 7);
        shadow.setFill(Color.web("#EAD8DF", 0.55));

        // Compact lid
        Ellipse lid = new Ellipse(95, 79, 65, 37);
        lid.setFill(Color.web("#EABACD"));
        lid.setStroke(Color.web("#C98EA7"));
        lid.setStrokeWidth(2.5);

        // Mirror
        Ellipse mirror = new Ellipse(95, 79, 55, 29);
        mirror.setFill(Color.web("#FFF9FC"));
        mirror.setStroke(Color.web("#F4E4ED"));
        mirror.setStrokeWidth(3);

        // Mirror highlight
        Ellipse mirrorShine = new Ellipse(75, 70, 14, 5);
        mirrorShine.setRotate(-20);
        mirrorShine.setFill(Color.WHITE);
        mirrorShine.setOpacity(0.8);

        // Hinge
        Rectangle hinge = new Rectangle(80, 111, 30, 9);
        hinge.setArcWidth(5);
        hinge.setArcHeight(5);
        hinge.setFill(Color.web("#D9B67B"));

        // Compact base
        Ellipse base = new Ellipse(95, 151, 66, 43);
        base.setFill(Color.web("#DFA7BE"));
        base.setStroke(Color.web("#BE819A"));
        base.setStrokeWidth(2.5);

        // Gold rim
        Ellipse goldRim = new Ellipse(95, 147, 55, 34);
        goldRim.setFill(Color.web("#E4C28C"));

        // White tray
        Ellipse tray = new Ellipse(95, 146, 51, 30);
        tray.setFill(Color.web("#FFF1F6"));

        // Actual blush powder
        powder = new Ellipse(95, 145, 45, 25);
        powder.setFill(Color.web("#F5A9C0"));

        // Powder highlight
        Ellipse highlight = new Ellipse(78, 137, 12, 4);
        highlight.setRotate(-20);
        highlight.setFill(Color.WHITE);
        highlight.setOpacity(0.18);

        // Front clasp
        Rectangle clasp = new Rectangle(86, 184, 18, 5);
        clasp.setArcWidth(4);
        clasp.setArcHeight(4);
        clasp.setFill(Color.web("#E8C991"));

        canvas.getChildren().addAll(
                shadow, lid, mirror, mirrorShine,
                hinge, base, goldRim, tray,
                powder, highlight, clasp
        );

        getChildren().add(canvas);
    }

    public void setShadeColor(String hexColor) {
        powder.setFill(Color.web(hexColor));
    }
}
