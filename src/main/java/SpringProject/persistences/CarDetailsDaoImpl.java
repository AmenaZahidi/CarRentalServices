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
                .dailyRate(calculateDailyRate(rs.getInt("carYear"), rs.getString("fuelType"), rs.getString("transmission")))
                .imageUrl(getCarImageUrl(rs.getString("make"), rs.getString("model")))
                .build();
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

    private static String getCarImageUrl(String make, String model) {
        String carName = ((make == null ? "" : make) + " " + (model == null ? "" : model)).toLowerCase();

        if (carName.contains("toyota")) {
            return "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("volkswagen") || carName.contains("golf")) {
            return "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("nissan") || carName.contains("leaf")) {
            return "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("bmw")) {
            return "https://images.unsplash.com/photo-1555215695-3004980ad54e?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("audi")) {
            return "https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("ford")) {
            return "https://images.unsplash.com/photo-1590362891991-f776e747a588?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("hyundai")) {
            return "https://images.unsplash.com/photo-1609521263047-f8f205293f24?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("tesla")) {
            return "https://images.unsplash.com/photo-1560958089-b8a1929cea89?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("kia")) {
            return "https://images.unsplash.com/photo-1609521263047-f8f205293f24?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("mercedes")) {
            return "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?q=80&w=1200&auto=format&fit=crop";
        }
        if (carName.contains("skoda")) {
            return "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?q=80&w=1200&auto=format&fit=crop";
        }

        return "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?q=80&w=1200&auto=format&fit=crop";
    }

    @Override
    public List<CarDetails> getAllCars() throws SQLException {
        List<CarDetails> cars = new ArrayList<>();

        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getAllCars(): Could not establish connection to database.");

        ensureSampleCars(conn);

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

        ensureSampleCars(conn);

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

    private void ensureSampleCars(Connection conn) throws SQLException {
        String sql = """
                INSERT IGNORE INTO carDetails
                    (regNumber, make, model, carYear, colour, mileage, transmission, currentStatus, fuelType)
                VALUES
                    ('222D45781', 'BMW', '3 Series', 2022, 'Black', 18000, 'automatic', 'available', 'petrol'),
                    ('231D77890', 'Audi', 'A4', 2023, 'Grey', 12000, 'automatic', 'available', 'diesel'),
                    ('202D33445', 'Ford', 'Focus', 2020, 'Red', 39000, 'manual', 'available', 'petrol'),
                    ('211L90876', 'Hyundai', 'Tucson', 2021, 'White', 28000, 'automatic', 'available', 'hybrid'),
                    ('232C11223', 'Tesla', 'Model 3', 2023, 'Blue', 9000, 'automatic', 'available', 'electric'),
                    ('191G66118', 'Kia', 'Sportage', 2019, 'Silver', 47000, 'manual', 'available', 'diesel'),
                    ('221D77001', 'Mercedes-Benz', 'C-Class', 2022, 'Navy', 21000, 'automatic', 'available', 'petrol'),
                    ('201D88002', 'Skoda', 'Octavia', 2020, 'Green', 36000, 'manual', 'available', 'diesel'),
                    ('241D99112', 'Toyota', 'Yaris Cross', 2024, 'Pearl', 5000, 'automatic', 'available', 'hybrid')
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
