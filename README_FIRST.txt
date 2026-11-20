BLUSH & BLOOM — MERGED GROUP PROJECT (JavaFX + Maven)

This is a NEW, SEPARATE project folder based on Austine's supplied
Oopro_CLE_fixed+suggestions.zip, with Ehrica's cart-removal confirmation
included and FXML loading fixes. Your current IntelliJ project was not edited.

FEATURES
 • Your login and registration (customer data persists after restart)
 • Salted password hashing; compatible with the older customers.properties
 • Austine: Admin + Person, Inventory CRUD, search/sort, stock tracking
 • Shop Products: Foundation, Blush, Lipstick with images/previews
 • Ehrica: confirm before Remove or decreasing a cart item from 1 to 0
 • Persistent cart and Order History; correct ShoppingCart.placeOrder()
 • Scene Builder-compatible FXML for all screens

OPEN IN INTELLIJ (WINDOWS)
 1. Extract the ZIP to a DIFFERENT folder. Do not overwrite your old project.
 2. Open IntelliJ -> File -> Open -> choose this folder / pom.xml.
 3. Trust project, import Maven dependencies; use JDK 21 or newer.
 4. In Maven tool window run Plugins -> javafx -> javafx:run.
    Alternative command in terminal: mvnw.cmd javafx:run
 5. At first run on a NEW data folder, an admin login/password is displayed ONCE.
    Write it down so you can log in and access Manage Inventory.
 6. Log in as a customer to test normal Shopping Cart and Order History.

DATA SAFETY — YOUR ORIGINAL APP AND SAVED ACCOUNTS STAY SEPARATE
 This MERGED project now saves data in a DIFFERENT Windows folder by default:
      %USERPROFILE%\BlushAndBloom_Merged
 Your current app uses:
      %USERPROFILE%\BlushAndBloom
 So testing the merged app will NOT overwrite your current saved accounts,
 products, carts, or order histories.

 Accounts registered in the merged app stay saved across restarts too.
 To reuse original accounts, first back up both folders. Then copy the
 contents of %USERPROFILE%\BlushAndBloom into
 %USERPROFILE%\BlushAndBloom_Merged BEFORE you register or order in
 the merged app. This is an optional migration, not required for testing.

 You may set the JVM system property -Dbandb.data=<your-folder> to use
 another folder instead. Leave it unset for the safest first run.

MORE NOTES: contributions/Ehrica_CartController_README.txt

LIMITATIONS
 • JavaFX GUI was not run in this environment because Maven/JavaFX
   dependencies are not available offline. Please test on Windows.
 • Ehrica's edited cart.fxml was not provided; her confirmation and cart
   behavior were integrated, but her unknown FXML design was not copied.
 • New classes Person/Admin mean you should REGENERATE your UML diagram.


UPDATED CART FXML EDITION (November 20, 2026)
This copy includes the updated cart, cart-item, checkout, order-history,
and added-to-cart layouts from the groupmate shared Drive folder ZIP
Oopro_CLE_fixed+suggestions (2).zip. All other working merged application
code is retained, including @FXML/namespace-safe control loading, order
history persistence, placeOrder checkout and removal confirmation.
The new ZIP is a separate project: it does not overwrite old source files.
Open the inner project folder with pom.xml in IntelliJ. Run .\mvnw.cmd javafx:run.
FULL GUI MUST BE TESTED IN INTELLIJ.
