package SpringProject.persistences;

import SpringProject.dtos.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class PaymentDaoImpl implements PaymentDao {
    private final Connector connector;

    public PaymentDaoImpl(Connector connector) {
        this.connector = connector;
    }

    @Override
    public boolean addPayment(Payment payment) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("addPayment(): Could not establish connection to database.");

        boolean hasPaymentStatus = hasColumn(conn, "payment", "paymentStatus");
        boolean hasTransactionRef = hasColumn(conn, "payment", "transactionRef");

        String sql = hasPaymentStatus && hasTransactionRef
                ? "INSERT INTO payment (bookingId, amount, paymentStatus, transactionRef) VALUES (?, ?, ?, ?)"
                : "INSERT INTO payment (bookingId, amount) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment.getBookingId());
            ps.setDouble(2, payment.getAmount());
            if (hasPaymentStatus && hasTransactionRef) {
                ps.setString(3, payment.getPaymentStatus());
                ps.setString(4, payment.getTransactionRef());
            }
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("addPayment(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public Payment getPaymentByBookingId(int bookingId) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getPaymentByBookingId(): Could not establish connection to database.");

        boolean hasPaymentStatus = hasColumn(conn, "payment", "paymentStatus");
        boolean hasTransactionRef = hasColumn(conn, "payment", "transactionRef");

        String sql = hasPaymentStatus && hasTransactionRef
                ? """
                  SELECT paymentId, bookingId, paymentDate, amount, paymentStatus, transactionRef
                  FROM payment
                  WHERE bookingId = ?
                  ORDER BY paymentId DESC
                  LIMIT 1
                  """
                : """
                  SELECT paymentId, bookingId, paymentDate, amount
                  FROM payment
                  WHERE bookingId = ?
                  ORDER BY paymentId DESC
                  LIMIT 1
                  """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Payment.builder()
                            .paymentId(rs.getInt("paymentId"))
                            .bookingId(rs.getInt("bookingId"))
                            .paymentDate(rs.getTimestamp("paymentDate"))
                            .amount(rs.getDouble("amount"))
                            .paymentStatus(hasPaymentStatus ? rs.getString("paymentStatus") : "paid")
                            .transactionRef(hasTransactionRef ? rs.getString("transactionRef") : "SIM-" + rs.getInt("paymentId"))
                            .build();
                }
            }
        } catch (SQLException e) {
            log.error("getPaymentByBookingId(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }

        return null;
    }

    @Override
    public List<Payment> getAllPayments() throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getAllPayments(): Could not establish connection to database.");

        boolean hasPaymentStatus = hasColumn(conn, "payment", "paymentStatus");
        boolean hasTransactionRef = hasColumn(conn, "payment", "transactionRef");

        String sql = hasPaymentStatus && hasTransactionRef
                ? """
                  SELECT paymentId, bookingId, paymentDate, amount, paymentStatus, transactionRef
                  FROM payment
                  ORDER BY paymentDate DESC, paymentId DESC
                  """
                : """
                  SELECT paymentId, bookingId, paymentDate, amount
                  FROM payment
                  ORDER BY paymentDate DESC, paymentId DESC
                  """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Payment> payments = new ArrayList<>();
            while (rs.next()) {
                payments.add(Payment.builder()
                        .paymentId(rs.getInt("paymentId"))
                        .bookingId(rs.getInt("bookingId"))
                        .paymentDate(rs.getTimestamp("paymentDate"))
                        .amount(rs.getDouble("amount"))
                        .paymentStatus(hasPaymentStatus ? rs.getString("paymentStatus") : "paid")
                        .transactionRef(hasTransactionRef ? rs.getString("transactionRef") : "SIM-" + rs.getInt("paymentId"))
                        .build());
            }
            return payments;
        } catch (SQLException e) {
            log.error("getAllPayments(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return rs.next();
        }
    }
}
