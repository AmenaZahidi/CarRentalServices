package SpringProject.services;

import SpringProject.dtos.Bookings;
import SpringProject.persistences.BookingsDao;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;


@Service
public class BookingService  {
    private final BookingsDao bookingsDao;

    public BookingService(BookingsDao dao) {
        this.bookingsDao = dao;
    }

    public boolean addBooking(Bookings booking) throws SQLException {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        return bookingsDao.addBookings(booking);
    }

    public Bookings getBookingById(int bookingId) throws SQLException {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("Invalid booking Id");
        }
        return bookingsDao.getBookingsById(bookingId);
    }

    public List<Bookings> getAllBookings() throws SQLException {
        return bookingsDao.getAllBookings();
    }

    public List<Map<String, Object>> getAdminBookingSummaries() throws SQLException {
        return bookingsDao.getAdminBookingSummaries();
    }

    public List<Bookings> getBookingsByUserId(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user Id");
        }
        return bookingsDao.getBookingsByUserId(userId);
    }

    public boolean belongsToUser(Bookings booking, int userId) {
        return booking != null && booking.getUserId() != null && booking.getUserId() == userId;
    }

    public boolean updateBooking(Bookings booking) throws SQLException {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        if (booking.getBookingId() == null || booking.getBookingId() <= 0) {
            throw new IllegalArgumentException("Invalid booking Id");
        }
        return bookingsDao.updateBooking(booking);
    }
    public boolean deleteBooking(int bookingId) throws SQLException {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("Invalid booking Id");
        }
        return bookingsDao.deleteBooking(bookingId);
    }

    public boolean updateBookingDriver(int bookingId, int driverId) throws SQLException {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("Invalid booking Id");
        }
        if (driverId <= 0) {
            throw new IllegalArgumentException("Invalid driver Id");
        }
        return bookingsDao.updateBookingDriver(bookingId, driverId);
    }

    public boolean userExists(int userId) throws SQLException {
        return userId > 0 && bookingsDao.userExists(userId);
    }

    public boolean carExists(int carId) throws SQLException {
        return carId > 0 && bookingsDao.carExists(carId);
    }

    public boolean driverExists(int driverId) throws SQLException {
        return driverId > 0 && bookingsDao.driverExists(driverId);
    }

    public boolean driverHasCompleteProfile(int driverId) throws SQLException {
        return driverId > 0 && bookingsDao.driverHasCompleteProfile(driverId);
    }

    public boolean driverCanDriveCar(int driverId, int carId) throws SQLException {
        return driverId > 0 && carId > 0 && bookingsDao.driverCanDriveCar(driverId, carId);
    }

    public boolean locationExists(int locationId) throws SQLException {
        return locationId > 0 && bookingsDao.locationExists(locationId);
    }

    public double getCarDailyRate(int carId) throws SQLException {
        if (carId <= 0) {
            throw new IllegalArgumentException("Invalid car Id");
        }
        return bookingsDao.getCarDailyRate(carId);
    }

    public List<Map<String, Object>> getCarOptions() throws SQLException {
        return bookingsDao.getCarOptions();
    }

    public List<Map<String, Object>> getDriverOptions() throws SQLException {
        return bookingsDao.getDriverOptions();
    }

    public List<Map<String, Object>> getLocationOptions() throws SQLException {
        return bookingsDao.getLocationOptions();
    }



}
