
package com.example.bandb;

import com.example.bandb.model.Makeup;
import com.example.bandb.model.Product;
import com.example.bandb.service.ProductData;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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

    private final ArrayList<Product> products =
            new ArrayList<>();

    private String selectedSkinTone = "Medium";
    private String activeCategory = "All";
    private boolean searchInstalled = false;

    @FXML
    public void initialize() {

        String tone = SkinToneController.getSelectedSkinTone();

        if (tone != null && !tone.isBlank()) {
            selectedSkinTone = tone;
        }

        if (skinToneLabel != null) {
            skinToneLabel.setText(
                    "Your skin tone: " + selectedSkinTone
            );
        }

        refreshShop();

        if (searchField != null) {
            setupSearchField(searchField);
            searchInstalled = true;
        } else {
            Platform.runLater(this::installSearchBar);
        }

        Platform.runLater(this::updateCartCount);

        if (productGrid != null) {
            productGrid.sceneProperty().addListener(
                    (observable, oldScene, newScene) -> {
                        if (newScene != null) {
                            Platform.runLater(this::updateCartCount);
                        }
                    }
            );
        }
    }

    private void setupSearchField(TextField field) {

        field.setPromptText(
                "Search makeup, category, or shade..."
        );

        field.setMinWidth(350);
        field.setPrefWidth(650);
        field.setMaxWidth(750);
        field.setPrefHeight(48);

        field.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 26;" +
                        "-fx-border-color: #E9A6C0;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 26;" +
                        "-fx-padding: 12 20;" +
                        "-fx-font-size: 14px;" +
                        "-fx-text-fill: #674457;" +
                        "-fx-prompt-text-fill: #B79BA8;"
        );

        field.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        displayProducts(activeCategory)
        );
    }

    private void installSearchBar() {

        if (searchInstalled || productGrid == null) {
            return;
        }

        if (productGrid.getScene() == null) {
            productGrid.sceneProperty().addListener(
                    (observable, oldScene, newScene) -> {
                        if (newScene != null) {
                            Platform.runLater(this::installSearchBar);
                        }
                    }
            );
            return;
        }

        Label categoryTitle = findCategoryTitle(
                productGrid.getScene().getRoot()
        );

        if (categoryTitle == null) {
            return;
        }

        Node anchor = categoryTitle;
        Parent parent = anchor.getParent();

        while (parent != null && !(parent instanceof VBox)) {
            anchor = parent;
            parent = parent.getParent();
        }

        if (!(parent instanceof VBox)) {
            return;
        }

        VBox container = (VBox) parent;

        int position = container.getChildren().indexOf(anchor);

        if (position < 0) {
            return;
        }

        searchField = new TextField();
        setupSearchField(searchField);

        Label searchTitle = new Label("Find Your Beauty");

        searchTitle.setStyle(
                "-fx-text-fill: #A65378;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        Label searchIcon = new Label("⌕");

        searchIcon.setStyle(
                "-fx-text-fill: #C45D89;" +
                        "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;"
        );

        HBox searchRow = new HBox(
                14,
                searchIcon,
                searchField
        );

        searchRow.setAlignment(Pos.CENTER_LEFT);

        VBox searchSection = new VBox(
                13,
                searchTitle,
                searchRow
        );

        searchSection.setPadding(
                new Insets(10, 0, 28, 0)
        );

        searchSection.setMaxWidth(Double.MAX_VALUE);

        container.getChildren().add(
                position,
                searchSection
        );

        searchInstalled = true;
    }

    private Label findCategoryTitle(Node node) {

        if (node instanceof Label) {

            Label label = (Label) node;

            if (label.getText() != null
                    && label.getText().trim()
                    .equalsIgnoreCase("Shop by Category")) {
                return label;
            }
        }

        if (node instanceof Parent) {

            Parent parent = (Parent) node;

            for (Node child : parent.getChildrenUnmodifiable()) {

                Label found = findCategoryTitle(child);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
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

        String searchText = "";

        if (searchField != null
                && searchField.getText() != null) {

            searchText = searchField.getText()
                    .trim()
                    .toLowerCase(Locale.ROOT);
        }

        int count = 0;

        for (Product product : products) {

            if (!(product instanceof Makeup)) {
                continue;
            }

            Makeup makeup = (Makeup) product;

            boolean categoryMatches =
                    category.equals("All")
                            || category.equals(makeup.getCategory());

            String searchableText = (
                    makeup.getName() + " " +
                            makeup.getShade() + " " +
                            makeup.getCategory() + " " +
                            makeup.getBrand() + " " +
                            makeup.getRecommendedSkinTone()
            ).toLowerCase(Locale.ROOT);

            boolean searchMatches =
                    searchText.isEmpty()
                            || searchableText.contains(searchText);

            if (!categoryMatches || !searchMatches) {
                continue;
            }

            productGrid.getChildren().add(
                    createProductCard(makeup)
            );

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

            Label emptyLabel = new Label(
                    "No makeup found. Try another search."
            );

            emptyLabel.setStyle(
                    "-fx-text-fill: #A78495;" +
                            "-fx-font-size: 15px;" +
                            "-fx-padding: 30;"
            );

            productGrid.getChildren().add(emptyLabel);
        }

        updateFilterButtons();
        updateCartCount();
    }

    private VBox createProductCard(Makeup product) {

        boolean recommended =
                product.getRecommendedSkinTone()
                        .equalsIgnoreCase(selectedSkinTone);

        VBox card = new VBox(9);

        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(14));

        card.setPrefWidth(225);
        card.setMinWidth(225);
        card.setMaxWidth(225);

        card.setPrefHeight(455);
        card.setMinHeight(455);
        card.setMaxHeight(455);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " +
                        (recommended ? "#D97DA5" : "#F2DAE4") + ";" +
                        "-fx-border-width: " +
                        (recommended ? "2" : "1") + ";" +
                        "-fx-border-radius: 18;"
        );

        Label recommendation = new Label(
                recommended ? "♥ RECOMMENDED FOR YOU" : " "
        );

        recommendation.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #C45D89;"
        );

        StackPane imageArea = new StackPane();

        imageArea.setPrefSize(190, 220);
        imageArea.setMinSize(190, 220);
        imageArea.setMaxSize(190, 220);

        imageArea.setStyle(
                "-fx-background-color: #FFF5F9;" +
                        "-fx-background-radius: 14;"
        );

        imageArea.getChildren().add(
                createIllustration(product)
        );

        Label categoryLabel = new Label(
                product.getCategory().toUpperCase()
        );

        categoryLabel.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-text-fill: #B67B91;" +
                        "-fx-font-weight: bold;"
        );

        Label name = new Label(product.getName());
        name.setWrapText(true);

        name.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #563C49;"
        );

        Circle color = new Circle(
                7,
                Color.web(product.getShadeColor())
        );

        color.setStroke(Color.web("#E4C7D3"));

        Label shade = new Label(
                "Shade: " + product.getShade()
        );

        shade.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #846A76;"
        );

        HBox shadeRow = new HBox(7, color, shade);
        shadeRow.setAlignment(Pos.CENTER);

        Label price = new Label(
                String.format("₱%,.2f", product.getPrice())
        );

        price.setStyle(
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

        addButton.setOnAction(e -> addToCart(product));

        card.getChildren().addAll(
                recommendation,
                imageArea,
                categoryLabel,
                name,
                shadeRow,
                price,
                spacer,
                addButton
        );

        return card;
    }

    private Node createIllustration(Makeup product) {

        switch (product.getCategory()) {

            case "Foundation":

                FoundationPreview foundation =
                        new FoundationPreview();

                foundation.setShadeColor(product.getShadeColor());

                return foundation;

            case "Blush":

                BlushPreview blush = new BlushPreview();

                blush.setShadeColor(product.getShadeColor());

                return blush;

            case "Lipstick":

                LipstickPreview lipstick =
                        new LipstickPreview();

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

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Blush & Bloom");

        if (productGrid != null
                && productGrid.getScene() != null) {

            dialog.initOwner(
                    productGrid.getScene().getWindow()
            );
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

        pane.getButtonTypes().addAll(
                continueType,
                cartType
        );

        pane.setStyle(
                "-fx-background-color: #FFF8FB;"
        );

        StackPane icon = new StackPane();

        icon.setMinSize(75, 75);
        icon.setMaxSize(75, 75);

        icon.setStyle(
                "-fx-background-color: #F9D5E5;" +
                        "-fx-background-radius: 100;"
        );

        Label check = new Label("✓");

        check.setStyle(
                "-fx-font-size: 42px;" +
                        "-fx-text-fill: #C65D8D;" +
                        "-fx-font-weight: bold;"
        );

        icon.getChildren().add(check);

        Label title = new Label("Added to Your Cart!");

        title.setStyle(
                "-fx-font-size: 23px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #A65378;"
        );

        Label subtitle = new Label(
                "A little beauty is on its way to you."
        );

        subtitle.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #A27A8D;"
        );

        Label name = new Label(product.getName());

        name.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #694657;"
        );

        Circle swatch = new Circle(
                12,
                Color.web(product.getShadeColor())
        );

        Label shade = new Label(
                "Shade: " + product.getShade()
        );

        shade.setStyle("-fx-text-fill: #967487;");

        HBox shadeRow = new HBox(
                9,
                swatch,
                shade
        );

        shadeRow.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(
                String.format("₱%,.2f", product.getPrice())
        );

        price.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #C45D89;"
        );

        VBox details = new VBox(
                12,
                name,
                shadeRow,
                price
        );

        details.setAlignment(Pos.CENTER_LEFT);

        Node illustration = createIllustration(product);

        illustration.setScaleX(0.6);
        illustration.setScaleY(0.6);

        StackPane preview = new StackPane(illustration);

        preview.setMinSize(120, 145);
        preview.setPrefSize(120, 145);
        preview.setMaxSize(120, 145);

        preview.setStyle(
                "-fx-background-color: #FFF1F7;" +
                        "-fx-background-radius: 12;"
        );

        HBox productRow = new HBox(
                16,
                preview,
                details
        );

        productRow.setAlignment(Pos.CENTER_LEFT);
        productRow.setPadding(new Insets(14));

        productRow.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #F3DCE6;" +
                        "-fx-border-radius: 15;"
        );

        Label count = new Label(
                ShoppingCart.getTotalQuantity() +
                        " item(s) in your cart"
        );

        count.setStyle("-fx-text-fill: #A27A8D;");

        VBox content = new VBox(
                14,
                icon,
                title,
                subtitle,
                productRow,
                count
        );

        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(25));
        content.setPrefWidth(440);

        content.setStyle(
                "-fx-background-color: #FFF8FB;"
        );

        pane.setContent(content);

        Button continueButton =
                (Button) pane.lookupButton(continueType);

        continueButton.setStyle(
                "-fx-background-color: #FCE3ED;" +
                        "-fx-text-fill: #A65378;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 11 17;"
        );

        Button cartButton =
                (Button) pane.lookupButton(cartType);

        cartButton.setStyle(
                "-fx-background-color: #D97DA5;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 11 17;"
        );

        Optional<ButtonType> selected =
                dialog.showAndWait();

        updateCartCount();

        if (selected.isPresent()
                && selected.get() == cartType) {
            handleCart();
        }
    }

    private void showWarning(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        if (productGrid != null
                && productGrid.getScene() != null) {

            alert.initOwner(
                    productGrid.getScene().getWindow()
            );
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

        int quantity = 0;

        if (ShoppingCart.isLoggedIn()) {
            quantity = ShoppingCart.getTotalQuantity();
        }

        cartCountButton.setText(
                "My Cart (" + quantity + ")"
        );
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

        if (productGrid == null
                || productGrid.getScene() == null) {
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
