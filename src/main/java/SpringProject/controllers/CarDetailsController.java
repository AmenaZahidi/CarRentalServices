package SpringProject.controllers;

import SpringProject.dtos.CarDetails;
import SpringProject.services.CarDetailsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarDetailsController {

    private final CarDetailsService carDetailsService;

    public CarDetailsController(CarDetailsService carDetailsService) {
        this.carDetailsService = carDetailsService;
    }

    private boolean notLoggedIn(HttpSession session) {
        return session == null || session.getAttribute("loggedInUser") == null;
    }

    // GET /api/cars
    @GetMapping
    public ResponseEntity<?> getAllCars(HttpSession session) {
        try {
            // protection
            if (notLoggedIn(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");

            List<CarDetails> cars = carDetailsService.getAllCars();
            return ResponseEntity.ok(cars);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load cars");
        }
    }
//    @GetMapping
//    public ResponseEntity<?> getAllCars() {
//        try {
//            return ResponseEntity.ok(carDetailsService.getAllCars());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load cars");
//        }
//    }

    // GET /api/cars/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getCarById(@PathVariable int id, HttpSession session) {
        try {
            if (notLoggedIn(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");

            CarDetails car = carDetailsService.getCarById(id);
            if (car == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
            return ResponseEntity.ok(car);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load car");
        }
    }

    // GET /api/cars/reg/regNumber
    @GetMapping("/reg/{regNumber}")
    public ResponseEntity<?> getCarByRegNumber(@PathVariable String regNumber, HttpSession session) {
        try {
           if (notLoggedIn(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");

            CarDetails car = carDetailsService.getCarByRegNumber(regNumber);
            if (car == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found");
            return ResponseEntity.ok(car);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load car");
        }
    }

    // GET /api/cars/available?pickUpDate=2026-02-16&returnDate=2026-02-20
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCars(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickUpDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            HttpSession session
    ) {
        try {
            if (notLoggedIn(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");

            if (!returnDate.isAfter(pickUpDate)) {
                return ResponseEntity.badRequest().body("returnDate must be after pickUpDate");
            }

            List<CarDetails> cars = carDetailsService.getAvailableCars(pickUpDate, returnDate);
            return ResponseEntity.ok(cars);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load available cars");
        }
    }

    // POST /api/cars
    // Body JSON for CarDetails no carId needed
    @PostMapping
    public ResponseEntity<?> addCar(@RequestBody CarDetails car, HttpSession session) {
        try {
            if (notLoggedIn(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");

            // Basic validation (keep simple)
            if (car.getRegNumber() == null || car.getRegNumber().isBlank())
                return ResponseEntity.badRequest().body("regNumber is required");
            if (car.getMake() == null || car.getMake().isBlank())
                return ResponseEntity.badRequest().body("make is required");
            if (car.getModel() == null || car.getModel().isBlank())
                return ResponseEntity.badRequest().body("model is required");

            int newId = carDetailsService.addCar(car);
            if (newId == -1) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Car was not added");

            return ResponseEntity.status(HttpStatus.CREATED).body(newId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not add car");
        }
    }

    // PUT /api/cars//status
    // Example: /api/cars/5/status?status=available
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCarStatus(
            @PathVariable int id,
            @RequestParam String status,
            HttpSession session
    ) {
        try {
            if (notLoggedIn(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");

            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().body("status is required");
            }

            boolean ok = carDetailsService.updateCarStatus(id, status);
            if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Car not found / not updated");

            return ResponseEntity.ok("Status updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not update status");
        }
    }
}