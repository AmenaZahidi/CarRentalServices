package SpringProject.services;

import SpringProject.dtos.Bookings;
import SpringProject.dtos.Payment;
import SpringProject.persistences.PaymentDao;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentDao paymentDao;
    private final BookingService bookingService;

    public PaymentService(PaymentDao paymentDao, BookingService bookingService) {
        this.paymentDao = paymentDao;
        this.bookingService = bookingService;
    }

    public Payment getPaymentByBookingId(int bookingId) throws SQLException {
        return paymentDao.getPaymentByBookingId(bookingId);
    }

    public List<Payment> getAllPayments() throws SQLException {
        return paymentDao.getAllPayments();
    }

    public Bookings getBookingForPayment(int bookingId) throws SQLException {
        return bookingService.getBookingById(bookingId);
    }

    public Payment processPayment(int bookingId,
                                  String cardholderName,
                                  String cardNumber,
                                  String expiry,
                                  String cvv,
                                  String paymentMethod,
                                  String paypalEmail) throws SQLException {
        Bookings booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }

        Payment existingPayment = paymentDao.getPaymentByBookingId(bookingId);
        if (existingPayment != null) {
            return existingPayment;
        }

        if (booking.getTotalPrice() == null || booking.getTotalPrice() <= 0) {
            throw new IllegalArgumentException("Booking total must be greater than zero");
        }

        String cleanPaymentMethod = normalisePaymentMethod(paymentMethod);
        if ("paypal".equals(cleanPaymentMethod)) {
            validatePayPalInput(paypalEmail);
        } else {
            validatePaymentInput(cardholderName, cardNumber, expiry, cvv);
        }

        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .amount(booking.getTotalPrice())
                .paymentStatus("paid")
                .transactionRef(buildTransactionRef(cleanPaymentMethod))
                .build();

        if (!paymentDao.addPayment(payment)) {
            throw new SQLException("Payment could not be saved");
        }

        return payment;
    }

    private void validatePaymentInput(String cardholderName,
                                      String cardNumber,
                                      String expiry,
                                      String cvv) {
        if (cardholderName == null || cardholderName.trim().length() < 2) {
            throw new IllegalArgumentException("Cardholder name is required");
        }

        String digitsOnly = normaliseDigits(cardNumber);
        if (digitsOnly.length() != 16) {
            throw new IllegalArgumentException("Card number must contain 16 digits");
        }

        String cleanExpiry = expiry == null ? "" : expiry.trim();
        if (!cleanExpiry.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            throw new IllegalArgumentException("Expiry must be in MM/YY format");
        }

        String cleanCvv = cvv == null ? "" : cvv.trim();
        if (!cleanCvv.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV must contain 3 or 4 digits");
        }
    }

    private String normalisePaymentMethod(String paymentMethod) {
        if (paymentMethod == null) {
            return "card";
        }

        String cleanPaymentMethod = paymentMethod.trim().toLowerCase();
        return "paypal".equals(cleanPaymentMethod) ? "paypal" : "card";
    }

    private String buildTransactionRef(String paymentMethod) {
        String prefix = "paypal".equals(paymentMethod) ? "SIM-PAYPAL-" : "SIM-CARD-";
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validatePayPalInput(String paypalEmail) {
        if (paypalEmail == null || !paypalEmail.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Enter a valid PayPal email address");
        }
    }

    private String normaliseDigits(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (Character.isDigit(character)) {
                digits.append(Character.getNumericValue(character));
            }
        }
        return digits.toString();
    }
}
