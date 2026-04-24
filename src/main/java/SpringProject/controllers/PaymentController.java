package SpringProject.controllers;

import SpringProject.dtos.Bookings;
import SpringProject.dtos.Payment;
import SpringProject.services.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    private boolean notLoggedIn(HttpSession session) {
        return session == null || session.getAttribute("loggedInUser") == null;
    }

    private void addHeaderData(HttpSession session, Model model) {
        if (session != null) {
            model.addAttribute("username", session.getAttribute("loggedInUser"));
        }
    }

    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId instanceof Integer ? (Integer) userId : null;
    }

    private boolean isNotUsersBooking(Bookings booking, HttpSession session) {
        Integer userId = getSessionUserId(session);
        return userId == null || booking.getUserId() == null || !booking.getUserId().equals(userId);
    }

    @GetMapping("/{bookingId}")
    public String paymentForm(@PathVariable int bookingId, HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        try {
            Bookings booking = paymentService.getBookingForPayment(bookingId);
            if (booking == null) {
                model.addAttribute("error", "Booking not found.");
                return "error";
            }
            if (isNotUsersBooking(booking, session)) {
                model.addAttribute("error", "You can only pay for your own bookings.");
                return "error";
            }

            Payment existingPayment = paymentService.getPaymentByBookingId(bookingId);
            model.addAttribute("booking", booking);
            model.addAttribute("payment", existingPayment);
            addHeaderData(session, model);
            return "payment";
        } catch (Exception e) {
            model.addAttribute("error", "Payment page could not be loaded.");
            return "error";
        }
    }

    @PostMapping("/{bookingId}")
    public String processPayment(@PathVariable int bookingId,
                                 @RequestParam(required = false) String paymentMethod,
                                 @RequestParam(required = false) String cardholderName,
                                 @RequestParam(required = false) String cardNumber,
                                 @RequestParam(required = false) String expiry,
                                 @RequestParam(required = false) String cvv,
                                 @RequestParam(required = false) String paypalEmail,
                                 HttpSession session,
                                 Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        try {
            Bookings booking = paymentService.getBookingForPayment(bookingId);
            if (booking == null) {
                model.addAttribute("error", "Booking not found.");
                return "error";
            }
            if (isNotUsersBooking(booking, session)) {
                model.addAttribute("error", "You can only pay for your own bookings.");
                return "error";
            }

            Payment payment = paymentService.processPayment(bookingId, cardholderName, cardNumber, expiry, cvv,
                    paymentMethod, paypalEmail);
            model.addAttribute("payment", payment);
            model.addAttribute("booking", booking);
            model.addAttribute("paymentMethod", displayPaymentMethod(paymentMethod));
            addHeaderData(session, model);
            return "paymentSuccess";
        } catch (Exception e) {
            try {
                model.addAttribute("booking", paymentService.getBookingForPayment(bookingId));
            } catch (Exception ignored) {
                model.addAttribute("error", "Booking not found.");
                return "error";
            }

            model.addAttribute("error", e.getMessage());
            model.addAttribute("paymentMethod", paymentMethod);
            addHeaderData(session, model);
            return "payment";
        }
    }

    private String displayPaymentMethod(String paymentMethod) {
        return "paypal".equalsIgnoreCase(paymentMethod) ? "PayPal" : "Debit / Credit Card";
    }
}
