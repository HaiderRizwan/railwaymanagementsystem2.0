package com.example.railwaymanagementsystem.services;

import com.example.railwaymanagementsystem.models.Booking;
import com.example.railwaymanagementsystem.models.Schedule;
import com.example.railwaymanagementsystem.models.Train;
import com.example.railwaymanagementsystem.models.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Facade over the in-memory repository that provides business operations.
 */
public final class BackendService {
    private static final BackendService INSTANCE = new BackendService();
    private final BackendRepository repo = BackendRepository.getInstance();

    private BackendService() {}

    public static BackendService getInstance() {
        return INSTANCE;
    }

    public Optional<User> authenticate(String email, String password, String role) {
        return repo.findUserByEmail(email)
                .filter(user -> user.getPassword().equals(password))
                .filter(user -> user.getRole().equalsIgnoreCase(role));
    }

    public Optional<User> register(User user) {
        if (repo.findUserByEmail(user.getEmail()).isPresent()) {
            return Optional.empty();
        }
        User copy = new User(repo.nextUserId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRole(), user.getPassword());
        return Optional.of(repo.addUser(copy));
    }

    public boolean emailExists(String email) {
        return repo.findUserByEmail(email).isPresent();
    }

    public Optional<User> getUserById(String userId) {
        return repo.findUserById(userId);
    }

    public Optional<User> updateUser(User updatedUser) {
        Optional<User> existing = repo.findUserById(updatedUser.getId());
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        if (repo.emailExists(updatedUser.getEmail(), updatedUser.getId())) {
            return Optional.empty();
        }
        if (repo.updateUser(updatedUser)) {
            return Optional.of(updatedUser);
        }
        return Optional.empty();
    }

    public List<Train> getTrains() {
        return repo.getTrains().stream().collect(Collectors.toList());
    }

    public List<Schedule> getSchedules() {
        return repo.getSchedules().stream().collect(Collectors.toList());
    }

    public List<Train> searchTrains(String from, String to) {
        String normalizedFrom = from.toLowerCase();
        String normalizedTo = to.toLowerCase();
        return repo.getTrains().stream()
                .filter(train -> train.getRoute().toLowerCase().contains(normalizedFrom))
                .filter(train -> train.getRoute().toLowerCase().contains(normalizedTo))
                .collect(Collectors.toList());
    }

    public Optional<Train> getTrainByNumber(String trainNumber) {
        return repo.findTrainByNumber(trainNumber);
    }

    public Booking bookTicket(User user, Train train, String from, String to,
                              LocalDate date, int seats, String seatClass, double totalAmount) {
        Booking booking = new Booking(
                generateBookingId(),
                user.getId(),
                train.getId(),
                train.getTrainNumber(),
                train.getTrainName(),
                from,
                to,
                date,
                seats,
                seatClass,
                totalAmount,
                "Pending",
                LocalDateTime.now(),
                "",
                "Pending"
        );
        return repo.addBooking(booking);
    }

    public boolean processPayment(String bookingId, String paymentMethod) {
        Optional<Booking> bookingOpt = repo.findBookingById(bookingId);
        if (bookingOpt.isEmpty()) {
            return false;
        }
        Booking booking = bookingOpt.get();
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentStatus("Paid");
        booking.setStatus("Confirmed");
        return repo.updateBooking(booking);
    }

    public List<Booking> getPendingPaymentsForUser(String userId) {
        return repo.getBookings().stream()
                .filter(booking -> booking.getUserId().equals(userId))
                .filter(booking -> "Pending".equals(booking.getPaymentStatus()))
                .collect(Collectors.toList());
    }

    private String generateBookingId() {
        return repo.nextBookingId();
    }

    public List<Booking> getBookingsForUser(String userId) {
        return repo.getBookings().stream()
                .filter(booking -> booking.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public Optional<Schedule> getScheduleForTrain(String trainNumber) {
        return repo.findScheduleByTrainNumber(trainNumber);
    }

    public Train createTrain(String trainNumber, String trainName, String type, String route, String status) {
        return repo.addTrain(new Train(repo.nextTrainId(), trainNumber, trainName, type, route, status));
    }

    public void deleteTrain(Train train) {
        repo.removeTrain(train.getId());
    }

    public Schedule createSchedule(String trainNumber, String trainName, String departureTime,
                                   String arrivalTime, String route, String days, String status) {
        return repo.addSchedule(new Schedule(repo.nextScheduleId(), trainNumber, trainName,
                departureTime, arrivalTime, route, days, status));
    }

    public void removeSchedule(Schedule schedule) {
        repo.removeSchedule(schedule);
    }

    public List<Booking> getAllBookings() {
        return repo.getBookings();
    }

    public Optional<Booking> getBookingById(String bookingId) {
        return repo.findBookingById(bookingId);
    }
}

