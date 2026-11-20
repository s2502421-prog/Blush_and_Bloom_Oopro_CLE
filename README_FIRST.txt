BLUSH & BLOOM — CUSTOMER-ONLY FINAL PROJECT

JavaFX + Maven application. No admin login or inventory management UI.

HOW TO OPEN ON WINDOWS
1. Extract this ZIP (do not open it as a ZIP inside IntelliJ).
2. In IntelliJ IDEA, choose File > Open and select the extracted folder containing pom.xml.
3. Use JDK 21, allow IntelliJ to import Maven dependencies, and wait for indexing.
4. Run the Maven javafx:run goal, or use the terminal command: mvnw.cmd javafx:run
5. Test login, registration, skin-tone selection, products, add-to-cart, cart, checkout, and order history.

Customer screens are implemented with FXML. Customer records, carts, and orders are stored locally.
The project uses the existing BlushAndBloom_Merged user-data location; keep a backup before migrating data.

VALIDATION
XML parsing, controller/action references, and source/resource references have been statically checked.
The full Maven compilation and interactive JavaFX run were not verified in this environment.
