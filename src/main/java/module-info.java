module com.example.railwaymanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.railwaymanagementsystem to javafx.fxml;
    exports com.example.railwaymanagementsystem;
    exports com.example.railwaymanagementsystem.controllers;
    opens com.example.railwaymanagementsystem.controllers to javafx.fxml;
    exports com.example.railwaymanagementsystem.models;
    opens com.example.railwaymanagementsystem.models to javafx.fxml;
}