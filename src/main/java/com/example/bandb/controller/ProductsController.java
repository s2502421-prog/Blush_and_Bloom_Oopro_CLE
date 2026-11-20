package com.example.bandb.controller;

import com.example.bandb.app.SceneManager;
import com.example.bandb.service.Session;
import com.example.bandb.ui.BeautyDialogs;
import com.example.bandb.service.ShoppingCart;
import com.example.bandb.ui.BlushPreview;
import com.example.bandb.ui.FoundationPreview;
import com.example.bandb.ui.LipstickPreview;


import com.example.bandb.model.Makeup;
import com.example.bandb.model.Product;
import com.example.bandb.service.ProductData;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.transform.Scale;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

public class ProductsController {

    @FXML private FlowPane productGrid;
    @FXML private Label skinToneLabel;
    @FXML private Label productCountLabel;
    @FXML private Button cartCountButton;
    @FXML private Button allButton;
    @FXML private Button foundationButton;
    @FXML private Button blushButton;
    @FXML private Button lipstickButton;
    @FXML private TextField searchField;

    private final ArrayList<Product> products = new ArrayList<>();
    private String selectedSkinTone = "Medium";
    private String activeCategory = "All";

    @FXML
    public void initialize() {
        String tone = SkinToneController.getSelectedSkinTone();

        if (tone != null && !tone.isBlank()) {
            selectedSkinTone = tone;
        }

        if (skinToneLabel != null) {
            skinToneLabel.setText("Your skin tone: " + selectedSkinTone);
        }

        if (searchField != null) {
            searchField.textProperty().addListener(
                    (obs, oldValue, newValue) ->
                            displayProducts(activeCategory)
            );
        }


        refreshShop();
        Platform.runLater(this::updateCartCount);
    }

    private void refreshShop() {
        products.clear();
        products.addAll(ProductData.getAllProducts());
        displayProducts(activeCategory);
        updateCartCount();
    }

    private void displayProducts(String category) {
        activeCategory = category;

        if (productGrid == null) {
            return;
        }

        productGrid.getChildren().clear();

        String query = searchField == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        int count = 0;

        for (Product product : products) {
            if (!(product instanceof Makeup)) {
                continue;
            }

            Makeup makeup = (Makeup) product;

            boolean categoryMatches = category.equals("All")
                    || category.equals(makeup.getCategory());

            String searchable = (
                    makeup.getName() + " " +
                            makeup.getCategory() + " " +
                            makeup.getShade() + " " +
                            makeup.getBrand() + " " +
                            makeup.getRecommendedSkinTone()
            ).toLowerCase(Locale.ROOT);

            if (!categoryMatches || !searchable.contains(query)) {
                continue;
            }

            productGrid.getChildren().add(createProductCard(makeup));
            count++;
        }

        if (productCountLabel != null) {
            productCountLabel.setText(
                    count == 0
                            ? "No matching products found"
                            : count + " shades available"
            );
        }

        if (count == 0) {
            try {
                productGrid.getChildren().add(
                        FXMLLoader.load(SceneManager.resource("no-products.fxml")));
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to load no-products.fxml", ex);
            }
        }

        updateFilterButtons();
        updateCartCount();
    }

    private VBox createProductCard(Makeup product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.resource("product-card.fxml")
            );

            VBox card = loader.load();

            Label recommendation =
                    (Label) loader.getNamespace().get("cardRecommendation");
            StackPane preview =
                    (StackPane) loader.getNamespace().get("cardPreview");
            Label category =
                    (Label) loader.getNamespace().get("cardCategory");
            Label name =
                    (Label) loader.getNamespace().get("cardName");
            Label shade =
                    (Label) loader.getNamespace().get("cardShade");
            Label color =
                    (Label) loader.getNamespace().get("cardColor");
            Label price =
                    (Label) loader.getNamespace().get("cardPrice");
            Button add =
                    (Button) loader.getNamespace().get("cardAddButton");

            boolean recommended =
                    product.getRecommendedSkinTone()
                            .equalsIgnoreCase(selectedSkinTone);

            recommendation.setText(
                    recommended ? "♥ RECOMMENDED FOR YOU" : " "
            );

            category.setText(product.getCategory().toUpperCase(Locale.ROOT));
            name.setText(product.getName());
            shade.setText("Shade: " + product.getShade());
            color.setStyle(
                    "-fx-font-size: 16px; -fx-text-fill: " +
                            product.getShadeColor() + ";"
            );
            price.setText(
                    String.format("₱%,.2f", product.getPrice())
            );

            preview.getChildren().setAll(createIllustration(product));

            if (product.getStock() < 1) {
                add.setText("Sold Out");
                add.setDisable(true);
            } else {
                add.setOnAction(e -> addToCart(product));
            }

            card.setStyle(
                    card.getStyle() +
                            "-fx-border-color: " +
                            (recommended ? "#D97DA5;" : "#F2DAE4;") +
                            "-fx-border-width: " + (recommended ? "2;" : "1;")
            );

