module com.example.group_assignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;
    requires java.desktop;
    requires itextpdf;


    opens com.example.group_assignment to javafx.fxml;
    exports com.example.group_assignment;
}