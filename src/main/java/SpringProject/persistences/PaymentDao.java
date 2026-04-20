package SpringProject.persistences;

import SpringProject.dtos.Payment;

import java.sql.SQLException;
import java.util.List;

public interface PaymentDao {
    boolean addPayment(Payment payment) throws SQLException;
    Payment getPaymentByBookingId(int bookingId) throws SQLException;
    List<Payment> getAllPayments() throws SQLException;
}
