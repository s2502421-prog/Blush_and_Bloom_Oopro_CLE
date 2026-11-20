EHRICA — MY CART CONTRIBUTION

The groupmate provided CartController.java code in chat, and mentioned edits to cart.fxml.
The complete updated cart.fxml was not provided, so exact visual edits could not be imported.

The MERGED working application keeps the FXML-based CartWindow.java + cart.fxml +
cart-item.fxml, which already handles quantity changes, stock limits, checkout, and
persistent order history. The essential new behavior integrated from Ehrica's code
is the confirmation alert before removing a cart item (also when pressing − on
the last remaining unit). Search for confirmRemove() in CartWindow.java.

Ehrica's original draft also contained a checkout placeholder which called
ShoppingCart.clear() without creating an order; that placeholder was NOT used.

Austine's InventoryController.java, inventory.fxml, Person/Admin, Session,
SecureFiles, and product shop improvements come from the provided Drive ZIP.
The original Blush & Bloom application supplies the customer account concept,
product models, login/registration, and cart history that this revision retains.

The final code must be tested by all three group members and contributions
verified honestly before submission.
