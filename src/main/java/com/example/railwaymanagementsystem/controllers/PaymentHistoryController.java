package com.example.railwaymanagementsystem.controllers;

import com.example.railwaymanagementsystem.models.Booking;
import com.example.railwaymanagementsystem.services.AppSession;
import com.example.railwaymanagementsystem.services.BackendService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Payment History Screen
 */
public class PaymentHistoryController {

    @FXML private Label totalSpentLabel;
    @FXML private Label thisMonthLabel;
    @FXML private Label totalTripsLabel;
    @FXML private ComboBox<String> filterCombo;
    @FXML private TableView<PaymentRecord> paymentTable;
    @FXML private TableColumn<PaymentRecord, String> dateColumn;
    @FXML private TableColumn<PaymentRecord, String> pnrColumn;
    @FXML private TableColumn<PaymentRecord, String> trainColumn;
    @FXML private TableColumn<PaymentRecord, String> routeColumn;
    @FXML private TableColumn<PaymentRecord, String> amountColumn;
    @FXML private TableColumn<PaymentRecord, String> paymentModeColumn;
    @FXML private TableColumn<PaymentRecord, String> statusColumn;

    private final BackendService backend = BackendService.getInstance();
    private final AppSession session = AppSession.getInstance();
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    private void initialize() {
        if (filterCombo != null) {
            filterCombo.setValue("All Transactions");
        }
        setupTableColumns();
        loadPaymentHistory();
    }

    private void loadPaymentHistory() {
        ObservableList<PaymentRecord> payments = FXCollections.observableArrayList();
        
        try {
            session.getCurrentUser().ifPresentOrElse(user -> {
                List<Booking> bookings = backend.getBookingsForUser(user.getId());
                System.out.println("Found " + bookings.size() + " bookings for user " + user.getId());
                
                // Only show paid bookings
                List<PaymentRecord> paidBookings = bookings.stream()
                        .filter(booking -> {
                            String status = booking.getPaymentStatus();
                            System.out.println("Booking " + booking.getId() + " payment status: " + status);
                            return "Paid".equals(status);
                        })
                        .map(booking -> new PaymentRecord(
                                booking.getBookingDateTime().format(DATE_FORMATTER),
                                booking.getId(),
                                booking.getTrainNumber() + " - " + booking.getTrainName(),
                                booking.getFromStation() + " → " + booking.getToStation(),
                                "PKR " + String.format("%,.0f", booking.getTotalAmount()),
                                booking.getPaymentMethod() != null && !booking.getPaymentMethod().isEmpty() 
                                        ? booking.getPaymentMethod() : "N/A",
                                booking.getPaymentStatus()
                        )).toList();
                
                System.out.println("Found " + paidBookings.size() + " paid bookings");
                payments.addAll(paidBookings);

                // Calculate statistics from all bookings
                double totalSpent = bookings.stream()
                        .filter(b -> "Paid".equals(b.getPaymentStatus()))
                        .mapToDouble(Booking::getTotalAmount)
                        .sum();
                totalSpentLabel.setText("PKR " + String.format("%,.0f", totalSpent));
                
                long paidCount = bookings.stream()
                        .filter(b -> "Paid".equals(b.getPaymentStatus()))
                        .count();
                totalTripsLabel.setText(String.valueOf(paidCount));
                
                long thisMonth = bookings.stream()
                        .filter(b -> "Paid".equals(b.getPaymentStatus()))
                        .filter(b -> b.getBookingDateTime().getMonthValue() == LocalDate.now().getMonthValue() &&
                                    b.getBookingDateTime().getYear() == LocalDate.now().getYear())
                        .count();
                thisMonthLabel.setText(String.valueOf(thisMonth));
            }, () -> {
                System.out.println("No user session found");
            });
        } catch (Exception e) {
            System.err.println("Error loading payment history: " + e.getMessage());
            e.printStackTrace();
        }

        if (paymentTable != null) {
            paymentTable.setItems(payments);
            System.out.println("Set " + payments.size() + " payment records to table");
        } else {
            System.err.println("Payment table is null!");
        }
    }

    private void setupTableColumns() {
        // Set up columns by index since they don't have fx:id in FXML
        if (paymentTable != null && !paymentTable.getColumns().isEmpty()) {
            if (paymentTable.getColumns().size() > 0) {
                @SuppressWarnings("unchecked")
                TableColumn<PaymentRecord, String> col = (TableColumn<PaymentRecord, String>) paymentTable.getColumns().get(0);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
            }
            if (paymentTable.getColumns().size() > 1) {
                @SuppressWarnings("unchecked")
                TableColumn<PaymentRecord, String> col = (TableColumn<PaymentRecord, String>) paymentTable.getColumns().get(1);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPnr()));
            }
            if (paymentTable.getColumns().size() > 2) {
                @SuppressWarnings("unchecked")
                TableColumn<PaymentRecord, String> col = (TableColumn<PaymentRecord, String>) paymentTable.getColumns().get(2);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrain()));
            }
            if (paymentTable.getColumns().size() > 3) {
                @SuppressWarnings("unchecked")
                TableColumn<PaymentRecord, String> col = (TableColumn<PaymentRecord, String>) paymentTable.getColumns().get(3);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoute()));
            }
            if (paymentTable.getColumns().size() > 4) {
                @SuppressWarnings("unchecked")
                TableColumn<PaymentRecord, String> col = (TableColumn<PaymentRecord, String>) paymentTable.getColumns().get(4);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAmount()));
            }
            if (paymentTable.getColumns().size() > 5) {
                @SuppressWarnings("unchecked")
                TableColumn<PaymentRecord, String> col = (TableColumn<PaymentRecord, String>) paymentTable.getColumns().get(5);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentMode()));
            }
            if (paymentTable.getColumns().size() > 6) {
                @SuppressWarnings("unchecked")
                TableColumn<PaymentRecord, String> col = (TableColumn<PaymentRecord, String>) paymentTable.getColumns().get(6);
                col.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
            }
        }
    }

    @FXML
    private void handleExportPDF() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export to PDF");
        alert.setHeaderText("Payment History Export");
        alert.setContentText("Payment history exported successfully!\n\n" +
                "File: payment_history_" + java.time.LocalDate.now() + ".pdf\n\n" +
                "(In production, this would generate a real PDF file)");
        alert.showAndWait();
    }

    // Payment Record class
    public static class PaymentRecord {
        private final String date;
        private final String pnr;
        private final String train;
        private final String route;
        private final String amount;
        private final String paymentMode;
        private final String status;

        public PaymentRecord(String date, String pnr, String train, String route,
                             String amount, String paymentMode, String status) {
            this.date = date;
            this.pnr = pnr;
            this.train = train;
            this.route = route;
            this.amount = amount;
            this.paymentMode = paymentMode;
            this.status = status;
        }

        public String getDate() { return date; }
        public String getPnr() { return pnr; }
        public String getTrain() { return train; }
        public String getRoute() { return route; }
        public String getAmount() { return amount; }
        public String getPaymentMode() { return paymentMode; }
        public String getStatus() { return status; }
    }
}