package com.example.railwaymanagementsystem.controllers;

import com.example.railwaymanagementsystem.models.Schedule;
import com.example.railwaymanagementsystem.services.BackendService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.Set;
import java.util.TreeSet;

/**
 * Controller for View Schedule Screen
 */
public class ViewScheduleController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> fromStationCombo;
    @FXML private ComboBox<String> toStationCombo;
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private Label countLabel;

    private final BackendService backend = BackendService.getInstance();
    private ObservableList<Schedule> scheduleData;
    private FilteredList<Schedule> filteredData;

    @FXML
    private void initialize() {
        setupTableColumns();
        initializeData();
        setupFilters();
        populateStationFilters();
    }

    private void setupTableColumns() {
        if (scheduleTable == null || scheduleTable.getColumns().isEmpty()) {
            return;
        }
        
        // Set up cell value factories for each column
        @SuppressWarnings("unchecked")
        TableColumn<Schedule, String> trainNumberCol = (TableColumn<Schedule, String>) scheduleTable.getColumns().get(0);
        trainNumberCol.setCellValueFactory(cellData -> cellData.getValue().trainNumberProperty());
        
        @SuppressWarnings("unchecked")
        TableColumn<Schedule, String> trainNameCol = (TableColumn<Schedule, String>) scheduleTable.getColumns().get(1);
        trainNameCol.setCellValueFactory(cellData -> cellData.getValue().trainNameProperty());
        
        @SuppressWarnings("unchecked")
        TableColumn<Schedule, String> departureCol = (TableColumn<Schedule, String>) scheduleTable.getColumns().get(2);
        departureCol.setCellValueFactory(cellData -> cellData.getValue().departureTimeProperty());
        
        @SuppressWarnings("unchecked")
        TableColumn<Schedule, String> arrivalCol = (TableColumn<Schedule, String>) scheduleTable.getColumns().get(3);
        arrivalCol.setCellValueFactory(cellData -> cellData.getValue().arrivalTimeProperty());
        
        @SuppressWarnings("unchecked")
        TableColumn<Schedule, String> routeCol = (TableColumn<Schedule, String>) scheduleTable.getColumns().get(4);
        routeCol.setCellValueFactory(cellData -> cellData.getValue().routeProperty());
        
        @SuppressWarnings("unchecked")
        TableColumn<Schedule, String> daysCol = (TableColumn<Schedule, String>) scheduleTable.getColumns().get(5);
        daysCol.setCellValueFactory(cellData -> cellData.getValue().daysProperty());
        
        @SuppressWarnings("unchecked")
        TableColumn<Schedule, String> statusCol = (TableColumn<Schedule, String>) scheduleTable.getColumns().get(6);
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void initializeData() {
        try {
            scheduleData = FXCollections.observableArrayList(backend.getSchedules());
            filteredData = new FilteredList<>(scheduleData, p -> true);
            scheduleTable.setItems(filteredData);
            updateCountLabel();
        } catch (Exception e) {
            System.err.println("Error loading schedules: " + e.getMessage());
            e.printStackTrace();
            scheduleData = FXCollections.observableArrayList();
            filteredData = new FilteredList<>(scheduleData, p -> true);
            scheduleTable.setItems(filteredData);
        }
    }

    private void populateStationFilters() {
        Set<String> stations = new TreeSet<>();
        for (Schedule schedule : scheduleData) {
            String[] parts = schedule.getRoute().split("-");
            if (parts.length >= 2) {
                stations.add(parts[0].trim());
                stations.add(parts[parts.length - 1].trim());
            }
        }

        ObservableList<String> options = FXCollections.observableArrayList();
        options.add("All Stations");
        options.addAll(stations);

        fromStationCombo.setItems(options);
        toStationCombo.setItems(FXCollections.observableArrayList(options));
        fromStationCombo.setValue("All Stations");
        toStationCombo.setValue("All Stations");
    }

    private void setupFilters() {
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        fromStationCombo.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        toStationCombo.valueProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void applyFilters() {
        filteredData.setPredicate(schedule -> {
            String searchText = searchField.getText().toLowerCase();
            boolean matchesSearch = searchText.isEmpty() ||
                    schedule.getTrainNumber().toLowerCase().contains(searchText) ||
                    schedule.getTrainName().toLowerCase().contains(searchText) ||
                    schedule.getRoute().toLowerCase().contains(searchText);

            String origin = extractStation(schedule.getRoute(), true);
            String destination = extractStation(schedule.getRoute(), false);

            String fromFilter = fromStationCombo.getValue();
            boolean matchesFrom = fromFilter == null || "All Stations".equals(fromFilter) ||
                    origin.equalsIgnoreCase(fromFilter);

            String toFilter = toStationCombo.getValue();
            boolean matchesTo = toFilter == null || "All Stations".equals(toFilter) ||
                    destination.equalsIgnoreCase(toFilter);

            return matchesSearch && matchesFrom && matchesTo;
        });

        updateCountLabel();
    }

    private void updateCountLabel() {
        countLabel.setText("Showing " + filteredData.size() + " schedules");
    }

    private String extractStation(String route, boolean first) {
        String[] parts = route.split("-");
        if (parts.length == 0) {
            return "";
        }
        return first ? parts[0].trim() : parts[parts.length - 1].trim();
    }
}
