package SpringProject.services;

import SpringProject.dtos.Bookings;
import SpringProject.persistences.BookingsDao;

import java.sql.SQLException;
import java.util.List;

public class BookingService  {
    private final BookingsDao bookingsDao;

    public BookingService(BookingsDao dao) {
        this.bookingsDao = dao;
    }

    public boolean addBooking(Bookings booking) throws SQLException {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        return bookingsDao.AddBookings(booking);
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

    public boolean updateBooking(Bookings booking) throws SQLException {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        if (booking.getBookingId() <= 0) {
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



}
