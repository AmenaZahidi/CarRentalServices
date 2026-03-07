package SpringProject.controllers;

import SpringProject.dtos.CarDetails;
import SpringProject.services.CarDetailsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/carDetails")
public class CarDetailsController {

    private final CarDetailsService carDetailsService;

    public CarDetailsController(CarDetailsService carDetailsService) {
        this.carDetailsService = carDetailsService;
    }

    private boolean notLoggedIn(HttpSession session) {
        return session == null || session.getAttribute("loggedInUser") == null;
    }

    // Displays the main Car Details HTML page
    @GetMapping
    public String showAllCarsPage(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        try {
            List<CarDetails> cars = carDetailsService.getAllCars();
            model.addAttribute("cars", cars);
            return "carDetails"; // This must match carDetails.html
        } catch (Exception e) {
            return "redirect:/dashboard";
        }
    }

    // Search by Make (e.g., /carDetails/search?make=BMW)
    @GetMapping("/search")
    public String searchByMake(@RequestParam String make, HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        try {
            List<CarDetails> filteredCars = carDetailsService.getAllCars().stream()
                    .filter(c -> c.getMake().equalsIgnoreCase(make))
                    .collect(Collectors.toList());

            model.addAttribute("cars", filteredCars);
            model.addAttribute("searchQuery", make);
            return "carDetails";
        } catch (Exception e) {
            return "redirect:/carDetails";
        }
    }

    // View specific car by ID (e.g., /carDetails/1)
    @GetMapping("/{id}")
    public String getCarById(@PathVariable int id, HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        try {
            CarDetails car = carDetailsService.getCarById(id);
            model.addAttribute("car", car);
            return "carView"; // Requires a carView.html file
        } catch (Exception e) {
            return "redirect:/carDetails";
        }
    }
}