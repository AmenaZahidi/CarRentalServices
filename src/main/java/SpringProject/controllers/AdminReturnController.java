package SpringProject.controllers;

import SpringProject.dtos.AdminReturnInspection;
import SpringProject.services.AdminReturnInspectionService;
import SpringProject.services.BookingService;
import SpringProject.services.CarDetailsService;
import SpringProject.services.DriverLicenceService;
import SpringProject.services.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminReturnController {

    private final AdminReturnInspectionService inspectionService;
    private final CarDetailsService carDetailsService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final DriverLicenceService driverLicenceService;

    public AdminReturnController(AdminReturnInspectionService inspectionService,
                                 CarDetailsService carDetailsService,
                                 BookingService bookingService,
                                 PaymentService paymentService,
                                 DriverLicenceService driverLicenceService) {
        this.inspectionService = inspectionService;
        this.carDetailsService = carDetailsService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.driverLicenceService = driverLicenceService;
    }

    private boolean isNotAdmin(HttpSession session) {
        if (session == null || session.getAttribute("loggedInUser") == null) {
            return true;
        }
        Object userType = session.getAttribute("userType");
        return !(userType instanceof Integer && (Integer) userType == 2);
    }

    private void addHeaderData(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("loggedInUser"));
        model.addAttribute("userType", session.getAttribute("userType"));
    }

    @GetMapping({"", "/", "/dashboard"})
    public String adminDashboard(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            model.addAttribute("bookingCount", bookingService.getAllBookings().size());
            model.addAttribute("paymentCount", paymentService.getAllPayments().size());
            model.addAttribute("licenceCount", driverLicenceService.getLicenceProofs().size());
            model.addAttribute("inspectionCount", inspectionService.getAllInspections().size());
        } catch (Exception e) {
            model.addAttribute("error", "Admin dashboard counts could not be loaded.");
        }

        addHeaderData(session, model);
        return "adminDashboard";
    }

    @GetMapping("/bookings")
    public String adminBookings(@RequestParam(required = false) String customerName,
                                @RequestParam(required = false) String paymentStatus,
                                @RequestParam(required = false) String pickupDate,
                                HttpSession session,
                                Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            model.addAttribute("bookings",
                    bookingService.filterAdminBookingSummaries(customerName, paymentStatus, pickupDate));
        } catch (Exception e) {
            model.addAttribute("error", "Bookings could not be loaded.");
        }

        model.addAttribute("customerName", customerName);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("pickupDate", pickupDate);
        addHeaderData(session, model);
        return "adminBookings";
    }

    @GetMapping("/payments")
    public String adminPayments(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            model.addAttribute("payments", paymentService.getAllPayments());
        } catch (Exception e) {
            model.addAttribute("error", "Payments could not be loaded.");
        }

        addHeaderData(session, model);
        return "adminPayments";
    }

    @GetMapping("/licences")
    public String adminLicences(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            model.addAttribute("licences", driverLicenceService.getLicenceProofs());
        } catch (Exception e) {
            model.addAttribute("error", "Driver licence proofs could not be loaded.");
        }

        addHeaderData(session, model);
        return "adminLicences";
    }

    @PostMapping("/licences/approve/{driverId}")
    public String approveLicence(@PathVariable int driverId, HttpSession session, RedirectAttributes ra) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            if (driverLicenceService.approveLicence(driverId)) {
                ra.addFlashAttribute("success", "Driver licence approved.");
            } else {
                ra.addFlashAttribute("error", "Driver licence could not be approved.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Driver licence could not be approved.");
        }

        return "redirect:/admin/licences";
    }

    @PostMapping("/licences/reject/{driverId}")
    public String rejectLicence(@PathVariable int driverId, HttpSession session, RedirectAttributes ra) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            if (driverLicenceService.rejectLicence(driverId)) {
                ra.addFlashAttribute("success", "Driver licence marked as not verified.");
            } else {
                ra.addFlashAttribute("error", "Driver licence could not be updated.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Driver licence could not be updated.");
        }

        return "redirect:/admin/licences";
    }

    @GetMapping("/return-check")
    public String showForm(@RequestParam int bookingId,
                           @RequestParam int carId,
                           HttpSession session,
                           Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        model.addAttribute("bookingId", bookingId);
        model.addAttribute("carId", carId);
        model.addAttribute("today", LocalDate.now());
        addHeaderData(session, model);

        return "returnCheck";
    }

    @PostMapping("/return-check")
    public String submitForm(@RequestParam int bookingId,
                             @RequestParam int carId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate actualReturnDate,
                             @RequestParam(defaultValue = "false") boolean returnedOnTime,
                             @RequestParam(defaultValue = "false") boolean damageFound,
                             @RequestParam(required = false) String damageNotes,
                             @RequestParam(required = false) Integer mileageIn,
                             @RequestParam(required = false) String fuelLevel,
                             HttpSession session,
                             Model model) {

        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            AdminReturnInspection inspection = new AdminReturnInspection();
            inspection.setBookingId(bookingId);
            inspection.setInspectedByUserId((Integer) session.getAttribute("userId"));
            inspection.setActualReturnDate(actualReturnDate);
            inspection.setReturnedOnTime(returnedOnTime);
            inspection.setDamageFound(damageFound);
            inspection.setDamageNotes(damageNotes);
            inspection.setMileageIn(mileageIn);
            inspection.setFuelLevel(fuelLevel);

            inspectionService.addInspection(inspection);
            carDetailsService.updateCarStatus(carId, "available");

            model.addAttribute("success", "Car returned successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }

        model.addAttribute("bookingId", bookingId);
        model.addAttribute("carId", carId);
        model.addAttribute("today", LocalDate.now());
        addHeaderData(session, model);
        return "returnCheck";
    }

    @GetMapping("/history")
    public String showInspectionHistory(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            List<AdminReturnInspection> inspections = inspectionService.getAllInspections();
            model.addAttribute("inspections", inspections);
            addHeaderData(session, model);
            return "inspectionHistory";
        } catch (Exception e) {
            model.addAttribute("error", "Could not load history.");
            return "inspectionHistory";
        }
    }

    @GetMapping("/view/{id}")
    public String viewInspectionDetail(@PathVariable int id, HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            AdminReturnInspection inspection = inspectionService.getInspectionById(id);
            model.addAttribute("inspection", inspection);
            addHeaderData(session, model);
            return "inspectionDetail";
        } catch (Exception e) {
            return "redirect:/admin/history";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteInspection(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            inspectionService.deleteInspection(id);
            ra.addFlashAttribute("success", "Record deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Delete failed.");
        }
        return "redirect:/admin/history";
    }
}
