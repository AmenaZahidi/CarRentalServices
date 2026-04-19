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
    @GetMapping
    public String showAllCarsPage(@RequestParam(required = false) String sort, HttpSession session, Model model) {
        try {
            List<CarDetails> cars = carDetailsService.getAllCars();

            if ("mileageDesc".equals(sort)) {
                cars.sort((c1, c2) -> Integer.compare(c2.getMileage(), c1.getMileage()));
            } else if ("mileageAsc".equals(sort)) {
                cars.sort((c1, c2) -> Integer.compare(c1.getMileage(), c2.getMileage()));
            }

            model.addAttribute("cars", cars);

            if (session != null && session.getAttribute("loggedInUser") != null) {
                model.addAttribute("username", session.getAttribute("loggedInUser"));
            }

            return "carDetails";
        } catch (Exception e) {
            return "carDetails";
        }
    }
    @GetMapping("/search")
    public String searchByMake(@RequestParam String make, HttpSession session, Model model) {
        try {
            List<CarDetails> filteredCars = carDetailsService.getAllCars().stream()
                    .filter(c -> c.getMake().equalsIgnoreCase(make))
                    .collect(Collectors.toList());

            model.addAttribute("cars", filteredCars);
            model.addAttribute("searchQuery", make);

            if (session.getAttribute("loggedInUser") != null) {
                model.addAttribute("username", session.getAttribute("loggedInUser"));
            }
            return "carDetails";
        } catch (Exception e) {
            return "redirect:/carDetails";
        }
    }

    @GetMapping("/contact")
    public String showContactPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") != null) {
            model.addAttribute("username", session.getAttribute("loggedInUser"));
        }
        return "contact";
    }
    @GetMapping("/location")
    public String showLocationPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") != null) {
            model.addAttribute("username", session.getAttribute("loggedInUser"));
        }
        return "locations";
    }

    @GetMapping("/{id}")
    public String getCarById(@PathVariable int id, HttpSession session, Model model) {
        try {
            CarDetails car = carDetailsService.getCarById(id);
            model.addAttribute("car", car);
            return "carView";
        } catch (Exception e) {
            return "redirect:/carDetails";
        }
    }
}
