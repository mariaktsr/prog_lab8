module client.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.jooq;

    opens client.gui.controller to javafx.fxml;
    opens client.gui to javafx.fxml;

    exports client.gui;
    exports client.gui.controller;
}