package com.example.railwaymanagementsystem.services;

import com.example.railwaymanagementsystem.models.Booking;
import com.example.railwaymanagementsystem.models.Schedule;
import com.example.railwaymanagementsystem.models.Train;
import com.example.railwaymanagementsystem.models.User;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repository layer that delegates to DatabaseService for persistent storage
 */
public final class BackendRepository {
    private static final BackendRepository INSTANCE = new BackendRepository();
    private final DatabaseService db = DatabaseService.getInstance();

    private BackendRepository() {}

    public static BackendRepository getInstance() {
        return INSTANCE;
    }

    // User operations
    public Optional<User> findUserByEmail(String email) {
        try {
            return db.findUserByEmail(email);
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<User> findUserById(String id) {
        try {
            return db.findUserById(id);
        } catch (SQLException e) {
            System.err.println("Error finding user by id: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Collection<User> getUsers() {
        try {
            return db.getAllUsers();
        } catch (SQLException e) {
            System.err.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public User addUser(User user) {
        try {
            return db.addUser(user);
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
            return user;
        }
    }

    public boolean updateUser(User user) {
        try {
            return db.updateUser(user);
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email, String excludeUserId) {
        try {
            return db.emailExists(email, excludeUserId);
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String nextUserId() {
        try {
            return db.getNextUserId();
        } catch (SQLException e) {
            System.err.println("Error getting next user id: " + e.getMessage());
            e.printStackTrace();
            return "1";
        }
    }

    // Train operations
    public List<Train> getTrains() {
        try {
            return db.getAllTrains();
        } catch (SQLException e) {
            System.err.println("Error getting trains: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Optional<Train> findTrainById(String id) {
        try {
            return db.findTrainById(id);
        } catch (SQLException e) {
            System.err.println("Error finding train by id: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Train> findTrainByNumber(String trainNumber) {
        try {
            return db.findTrainByNumber(trainNumber);
        } catch (SQLException e) {
            System.err.println("Error finding train by number: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Train addTrain(Train train) {
        try {
            return db.addTrain(train);
        } catch (SQLException e) {
            System.err.println("Error adding train: " + e.getMessage());
            e.printStackTrace();
            return train;
        }
    }

    public boolean updateTrain(Train train) {
        try {
            return db.updateTrain(train);
        } catch (SQLException e) {
            System.err.println("Error updating train: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void removeTrain(String id) {
        try {
            db.removeTrain(id);
        } catch (SQLException e) {
            System.err.println("Error removing train: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String nextTrainId() {
        try {
            return db.getNextTrainId();
        } catch (SQLException e) {
            System.err.println("Error getting next train id: " + e.getMessage());
            e.printStackTrace();
            return "1";
        }
    }

    // Schedule operations
    public List<Schedule> getSchedules() {
        try {
            return db.getAllSchedules();
        } catch (SQLException e) {
            System.err.println("Error getting schedules: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Optional<Schedule> findScheduleByTrainNumber(String trainNumber) {
        try {
            return db.findScheduleByTrainNumber(trainNumber);
        } catch (SQLException e) {
            System.err.println("Error finding schedule: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Schedule addSchedule(Schedule schedule) {
        try {
            return db.addSchedule(schedule);
        } catch (SQLException e) {
            System.err.println("Error adding schedule: " + e.getMessage());
            e.printStackTrace();
            return schedule;
        }
    }

    public boolean updateSchedule(Schedule schedule) {
        try {
            return db.updateSchedule(schedule);
        } catch (SQLException e) {
            System.err.println("Error updating schedule: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void removeSchedule(Schedule schedule) {
        try {
            db.removeSchedule(schedule.getId());
        } catch (SQLException e) {
            System.err.println("Error removing schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String nextScheduleId() {
        try {
            return db.getNextScheduleId();
        } catch (SQLException e) {
            System.err.println("Error getting next schedule id: " + e.getMessage());
            e.printStackTrace();
            return "1";
        }
    }

    // Booking operations
    public List<Booking> getBookings() {
        try {
            return db.getAllBookings();
        } catch (SQLException e) {
            System.err.println("Error getting bookings: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Optional<Booking> findBookingById(String id) {
        try {
            return db.findBookingById(id);
        } catch (SQLException e) {
            System.err.println("Error finding booking: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Booking addBooking(Booking booking) {
        try {
            return db.addBooking(booking);
        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
            e.printStackTrace();
            return booking;
        }
    }

    public boolean updateBooking(Booking booking) {
        try {
            return db.updateBooking(booking);
        } catch (SQLException e) {
            System.err.println("Error updating booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String nextBookingId() {
        try {
            return db.getNextBookingId();
        } catch (SQLException e) {
            System.err.println("Error getting next booking id: " + e.getMessage());
            e.printStackTrace();
            return "1";
        }
    }
}
