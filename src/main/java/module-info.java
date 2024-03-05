module org.yunusov.lifegameproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.yunusov.lifegameproject to javafx.fxml;
    exports org.yunusov.lifegameproject;
}