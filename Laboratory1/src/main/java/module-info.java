module com.example.laboratory1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.laboratory1 to javafx.fxml;
    exports com.example.laboratory1;
}