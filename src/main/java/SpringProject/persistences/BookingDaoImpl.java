package SpringProject.persistences;

import SpringProject.dtos.Bookings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class BookingDaoImpl implements BookingsDao {
    private final Connector connector;

    public BookingDaoImpl(Connector connector) {
        this.connector = connector;
    }

    @Override
    public List<Bookings> getAllBookings() throws SQLException {
        List<Bookings> bookings = new ArrayList<>();
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getAllBookings(): Could not establish connection to database.");

        String sql = """
                SELECT bookingId, driverId, userId, carId, pickupLocationId,
                       pickupDatetime, returnDatetime, status, totalPrice
                FROM bookings
                ORDER BY bookingId
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookings.add(mapBookingsRow(rs));
            }
        } catch (SQLException e) {
            log.error("getAllBookings(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return bookings;
    }

    @Override
    public List<Map<String, Object>> getAdminBookingSummaries() throws SQLException {
        List<Map<String, Object>> bookings = new ArrayList<>();
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getAdminBookingSummaries(): Could not establish connection to database.");

        ensureReturnInspectionsTable(conn);

        String sql = """
                SELECT b.bookingId, b.userId, u.username, u.email,
                       b.carId, c.make, c.model, c.regNumber,
                       b.driverId, d.firstName, d.lastName, d.licenseNumber,
                       b.pickupDatetime, b.returnDatetime, b.status, b.totalPrice,
                       p.paymentId,
                       MAX(ri.inspectionId) AS inspectionId,
                       COUNT(ri.inspectionId) AS inspectionCount
                FROM bookings b
                JOIN users u ON u.userId = b.userId
                JOIN carDetails c ON c.carId = b.carId
                JOIN driverdetails d ON d.driverId = b.driverId
                LEFT JOIN payment p ON p.bookingId = b.bookingId
                LEFT JOIN return_inspections ri ON ri.bookingId = b.bookingId
                GROUP BY b.bookingId, b.userId, u.username, u.email,
                         b.carId, c.make, c.model, c.regNumber,
                         b.driverId, d.firstName, d.lastName, d.licenseNumber,
                         b.pickupDatetime, b.returnDatetime, b.status, b.totalPrice,
                         p.paymentId
                ORDER BY b.pickupDatetime DESC, b.bookingId DESC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> booking = new LinkedHashMap<>();
                booking.put("bookingId", rs.getInt("bookingId"));
                booking.put("userId", rs.getInt("userId"));
                booking.put("customerName", rs.getString("username"));
                booking.put("customerEmail", rs.getString("email"));
                booking.put("carId", rs.getInt("carId"));
                booking.put("carName", rs.getString("make") + " " + rs.getString("model"));
                booking.put("regNumber", rs.getString("regNumber"));
                booking.put("driverId", rs.getInt("driverId"));
                booking.put("driverName", rs.getString("firstName") + " " + rs.getString("lastName"));
                booking.put("licenseNumber", rs.getString("licenseNumber"));
                booking.put("pickupDateTime", rs.getTimestamp("pickupDatetime"));
                booking.put("returnDateTime", rs.getTimestamp("returnDatetime"));
                booking.put("status", rs.getString("status"));
                booking.put("totalPrice", rs.getDouble("totalPrice"));
                booking.put("paid", rs.getObject("paymentId") != null);
                booking.put("inspectionId", rs.getObject("inspectionId"));
                booking.put("inspectionCount", rs.getInt("inspectionCount"));
                booking.put("inspected", rs.getInt("inspectionCount") > 0);
                bookings.add(booking);
            }
        } catch (SQLException e) {
            log.error("getAdminBookingSummaries(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return bookings;
    }

    @Override
    public List<Bookings> getBookingsByUserId(int userId) throws SQLException {
        List<Bookings> bookings = new ArrayList<>();
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getBookingsByUserId(): Could not establish connection to database.");

        String sql = """
                SELECT bookingId, driverId, userId, carId, pickupLocationId,
                       pickupDatetime, returnDatetime, status, totalPrice
                FROM bookings
                WHERE userId = ?
                ORDER BY bookingId
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapBookingsRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("getBookingsByUserId(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return bookings;
    }

    @Override
    public Bookings getBookingsById(int bookingId) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getBookingsById(): Could not establish connection to database.");

        String sql = """
                SELECT bookingId, driverId, userId, carId, pickupLocationId,
                       pickupDatetime, returnDatetime, status, totalPrice
                FROM bookings
                WHERE bookingId = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBookingsRow(rs);
                }
            }
        } catch (SQLException e) {
            log.error("getBookingsById(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return null;
    }

    @Override
    public boolean deleteBooking(int bookingId) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("deleteBooking(): Could not establish connection to database.");

        String sql = "DELETE FROM bookings WHERE bookingId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("deleteBooking(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public boolean addBookings(Bookings bookings) throws SQLException {
        if (bookings == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("addBookings(): Could not establish connection to database.");

        String sql = """
                INSERT INTO bookings
                    (driverId, userId, carId, pickupLocationId, pickupDatetime, returnDatetime, status, totalPrice)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookings.getDriverId());
            ps.setInt(2, bookings.getUserId());
            ps.setInt(3, bookings.getCarId());
            ps.setInt(4, bookings.getPickupLocationId());
            ps.setTimestamp(5, new java.sql.Timestamp(bookings.getPickupDateTime().getTime()));
            ps.setTimestamp(6, new java.sql.Timestamp(bookings.getReturnDateTime().getTime()));
            ps.setString(7, bookings.getStatus());
            ps.setDouble(8, bookings.getTotalPrice());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("addBookings(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public boolean updateBooking(Bookings bookings) throws SQLException {
        if (bookings == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("updateBooking(): Could not establish connection to database.");

        String sql = """
                UPDATE bookings
                SET driverId = ?, userId = ?, carId = ?, pickupLocationId = ?,
                    pickupDatetime = ?, returnDatetime = ?, totalPrice = ?, status = ?
                WHERE bookingId = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookings.getDriverId());
            ps.setInt(2, bookings.getUserId());
            ps.setInt(3, bookings.getCarId());
            ps.setInt(4, bookings.getPickupLocationId());
            ps.setTimestamp(5, new java.sql.Timestamp(bookings.getPickupDateTime().getTime()));
            ps.setTimestamp(6, new java.sql.Timestamp(bookings.getReturnDateTime().getTime()));
            ps.setDouble(7, bookings.getTotalPrice());
            ps.setString(8, bookings.getStatus());
            ps.setInt(9, bookings.getBookingId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updateBooking(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public boolean updateBookingDriver(int bookingId, int driverId) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("updateBookingDriver(): Could not establish connection to database.");

        String sql = "UPDATE bookings SET driverId = ? WHERE bookingId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updateBookingDriver(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public boolean userExists(int userId) throws SQLException {
        return existsById("users", "userId", userId);
    }

    @Override
    public boolean carExists(int carId) throws SQLException {
        return existsById("carDetails", "carId", carId);
    }

    @Override
    public boolean driverExists(int driverId) throws SQLException {
        return existsById("driverdetails", "driverId", driverId);
    }

    @Override
    public boolean driverHasCompleteProfile(int driverId) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("driverHasCompleteProfile(): Could not establish connection to database.");

        ensureDriverLicenceColumns(conn);

        String sql = """
                SELECT 1
                FROM driverdetails
                WHERE driverId = ?
                  AND firstName IS NOT NULL AND firstName <> ''
                  AND lastName IS NOT NULL AND lastName <> ''
                  AND email IS NOT NULL AND email <> ''
                  AND licenseNumber IS NOT NULL AND licenseNumber <> ''
                  AND dateOfBirth IS NOT NULL
                  AND permitType IN ('manual', 'automatic')
                  AND licenseProofPath IS NOT NULL AND licenseProofPath <> ''
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("driverHasCompleteProfile(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public boolean driverCanDriveCar(int driverId, int carId) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("driverCanDriveCar(): Could not establish connection to database.");

        ensureDriverLicenceColumns(conn);

        String sql = """
                SELECT d.permitType, c.transmission
                FROM driverdetails d
                JOIN carDetails c ON c.carId = ?
                WHERE d.driverId = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carId);
            ps.setInt(2, driverId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String permitType = rs.getString("permitType");
                String transmission = rs.getString("transmission");
                return "manual".equalsIgnoreCase(permitType)
                        || "automatic".equalsIgnoreCase(transmission);
            }
        } catch (SQLException e) {
            log.error("driverCanDriveCar(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public boolean locationExists(int locationId) throws SQLException {
        return existsById("location", "locationId", locationId);
    }

    @Override
    public double getCarDailyRate(int carId) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getCarDailyRate(): Could not establish connection to database.");

        String sql = """
                SELECT carYear, fuelType, transmission
                FROM carDetails
                WHERE carId = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return calculateDailyRate(rs.getInt("carYear"),
                            rs.getString("fuelType"),
                            rs.getString("transmission"));
                }
            }
        } catch (SQLException e) {
            log.error("getCarDailyRate(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        throw new SQLException("Car not found.");
    }

    @Override
    public List<Map<String, Object>> getCarOptions() throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getCarOptions(): Could not establish connection to database.");

        String sql = """
                SELECT carId, make, model, regNumber, carYear, fuelType, transmission
                FROM carDetails
                ORDER BY make, model
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> cars = new ArrayList<>();
            while (rs.next()) {
                double dailyRate = calculateDailyRate(rs.getInt("carYear"),
                        rs.getString("fuelType"),
                        rs.getString("transmission"));
                Map<String, Object> car = new LinkedHashMap<>();
                car.put("id", rs.getInt("carId"));
                car.put("label", rs.getString("make") + " " + rs.getString("model")
                        + " (" + rs.getString("regNumber") + ") - EUR " + String.format("%.2f", dailyRate) + "/day");
                car.put("dailyRate", dailyRate);
                car.put("transmission", rs.getString("transmission"));
                cars.add(car);
            }
            return cars;
        } catch (SQLException e) {
            log.error("getCarOptions(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public List<Map<String, Object>> getDriverOptions() throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getDriverOptions(): Could not establish connection to database.");

        String sql = """
                SELECT driverId, firstName, lastName, email, licenseNumber, permitType
                FROM driverdetails
                WHERE firstName IS NOT NULL AND firstName <> ''
                  AND lastName IS NOT NULL AND lastName <> ''
                  AND email IS NOT NULL AND email <> ''
                  AND licenseNumber IS NOT NULL AND licenseNumber <> ''
                  AND dateOfBirth IS NOT NULL
                  AND permitType IN ('manual', 'automatic')
                  AND licenseProofPath IS NOT NULL AND licenseProofPath <> ''
                ORDER BY firstName, lastName
                """;

        ensureDriverLicenceColumns(conn);

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> drivers = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> driver = new LinkedHashMap<>();
                driver.put("id", rs.getInt("driverId"));
                driver.put("label", rs.getString("firstName") + " " + rs.getString("lastName")
                        + " - " + rs.getString("licenseNumber")
                        + " (" + rs.getString("permitType") + ")");
                driver.put("permitType", rs.getString("permitType"));
                driver.put("email", rs.getString("email"));
                drivers.add(driver);
            }
            return drivers;
        } catch (SQLException e) {
            log.error("getDriverOptions(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public List<Map<String, Object>> getLocationOptions() throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getLocationOptions(): Could not establish connection to database.");

        String sql = """
                SELECT locationId, branchName, address, phoneNumber
                FROM location
                ORDER BY branchName
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> locations = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> location = new LinkedHashMap<>();
                location.put("id", rs.getInt("locationId"));
                location.put("label", rs.getString("branchName") + " - " + rs.getString("address"));
                location.put("phoneNumber", rs.getString("phoneNumber"));
                locations.add(location);
            }
            return locations;
        } catch (SQLException e) {
            log.error("getLocationOptions(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    private boolean existsById(String tableName, String idColumn, int id) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("existsById(): Could not establish connection to database.");

        String sql = "SELECT 1 FROM " + tableName + " WHERE " + idColumn + " = ? LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("existsById(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    private void ensureDriverLicenceColumns(Connection conn) throws SQLException {
        if (!hasColumn(conn, "driverdetails", "licenseProofPath")) {
            try (java.sql.Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE driverdetails ADD COLUMN licenseProofPath VARCHAR(255)");
            }
        }

        if (!hasColumn(conn, "driverdetails", "licenseVerified")) {
            try (java.sql.Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE driverdetails ADD COLUMN licenseVerified BOOLEAN NOT NULL DEFAULT FALSE");
            }
        }
    }

    private void ensureReturnInspectionsTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS return_inspections (
                    inspectionId INT AUTO_INCREMENT PRIMARY KEY,
                    bookingId INT NOT NULL,
                    inspectedByUserId INT NOT NULL,
                    actualReturnDate DATE NOT NULL,
                    returnedOnTime BOOLEAN NOT NULL DEFAULT TRUE,
                    damageFound BOOLEAN NOT NULL DEFAULT FALSE,
                    damageNotes TEXT,
                    mileageIn INT,
                    fuelLevel VARCHAR(20),
                    FOREIGN KEY (bookingId) REFERENCES bookings(bookingId) ON DELETE CASCADE,
                    FOREIGN KEY (inspectedByUserId) REFERENCES users(userId)
                )
                """;

        try (java.sql.Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        java.sql.DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static double calculateDailyRate(int carYear, String fuelType, String transmission) {
        double rate = 42.0;

        if (carYear >= 2022) {
            rate += 18.0;
        } else if (carYear >= 2020) {
            rate += 10.0;
        } else if (carYear <= 2018) {
            rate -= 4.0;
        }

        if ("electric".equalsIgnoreCase(fuelType)) {
            rate += 14.0;
        } else if ("hybrid".equalsIgnoreCase(fuelType)) {
            rate += 9.0;
        } else if ("diesel".equalsIgnoreCase(fuelType)) {
            rate += 5.0;
        }

        if ("automatic".equalsIgnoreCase(transmission)) {
            rate += 6.0;
        }

        return Math.max(rate, 35.0);
    }

    private static Bookings mapBookingsRow(ResultSet rs) throws SQLException {
        return Bookings.builder()
                .bookingId(rs.getInt("bookingId"))
                .driverId(rs.getInt("driverId"))
                .userId(rs.getInt("userId"))
                .carId(rs.getInt("carId"))
                .pickupLocationId(rs.getInt("pickupLocationId"))
                .pickupDateTime(rs.getTimestamp("pickupDatetime"))
                .returnDateTime(rs.getTimestamp("returnDatetime"))
                .totalPrice(rs.getDouble("totalPrice"))
                .status(rs.getString("status"))
                .build();
    }
}
