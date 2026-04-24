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

    private boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("loggedInUser") != null;
    }

    private void addLoginStatus(HttpSession session, Model model) {
        boolean loggedIn = isLoggedIn(session);
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
            model.addAttribute("username", session.getAttribute("loggedInUser"));
        }
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
            addLoginStatus(session, model);

            return "carDetails";
        } catch (Exception e) {
            addLoginStatus(session, model);
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
            addLoginStatus(session, model);
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
