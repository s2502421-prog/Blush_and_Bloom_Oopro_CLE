module com.example.bandb {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bandb to javafx.fxml;
    exports com.example.bandb;
}