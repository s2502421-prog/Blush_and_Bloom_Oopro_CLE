package com.example.bandb.controller;

import com.example.bandb.app.SceneManager;
import com.example.bandb.model.Makeup;
import com.example.bandb.model.Product;
import com.example.bandb.service.ProductData;
import com.example.bandb.service.Session;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * Admin-only screen: full CRUD on the product ArrayList.
 *
 *  Create -> handleAdd      Read   -> TableView + search + sort
 *  Update -> handleUpdate   Delete -> handleDelete
 *
 * The controller never edits fields directly: it builds model objects
 * and calls ProductData, so the validation stays in the model classes.
 * This screen shows products only - no customer data is reachable here.
 */
public class InventoryController {

    @FXML private TableView<Product> productTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Label summaryLabel;

    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField nameField;
    @FXML private TextField brandField;
    @FXML private TextField shadeField;
    @FXML private TextField colorField;
    @FXML private ComboBox<String> toneCombo;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private Label formTitle;

    private Product selected;

    @FXML
    public void initialize() {

        if (!Session.isAdmin()) {
            // Not allowed here: go back once this screen has finished loading.
            Platform.runLater(() -> {
                alert(Alert.AlertType.WARNING, "Access Denied",
                        "Only the shop administrator can manage inventory.");
                SceneManager.switchScene("home.fxml");
            });
            return;
        }

        categoryCombo.getItems().addAll("Foundation", "Blush", "Lipstick");

        toneCombo.getItems().addAll(
                "Fair", "Light", "Medium", "Tan", "Deep", "Rich Deep");

        sortCombo.getItems().addAll(
                "Name (A-Z)",
                "Category",
                "Price (low to high)",
                "Price (high to low)",
                "Stock (low to high)");
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.setOnAction(event -> refreshTable());

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> refreshTable());

