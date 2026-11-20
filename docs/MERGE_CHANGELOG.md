# Blush & Bloom — Three-Member Merge Notes

## Kept from the working application

- Login and registration, saved user accounts across restarts
- Shop products with product previews and cart
- Cart quantity limits, checkout, persistent orders, and order history
- FXML layouts that can be opened using Scene Builder

## Austine: Shop Products and complete suggestions ZIP

- Used the complete revised Java package structure (`app`, `controller`, `model`, `service`, `ui`).
- Added the admin account feature and access-controlled product inventory UI.
- `InventoryController.java` + `inventory.fxml` let an authorized admin create, read, update, and delete makeup products, as well as search and sort records.
- Added `Person.java`, `Admin.java`, `Session.java`, and `SecureFiles.java`.
- Kept product model inheritance and persistent account/order formats.

## Ehrica: My Cart

- Adapted the provided `CartController.java` removal confirmation to the actively used `CartWindow.java` (`confirmRemove`).
- Confirmation is now required when pressing **Remove** or decreasing an item from quantity 1.
- Added checkout disablement when empty and singular/plural item counts.
- Kept the working checkout (`ShoppingCart.placeOrder()`) rather than her temporary `ShoppingCart.clear()` placeholder, which would lose order history.
- Continued using `cart.fxml`, `cart-item.fxml`, `checkout.fxml`, and `order-history.fxml` with proper FXML-backed cart rows.
- **Limitation:** The actual edited `cart.fxml` from Ehrica was not supplied, so it was not copied.

## Integration improvements

- FXML child controls are now retrieved using `FXMLLoader.getNamespace()` instead of `.lookup(...)` before CSS, preventing null control issues in `ProductsController.java` and `CartWindow.java`.
- Fixed Maven compile target from Java 25 to Java 21 (`release 21`) for wider JDK compatibility.
- A corrupt inventory file is never silently overwritten if making a backup fails.
- Default saved-data folder is now `BlushAndBloom_Merged` (separate from your existing `BlushAndBloom` folder); persistent accounts and order history still work.

## Verification

- Java model and service classes compile using `javac` JDK 21.
- New customer account, admin login, inventory CRUD, product stock deduction, checkout, and order persistence were exercised with a temporary test data folder.
- Old customer account data created by the original implementation was used to successfully log in with the merged version.
- All 13 FXML files parsed as XML and declared controllers/handlers were checked.
- Full JavaFX GUI/Maven build **not tested** here because JavaFX/Maven dependencies were not available without internet. Windows IntelliJ testing is still required.

**For group documentation:** Contributions here describe components supplied or integrated, not necessarily who independently wrote each line. Verify individual work descriptions with your group before submission.
