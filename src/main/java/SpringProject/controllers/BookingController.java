package SpringProject.controllers;

import SpringProject.dtos.Bookings;
import SpringProject.services.BookingService;
import SpringProject.services.DriverLicenceService;
import SpringProject.services.PaymentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Controller
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final DriverLicenceService driverLicenceService;
    private final PaymentService paymentService;

    public BookingController(BookingService bookingService,
                             DriverLicenceService driverLicenceService,
                             PaymentService paymentService) {
        this.bookingService = bookingService;
        this.driverLicenceService = driverLicenceService;
        this.paymentService = paymentService;
    }

    private boolean notLoggedIn(HttpSession session) {
        return session == null || session.getAttribute("loggedInUser") == null;
    }

    private boolean isAdmin(HttpSession session) {
        Object userType = session.getAttribute("userType");
        return userType instanceof Integer && (Integer) userType == 2;
    }

    private String redirectAdmin(HttpSession session) {
        return isAdmin(session) ? "redirect:/admin/dashboard" : null;
    }

    private void addHeaderData(HttpSession session, Model model) {
        if (session != null) {
            model.addAttribute("username", session.getAttribute("loggedInUser"));
            model.addAttribute("sessionUserId", session.getAttribute("userId"));
        }
    }

    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId instanceof Integer ? (Integer) userId : null;
    }

    private boolean isNotUsersBooking(Bookings booking, HttpSession session) {
        Integer userId = getSessionUserId(session);
        return userId == null || !bookingService.belongsToUser(booking, userId);
    }

    private void applySessionUser(Bookings booking, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId != null) {
            booking.setUserId(userId);
        }
    }

    private void applyLoggedInDriver(Bookings booking, HttpSession session) {
        Integer userId = getSessionUserId(session);
        if (userId == null || booking == null) {
            return;
        }

        try {
            Map<String, Object> driver = driverLicenceService.getDriverByUserId(userId);
            if (driver != null) {
                Object driverId = driver.get("driverId");
                if (driverId instanceof Number) {
                    booking.setDriverId(((Number) driverId).intValue());
                }
            }
        } catch (SQLException e) {
            booking.setDriverId(null);
        }
    }

    private void addBookingFormData(HttpSession session,
                                    Model model,
                                    Bookings booking,
                                    String formAction,
                                    String pageTitle) throws SQLException {
        List<Map<String, Object>> cars = bookingService.getCarOptions();
        List<Map<String, Object>> locations = bookingService.getLocationOptions();
        Map<String, Object> driver = null;
        Integer userId = getSessionUserId(session);
        if (userId != null) {
            try {
                driver = driverLicenceService.getDriverByUserId(userId);
            } catch (SQLException e) {
                driver = null;
            }
        }

        model.addAttribute("formAction", formAction);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("cars", cars);
        model.addAttribute("locations", locations);
        model.addAttribute("driver", driver);
        model.addAttribute("isEditMode", formAction != null && formAction.contains("/edit/"));
        if (booking != null) {
            model.addAttribute("selectedCarLabel", findLabel(cars, booking.getCarId()));
            model.addAttribute("selectedDriverLabel", booking.getDriverId() != null ? "Driver ID " + booking.getDriverId() : "");
            model.addAttribute("selectedPickupLocationLabel", findLabel(locations, booking.getPickupLocationId()));
            model.addAttribute("selectedDropOffLocationLabel", findLabel(locations, booking.getDropOffLocationId()));
            model.addAttribute("selectedCarRate", findNumber(cars, booking.getCarId(), "dailyRate"));
        }
        addHeaderData(session, model);
    }

    @GetMapping
    public String getAllBookings(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/bookings";

        try {
            Integer userId = getSessionUserId(session);
            if (userId == null) {
                model.addAttribute("error", "Your user ID could not be found. Please log in again.");
                return "error";
            }

            model.addAttribute("bookings", bookingService.getBookingsByUserId(userId));
            addHeaderData(session, model);
            return "bookings";
        } catch (SQLException e) {
            model.addAttribute("error", "Bookings could not be loaded.");
            return "error";
        }
    }

    @GetMapping({"/form", "/create"})
    public String addForm(@RequestParam(required = false) Integer carId,
                          HttpSession session,
                          Model model) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/dashboard";

        Bookings booking = new Bookings();
        booking.setStatus("confirmed");
        if (carId != null) booking.setCarId(carId);
        applySessionUser(booking, session);
        applyLoggedInDriver(booking, session);

        try {
            model.addAttribute("booking", booking);
            addBookingFormData(session, model, booking, "/bookings", "Create Booking");
            return "booking";
        } catch (SQLException e) {
            model.addAttribute("error", "Booking form data could not be loaded.");
            return "error";
        }
    }

    @PostMapping
    public String addBooking(@Valid @ModelAttribute("booking") Bookings booking,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model,
                             RedirectAttributes ra) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/dashboard";

        applySessionUser(booking, session);
        applyLoggedInDriver(booking, session);
        validateBookingDates(booking, bindingResult);
        applyCalculatedTotal(booking, bindingResult);
        validateBookingReferences(booking, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormDataAfterError(session, model, "/bookings", "Create Booking");
            return "booking";
        }

        try {
            if (bookingService.addBooking(booking)) {
                ra.addFlashAttribute("success", "Booking added.");
                return "redirect:/bookings";
            }

            model.addAttribute("error", "Could not add booking.");
            addFormDataAfterError(session, model, "/bookings", "Create Booking");
            return "booking";
        } catch (Exception e) {
            model.addAttribute("error", "Could not add booking: " + e.getMessage());
            addFormDataAfterError(session, model, "/bookings", "Create Booking");
            return "booking";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/bookings";

        try {
            Bookings booking = bookingService.getBookingById(id);
            if (booking == null) {
                model.addAttribute("error", "Booking not found.");
                return "error";
            }
            if (isNotUsersBooking(booking, session)) {
                model.addAttribute("error", "You can only edit your own bookings.");
                return "error";
            }

            model.addAttribute("booking", booking);
            addBookingFormData(session, model, booking, "/bookings/edit/" + id, "Edit Booking");
            return "booking";
        } catch (Exception e) {
            model.addAttribute("error", "Booking could not be loaded.");
            return "error";
        }
    }

    @GetMapping("/driver-claim/{id}")
    public String driverClaimForm(@PathVariable int id, HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/bookings";

        try {
            Bookings booking = bookingService.getBookingById(id);
            if (booking == null) {
                model.addAttribute("error", "Booking not found.");
                return "error";
            }
            if (isNotUsersBooking(booking, session)) {
                model.addAttribute("error", "You can only manage your own bookings.");
                return "error";
            }

            model.addAttribute("booking", booking);
            model.addAttribute("driverId", booking.getDriverId());
            addHeaderData(session, model);
            return "driverClaim";
        } catch (Exception e) {
            model.addAttribute("error", "Booking could not be loaded.");
            return "error";
        }
    }

    @PostMapping("/driver-claim/{id}")
    public String driverClaim(@PathVariable int id,
                              @RequestParam(required = false) Integer driverId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes ra) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/bookings";

        try {
            Bookings booking = bookingService.getBookingById(id);
            if (booking == null) {
                model.addAttribute("error", "Booking not found.");
                return "error";
            }
            if (isNotUsersBooking(booking, session)) {
                model.addAttribute("error", "You can only manage your own bookings.");
                return "error";
            }

            if (driverId == null || driverId <= 0) {
                model.addAttribute("booking", booking);
                model.addAttribute("driverId", driverId);
                model.addAttribute("error", "Enter a valid Driver ID.");
                addHeaderData(session, model);
                return "driverClaim";
            }

            if (!bookingService.driverExists(driverId)) {
                model.addAttribute("booking", booking);
                model.addAttribute("driverId", driverId);
                model.addAttribute("error", "Driver ID does not exist.");
                addHeaderData(session, model);
                return "driverClaim";
            }

            if (!bookingService.driverHasCompleteProfile(driverId)) {
                model.addAttribute("booking", booking);
                model.addAttribute("driverId", driverId);
                model.addAttribute("error", "Driver must complete their profile and upload licence proof before booking.");
                addHeaderData(session, model);
                return "driverClaim";
            }

            if (!bookingService.driverCanDriveCar(driverId, booking.getCarId())) {
                model.addAttribute("booking", booking);
                model.addAttribute("driverId", driverId);
                model.addAttribute("error", "Automatic licence holders cannot be assigned to a manual car.");
                addHeaderData(session, model);
                return "driverClaim";
            }

            if (bookingService.updateBookingDriver(id, driverId)) {
                ra.addFlashAttribute("success", "Driver assigned to booking.");
                return "redirect:/bookings";
            }

            model.addAttribute("booking", booking);
            model.addAttribute("driverId", driverId);
            model.addAttribute("error", "Could not assign driver.");
            addHeaderData(session, model);
            return "driverClaim";
        } catch (Exception e) {
            model.addAttribute("error", "Could not assign driver: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateBooking(@PathVariable int id,
                                @Valid @ModelAttribute("booking") Bookings booking,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes ra) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/bookings";

        booking.setBookingId(id);
        applySessionUser(booking, session);

        try {
            Bookings existingBooking = bookingService.getBookingById(id);
            if (existingBooking == null) {
                model.addAttribute("error", "Booking not found.");
                return "error";
            }
            if (isNotUsersBooking(existingBooking, session)) {
                model.addAttribute("error", "You can only update your own bookings.");
                return "error";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Booking could not be loaded.");
            return "error";
        }

        validateBookingDates(booking, bindingResult);
        applyCalculatedTotal(booking, bindingResult);
        validateBookingReferences(booking, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormDataAfterError(session, model, "/bookings/edit/" + id, "Edit Booking");
            return "booking";
        }

        try {
            if (bookingService.updateBooking(booking)) {
                ra.addFlashAttribute("success", "Booking updated.");
                return "redirect:/bookings";
            }

            model.addAttribute("error", "Could not update booking.");
            addFormDataAfterError(session, model, "/bookings/edit/" + id, "Edit Booking");
            return "booking";
        } catch (Exception e) {
            model.addAttribute("error", "Could not update booking: " + e.getMessage());
            addFormDataAfterError(session, model, "/bookings/edit/" + id, "Edit Booking");
            return "booking";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable int id,
                                HttpSession session,
                                RedirectAttributes ra) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/bookings";

        try {
            Bookings booking = bookingService.getBookingById(id);
            if (booking == null || isNotUsersBooking(booking, session)) {
                ra.addFlashAttribute("error", "You can only delete your own bookings.");
                return "redirect:/bookings";
            }

            if (bookingService.deleteBooking(id)) {
                ra.addFlashAttribute("success", "Booking deleted.");
            } else {
                ra.addFlashAttribute("error", "Could not delete booking.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Database error. Booking was not deleted.");
        }

        return "redirect:/bookings";
    }

    @GetMapping("/{id}")
    public String getBookingById(@PathVariable int id, HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";
        if (isAdmin(session)) return "redirect:/admin/bookings";

        try {
            Bookings booking = bookingService.getBookingById(id);
            if (booking == null) {
                model.addAttribute("error", "Booking not found.");
                return "error";
            }
            if (isNotUsersBooking(booking, session)) {
                model.addAttribute("error", "You can only view your own bookings.");
                return "error";
            }

            model.addAttribute("booking", booking);
            model.addAttribute("payment", paymentService.getPaymentByBookingId(id));
            addHeaderData(session, model);
            return "bookingDetails";
        } catch (Exception e) {
            model.addAttribute("error", "Booking could not be loaded.");
            return "error";
        }
    }

    private void validateBookingDates(Bookings booking, BindingResult bindingResult) {
        if (booking.getPickupDateTime() != null
                && booking.getReturnDateTime() != null
                && !booking.getReturnDateTime().after(booking.getPickupDateTime())) {
            bindingResult.rejectValue("returnDateTime", "booking.returnDateTime.invalid",
                    "Return date and time must be after pickup date and time.");
        }
    }

    private void validateBookingReferences(Bookings booking, BindingResult bindingResult) {
        try {
            if (booking.getUserId() == null || booking.getUserId() <= 0) {
                bindingResult.rejectValue("userId", "booking.userId.required",
                        "User ID could not be found. Please log in again.");
            } else if (!bookingService.userExists(booking.getUserId())) {
                bindingResult.rejectValue("userId", "booking.userId.invalid",
                        "User ID does not exist.");
            }

            if (booking.getCarId() != null
                    && booking.getCarId() > 0
                    && !bookingService.carExists(booking.getCarId())) {
                bindingResult.rejectValue("carId", "booking.carId.invalid",
                        "Car ID does not exist.");
            }

            if (booking.getDriverId() != null && booking.getDriverId() > 0) {
                if (!bookingService.driverExists(booking.getDriverId())) {
                    bindingResult.rejectValue("driverId", "booking.driverId.invalid",
                            "Driver ID does not exist.");
                } else if (!bookingService.driverHasCompleteProfile(booking.getDriverId())) {
                    bindingResult.rejectValue("driverId", "booking.driverId.profileIncomplete",
                            "Driver must complete their profile and upload licence proof before booking.");
                } else if (booking.getCarId() != null
                        && booking.getCarId() > 0
                        && !bookingService.driverCanDriveCar(booking.getDriverId(), booking.getCarId())) {
                    bindingResult.rejectValue("driverId", "booking.driverId.licenceType",
                            "Automatic licence holders cannot book a manual car.");
                }
            }

            if (booking.getPickupLocationId() != null
                    && booking.getPickupLocationId() > 0
                    && !bookingService.locationExists(booking.getPickupLocationId())) {
                bindingResult.rejectValue("pickupLocationId", "booking.pickupLocationId.invalid",
                        "Pickup Location ID does not exist.");
            }

            if (booking.getDropOffLocationId() != null
                    && booking.getDropOffLocationId() > 0
                    && !bookingService.locationExists(booking.getDropOffLocationId())) {
                bindingResult.rejectValue("dropOffLocationId", "booking.dropOffLocationId.invalid",
                        "Drop-off Location ID does not exist.");
            }
        } catch (SQLException e) {
            bindingResult.reject("booking.references.database",
                    "Could not validate booking IDs. Please try again.");
        }
    }

    private void applyCalculatedTotal(Bookings booking, BindingResult bindingResult) {
        if (booking.getCarId() == null || booking.getPickupDateTime() == null || booking.getReturnDateTime() == null) {
            return;
        }

        if (!booking.getReturnDateTime().after(booking.getPickupDateTime())) {
            return;
        }

        try {
            long rentalMillis = booking.getReturnDateTime().getTime() - booking.getPickupDateTime().getTime();
            long rentalDays = Math.max(1, (long) Math.ceil(rentalMillis / (double) TimeUnit.DAYS.toMillis(1)));
            double dailyRate = bookingService.getCarDailyRate(booking.getCarId());
            double total = Math.round(dailyRate * rentalDays * 100.0) / 100.0;
            booking.setTotalPrice(total);
        } catch (Exception e) {
            bindingResult.rejectValue("totalPrice", "booking.totalPrice.calculate",
                    "Could not calculate booking total.");
        }
    }

    private void addFormDataAfterError(HttpSession session,
                                       Model model,
                                       String formAction,
                                       String pageTitle) {
        try {
            Bookings booking = (Bookings) model.asMap().get("booking");
            addBookingFormData(session, model, booking, formAction, pageTitle);
        } catch (SQLException e) {
            model.addAttribute("error", "Could not reload booking form choices.");
        }
    }

    private String findLabel(List<Map<String, Object>> options, Integer id) {
        if (id == null || options == null) {
            return "";
        }

        for (Map<String, Object> option : options) {
            Object optionId = option.get("id");
            if (optionId instanceof Number && ((Number) optionId).intValue() == id) {
                Object label = option.get("label");
                return label != null ? label.toString() : "";
            }
        }

        return "";
    }

    private Double findNumber(List<Map<String, Object>> options, Integer id, String key) {
        if (id == null || options == null) {
            return null;
        }

        for (Map<String, Object> option : options) {
            Object optionId = option.get("id");
            if (optionId instanceof Number && ((Number) optionId).intValue() == id) {
                Object value = option.get(key);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }
        }

        return null;
    }
}
