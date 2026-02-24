package SpringProject.persistences;

import SpringProject.dtos.Bookings;
import SpringProject.dtos.CarDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class BookingDaoImpl implements BookingsDao {
    private Connector connector;
    private Connection conn;
public BookingDaoImpl(Connector connector) {this.connector = connector;}
    @Override
    public List<Bookings> getAllBookings() throws SQLException {
        List<Bookings> bookings = new ArrayList<>();

        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getAllBookings(): Could not establish connection to database.");

        String sql = """
                SELECT bookingId, pickupDateTime, returnDateTime, status, totalPrice
                FROM Bookings
                ORDER BY bookingId
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) bookings.add(mapBookingsRow(rs));

        } catch (SQLException e) {
            log.error("getAllBookings(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return bookings;
    }

    @Override
    public Bookings getBookingsById(int bookingId) throws SQLException {
        return null;
    }

    @Override
    public boolean deleteBooking(int bookingId) throws SQLException {
        return false;
    }

    @Override
    public boolean AddBookings(Bookings bookings) throws SQLException {
        return false;
    }

    @Override
    public boolean updateBooking(Bookings booking) throws SQLException {
        return false;
    }

    private static Bookings mapBookingsRow(ResultSet rs) throws SQLException{
        return Bookings.builder()
                .bookingId(rs.getInt("bookingId"))
                .pickupDateTime(rs.getDate("pickupDateTime"))
                .returnDateTime(rs.getDate("returnDateTime"))
                .totalPrice(rs.getDouble("totalPrice"))
                .status(rs.getString("status"))
                .build();
    }
}
