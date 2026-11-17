
package com.example.bandb;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class HomeController {

    @FXML private Label skinToneLabel;
    @FXML private Label foundationShade;
    @FXML private Label blushShade;
    @FXML private Label lipstickShade;
    @FXML private Label beautyMessage;

    @FXML private StackPane foundationContainer;
    @FXML private StackPane blushContainer;
    @FXML private StackPane lipstickContainer;
    @FXML private StackPane bannerContainer;
    @FXML private ImageView bannerImage;

    @FXML
    public void initialize() {

        if (bannerImage != null && bannerContainer != null) {
            bannerImage.fitWidthProperty().bind(
                    bannerContainer.widthProperty()
            );
            bannerImage.fitHeightProperty().bind(
                    bannerContainer.heightProperty()
            );
        }

        String tone = SkinToneController.getSelectedSkinTone();

        if (tone == null || tone.isBlank()) {
            tone = "Medium";
        }

        String foundationName;
        String foundationColor;
        String blushName;
        String blushColor;
        String lipstickName;
        String lipstickColor;

        switch (tone) {
            case "Fair":
                foundationName = "Porcelain";
                foundationColor = "#F4D9C9";
                blushName = "Baby Pink";
                blushColor = "#F5A9C0";
                lipstickName = "Soft Rose";
                lipstickColor = "#C97987";
                break;

            case "Light":
                foundationName = "Ivory";
                foundationColor = "#E8C3A6";
                blushName = "Peach Pink";
                blushColor = "#F5A38E";
                lipstickName = "Pink Nude";
                lipstickColor = "#B76E78";
                break;

            case "Tan":
                foundationName = "Warm Honey";
                foundationColor = "#B77747";
                blushName = "Terracotta";
                blushColor = "#B96750";
                lipstickName = "Caramel Nude";
                lipstickColor = "#8E4E3C";
                break;

            case "Deep":
                foundationName = "Cocoa";
                foundationColor = "#805037";
                blushName = "Berry";
                blushColor = "#A44266";
                lipstickName = "Burgundy";
                lipstickColor = "#70283E";
                break;

            case "Rich Deep":
                foundationName = "Espresso";
                foundationColor = "#4E3028";
                blushName = "Plum";
                blushColor = "#753C65";
                lipstickName = "Deep Wine";
                lipstickColor = "#512438";
                break;

            default:
                foundationName = "Natural Beige";
                foundationColor = "#C8956D";
                blushName = "Coral";
                blushColor = "#ED846D";
                lipstickName = "Rosewood";
                lipstickColor = "#96535B";
                break;
        }

        if (skinToneLabel != null) {
            skinToneLabel.setText(tone);
        }
        if (foundationShade != null) {
            foundationShade.setText("Shade: " + foundationName);
        }
        if (blushShade != null) {
            blushShade.setText("Shade: " + blushName);
        }
        if (lipstickShade != null) {
            lipstickShade.setText("Shade: " + lipstickName);
        }
        if (beautyMessage != null) {
            beautyMessage.setText(
                    "Beautiful shades selected for your "
                            + tone + " skin tone."
            );
        }

        if (foundationContainer != null) {
            FoundationPreview foundation = new FoundationPreview();
            foundation.setShadeColor(foundationColor);
            foundationContainer.getChildren().setAll(foundation);
        }

        if (blushContainer != null) {
            BlushPreview blush = new BlushPreview();
            blush.setShadeColor(blushColor);
            blushContainer.getChildren().setAll(blush);
        }

        if (lipstickContainer != null) {
            LipstickPreview lipstick = new LipstickPreview();
            lipstick.setShadeColor(lipstickColor);
            lipstickContainer.getChildren().setAll(lipstick);
        }
    }

    @FXML
    private void handleProducts() {

        Alert test = new Alert(Alert.AlertType.INFORMATION);
        test.setTitle("Navigation Test");
        test.setHeaderText("Shop Products button clicked!");
        test.setContentText(
                "JavaFX received your click. Press OK to open the shop."
        );
        test.showAndWait();

        try {
            SceneManager.switchScene("products.fxml");
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Shop Products Error");
            error.setHeaderText("Cannot open products.fxml");
            error.setContentText(e.toString());
            error.showAndWait();
        }
    }

    @FXML
    private void handleShopProducts() {
        handleProducts();
    }

    @FXML
    private void handleShopNow() {
        handleProducts();
    }

    @FXML
    private void handleCart() {
        new Alert(
                Alert.AlertType.INFORMATION,
                "Your dedicated cart page is not ready yet."
        ).showAndWait();
    }

    @FXML
    private void handleLogout() throws IOException {
        SceneManager.switchScene("login.fxml");
    }
}
