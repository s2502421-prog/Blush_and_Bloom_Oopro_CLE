# Groupmate Cart Design Integration

- Source: shared Drive folder containing `Oopro_CLE_fixed+suggestions (2).zip`.
- Replaced only the visual FXML files: `cart.fxml`, `cart-item.fxml`, `checkout.fxml`, `order-history.fxml`, and `added-to-cart.fxml`.
- Kept existing merged `CartWindow.java` to preserve confirmed item removal, safe `fx:id` control loading, order-save behavior, and animations.
- Kept existing merged `ShoppingCart.java`, `CustomerData.java`, other features, and isolated test-data configuration.
- Changed cart: header with order history, bottom estimated total & checkout buttons, rounded item cards; revised checkout and history popups.
- All five new FXML files were XML parsed and their expected `fx:id` values were checked against the existing controller.
- Full JavaFX UI runtime testing is still needed on Windows/IntelliJ.
