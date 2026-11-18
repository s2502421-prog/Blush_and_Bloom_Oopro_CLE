
package com.example.bandb;

import com.example.bandb.model.Makeup;
import com.example.bandb.model.Product;
import com.example.bandb.service.ProductData;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
            Label empty = new Label(
                    "No makeup found. Try another search."
            );
            empty.setStyle(
                    "-fx-text-fill: #A78495; -fx-font-size: 15px; -fx-padding: 30;"
            );
            productGrid.getChildren().add(empty);
        }

        updateFilterButtons();
        updateCartCount();
    }

    private VBox createProductCard(Makeup product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("product-card.fxml")
            );

            VBox card = loader.load();

            Label recommendation =
                    (Label) card.lookup("#cardRecommendation");
            StackPane preview =
                    (StackPane) card.lookup("#cardPreview");
            Label category =
                    (Label) card.lookup("#cardCategory");
            Label name =
                    (Label) card.lookup("#cardName");
            Label shade =
                    (Label) card.lookup("#cardShade");
            Label color =
                    (Label) card.lookup("#cardColor");
            Label price =
                    (Label) card.lookup("#cardPrice");
            Button add =
                    (Button) card.lookup("#cardAddButton");

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
            add.setOnAction(e -> addToCart(product));

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
                        "You can order up to 20 units of each shade per order."
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
                    getClass().getResource("added-to-cart.fxml")
            );

            VBox content = loader.load();

            ((Label) content.lookup("#addedProductName"))
                    .setText(product.getName());

            ((Label) content.lookup("#addedShade"))
                    .setText("Shade: " + product.getShade());

            ((Label) content.lookup("#addedPrice"))
                    .setText(String.format("₱%,.2f", product.getPrice()));

            ((Label) content.lookup("#addedCartCount"))
                    .setText(
                            ShoppingCart.getTotalQuantity() +
                                    " item(s) in your cart"
                    );

            Node illustration = createIllustration(product);
            illustration.setScaleX(0.6);
            illustration.setScaleY(0.6);

            ((StackPane) content.lookup("#addedPreview"))
                    .getChildren().setAll(illustration);

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
    private void handleHome() throws IOException {
        SceneManager.switchScene("home.fxml");
    }

    @FXML
    private void handleLogout() throws IOException {
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
