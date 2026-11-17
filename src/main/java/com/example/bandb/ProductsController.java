
package com.example.bandb;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductsController {

    @FXML private FlowPane productGrid;
    @FXML private Label skinToneLabel;
    @FXML private Label productCountLabel;
    @FXML private Button cartCountButton;

    @FXML private Button allButton;
    @FXML private Button foundationButton;
    @FXML private Button blushButton;
    @FXML private Button lipstickButton;

    private final List<ShopProduct> products =
            new ArrayList<>();

    private String selectedSkinTone = "Medium";
    private String activeCategory = "All";

    private static class ShopProduct {

        final String category;
        final String name;
        final String shade;
        final String hexColor;
        final String recommendedTone;
        final double price;

        ShopProduct(
                String category,
                String name,
                String shade,
                String hexColor,
                String recommendedTone,
                double price
        ) {
            this.category = category;
            this.name = name;
            this.shade = shade;
            this.hexColor = hexColor;
            this.recommendedTone = recommendedTone;
            this.price = price;
        }
    }

    @FXML
    public void initialize() {

        String tone =
                SkinToneController.getSelectedSkinTone();

        if (tone != null && !tone.isBlank()) {
            selectedSkinTone = tone;
        }

        skinToneLabel.setText(
                "Your skin tone: " + selectedSkinTone
        );

        loadProducts();
        displayProducts("All");
        updateCartCount();
    }

    private void loadProducts() {

        products.clear();

        // GLOW FOUNDATION
        add("Foundation", "Glow Foundation",
                "Porcelain", "#F4D9C9", "Fair", 399);

        add("Foundation", "Glow Foundation",
                "Ivory", "#E8C3A6", "Light", 399);

        add("Foundation", "Glow Foundation",
                "Natural Beige", "#C8956D", "Medium", 399);

        add("Foundation", "Glow Foundation",
                "Warm Honey", "#B77747", "Tan", 399);

        add("Foundation", "Glow Foundation",
                "Cocoa", "#805037", "Deep", 399);

        add("Foundation", "Glow Foundation",
                "Espresso", "#4E3028", "Rich Deep", 399);

        // DREAMY BLUSH
        add("Blush", "Dreamy Blush",
                "Baby Pink", "#F5A9C0", "Fair", 249);

        add("Blush", "Dreamy Blush",
                "Peach Pink", "#F5A38E", "Light", 249);

        add("Blush", "Dreamy Blush",
                "Coral", "#ED846D", "Medium", 249);

        add("Blush", "Dreamy Blush",
                "Terracotta", "#B96750", "Tan", 249);

        add("Blush", "Dreamy Blush",
                "Berry", "#A44266", "Deep", 249);

        add("Blush", "Dreamy Blush",
                "Plum", "#753C65", "Rich Deep", 249);

        // BLOOM KISS LIPSTICK
        add("Lipstick", "Bloom Kiss Lipstick",
                "Soft Rose", "#C97987", "Fair", 199);

        add("Lipstick", "Bloom Kiss Lipstick",
                "Pink Nude", "#B76E78", "Light", 199);

        add("Lipstick", "Bloom Kiss Lipstick",
                "Rosewood", "#96535B", "Medium", 199);

        add("Lipstick", "Bloom Kiss Lipstick",
                "Caramel Nude", "#8E4E3C", "Tan", 199);

        add("Lipstick", "Bloom Kiss Lipstick",
                "Burgundy", "#70283E", "Deep", 199);

        add("Lipstick", "Bloom Kiss Lipstick",
                "Deep Wine", "#512438", "Rich Deep", 199);
    }

    private void add(
            String category,
            String name,
            String shade,
            String color,
            String tone,
            double price
    ) {
        products.add(new ShopProduct(
                category, name, shade,
                color, tone, price
        ));
    }

    private void displayProducts(String category) {

        activeCategory = category;
        productGrid.getChildren().clear();

        int count = 0;

        for (ShopProduct product : products) {

            if (!category.equals("All")
                    && !product.category.equals(category)) {
                continue;
            }

            productGrid.getChildren().add(
                    createProductCard(product)
            );

            count++;
        }

        productCountLabel.setText(
                count + " shades available"
        );

        updateFilterButtons();
    }

    private VBox createProductCard(ShopProduct product) {

        boolean recommended =
                product.recommendedTone.equalsIgnoreCase(
                        selectedSkinTone
                );

        VBox card = new VBox(9);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(14));

        card.setPrefWidth(225);
        card.setMinWidth(225);
        card.setMaxWidth(225);

        card.setPrefHeight(455);
        card.setMinHeight(455);
        card.setMaxHeight(455);

        String borderColor =
                recommended ? "#D97DA5" : "#F2DAE4";

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: " +
                        (recommended ? "2" : "1") + ";" +
                        "-fx-border-radius: 18;"
        );

        Label recommendation = new Label(
                recommended
                        ? "♥ RECOMMENDED FOR YOU"
                        : " "
        );

        recommendation.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #C45D89;"
        );

        StackPane illustrationArea = new StackPane();

        illustrationArea.setPrefSize(190, 220);
        illustrationArea.setMinSize(190, 220);
        illustrationArea.setMaxSize(190, 220);

        illustrationArea.setStyle(
                "-fx-background-color: #FFF5F9;" +
                        "-fx-background-radius: 14;"
        );

        illustrationArea.getChildren().add(
                createIllustration(product)
        );

        Label categoryLabel = new Label(
                product.category.toUpperCase()
        );

        categoryLabel.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-text-fill: #B67B91;" +
                        "-fx-font-weight: bold;"
        );

        Label nameLabel = new Label(product.name);

        nameLabel.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #563C49;"
        );

        Label shadeLabel = new Label(
                "Shade: " + product.shade
        );

        shadeLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #846A76;"
        );

        Circle swatch = new Circle(7);
        swatch.setFill(Color.web(product.hexColor));
        swatch.setStroke(Color.web("#E4C7D3"));

        HBox shadeRow = new HBox(
                7, swatch, shadeLabel
        );

        shadeRow.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(
                String.format("₱%.2f", product.price)
        );

        priceLabel.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #C45D89;"
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("Add to Cart");

        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setPrefHeight(38);

        addButton.setStyle(
                "-fx-background-color: #E9A6C0;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );

        addButton.setOnAction(event ->
                addToCart(product)
        );

        card.getChildren().addAll(
                recommendation,
                illustrationArea,
                categoryLabel,
                nameLabel,
                shadeRow,
                priceLabel,
                spacer,
                addButton
        );

        return card;
    }

    private Node createIllustration(ShopProduct product) {

        switch (product.category) {

            case "Foundation":
                FoundationPreview foundation =
                        new FoundationPreview();

                foundation.setShadeColor(
                        product.hexColor
                );

                return foundation;

            case "Blush":
                BlushPreview blush =
                        new BlushPreview();

                blush.setShadeColor(
                        product.hexColor
                );

                return blush;

            case "Lipstick":
                LipstickPreview lipstick =
                        new LipstickPreview();

                lipstick.setShadeColor(
                        product.hexColor
                );

                return lipstick;

            default:
                return new Label("Makeup");
        }
    }

    private void addToCart(ShopProduct product) {

        ShoppingCart.addItem(
                product.category,
                product.name,
                product.shade,
                product.price
        );

        updateCartCount();

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle("Blush & Bloom");
        alert.setHeaderText("Added to Cart! ♥");

        alert.setContentText(
                product.name + "\n" +
                        "Shade: " + product.shade + "\n" +
                        String.format(
                                "Price: ₱%.2f",
                                product.price
                        )
        );

        alert.showAndWait();
    }

    private void updateCartCount() {

        cartCountButton.setText(
                "My Cart (" +
                        ShoppingCart.getTotalQuantity() +
                        ")"
        );
    }

    private void updateFilterButtons() {

        styleFilter(allButton, "All");
        styleFilter(foundationButton, "Foundation");
        styleFilter(blushButton, "Blush");
        styleFilter(lipstickButton, "Lipstick");
    }

    private void styleFilter(
            Button button,
            String category
    ) {

        boolean selected =
                activeCategory.equals(category);

        button.setStyle(
                "-fx-background-color: " +
                        (selected ? "#D97DA5" : "#FBE7EF") + ";" +
                        "-fx-text-fill: " +
                        (selected ? "white" : "#A75B7C") + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 9 17;" +
                        "-fx-cursor: hand;"
        );
    }

    @FXML
    private void showAll() {
        displayProducts("All");
    }

    @FXML
    private void showFoundation() {
        displayProducts("Foundation");
    }

    @FXML
    private void showBlush() {
        displayProducts("Blush");
    }

    @FXML
    private void showLipstick() {
        displayProducts("Lipstick");
    }

    @FXML
    private void handleHome() throws IOException {
        SceneManager.switchScene("home.fxml");
    }

    @FXML
    private void handleLogout() throws IOException {
        SceneManager.switchScene("login.fxml");
    }

    @FXML
    private void handleCart() {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle("Blush & Bloom");
        alert.setHeaderText("Your Shopping Cart");

        if (ShoppingCart.getItems().isEmpty()) {

            alert.setContentText(
                    "Your cart is currently empty."
            );

        } else {

            StringBuilder details =
                    new StringBuilder();

            for (ShoppingCart.CartItem item :
                    ShoppingCart.getItems()) {

                details.append(item.getProductName())
                        .append(" - ")
                        .append(item.getShade())
                        .append(" x")
                        .append(item.getQuantity())
                        .append("\n");
            }

            details.append("\nTotal: ")
                    .append(String.format(
                            "₱%.2f",
                            ShoppingCart.getTotalPrice()
                    ));

            alert.setContentText(details.toString());
        }

        alert.showAndWait();
    }
}
