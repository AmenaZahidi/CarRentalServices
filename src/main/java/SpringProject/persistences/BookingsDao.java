package SpringProject.persistences;

import SpringProject.dtos.Bookings;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
/**bookingId INT AUTO_INCREMENT PRIMARY KEY,
driverId INT NOT NULL,
userId INT NOT NULL,
carId INT NOT NULL,
pickupDatetime DATETIME NOT NULL,
returnDatetime DATETIME NOT NULL,
pickupLocationId INT,
totalPrice DECIMAL(10, 2),
status ENUM('confirmed', 'active', 'returned', 'cancelled') DEFAULT 'confirmed',
 */
public interface BookingsDao {
    List<Bookings> getAllBookings() throws SQLException;
    List<Bookings> getBookingsByUserId(int userId) throws SQLException;
    Bookings getBookingsById(int bookingId) throws SQLException;
    boolean deleteBooking(int bookingId) throws SQLException;
    boolean addBookings(Bookings bookings) throws SQLException;
    boolean updateBooking(Bookings booking) throws SQLException;
    boolean updateBookingDriver(int bookingId, int driverId) throws SQLException;
    boolean userExists(int userId) throws SQLException;
    boolean carExists(int carId) throws SQLException;
    boolean driverExists(int driverId) throws SQLException;
    boolean driverHasCompleteProfile(int driverId) throws SQLException;
    boolean driverCanDriveCar(int driverId, int carId) throws SQLException;
    boolean locationExists(int locationId) throws SQLException;
    double getCarDailyRate(int carId) throws SQLException;
    List<Map<String, Object>> getCarOptions() throws SQLException;
    List<Map<String, Object>> getDriverOptions() throws SQLException;
    List<Map<String, Object>> getLocationOptions() throws SQLException;

}
