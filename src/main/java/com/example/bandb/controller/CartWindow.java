package com.example.bandb.controller;

import com.example.bandb.app.SceneManager;
import com.example.bandb.service.ShoppingCart;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Optional;
import java.util.Map;
import java.util.Objects;

public class CartWindow {

    private final Stage stage = new Stage();
    private final Random random = new Random();

    @FXML
    private StackPane root;

    @FXML
    private VBox itemList;

    @FXML
    private Label countLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label messageLabel;

    @FXML private Button continueButton;
    @FXML private Button historyButton;
    @FXML private Button checkoutButton;

    private StackPane overlay;
    private AnimationTimer animation;
    private boolean orderPlaced;

    public static void show(Window owner) {
        new CartWindow(owner).stage.showAndWait();
    }

    public static boolean showForOrder(Window owner) {
        CartWindow window = new CartWindow(owner);
        window.stage.showAndWait();
        return window.orderPlaced;
    }

    private CartWindow(Window owner) {
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }

        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.resource("cart.fxml"));
            loader.setController(this);
            this.root = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load cart.fxml", exception);
        }

        stage.setTitle("Blush & Bloom - My Cart");
        stage.setMinWidth(570);
        stage.setMinHeight(480);

        // FXML @FXML injection works before CSS is applied; Node.lookup() may not.
        continueButton.setOnAction(e -> stage.close());
        historyButton.setOnAction(e -> showOrderHistory());
        checkoutButton.setOnAction(e -> showCheckout());

        stage.setScene(new Scene(this.root, 650, 635));
        stage.setOnHidden(e -> stopAnimation());

        refreshCart();
    }

    /** Keep the FXML namespace so fx:id controls can be retrieved reliably. */
    private static final class View {
        final Parent root;
        final Map<String, Object> controls;
        View(Parent root, Map<String, Object> controls) {
            this.root = root;
            this.controls = controls;
        }
        Object get(String fxId) {
            return Objects.requireNonNull(controls.get(fxId),
                    "Missing fx:id '" + fxId + "' in loaded FXML");
        }
    }

    private static View loadView(String filename) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.resource(filename));
            Parent root = loader.load();
            return new View(root, loader.getNamespace());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + filename, exception);
        }
    }

    private Label label(
            String text, int size, String color, boolean bold
    ) {
        Label result = new Label(text);
        result.setWrapText(true);

        result.setStyle(
                "-fx-font-size: " + size + "px;" +
                        "-fx-text-fill: " + color + ";" +
                        (bold ? "-fx-font-weight: bold;" : "")
        );

        return result;
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private void refreshCart() {
        itemList.getChildren().clear();

        if (ShoppingCart.isEmpty()) {
            itemList.getChildren().add(loadView("empty-cart.fxml").root);

        } else {
            for (ShoppingCart.CartItem item : ShoppingCart.getItems()) {
                itemList.getChildren().add(createRow(item));
            }
        }

        updateTotals();
        checkoutButton.setDisable(ShoppingCart.isEmpty());
    }

    private void updateTotals() {
        int count = ShoppingCart.getTotalQuantity();
        countLabel.setText(count + (count == 1 ? " item" : " items"));

        totalLabel.setText(
                String.format("₱%,.2f", ShoppingCart.getTotalPrice())
        );
    }

    private HBox createRow(ShoppingCart.CartItem item) {
        View view = loadView("cart-item.fxml");
        HBox row = (HBox) view.root;

        Label swatch = (Label) view.get("itemSwatch");
        Label name = (Label) view.get("itemName");
        Label shade = (Label) view.get("itemShade");
        Label subtotal = (Label) view.get("itemSubtotal");
        Label quantity = (Label) view.get("itemQuantity");
        Label limit = (Label) view.get("itemLimit");

        Button minus = (Button) view.get("itemMinus");
        Button plus = (Button) view.get("itemPlus");
        Button remove = (Button) view.get("itemRemove");

        swatch.setStyle(
                "-fx-font-size: 54px; -fx-text-fill: " +
                        shadeColor(item.getShade()) + ";"
        );

        name.setText(item.getProductName());
        shade.setText("Shade: " + item.getShade());

        Runnable updateRow = () -> {
            quantity.setText(
                    String.valueOf(item.getQuantity())
            );

            subtotal.setText(
                    String.format("₱%,.2f", item.getSubtotal())
            );

            boolean reached =
                    item.getQuantity() >= item.getMaxAllowed();

            plus.setDisable(reached);
            limit.setVisible(reached);
            limit.setManaged(reached);
            updateTotals();
        };

        minus.setOnAction(e -> {
            try {
                if (item.getQuantity() == 1) {
                    // Ehrica's confirmation design: never remove accidentally.
                    if (confirmRemove(item)) {
                        ShoppingCart.removeItem(item);
                        refreshCart();
                    }

                } else if (ShoppingCart.updateQuantity(
                        item, item.getQuantity() - 1
                )) {
                    updateRow.run();
                }

                hideMessage();

            } catch (RuntimeException exception) {
                showMessage(
                        "Unable to update cart: " +
                                exception.getMessage()
                );
            }
        });

        plus.setOnAction(e -> {
            try {
                if (ShoppingCart.updateQuantity(
                        item, item.getQuantity() + 1
                )) {
                    updateRow.run();
                    hideMessage();
                }

            } catch (RuntimeException exception) {
                showMessage(
                        "Unable to update cart: " +
                                exception.getMessage()
                );
            }
        });

        remove.setOnAction(e -> {
            if (!confirmRemove(item)) {
                return;
            }
            try {
                ShoppingCart.removeItem(item);
                refreshCart();
                hideMessage();

            } catch (RuntimeException exception) {
                showMessage(
                        "Unable to remove item: " +
                                exception.getMessage()
                );
            }
        });

        updateRow.run();
        return row;
    }

    /** Adapted from Ehrica's CartController.confirmRemove() contribution. */
    private boolean confirmRemove(ShoppingCart.CartItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove " + item.getProductName() + " from your cart?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Blush & Bloom");
        alert.setHeaderText(null);
        alert.initOwner(stage);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private String shadeColor(String shade) {
        switch (shade) {
            case "Porcelain": return "#F4D9C9";
            case "Ivory": return "#E8C3A6";
            case "Natural Beige": return "#C8956D";
            case "Warm Honey": return "#B77747";
            case "Cocoa": return "#805037";
            case "Espresso": return "#4E3028";

            case "Baby Pink": return "#F5A9C0";
            case "Peach Pink": return "#F5A38E";
            case "Coral": return "#ED846D";
            case "Terracotta": return "#B96750";
            case "Berry": return "#A44266";
            case "Plum": return "#753C65";

            case "Soft Rose": return "#C97987";
            case "Pink Nude": return "#B76E78";
            case "Rosewood": return "#96535B";
            case "Caramel Nude": return "#8E4E3C";
            case "Burgundy": return "#70283E";
            case "Deep Wine": return "#512438";

            default: return "#E9A6C0";
        }
    }

    private void showOrderHistory() {
        if (overlay != null) {
            return;
        }

        List<String> orders;

        try {
            orders = ShoppingCart.getOrderHistory();

        } catch (RuntimeException exception) {
            showMessage(
                    "Unable to load order history: " +
                            exception.getMessage()
            );
            return;
        }

        hideMessage();

        View view = loadView("order-history.fxml");
        VBox content = (VBox) view.root;
        VBox list = (VBox) view.get("historyItems");

        if (orders.isEmpty()) {
            list.getChildren().add(loadView("empty-history.fxml").root);

        } else {
            for (String order : orders) {
                View entry = loadView("history-entry.fxml");
                ((Label) entry.get("historyEntry")).setText(order);
                list.getChildren().add(entry.root);
            }
        }

        ((Button) view.get("historyBackButton"))
                .setOnAction(e -> hideOverlay());

        overlay = new StackPane(content);
        overlay.setPadding(new Insets(15));

        overlay.setStyle(
                "-fx-background-color: rgba(75,35,55,0.48);"
        );

        root.getChildren().add(overlay);
    }

    private void showCheckout() {
        if (overlay != null) {
            return;
        }

        if (ShoppingCart.isEmpty()) {
            showMessage("Please add products before checkout.");
            return;
        }

        hideMessage();

        View view = loadView("checkout.fxml");
        StackPane panel = (StackPane) view.root;

        Pane petals = (Pane) view.get("petalsLayer");
        Label title = (Label) view.get("checkoutTitle");
        Label description =
                (Label) view.get("checkoutDescription");
        Label amount = (Label) view.get("checkoutAmount");
        Label count = (Label) view.get("checkoutCount");
        Label error = (Label) view.get("checkoutError");

        Button confirm =
                (Button) view.get("placeOrderButton");
        Button back =
                (Button) view.get("checkoutBackButton");

        amount.setText(
                String.format("₱%,.2f", ShoppingCart.getTotalPrice())
        );

        count.setText(
                ShoppingCart.getTotalQuantity() + " item(s)"
        );

        Rectangle roundedClip = new Rectangle();

        roundedClip.widthProperty().bind(
                panel.widthProperty()
        );

        roundedClip.heightProperty().bind(
                panel.heightProperty()
        );

        roundedClip.setArcWidth(40);
        roundedClip.setArcHeight(40);
        panel.setClip(roundedClip);

        overlay = new StackPane(panel);
        overlay.setPadding(new Insets(20));

        overlay.setStyle(
                "-fx-background-color: rgba(75,35,55,0.48);"
        );

        root.getChildren().add(overlay);

        back.setOnAction(e -> hideOverlay());

        confirm.setOnAction(e -> {
            confirm.setDisable(true);

            int ordered = ShoppingCart.getTotalQuantity();
            double total = ShoppingCart.getTotalPrice();

            try {
                ShoppingCart.placeOrder();

                orderPlaced = true;
                refreshCart();

                title.setText("Order Placed Successfully!");

                description.setText(
                        "Thank you for shopping with Blush & Bloom!\n" +
                                "Your purchase is saved in Order History."
                );

                amount.setText(String.format("₱%,.2f", total));
                count.setText(ordered + " item(s) ordered");

                confirm.setVisible(false);
                confirm.setManaged(false);

                back.setText("Continue Shopping");

                back.setOnAction(event -> {
                    hideOverlay();
                    stage.close();
                });

            } catch (RuntimeException exception) {
                error.setText(
                        "Unable to complete order: " +
                                exception.getMessage()
                );

                error.setVisible(true);
                error.setManaged(true);
                confirm.setDisable(false);
            }
        });

        startPetals(petals);
    }

    private void hideOverlay() {
        stopAnimation();

        if (overlay != null) {
            root.getChildren().remove(overlay);
            overlay = null;
        }
    }

    private void startPetals(Pane layer) {
        stopAnimation();

        List<Ellipse> shapes = new ArrayList<>();
        List<double[]> motions = new ArrayList<>();

        String[] colors = {
                "#F1A7C5",
                "#E9B3CA",
                "#D987AC",
                "#F6CDDC",
                "#E7A0BD"
        };

        for (int i = 0; i < 28; i++) {
            Ellipse petal = new Ellipse(
                    3 + random.nextDouble() * 3,
                    6 + random.nextDouble() * 6
            );

            petal.setFill(
                    Color.web(
                            colors[random.nextInt(colors.length)],
                            0.35 + random.nextDouble() * 0.3
                    )
            );

            double x = 10 + random.nextDouble() * 450;
            double y = random.nextDouble() * 520;

            petal.setLayoutX(x);
            petal.setLayoutY(y);

            layer.getChildren().add(petal);
            shapes.add(petal);

            motions.add(new double[]{
                    x,
                    y,
                    22 + random.nextDouble() * 32,
                    random.nextDouble() * Math.PI * 2
            });
        }

        animation = new AnimationTimer() {
            private long previous;

            @Override
            public void handle(long now) {
                if (previous == 0) {
                    previous = now;
                    return;
                }

                double dt = Math.min(
                        (now - previous) / 1_000_000_000.0,
                        0.05
                );

                previous = now;

                for (int i = 0; i < shapes.size(); i++) {
                    double[] motion = motions.get(i);

                    motion[1] += motion[2] * dt;
                    motion[3] += dt;

                    if (motion[1] > 540) {
                        motion[1] = -20;
                        motion[0] =
                                10 + random.nextDouble() * 450;
                    }

                    Ellipse petal = shapes.get(i);

                    petal.setLayoutX(
                            motion[0] +
                                    Math.sin(motion[3]) * 12
                    );

                    petal.setLayoutY(motion[1]);

                    petal.setRotate(
                            petal.getRotate() + 25 * dt
                    );
                }
            }
        };

        animation.start();
    }

    private void stopAnimation() {
        if (animation != null) {
            animation.stop();
            animation = null;
        }
    }
}