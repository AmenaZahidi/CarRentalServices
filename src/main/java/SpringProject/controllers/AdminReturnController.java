package SpringProject.controllers;

import SpringProject.services.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminReturnController {

    private final AdminService adminService;

    public AdminReturnController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/process-return")
    public String processReturn(@RequestParam Long carId,
                                @RequestParam String returnTiming,
                                @RequestParam(required = false, defaultValue = "false") boolean damaged,
                                HttpSession session) {

        if (session == null || !"admin".equals(session.getAttribute("loggedInUser"))) {
            return "redirect:/login";
        }

        adminService.recordInspection(carId, returnTiming, damaged);

        return "redirect:/carDetails?success=checked";
    }

    @PostMapping("/reset-car")
    public String resetCar(@RequestParam Long carId, HttpSession session) {
        if (session == null || !"admin".equals(session.getAttribute("loggedInUser"))) {
            return "redirect:/login";
        }

        adminService.resetCar(carId);
        return "redirect:/carDetails";
    }
}