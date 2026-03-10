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
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getBookingsById(): Could not establish connection to database.");

        String sql = """
                SELECT bookingId, pickupDateTime, returnDateTime, status, totalPrice
                FROM Bookings
                WHERE bookingId = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBookingsRow(rs);
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
        String sql="DELETE FROM users WHERE bookingId =?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, bookingId);
            return ps.executeUpdate()>0;
        }catch(SQLException e){
            log.info("deleteBooking(): SQL Exception occurred when attempting to prepare SQL for execution" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean AddBookings(Bookings bookings) throws SQLException {
        boolean added = false;
        conn = connector.getConnection();

        if (bookings == null) {
            throw new IllegalArgumentException("Booking cant be null");
        }
        if (connector == null) {
            throw new SQLException("Could not establish connection to database");
        }
        int addedRows = 0;
        String sql = "INSERT INTO bookings (bookingId, pickupDateTime, returnDateTime, status, totalPrice) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookings.getBookingId());
            ps.setTimestamp(2, new java.sql.Timestamp(bookings.getPickupDateTime().getTime()));
            ps.setTimestamp(3,new java.sql.Timestamp(bookings.getReturnDateTime().getTime()));
            ps.setString(4, bookings.getStatus());
            ps.setDouble(5, bookings.getTotalPrice());
            addedRows = ps.executeUpdate();
        }
        catch (SQLException e){
            log.error(("The SQL query could not be executed: " + e.getMessage()));
            throw e;
        }
        return addedRows ==1;
    }

    @Override
    public boolean updateBooking(Bookings bookings) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("updateBookingStatus(): Could not establish connection to database.");

        String sql = "UPDATE Bookings SET pickupDateTime = ?, returnDateTime= ?, totalPrice = ?, status = ? " +
                " WHERE bookingId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookings.getBookingId());
            ps.setTimestamp(2, new java.sql.Timestamp(bookings.getPickupDateTime().getTime()));
            ps.setTimestamp(3,new java.sql.Timestamp(bookings.getReturnDateTime().getTime()));
            ps.setString(4, bookings.getStatus());
            ps.setDouble(5, bookings.getTotalPrice());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("updateBookingStatus(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
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
