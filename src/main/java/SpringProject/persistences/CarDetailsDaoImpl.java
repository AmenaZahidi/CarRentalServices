package SpringProject.persistences;

import SpringProject.dtos.CarDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class CarDetailsDaoImpl implements CarDetailsDao {

    private final Connector connector;

    public CarDetailsDaoImpl(Connector connector) {
        this.connector = connector;
    }

    private static CarDetails mapCarRow(ResultSet rs) throws SQLException {
        return CarDetails.builder()
                .carId(rs.getInt("carId"))
                .regNumber(rs.getString("regNumber"))
                .make(rs.getString("make"))
                .model(rs.getString("model"))
                .carYear(rs.getInt("carYear"))
                .colour(rs.getString("colour"))
                .mileage((Integer) rs.getObject("mileage"))
                .transmission(rs.getString("transmission"))
                .currentStatus(rs.getString("currentStatus"))
                .fuelType(rs.getString("fuelType"))
                .build();
    }

    @Override
    public List<CarDetails> getAllCars() throws SQLException {
        List<CarDetails> cars = new ArrayList<>();

        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getAllCars(): Could not establish connection to database.");

        String sql = """
                SELECT carId, regNumber, make, model, carYear, colour, mileage,
                       transmission, currentStatus, fuelType
                FROM carDetails
                ORDER BY carId
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) cars.add(mapCarRow(rs));

        } catch (SQLException e) {
            log.error("getAllCars(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return cars;
    }

    @Override
    public CarDetails getCarById(int carID) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getCarById(): Could not establish connection to database.");

        String sql = """
                SELECT carId, regNumber, make, model, carYear, colour, mileage,
                       transmission, currentStatus, fuelType
                FROM carDetails
                WHERE carId = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, carID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCarRow(rs);
            }

        } catch (SQLException e) {
            log.error("getCarById(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return null;
    }

    @Override
    public CarDetails getCarByRegNumber(String regNumber) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getCarByRegNumber(): Could not establish connection to database.");

        String sql = """
                SELECT carId, regNumber, make, model, carYear, colour, mileage,
                       transmission, currentStatus, fuelType
                FROM carDetails
                WHERE regNumber = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCarRow(rs);
            }

        } catch (SQLException e) {
            log.error("getCarByRegNumber(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return null;
    }

    @Override
    public List<CarDetails> getAvailableCars(LocalDate pickUpDate, LocalDate returnDate) throws SQLException {
        // currentStatus availblea (no bookings table yet)
        List<CarDetails> cars = new ArrayList<>();

        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getAvailableCars(): Could not establish connection to database.");

        String sql = """
                SELECT carId, regNumber, make, model, carYear, colour, mileage,
                       transmission, currentStatus, fuelType
                FROM carDetails
                WHERE currentStatus = 'available'
                ORDER BY carId
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) cars.add(mapCarRow(rs));

        } catch (SQLException e) {
            log.error("getAvailableCars(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return cars;
    }

    @Override
    public int addCar(CarDetails car) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("addCar(): Could not establish connection to database.");

        String status = (car.getCurrentStatus() == null || car.getCurrentStatus().isBlank())
                ? "available"
                : car.getCurrentStatus();

        String sql = """
                INSERT INTO carDetails
                (regNumber, make, model, carYear, colour, mileage,
                 transmission, currentStatus, fuelType)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, car.getRegNumber());
            ps.setString(2, car.getMake());
            ps.setString(3, car.getModel());
            ps.setInt(4, car.getCarYear());
            ps.setString(5, car.getColour());
            ps.setObject(6, car.getMileage());
            ps.setString(7, car.getTransmission());
            ps.setString(8, status);
            ps.setString(9, car.getFuelType());

            int rows = ps.executeUpdate();
            if (rows == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            log.error("addCar(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return -1;
    }

    @Override
    public boolean updateCarStatus(int carID, String currentStatus) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("updateCarStatus(): Could not establish connection to database.");

        String sql = "UPDATE carDetails SET currentStatus = ? WHERE carId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentStatus);
            ps.setInt(2, carID);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("updateCarStatus(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }
}
