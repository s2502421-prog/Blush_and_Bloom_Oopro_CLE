
package com.example.bandb;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CartWindow {

    private final Stage stage = new Stage();
    private final StackPane root = new StackPane();
    private final VBox itemList = new VBox(12);
    private final Label countLabel = new Label();
    private final Label totalLabel = new Label();
    private final Label messageLabel = new Label();
    private final Random random = new Random();

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

        stage.setTitle("Blush & Bloom - My Cart");
        stage.setMinWidth(570);
        stage.setMinHeight(480);

        VBox header = new VBox(7,
                label("BLUSH & BLOOM", 11, "#BD6B91", true),
                label("My Shopping Cart", 27, "#994D72", true),
                label("Your favorite beauty finds, all in one place.", 13, "#A78495", false)
        );
        header.setPadding(new Insets(25, 28, 16, 28));

        ScrollPane scrollPane = new ScrollPane(itemList);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: #FFF8FB; -fx-background-color: transparent;");
        itemList.setPadding(new Insets(12, 28, 15, 28));

        messageLabel.setWrapText(true);
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        messageLabel.setStyle(
                "-fx-background-color: #FCE4ED; -fx-text-fill: #B94F7A;" +
                        "-fx-background-radius: 10; -fx-padding: 10;"
        );
        countLabel.setStyle("-fx-text-fill: #9B7688; -fx-font-size: 13px;");
        totalLabel.setStyle("-fx-text-fill: #AC527E; -fx-font-size: 24px; -fx-font-weight: bold;");

        VBox totals = new VBox(5,
                label("Subtotal", 15, "#7A5266", true), totalLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox summary = new HBox(14, countLabel, spacer, totals);
        summary.setAlignment(Pos.CENTER_LEFT);

        Button continueButton = button("Continue Shopping", "#FBE1EC", "#A65378");
        continueButton.setOnAction(e -> stage.close());
        Button historyButton = button("Order History", "#FBE1EC", "#A65378");
        historyButton.setOnAction(e -> showOrderHistory());
        Button checkoutButton = button("Checkout", "#D97DA5", "white");
        checkoutButton.setOnAction(e -> showCheckout());

        HBox buttons = new HBox(10, continueButton, historyButton, checkoutButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        VBox footer = new VBox(15, messageLabel, summary, buttons);
        footer.setPadding(new Insets(20, 28, 25, 28));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #F4DCE7; -fx-border-width: 1 0 0 0;");

        VBox page = new VBox(header, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        page.setStyle("-fx-background-color: #FFF8FB;");
        root.getChildren().add(page);
        stage.setScene(new Scene(root, 650, 635));
        stage.setOnHidden(e -> stopAnimation());
        refreshCart();
    }

    private Label label(String value, int size, String color, boolean bold) {
        Label result = new Label(value);
        result.setWrapText(true);
        result.setStyle("-fx-font-size: " + size + "px; -fx-text-fill: " + color + ";" +
                (bold ? "-fx-font-weight: bold;" : ""));
        return result;
    }

    private Button button(String value, String background, String foreground) {
        Button result = new Button(value);
        result.setStyle("-fx-background-color: " + background + ";" +
                "-fx-text-fill: " + foreground + ";" +
                "-fx-background-radius: 11; -fx-font-weight: bold;" +
                "-fx-padding: 12 17; -fx-cursor: hand;");
        return result;
    }

    private Button quantityButton(boolean plus) {
        Button result = new Button();
        Pane graphic = new Pane();
        graphic.setMinSize(18, 18);
        graphic.setPrefSize(18, 18);
        graphic.setMaxSize(18, 18);
        Line horizontal = new Line(2, 9, 16, 9);
        horizontal.setStroke(Color.web("#A65378"));
        horizontal.setStrokeWidth(2.5);
        graphic.getChildren().add(horizontal);
        if (plus) {
            Line vertical = new Line(9, 2, 9, 16);
            vertical.setStroke(Color.web("#A65378"));
            vertical.setStrokeWidth(2.5);
            graphic.getChildren().add(vertical);
        }
        result.setGraphic(graphic);
        result.setMinSize(36, 36);
        result.setPrefSize(36, 36);
        result.setMaxSize(36, 36);
        result.setStyle("-fx-background-color: #FBE4ED; -fx-background-radius: 9; -fx-padding: 0; -fx-cursor: hand;");
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
            VBox empty = new VBox(17,
                    label("♡", 65, "#CE78A0", false),
                    label("Your cart is waiting to bloom!", 21, "#A65378", true),
                    label("Add your favorite makeup shades to get started.", 13, "#A78495", false)
            );
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(65, 15, 65, 15));
            itemList.getChildren().add(empty);
        } else {
            for (ShoppingCart.CartItem item : ShoppingCart.getItems()) {
                itemList.getChildren().add(createRow(item));
            }
        }
        updateTotals();
    }

    private void updateTotals() {
        countLabel.setText(ShoppingCart.getTotalQuantity() + " item(s)");
        totalLabel.setText(String.format("₱%,.2f", ShoppingCart.getTotalPrice()));
    }

    private HBox createRow(ShoppingCart.CartItem item) {
        Circle swatch = new Circle(30, Color.web(shadeColor(item.getShade())));
        swatch.setStroke(Color.web("#E8C7D6"));
        StackPane preview = new StackPane(swatch);
        preview.setMinSize(90, 100);
        preview.setMaxSize(90, 100);
        preview.setStyle("-fx-background-color: #FFF0F6; -fx-background-radius: 12;");

        Label name = label(item.getProductName(), 16, "#674457", true);
        Label shade = label("Shade: " + item.getShade(), 12, "#987A88", false);
        Label subtotal = label(String.format("₱%,.2f", item.getSubtotal()), 17, "#C45D89", true);
        Label quantity = label(String.valueOf(item.getQuantity()), 14, "#674457", true);
        quantity.setMinWidth(27);
        quantity.setAlignment(Pos.CENTER);
        Label limit = label("Maximum 20 per order", 11, "#BA5B82", true);
        limit.setVisible(false);
        limit.setManaged(false);

        Button minus = quantityButton(false);
        Button plus = quantityButton(true);
        Runnable updateLimit = () -> {
            boolean reached = item.getQuantity() >= ShoppingCart.MAX_QUANTITY;
            plus.setDisable(reached);
            plus.setOpacity(reached ? 0.5 : 1);
            limit.setVisible(reached);
            limit.setManaged(reached);
        };
        Runnable updateRow = () -> {
            quantity.setText(String.valueOf(item.getQuantity()));
            subtotal.setText(String.format("₱%,.2f", item.getSubtotal()));
            updateTotals();
            updateLimit.run();
        };

        minus.setOnAction(e -> {
            try {
                if (item.getQuantity() == 1) {
                    ShoppingCart.removeItem(item);
                    refreshCart();
                } else if (ShoppingCart.updateQuantity(item, item.getQuantity() - 1)) {
                    updateRow.run();
                }
                hideMessage();
            } catch (RuntimeException ex) {
                showMessage("Unable to update cart: " + ex.getMessage());
            }
        });
        plus.setOnAction(e -> {
            try {
                if (ShoppingCart.updateQuantity(item, item.getQuantity() + 1)) {
                    updateRow.run();
                    hideMessage();
                }
            } catch (RuntimeException ex) {
                showMessage("Unable to update cart: " + ex.getMessage());
            }
        });

        HBox quantityBox = new HBox(8, minus, quantity, plus);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        Button remove = new Button("Remove");
        remove.setStyle("-fx-background-color: transparent; -fx-text-fill: #B86788; -fx-underline: true; -fx-cursor: hand;");
        remove.setOnAction(e -> {
            try {
                ShoppingCart.removeItem(item);
                refreshCart();
                hideMessage();
            } catch (RuntimeException ex) {
                showMessage("Unable to remove item: " + ex.getMessage());
            }
        });

        HBox actions = new HBox(12, quantityBox, remove);
        actions.setAlignment(Pos.CENTER_LEFT);
        VBox details = new VBox(8, name, shade, subtotal, actions, limit);
        details.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, Priority.ALWAYS);
        HBox row = new HBox(17, preview, details);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setMinHeight(145);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 16;" +
                "-fx-border-color: #F2DCE5; -fx-border-radius: 16;");
        updateLimit.run();
        return row;
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
            showMessage("Unable to load order history: " + exception.getMessage());
            return;
        }
        hideMessage();
        VBox list = new VBox(13);
        list.setPadding(new Insets(10));
        if (orders.isEmpty()) {
            Label empty = label("No completed orders yet.\nYour past purchases will appear here.",
                    15, "#9B7588", false);
            empty.setTextAlignment(TextAlignment.CENTER);
            empty.setAlignment(Pos.CENTER);
            empty.setMaxWidth(Double.MAX_VALUE);
            list.getChildren().add(empty);
        } else {
            for (String order : orders) {
                Label details = label(order, 13, "#7B5268", false);
                details.setMaxWidth(Double.MAX_VALUE);
                details.setPadding(new Insets(17));
                details.setStyle("-fx-background-color: #FCE9F1; -fx-background-radius: 14;" +
                        "-fx-text-fill: #7B5268; -fx-font-size: 13px;");
                list.getChildren().add(details);
            }
        }

        ScrollPane scrolling = new ScrollPane(list);
        scrolling.setFitToWidth(true);
        scrolling.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrolling.setPrefViewportHeight(375);
        scrolling.setStyle("-fx-background: #FFF8FB; -fx-background-color: transparent;");
        VBox.setVgrow(scrolling, Priority.ALWAYS);

        Button close = button("Back to My Cart", "#D97DA5", "white");
        close.setMaxWidth(Double.MAX_VALUE);
        close.setOnAction(e -> hideOverlay());
        VBox content = new VBox(15,
                label("♡  My Order History", 25, "#994D72", true),
                label("Your previous Blush & Bloom purchases", 12, "#A78495", false),
                scrolling,
                close
        );
        content.setPadding(new Insets(23));
        content.setPrefSize(490, 525);
        content.setMaxSize(490, 525);
        content.setStyle("-fx-background-color: #FFF8FB; -fx-background-radius: 20;");

        overlay = new StackPane(content);
        overlay.setPadding(new Insets(15));
        overlay.setStyle("-fx-background-color: rgba(75,35,55,0.48);");
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

        Pane petals = new Pane();
        petals.setMouseTransparent(true);
        petals.setPickOnBounds(false);
        petals.setMinSize(470, 520);
        petals.setPrefSize(470, 520);
        petals.setMaxSize(470, 520);

        Label title = label("Ready to Bloom?", 25, "#994D72", true);
        Label description = label(
                "Confirm your order to continue shopping.\n" +
                        "You can order the same makeup again anytime.",
                13, "#9C7A8B", false
        );
        description.setTextAlignment(TextAlignment.CENTER);
        description.setAlignment(Pos.CENTER);
        Label amount = label(String.format("₱%,.2f", ShoppingCart.getTotalPrice()),
                29, "#C45D89", true);
        Label count = label(ShoppingCart.getTotalQuantity() + " item(s)",
                12, "#A78495", false);
        VBox totalBox = new VBox(8,
                label("YOUR ORDER TOTAL", 11, "#A78495", true), amount, count);
        totalBox.setAlignment(Pos.CENTER);
        totalBox.setPadding(new Insets(20));
        totalBox.setMaxWidth(Double.MAX_VALUE);
        totalBox.setStyle("-fx-background-color: #FCE9F1; -fx-background-radius: 15;");

        Label error = label("", 12, "#B94F7A", true);
        error.setVisible(false);
        error.setManaged(false);
        error.setAlignment(Pos.CENTER);
        error.setTextAlignment(TextAlignment.CENTER);
        Button confirm = button("Place Order", "#D97DA5", "white");
        confirm.setMaxWidth(Double.MAX_VALUE);
        Button back = button("Back to My Cart", "#FBE1EC", "#A65378");
        back.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(17,
                label("♥", 44, "#C45D89", false), title, description,
                totalBox, error, confirm, back
        );
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(25));
        content.setMinWidth(440);
        content.setPrefWidth(440);
        content.setMaxWidth(440);
        content.setStyle("-fx-background-color: transparent;");

        StackPane panel = new StackPane(petals, content);
        panel.setMinSize(470, 520);
        panel.setPrefSize(470, 520);
        panel.setMaxSize(470, 520);
        panel.setStyle("-fx-background-color: #FFF8FB; -fx-background-radius: 20;");
        Rectangle roundedClip = new Rectangle();
        roundedClip.widthProperty().bind(panel.widthProperty());
        roundedClip.heightProperty().bind(panel.heightProperty());
        roundedClip.setArcWidth(40);
        roundedClip.setArcHeight(40);
        panel.setClip(roundedClip);

        overlay = new StackPane(panel);
        overlay.setPadding(new Insets(20));
        overlay.setStyle("-fx-background-color: rgba(75,35,55,0.48);");
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
                description.setText("Thank you for shopping with Blush & Bloom!\n" +
                        "Your purchase is saved in Order History.");
                amount.setText(String.format("₱%,.2f", total));
                count.setText(ordered + " item(s) ordered");
                content.getChildren().remove(confirm);
                back.setText("Continue Shopping");
                back.setOnAction(event -> {
                    hideOverlay();
                    stage.close();
                });
            } catch (RuntimeException exception) {
                error.setText("Unable to complete order: " + exception.getMessage());
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
        String[] colors = {"#F1A7C5", "#E9B3CA", "#D987AC", "#F6CDDC", "#E7A0BD"};
        for (int i = 0; i < 28; i++) {
            Ellipse petal = new Ellipse(
                    3 + random.nextDouble() * 3,
                    6 + random.nextDouble() * 6
            );
            petal.setFill(Color.web(
                    colors[random.nextInt(colors.length)],
                    0.35 + random.nextDouble() * 0.3
            ));
            double x = 10 + random.nextDouble() * 450;
            double y = random.nextDouble() * 520;
            petal.setLayoutX(x);
            petal.setLayoutY(y);
            layer.getChildren().add(petal);
            shapes.add(petal);
            motions.add(new double[]{x, y, 22 + random.nextDouble() * 32,
                    random.nextDouble() * Math.PI * 2});
        }
        animation = new AnimationTimer() {
            private long previous;
            @Override
            public void handle(long now) {
                if (previous == 0) {
                    previous = now;
                    return;
                }
                double dt = Math.min((now - previous) / 1_000_000_000.0, 0.05);
                previous = now;
                for (int i = 0; i < shapes.size(); i++) {
                    double[] motion = motions.get(i);
                    motion[1] += motion[2] * dt;
                    motion[3] += dt;
                    if (motion[1] > 540) {
                        motion[1] = -20;
                        motion[0] = 10 + random.nextDouble() * 450;
                    }
                    Ellipse petal = shapes.get(i);
                    petal.setLayoutX(motion[0] + Math.sin(motion[3]) * 12);
                    petal.setLayoutY(motion[1]);
                    petal.setRotate(petal.getRotate() + 25 * dt);
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
