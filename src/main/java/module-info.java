module ldn.ldn {
    requires javafx.fxml;
    requires java.sql;
    requires javafx.web;
    requires javafx.media;
    requires javafx.controls;
    requires java.desktop;


    opens ldn.ldn to javafx.fxml;
    exports ldn.ldn;
}