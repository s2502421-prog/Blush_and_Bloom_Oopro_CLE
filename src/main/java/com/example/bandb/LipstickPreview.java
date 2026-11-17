
package com.example.bandb;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class LipstickPreview extends StackPane {

    private final Path lipstickBullet;

    public LipstickPreview() {

        Pane canvas = new Pane();
        canvas.setPrefSize(190, 220);
        canvas.setMinSize(190, 220);
        canvas.setMaxSize(190, 220);

        // Soft ground shadow
        Ellipse shadow = new Ellipse(95, 207, 39, 5);
        shadow.setFill(Color.web("#EAD8DF", 0.65));

        // Rounded lipstick bullet
        lipstickBullet = new Path(
                new MoveTo(76, 113),
                new LineTo(76, 70),
                new CubicCurveTo(76, 54, 85, 45, 99, 43),
                new CubicCurveTo(109, 41, 114, 47, 114, 56),
                new LineTo(114, 113),
                new ClosePath()
        );

        lipstickBullet.setFill(Color.web("#C97987"));

        // Soft pink outline instead of black
        lipstickBullet.setStroke(Color.web("#C98EA7"));
        lipstickBullet.setStrokeWidth(1.5);
        lipstickBullet.setStrokeLineJoin(StrokeLineJoin.ROUND);

        // Gentle lipstick highlight
        Path highlight = new Path(
                new MoveTo(83, 102),
                new LineTo(83, 74),
                new CubicCurveTo(83, 63, 89, 57, 96, 53)
        );
        highlight.setStroke(Color.WHITE);
        highlight.setStrokeWidth(4);
        highlight.setStrokeLineCap(StrokeLineCap.ROUND);
        highlight.setOpacity(0.25);
        highlight.setFill(Color.TRANSPARENT);

        // Soft side shading
        Path sideShade = new Path(
                new MoveTo(108, 58),
                new LineTo(114, 55),
                new LineTo(114, 112),
                new LineTo(108, 112),
                new ClosePath()
        );
        sideShade.setFill(Color.web("#9E617B", 0.13));

        // Gold inner holder
        Rectangle holder = new Rectangle(70, 111, 50, 28);
        holder.setArcWidth(5);
        holder.setArcHeight(5);
        holder.setFill(Color.web("#DDBB87"));

        // Gold shine
        Rectangle holderShine = new Rectangle(77, 116, 5, 18);
        holderShine.setFill(Color.web("#FFF0D2", 0.65));

        // Soft gold collar
        Rectangle collar = new Rectangle(66, 138, 58, 10);
        collar.setArcWidth(4);
        collar.setArcHeight(4);
        collar.setFill(Color.web("#F0D5A4"));

        // Pink lipstick case
        Rectangle tube = new Rectangle(65, 147, 60, 55);
        tube.setArcWidth(12);
        tube.setArcHeight(12);
        tube.setFill(Color.web("#D991B1"));
        tube.setStroke(Color.web("#C98EA7"));
        tube.setStrokeWidth(2);

        // Glossy highlight
        Rectangle tubeShine = new Rectangle(73, 154, 6, 35);
        tubeShine.setArcWidth(6);
        tubeShine.setArcHeight(6);
        tubeShine.setFill(Color.WHITE);
        tubeShine.setOpacity(0.35);

        // Bottom gold accent
        Rectangle goldStripe = new Rectangle(69, 193, 52, 4);
        goldStripe.setFill(Color.web("#F0D5A4"));

        canvas.getChildren().addAll(
                shadow,
                lipstickBullet,
                sideShade,
                highlight,
                holder,
                holderShine,
                collar,
                tube,
                tubeShine,
                goldStripe
        );

        getChildren().add(canvas);
    }

    public void setShadeColor(String hexColor) {
        lipstickBullet.setFill(Color.web(hexColor));
    }
}
