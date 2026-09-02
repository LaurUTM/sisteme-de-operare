module org.task.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.task.demo to javafx.fxml;
    exports org.task.demo;
}