# Blush & Bloom — Customer-Only JavaFX Project

## Project overview
Blush & Bloom is a customer-facing makeup shopping desktop application written in Java 21 with JavaFX, FXML, and Maven. The application opens at the login screen and provides registration, skin-tone selection, a home page, product browsing, a shopping cart, checkout, and order history. There is no admin account interface or inventory management screen.

## Project structure
- `src/main/java/com/example/bandb/app`: JavaFX launcher and scene navigation.
- `src/main/java/com/example/bandb/controller`: customer screen controllers and cart window logic.
- `src/main/java/com/example/bandb/model`: `Person`, `Customer`, `Product`, `Makeup`, `FaceMakeup`, `Foundation`, `Blush`, and `Lipstick`.
- `src/main/java/com/example/bandb/service`: customer accounts, session, product data, local file storage, and shopping cart.
- `src/main/java/com/example/bandb/ui`: reusable product previews and dialogs.
- `src/main/resources/com/example/bandb/fxml`: FXML screen layouts, CSS, and image resources.

## Object-oriented programming
- **Encapsulation:** model attributes are private and accessed using validated constructors or methods.
- **Inheritance:** `Customer` extends `Person`; `Makeup` extends `Product`; `FaceMakeup` extends `Makeup`; `Foundation` and `Blush` extend `FaceMakeup`, while `Lipstick` extends `Makeup`.
- **Abstraction:** `Person`, `Product`, and `Makeup` define shared behaviors for subclasses.
- **Polymorphism:** product subclasses implement their own category and finish behavior, and customer objects provide their role through `getRole()`.

## Customer flow
1. Register or log in.
2. Select a skin tone and browse makeup products.
3. Add products to the cart, adjust quantities, or remove items with confirmation.
4. Checkout and view saved order history.

## Run
Extract the project and open the directory containing `pom.xml` in IntelliJ IDEA with JDK 21. Import Maven dependencies and run `mvnw.cmd javafx:run` on Windows.

## Verification limitations
FXML XML parsing, declared controllers/action handlers, and resource references were statically checked. A successful Maven compilation and interactive GUI test still need to be completed on a machine with the JavaFX dependencies installed.
