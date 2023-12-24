module com.example.javafxapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    exports com.example.javafxapp.Java2.Lesson6.Client;
    opens com.example.javafxapp.Java2.Lesson6.Client to javafx.fxml;
}