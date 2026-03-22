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
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Added this import

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

    private boolean isAdmin(HttpSession session) {
        if (session == null || session.getAttribute("loggedInUser") == null) return false;

        Object ut = session.getAttribute("userType");
        if (ut instanceof Integer && (Integer) ut == 2) return true;

        try {
            String username = (String) session.getAttribute("loggedInUser");
            User u = userService.getUserByUsername(username);
            if (u != null) {
                session.setAttribute("userType", u.getUserType());
                session.setAttribute("userId", u.getUserId());
                return u.getUserType() == 2;
            }
        } catch (Exception ignored) {}

        return false;
    }

    @GetMapping("/return-check")
    public String showForm(@RequestParam int bookingId,
                           @RequestParam int carId,
                           HttpSession session,
                           Model model) {

        if (!isAdmin(session)) return "redirect:/dashboard";

        model.addAttribute("bookingId", bookingId);
        model.addAttribute("carId", carId);
        model.addAttribute("today", LocalDate.now());
        return "returnCheck";
    }

    // Process the return form submission
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

        if (!isAdmin(session)) return "redirect:/dashboard";

        try {
            int inspectedByUserId = 0;
            Object uid = session.getAttribute("userId");

            if (uid instanceof Integer) {
                inspectedByUserId = (Integer) uid;
            } else {
                String username = (String) session.getAttribute("loggedInUser");
                User u = userService.getUserByUsername(username);
                if (u != null) inspectedByUserId = u.getUserId();
            }

            AdminReturnInspection inspection = AdminReturnInspection.builder()
                    .bookingId(bookingId)
                    .inspectedByUserId(inspectedByUserId)
                    .actualReturnDate(actualReturnDate)
                    .returnedOnTime(returnedOnTime)
                    .damageFound(damageFound)
                    .damageNotes(damageNotes)
                    .mileageIn(mileageIn)
                    .fuelLevel(fuelLevel)
                    .build();

            int id = inspectionService.addInspection(inspection);

            // Set car back to available
            carDetailsService.updateCarStatus(carId, "available");

            model.addAttribute("success", "Inspection saved (ID: " + id + "). Car is now available.");
        } catch (Exception e) {
            model.addAttribute("error", "Error saving inspection: " + e.getMessage());
        }

        model.addAttribute("bookingId", bookingId);
        model.addAttribute("carId", carId);
        model.addAttribute("today", LocalDate.now());
        return "returnCheck";
    }

    // View all past inspections
    @GetMapping("/history")
    public String showInspectionHistory(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/dashboard";

        try {
            List<AdminReturnInspection> list = inspectionService.getAllInspections();
            model.addAttribute("inspections", list);
        } catch (Exception e) {
            model.addAttribute("error", "Could not load history: " + e.getMessage());
        }
        return "inspectionHistory";
    }

    // View specific detail
    @GetMapping("/view/{id}")
    public String viewInspectionDetail(@PathVariable int id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/dashboard";

        try {
            AdminReturnInspection inspection = inspectionService.getInspectionById(id);
            if (inspection != null) {
                model.addAttribute("inspection", inspection);
                return "inspectionDetail";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading details: " + e.getMessage());
        }
        return "redirect:/admin/history";
    }

    // Delete an inspection
    @PostMapping("/delete/{id}")
    public String deleteInspection(@PathVariable int id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/dashboard";

        try {
            inspectionService.deleteInspection(id);
            redirectAttributes.addFlashAttribute("success", "Inspection deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not delete inspection.");
        }
        return "redirect:/admin/history";
    }
}