            return card;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load product-card.fxml",
                    exception
            );
        }
    }

    private Node createIllustration(Makeup product) {
        switch (product.getCategory()) {
            case "Foundation":
                FoundationPreview foundation = new FoundationPreview();
                foundation.setShadeColor(product.getShadeColor());
                return foundation;

            case "Blush":
                BlushPreview blush = new BlushPreview();
                blush.setShadeColor(product.getShadeColor());
                return blush;

            case "Lipstick":
                LipstickPreview lipstick = new LipstickPreview();
                lipstick.setShadeColor(product.getShadeColor());
                return lipstick;

            default:
                return new Label("Makeup");
        }
    }

    private void addToCart(Makeup product) {
        try {
            if (!ShoppingCart.isLoggedIn()) {
                showWarning(
                        "Login Required",
                        "Please log in before adding products."
                );
                return;
            }

            boolean added = ShoppingCart.addItem(
                    product.getCategory(),
                    product.getName(),
                    product.getShade(),
                    product.getPrice()
            );

            updateCartCount();

            if (!added) {
                showWarning(
                        "Quantity Limit",
                        "You can order up to "
                                + ShoppingCart.MAX_QUANTITY
                                + " units of each shade per order, "
                                + "or what we have in stock."
                );
                return;
            }

            showAddedPopup(product);
            updateCartCount();

        } catch (RuntimeException exception) {
            showWarning(
                    "Unable to Add Product",
                    exception.getMessage() == null
                            ? "Please try again."
                            : exception.getMessage()
            );
        }
    }

    private void showAddedPopup(Makeup product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.resource("added-to-cart.fxml")
            );

            VBox content = loader.load();

            ((Label) loader.getNamespace().get("addedProductName"))
                    .setText(product.getName());

            ((Label) loader.getNamespace().get("addedShade"))
                    .setText("Shade: " + product.getShade());

            ((Label) loader.getNamespace().get("addedPrice"))
                    .setText(String.format("₱%,.2f", product.getPrice()));

            ((Label) loader.getNamespace().get("addedCartCount"))
                    .setText(
                            ShoppingCart.getTotalQuantity() +
                                    " item(s) in your cart"
                    );

            // Scale the illustration inside a Group so JavaFX lays out its
            // scaled dimensions instead of its original 190 x 220 size.
            Node illustration = createIllustration(product);
            illustration.getTransforms().add(new Scale(0.36, 0.36, 0, 0));
            Group fittedIllustration = new Group(illustration);

            StackPane preview = (StackPane) loader.getNamespace().get("addedPreview");
            preview.getChildren().setAll(fittedIllustration);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Blush & Bloom");

            if (productGrid.getScene() != null) {
                dialog.initOwner(productGrid.getScene().getWindow());
            }

            ButtonType continueType = new ButtonType(
                    "Continue Shopping",
                    ButtonBar.ButtonData.CANCEL_CLOSE
            );

            ButtonType cartType = new ButtonType(
                    "View My Cart",
                    ButtonBar.ButtonData.OK_DONE
            );

            DialogPane pane = dialog.getDialogPane();
            pane.getButtonTypes().addAll(continueType, cartType);
            pane.setStyle("-fx-background-color: #FFF8FB;");
            pane.setContent(content);

            ((Button) pane.lookupButton(continueType)).setStyle(
                    "-fx-background-color: #FCE3ED;" +
                            "-fx-text-fill: #A65378;" +
                            "-fx-background-radius: 10;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 11 17;"
            );

            ((Button) pane.lookupButton(cartType)).setStyle(
                    "-fx-background-color: #D97DA5;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 11 17;"
            );

            Optional<ButtonType> choice = dialog.showAndWait();
            updateCartCount();

            if (choice.isPresent() && choice.get() == cartType) {
                handleCart();
            }

        } catch (IOException exception) {
            showWarning(
                    "Display Error",
                    "Unable to open added-to-cart.fxml"
            );
        }
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);

        if (productGrid != null && productGrid.getScene() != null) {
            alert.initOwner(productGrid.getScene().getWindow());
        }

        alert.setTitle("Blush & Bloom");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
        updateCartCount();
    }

    private void updateCartCount() {
        if (cartCountButton == null) {
            return;
        }

        int quantity = ShoppingCart.isLoggedIn()
                ? ShoppingCart.getTotalQuantity()
                : 0;

        cartCountButton.setText("My Cart (" + quantity + ")");
    }

    private void updateFilterButtons() {
        styleFilter(allButton, "All");
        styleFilter(foundationButton, "Foundation");
        styleFilter(blushButton, "Blush");
        styleFilter(lipstickButton, "Lipstick");
    }

    private void styleFilter(Button button, String category) {
        if (button == null) {
            return;
        }

        boolean selected = activeCategory.equals(category);

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
    private void handleHome() {
        SceneManager.switchScene("home.fxml");
    }


    @FXML
    private void handleLogout() {
        javafx.stage.Window owner = null;
        if (productGrid != null && productGrid.getScene() != null) {
            owner = productGrid.getScene().getWindow();
        }
        if (!BeautyDialogs.confirm(owner, "Log out of Blush & Bloom?",
                "You can sign back in whenever you like.", "Yes, Log Out")) {
            return;
        }
        Session.end();
        SceneManager.switchScene("login.fxml");
    }

    @FXML
    private void handleCart() {
        if (productGrid == null || productGrid.getScene() == null) {
            return;
        }

        if (!ShoppingCart.isLoggedIn()) {
            showWarning(
                    "Login Required",
                    "Please log in to view your cart."
            );
            return;
        }

        try {
            CartWindow.showForOrder(
                    productGrid.getScene().getWindow()
            );
            refreshShop();

        } catch (RuntimeException exception) {
            showWarning(
                    "Cart Error",
                    exception.getMessage() == null
                            ? "Unable to open your cart."
                            : exception.getMessage()
            );

        } finally {
            updateCartCount();
        }
    }
}
