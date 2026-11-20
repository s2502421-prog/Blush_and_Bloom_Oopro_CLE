module com.example.bandb {
    requires javafx.controls;
    requires javafx.fxml;

    // Application entry point (JavaFX launches it by reflection).
    exports com.example.bandb.app;
    opens com.example.bandb.app to javafx.graphics, javafx.fxml;

    // FXML injects @FXML fields and calls handlers by reflection.
    opens com.example.bandb.controller to javafx.fxml;

}
