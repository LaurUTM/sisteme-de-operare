module org.task.demoapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.task.demoapp to javafx.fxml;
    exports org.task.demoapp;
}