module md.utm.smarttimermanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens md.utm.smarttimermanager to javafx.fxml;
    exports md.utm.smarttimermanager;
}