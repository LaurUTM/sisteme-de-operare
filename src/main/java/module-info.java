module org.task.demoapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.task.demoapp to javafx.fxml;
    exports org.task.demoapp;
    exports org.task.demoapp.controller;
    opens org.task.demoapp.controller to javafx.fxml;
}