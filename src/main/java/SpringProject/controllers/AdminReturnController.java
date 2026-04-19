package SpringProject.controllers;

import SpringProject.dtos.AdminReturnInspection;
import SpringProject.dtos.User;
import SpringProject.services.AdminReturnInspectionService;
import SpringProject.services.CarDetailsService;
import SpringProject.services.UserService;
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
    private final UserService userService;

    public AdminReturnController(AdminReturnInspectionService inspectionService,
                                 CarDetailsService carDetailsService,
                                 UserService userService) {
        this.inspectionService = inspectionService;
        this.carDetailsService = carDetailsService;
        this.userService = userService;
    }

    private boolean isNotAdmin(HttpSession session) {
        return false;
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
        model.addAttribute("username", "Admin Test");

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
            inspection.setInspectedByUserId(5);
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
        return "returnCheck";
    }

    @GetMapping("/history")
    public String showInspectionHistory(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";

        try {
            List<AdminReturnInspection> inspections = inspectionService.getAllInspections();
            model.addAttribute("inspections", inspections);
            model.addAttribute("username", session.getAttribute("loggedInUser"));
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
            model.addAttribute("username", session.getAttribute("loggedInUser"));
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
    @GetMapping("/inspectionHistory")
    public String showInspectionHistory(Model model) {
        return "inspectionHistory";
    }
}