        setupColumns();

        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, product) -> {
                    if (product != null) {
                        fillForm(product);
                    }
                });

        refreshTable();
    }

    // ------------------------------------------------------------ table

    private void setupColumns() {
        addColumn("ID", 50, p -> String.valueOf(p.getId()));
        addColumn("Category", 100, Product::getCategory);
        addColumn("Name", 160, Product::getName);
        addColumn("Shade", 110, p -> p instanceof Makeup
                ? ((Makeup) p).getShade() : "");
        addColumn("Brand", 110, p -> p instanceof Makeup
                ? ((Makeup) p).getBrand() : "");
        addColumn("Skin tone", 90, p -> p instanceof Makeup
                ? ((Makeup) p).getRecommendedSkinTone() : "");
        // Polymorphism: each subclass answers getFinish() differently.
        addColumn("Finish", 80, p -> p instanceof Makeup
                ? ((Makeup) p).getFinish() : "");
        addColumn("Price", 90, p -> String.format("₱%,.2f", p.getPrice()));
        addColumn("Stock", 60, p -> String.valueOf(p.getStock()));
    }

    private void addColumn(
            String title, double width, Function<Product, String> getter
    ) {
        TableColumn<Product, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell ->
                new SimpleStringProperty(getter.apply(cell.getValue())));
        productTable.getColumns().add(column);
    }

    private Comparator<Product> currentSort() {
        String choice = sortCombo.getValue() == null
                ? "Name (A-Z)" : sortCombo.getValue();

        switch (choice) {
            case "Category":
                return Comparator.comparing(Product::getCategory)
                        .thenComparing(Product::getName);
            case "Price (low to high)":
                return Comparator.comparingDouble(Product::getPrice);
            case "Price (high to low)":
                return Comparator.comparingDouble(Product::getPrice)
                        .reversed();
            case "Stock (low to high)":
                return Comparator.comparingInt(Product::getStock);
            default:
                return Comparator.comparing(
                        (Product p) -> p.getName().toLowerCase(Locale.ROOT));
        }
    }

    private void refreshTable() {
        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        ArrayList<Product> all = ProductData.getAllProducts();
        ArrayList<Product> shown = new ArrayList<>();

        int totalStock = 0;
        double stockValue = 0;

        for (Product product : all) {
            totalStock += product.getStock();
            stockValue += product.getStock() * product.getPrice();

            String text = product.getName() + " " + product.getCategory();

            if (product instanceof Makeup) {
                Makeup makeup = (Makeup) product;
                text += " " + makeup.getShade() + " " + makeup.getBrand()
                        + " " + makeup.getRecommendedSkinTone();
            }

            if (text.toLowerCase(Locale.ROOT).contains(query)) {
                shown.add(product);
            }
        }

        shown.sort(currentSort());
        productTable.getItems().setAll(shown);

        summaryLabel.setText(String.format(
                "%d products  •  %d units in stock  •  stock value ₱%,.2f",
                all.size(), totalStock, stockValue));
    }

    // ------------------------------------------------------------- form

    private void fillForm(Product product) {
        if (!(product instanceof Makeup)) {
            return;
        }

        Makeup makeup = (Makeup) product;
        selected = makeup;

        formTitle.setText("Editing: " + makeup.getName()
                + " (" + makeup.getShade() + ")");
        categoryCombo.setValue(makeup.getCategory());
        categoryCombo.setDisable(true);
        nameField.setText(makeup.getName());
        brandField.setText(makeup.getBrand());
        shadeField.setText(makeup.getShade());
        colorField.setText(makeup.getShadeColor());
        toneCombo.setValue(makeup.getRecommendedSkinTone());
        priceField.setText(String.valueOf(makeup.getPrice()));
        stockField.setText(String.valueOf(makeup.getStock()));
    }

    @FXML
    private void handleClear() {
        selected = null;
        productTable.getSelectionModel().clearSelection();

        formTitle.setText("Add a new product");
        categoryCombo.setDisable(false);
        categoryCombo.setValue(null);
        toneCombo.setValue(null);
        nameField.clear();
        brandField.clear();
        shadeField.clear();
        colorField.clear();
        priceField.clear();
        stockField.clear();
    }

    private double readPrice() {
        try {
            return Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Price must be a number, for example 299.50.");
        }
    }

    private int readStock() {
        try {
            return Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Stock must be a whole number, for example 20.");
        }
    }

    // ------------------------------------------------------------- CRUD

    @FXML
    private void handleAdd() {
        try {
            Makeup product = ProductData.create(
                    categoryCombo.getValue(),
                    ProductData.nextId(),
                    nameField.getText(),
                    readPrice(),
                    readStock(),
                    brandField.getText(),
                    shadeField.getText(),
                    colorField.getText(),
                    toneCombo.getValue());

            ProductData.addProduct(product);

            alert(Alert.AlertType.INFORMATION, "Product Added",
                    product.getName() + " (" + product.getShade()
                            + ") was added.");

            handleClear();
            refreshTable();

        } catch (RuntimeException exception) {
            alert(Alert.AlertType.WARNING, "Please Check Your Input",
                    exception.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "No Product Selected",
                    "Click a product in the table first, then edit it.");
            return;
        }

        try {
            ProductData.updateMakeup(
                    selected.getId(),
                    nameField.getText(),
                    readPrice(),
                    readStock(),
                    brandField.getText(),
                    shadeField.getText(),
                    colorField.getText(),
                    toneCombo.getValue());

            alert(Alert.AlertType.INFORMATION, "Product Updated",
                    "Your changes were saved.");

            handleClear();
            refreshTable();

        } catch (RuntimeException exception) {
            alert(Alert.AlertType.WARNING, "Please Check Your Input",
                    exception.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "No Product Selected",
                    "Click a product in the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + selected.getName() + "? "
                        + "This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.initOwner(productTable.getScene().getWindow());
        confirm.setTitle("Blush & Bloom");
        confirm.setHeaderText("Delete product");

        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            try {
                ProductData.deleteProduct(selected.getId());
                handleClear();
                refreshTable();

            } catch (RuntimeException exception) {
                alert(Alert.AlertType.ERROR, "Unable to Delete",
                        exception.getMessage());
            }
        }
    }

    // ------------------------------------------------------ navigation

    @FXML
    private void handleHome() {
        SceneManager.switchScene("home.fxml");
    }

    @FXML
    private void handleShop() {
        SceneManager.switchScene("products.fxml");
    }

    @FXML
    private void handleLogout() {
        Session.end();
        SceneManager.switchScene("login.fxml");
    }

    private void alert(Alert.AlertType type, String header, String message) {
        Alert alert = new Alert(type);

        if (productTable != null && productTable.getScene() != null) {
            alert.initOwner(productTable.getScene().getWindow());
        }

        alert.setTitle("Blush & Bloom");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
