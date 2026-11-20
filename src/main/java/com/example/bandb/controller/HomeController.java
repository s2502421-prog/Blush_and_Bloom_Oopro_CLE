package com.example.bandb.controller;

import com.example.bandb.app.SceneManager;
import com.example.bandb.service.Session;
import com.example.bandb.service.ShoppingCart;
import com.example.bandb.ui.BlushPreview;
import com.example.bandb.ui.FoundationPreview;
import com.example.bandb.ui.LipstickPreview;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

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

    @FXML private Button cartCountButton;
    @FXML private Button inventoryButton;

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

        if (beautyMessage != null) {
            beautyMessage.setText(
                    "Beautiful shades selected for your " +
                            tone + " skin tone."
            );
        }

        if (foundationShade != null) {
            foundationShade.setText(
                    "Shade: " + foundationName
            );
        }

        if (blushShade != null) {
            blushShade.setText(
                    "Shade: " + blushName
            );
        }

        if (lipstickShade != null) {
            lipstickShade.setText(
                    "Shade: " + lipstickName
            );
        }

        if (foundationContainer != null) {

            FoundationPreview foundation =
                    new FoundationPreview();

            foundation.setShadeColor(foundationColor);

            foundationContainer.getChildren().setAll(
                    foundation
            );
        }

        if (blushContainer != null) {

            BlushPreview blush = new BlushPreview();

            blush.setShadeColor(blushColor);

            blushContainer.getChildren().setAll(
                    blush
            );
        }

        if (lipstickContainer != null) {

            LipstickPreview lipstick =
                    new LipstickPreview();

            lipstick.setShadeColor(lipstickColor);

            lipstickContainer.getChildren().setAll(
                    lipstick
            );
        }

        if (inventoryButton != null) {
            boolean admin = Session.isAdmin();
            inventoryButton.setVisible(admin);
            inventoryButton.setManaged(admin);
        }

        setupCartCounter();
    }

    private void setupCartCounter() {

        updateCartCount();

        Node anchor = getHomeAnchor();

        if (anchor == null) {
            return;
        }

        anchor.sceneProperty().addListener(
                (observable, oldScene, newScene) -> {

                    if (newScene != null) {
                        Platform.runLater(this::updateCartCount);
                    }
                }
        );

        if (anchor.getScene() != null) {
            Platform.runLater(this::updateCartCount);
        }
    }

    private Node getHomeAnchor() {

        if (bannerContainer != null) {
            return bannerContainer;
        }

        if (skinToneLabel != null) {
            return skinToneLabel;
        }

        if (foundationContainer != null) {
            return foundationContainer;
        }

        if (beautyMessage != null) {
            return beautyMessage;
        }

        if (cartCountButton != null) {
            return cartCountButton;
        }

        return null;
    }

    private Scene getHomeScene() {

        Node anchor = getHomeAnchor();

        if (anchor == null) {
            return null;
        }

        return anchor.getScene();
    }

    private void updateCartCount() {

        int count = 0;

        if (ShoppingCart.isLoggedIn()) {
            count = ShoppingCart.getTotalQuantity();
        }

        Button cartButton = cartCountButton;

        if (cartButton == null) {

            Scene scene = getHomeScene();

            if (scene != null) {
                cartButton = findCartButton(
                        scene.getRoot()
                );
            }
        }

        if (cartButton != null) {
            cartButton.setText(
                    "My Cart (" + count + ")"
            );
        }
    }

    private Button findCartButton(Parent parent) {

        for (Node child :
                parent.getChildrenUnmodifiable()) {

            if (child instanceof Button) {

                Button button = (Button) child;

                String text = button.getText();

                if (text != null &&
                        text.trim().matches(
                                "(?i)My Cart(?:\\s*\\(\\d+\\))?"
                        )) {

                    return button;
                }
            }

            if (child instanceof Parent) {

                Button found = findCartButton(
                        (Parent) child
                );

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    @FXML
    private void handleProducts() {
        SceneManager.switchScene("products.fxml");
    }

    @FXML
    private void handleInventory() {
        if (Session.isAdmin()) {
            SceneManager.switchScene("inventory.fxml");
        }
    }

    @FXML
    private void handleCart() {

        if (!ShoppingCart.isLoggedIn()) {
            return;
        }

        Scene scene = getHomeScene();

        if (scene == null) {
            return;
        }

        try {

            CartWindow.show(
                    scene.getWindow()
            );

        } catch (RuntimeException exception) {

            exception.printStackTrace();

        } finally {

            updateCartCount();
        }
    }

    @FXML
    private void handleLogout() {
        Session.end();
        SceneManager.switchScene("login.fxml");
    }
}
