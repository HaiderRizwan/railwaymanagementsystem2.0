package com.example.railwaymanagementsystem.services;

import com.example.railwaymanagementsystem.models.Booking;
import com.example.railwaymanagementsystem.models.Schedule;
import com.example.railwaymanagementsystem.models.Train;
import com.example.railwaymanagementsystem.models.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database service for persistent data storage using SQLite
 */
public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:railway_management.db";
    private static DatabaseService instance;
    private Connection connection;

    private DatabaseService() {
        initializeDatabase();
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            seedInitialData();
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        // Users table
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                phone TEXT,
                role TEXT NOT NULL,
                password TEXT NOT NULL,
                cnic TEXT,
                date_of_birth DATE,
                gender TEXT,
                address TEXT,
                city TEXT,
                postal_code TEXT
            )
        """;

        // Trains table
        String createTrainsTable = """
            CREATE TABLE IF NOT EXISTS trains (
                id TEXT PRIMARY KEY,
                train_number TEXT UNIQUE NOT NULL,
                train_name TEXT NOT NULL,
                type TEXT,
                route TEXT,
                status TEXT
            )
        """;

        // Schedules table
        String createSchedulesTable = """
            CREATE TABLE IF NOT EXISTS schedules (
                id TEXT PRIMARY KEY,
                train_number TEXT NOT NULL,
                train_name TEXT NOT NULL,
                departure_time TEXT,
                arrival_time TEXT,
                route TEXT,
                days TEXT,
                status TEXT,
                FOREIGN KEY (train_number) REFERENCES trains(train_number)
            )
        """;

        // Bookings table
        String createBookingsTable = """
            CREATE TABLE IF NOT EXISTS bookings (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                train_id TEXT NOT NULL,
                train_number TEXT NOT NULL,
                train_name TEXT NOT NULL,
                from_station TEXT NOT NULL,
                to_station TEXT NOT NULL,
                travel_date DATE NOT NULL,
                number_of_seats INTEGER NOT NULL,
                seat_class TEXT,
                total_amount REAL NOT NULL,
                status TEXT NOT NULL,
                booking_date_time TIMESTAMP NOT NULL,
                payment_method TEXT,
                payment_status TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (train_id) REFERENCES trains(id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createTrainsTable);
            stmt.execute(createSchedulesTable);
            stmt.execute(createBookingsTable);
        }
    }

    private void seedInitialData() throws SQLException {
        // Check if data already exists
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Data already seeded
            }
        }

        // Seed users
        seedUsers();
        seedTrains();
        seedSchedules();
        seedBookings();
    }

    private void seedUsers() throws SQLException {
        String sql = "INSERT OR IGNORE INTO users (id, name, email, phone, role, password, cnic, date_of_birth, gender, address, city, postal_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Admin user
            pstmt.setString(1, "100");
            pstmt.setString(2, "System Admin");
            pstmt.setString(3, "admin@railsafar.com");
            pstmt.setString(4, "0300-0000000");
            pstmt.setString(5, "admin");
            pstmt.setString(6, "admin123");
            pstmt.setString(7, "35202-1234567-1");
            pstmt.setDate(8, Date.valueOf(LocalDate.of(1985, 5, 12)));
            pstmt.setString(9, "Male");
            pstmt.setString(10, "HQ, Rail Safar Building");
            pstmt.setString(11, "Karachi");
            pstmt.setString(12, "75500");
            pstmt.executeUpdate();

            // Sample passenger
            pstmt.setString(1, "101");
            pstmt.setString(2, "Sarah Khan");
            pstmt.setString(3, "sarah.khan@example.com");
            pstmt.setString(4, "0300-1111111");
            pstmt.setString(5, "passenger");
            pstmt.setString(6, "password1");
            pstmt.setString(7, "35201-9876543-2");
            pstmt.setDate(8, Date.valueOf(LocalDate.of(1995, 8, 20)));
            pstmt.setString(9, "Female");
            pstmt.setString(10, "123 Main Street");
            pstmt.setString(11, "Lahore");
            pstmt.setString(12, "54000");
            pstmt.executeUpdate();
        }
    }

    private void seedTrains() throws SQLException {
        String sql = "INSERT OR IGNORE INTO trains (id, train_number, train_name, type, route, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String[][] trains = {
                {"1", "1UP", "Karachi Express", "Express", "Karachi - Lahore", "On-time"},
                {"2", "2DN", "Lahore Express", "Express", "Lahore - Karachi", "Delayed"},
                {"3", "3UP", "Green Line", "Passenger", "Islamabad - Multan", "On-time"},
                {"4", "4DN", "Freight Express", "Freight", "Port Qasim - Faisalabad", "Cancelled"},
                {"5", "5UP", "Business Express", "Express", "Rawalpindi - Quetta", "On-time"},
                {"6", "6DN", "Peshawar Mail", "Passenger", "Peshawar - Karachi", "Delayed"}
            };

            for (String[] train : trains) {
                pstmt.setString(1, train[0]);
                pstmt.setString(2, train[1]);
                pstmt.setString(3, train[2]);
                pstmt.setString(4, train[3]);
                pstmt.setString(5, train[4]);
                pstmt.setString(6, train[5]);
                pstmt.executeUpdate();
            }
        }
    }

    private void seedSchedules() throws SQLException {
        String sql = "INSERT OR IGNORE INTO schedules (id, train_number, train_name, departure_time, arrival_time, route, days, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String[][] schedules = {
                {"1", "1UP", "Karachi Express", "08:00 AM", "08:00 PM", "Karachi - Lahore", "Daily", "Active"},
                {"2", "2DN", "Lahore Express", "09:00 AM", "09:00 PM", "Lahore - Karachi", "Daily", "Active"},
                {"3", "3UP", "Green Line", "10:30 AM", "06:30 PM", "Islamabad - Multan", "Mon-Fri", "Active"},
                {"4", "5UP", "Business Express", "07:00 AM", "05:00 PM", "Rawalpindi - Quetta", "Daily", "Active"},
                {"5", "6DN", "Peshawar Mail", "11:00 AM", "11:00 PM", "Peshawar - Karachi", "Daily", "Active"}
            };

            for (String[] schedule : schedules) {
                pstmt.setString(1, schedule[0]);
                pstmt.setString(2, schedule[1]);
                pstmt.setString(3, schedule[2]);
                pstmt.setString(4, schedule[3]);
                pstmt.setString(5, schedule[4]);
                pstmt.setString(6, schedule[5]);
                pstmt.setString(7, schedule[6]);
                pstmt.setString(8, schedule[7]);
                pstmt.executeUpdate();
            }
        }
    }

    private void seedBookings() throws SQLException {
        // Only seed if no bookings exist
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bookings")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        String sql = "INSERT INTO bookings (id, user_id, train_id, train_number, train_name, from_station, to_station, travel_date, number_of_seats, seat_class, total_amount, status, booking_date_time, payment_method, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Sample paid booking
            pstmt.setString(1, "400");
            pstmt.setString(2, "101");
            pstmt.setString(3, "1");
            pstmt.setString(4, "1UP");
            pstmt.setString(5, "Karachi Express");
            pstmt.setString(6, "Karachi");
            pstmt.setString(7, "Lahore");
            pstmt.setDate(8, Date.valueOf(LocalDate.now().plusDays(2)));
            pstmt.setInt(9, 2);
            pstmt.setString(10, "Economy");
            pstmt.setDouble(11, 5000);
            pstmt.setString(12, "Confirmed");
            pstmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
            pstmt.setString(14, "Card");
            pstmt.setString(15, "Paid");
            pstmt.executeUpdate();
        }
    }

    // User operations
    public Optional<User> findUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUserFromResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findUserById(String id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUserFromResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUserFromResultSet(rs));
            }
        }
        return users;
    }

    public User addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (id, name, email, phone, role, password, cnic, date_of_birth, gender, address, city, postal_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setUserParameters(pstmt, user);
            pstmt.executeUpdate();
        }
        return user;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, role = ?, password = ?, cnic = ?, date_of_birth = ?, gender = ?, address = ?, city = ?, postal_code = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setUserParameters(pstmt, user);
            pstmt.setString(12, user.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean emailExists(String email, String excludeUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) AND id != ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, excludeUserId != null ? excludeUserId : "");
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public String getNextUserId() throws SQLException {
        return String.valueOf(getMaxId("users", "id") + 1);
    }

    // Train operations
    public List<Train> getAllTrains() throws SQLException {
        List<Train> trains = new ArrayList<>();
        String sql = "SELECT * FROM trains";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trains.add(mapTrainFromResultSet(rs));
            }
        }
        return trains;
    }

    public Optional<Train> findTrainById(String id) throws SQLException {
        String sql = "SELECT * FROM trains WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTrainFromResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Train> findTrainByNumber(String trainNumber) throws SQLException {
        String sql = "SELECT * FROM trains WHERE train_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, trainNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTrainFromResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Train addTrain(Train train) throws SQLException {
        String sql = "INSERT INTO trains (id, train_number, train_name, type, route, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, train.getId());
            pstmt.setString(2, train.getTrainNumber());
            pstmt.setString(3, train.getTrainName());
            pstmt.setString(4, train.getType());
            pstmt.setString(5, train.getRoute());
            pstmt.setString(6, train.getStatus());
            pstmt.executeUpdate();
        }
        return train;
    }

    public boolean updateTrain(Train train) throws SQLException {
        String sql = "UPDATE trains SET train_number = ?, train_name = ?, type = ?, route = ?, status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, train.getTrainNumber());
            pstmt.setString(2, train.getTrainName());
            pstmt.setString(3, train.getType());
            pstmt.setString(4, train.getRoute());
            pstmt.setString(5, train.getStatus());
            pstmt.setString(6, train.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean removeTrain(String id) throws SQLException {
        String sql = "DELETE FROM trains WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public String getNextTrainId() throws SQLException {
        return String.valueOf(getMaxId("trains", "id") + 1);
    }

    // Schedule operations
    public List<Schedule> getAllSchedules() throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                schedules.add(mapScheduleFromResultSet(rs));
            }
        }
        return schedules;
    }

    public Optional<Schedule> findScheduleByTrainNumber(String trainNumber) throws SQLException {
        String sql = "SELECT * FROM schedules WHERE train_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, trainNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapScheduleFromResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Schedule addSchedule(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO schedules (id, train_number, train_name, departure_time, arrival_time, route, days, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schedule.getId());
            pstmt.setString(2, schedule.getTrainNumber());
            pstmt.setString(3, schedule.getTrainName());
            pstmt.setString(4, schedule.getDepartureTime());
            pstmt.setString(5, schedule.getArrivalTime());
            pstmt.setString(6, schedule.getRoute());
            pstmt.setString(7, schedule.getDays());
            pstmt.setString(8, schedule.getStatus());
            pstmt.executeUpdate();
        }
        return schedule;
    }

    public boolean updateSchedule(Schedule schedule) throws SQLException {
        String sql = "UPDATE schedules SET train_number = ?, train_name = ?, departure_time = ?, arrival_time = ?, route = ?, days = ?, status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schedule.getTrainNumber());
            pstmt.setString(2, schedule.getTrainName());
            pstmt.setString(3, schedule.getDepartureTime());
            pstmt.setString(4, schedule.getArrivalTime());
            pstmt.setString(5, schedule.getRoute());
            pstmt.setString(6, schedule.getDays());
            pstmt.setString(7, schedule.getStatus());
            pstmt.setString(8, schedule.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean removeSchedule(String id) throws SQLException {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public String getNextScheduleId() throws SQLException {
        return String.valueOf(getMaxId("schedules", "id") + 1);
    }

    // Booking operations
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bookings.add(mapBookingFromResultSet(rs));
            }
        }
        return bookings;
    }

    public Optional<Booking> findBookingById(String id) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapBookingFromResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Booking addBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (id, user_id, train_id, train_number, train_name, from_station, to_station, travel_date, number_of_seats, seat_class, total_amount, status, booking_date_time, payment_method, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setBookingParameters(pstmt, booking);
            pstmt.executeUpdate();
        }
        return booking;
    }

    public boolean updateBooking(Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET user_id = ?, train_id = ?, train_number = ?, train_name = ?, from_station = ?, to_station = ?, travel_date = ?, number_of_seats = ?, seat_class = ?, total_amount = ?, status = ?, booking_date_time = ?, payment_method = ?, payment_status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, booking.getUserId());
            pstmt.setString(2, booking.getTrainId());
            pstmt.setString(3, booking.getTrainNumber());
            pstmt.setString(4, booking.getTrainName());
            pstmt.setString(5, booking.getFromStation());
            pstmt.setString(6, booking.getToStation());
            pstmt.setDate(7, Date.valueOf(booking.getTravelDate()));
            pstmt.setInt(8, booking.getNumberOfSeats());
            pstmt.setString(9, booking.getSeatClass());
            pstmt.setDouble(10, booking.getTotalAmount());
            pstmt.setString(11, booking.getStatus());
            pstmt.setTimestamp(12, Timestamp.valueOf(booking.getBookingDateTime()));
            pstmt.setString(13, booking.getPaymentMethod());
            pstmt.setString(14, booking.getPaymentStatus());
            pstmt.setString(15, booking.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public String getNextBookingId() throws SQLException {
        return String.valueOf(getMaxId("bookings", "id") + 1);
    }

    // Helper methods
    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("role"),
            rs.getString("password")
        );
        user.setCnic(rs.getString("cnic"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            user.setDateOfBirth(dob.toLocalDate());
        }
        user.setGender(rs.getString("gender"));
        user.setAddress(rs.getString("address"));
        user.setCity(rs.getString("city"));
        user.setPostalCode(rs.getString("postal_code"));
        return user;
    }

    private void setUserParameters(PreparedStatement pstmt, User user) throws SQLException {
        pstmt.setString(1, user.getId());
        pstmt.setString(2, user.getName());
        pstmt.setString(3, user.getEmail());
        pstmt.setString(4, user.getPhone());
        pstmt.setString(5, user.getRole());
        pstmt.setString(6, user.getPassword());
        pstmt.setString(7, user.getCnic());
        if (user.getDateOfBirth() != null) {
            pstmt.setDate(8, Date.valueOf(user.getDateOfBirth()));
        } else {
            pstmt.setNull(8, Types.DATE);
        }
        pstmt.setString(9, user.getGender());
        pstmt.setString(10, user.getAddress());
        pstmt.setString(11, user.getCity());
        pstmt.setString(12, user.getPostalCode());
    }

    private Train mapTrainFromResultSet(ResultSet rs) throws SQLException {
        return new Train(
            rs.getString("id"),
            rs.getString("train_number"),
            rs.getString("train_name"),
            rs.getString("type"),
            rs.getString("route"),
            rs.getString("status")
        );
    }

    private Schedule mapScheduleFromResultSet(ResultSet rs) throws SQLException {
        return new Schedule(
            rs.getString("id"),
            rs.getString("train_number"),
            rs.getString("train_name"),
            rs.getString("departure_time"),
            rs.getString("arrival_time"),
            rs.getString("route"),
            rs.getString("days"),
            rs.getString("status")
        );
    }

    private Booking mapBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking booking = new Booking(
            rs.getString("id"),
            rs.getString("user_id"),
            rs.getString("train_id"),
            rs.getString("train_number"),
            rs.getString("train_name"),
            rs.getString("from_station"),
            rs.getString("to_station"),
            rs.getDate("travel_date").toLocalDate(),
            rs.getInt("number_of_seats"),
            rs.getString("seat_class"),
            rs.getDouble("total_amount"),
            rs.getString("status"),
            rs.getTimestamp("booking_date_time").toLocalDateTime(),
            rs.getString("payment_method") != null ? rs.getString("payment_method") : "",
            rs.getString("payment_status") != null ? rs.getString("payment_status") : "Pending"
        );
        return booking;
    }

    private void setBookingParameters(PreparedStatement pstmt, Booking booking) throws SQLException {
        pstmt.setString(1, booking.getId());
        pstmt.setString(2, booking.getUserId());
        pstmt.setString(3, booking.getTrainId());
        pstmt.setString(4, booking.getTrainNumber());
        pstmt.setString(5, booking.getTrainName());
        pstmt.setString(6, booking.getFromStation());
        pstmt.setString(7, booking.getToStation());
        pstmt.setDate(8, Date.valueOf(booking.getTravelDate()));
        pstmt.setInt(9, booking.getNumberOfSeats());
        pstmt.setString(10, booking.getSeatClass());
        pstmt.setDouble(11, booking.getTotalAmount());
        pstmt.setString(12, booking.getStatus());
        pstmt.setTimestamp(13, Timestamp.valueOf(booking.getBookingDateTime()));
        pstmt.setString(14, booking.getPaymentMethod());
        pstmt.setString(15, booking.getPaymentStatus());
    }

    private int getMaxId(String table, String idColumn) throws SQLException {
        String sql = "SELECT MAX(CAST(" + idColumn + " AS INTEGER)) FROM " + table;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next() && rs.getObject(1) != null) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

