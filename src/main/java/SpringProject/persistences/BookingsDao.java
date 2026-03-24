package SpringProject.persistences;

import SpringProject.dtos.Bookings;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    Bookings getBookingsById(int bookingId) throws SQLException;
    boolean deleteBooking(int bookingId) throws SQLException;
    boolean addBookings(Bookings bookings) throws SQLException;
    boolean updateBooking(Bookings booking) throws SQLException;

}
