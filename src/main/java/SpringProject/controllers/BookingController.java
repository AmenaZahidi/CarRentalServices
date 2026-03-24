package SpringProject.controllers;

import SpringProject.dtos.Bookings;
import SpringProject.dtos.CarDetails;
import SpringProject.services.BookingService;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;


import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
@Controller
@RequestMapping( "/bookings")

public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public String getAllBookings(Model model) {

        try {
            //List<Bookings> bookings = BookingService;
            model.addAttribute("bookings", bookingService.getAllBookings());

            return "bookings";
        } catch (SQLException e) {
            model.addAttribute("error", "Booking not could not be found");
            return "error";
        }
    }

    @GetMapping("/{id}")
    public String getBookingById(@PathVariable int id, Model model) {
        try {
            Bookings booking = bookingService.getBookingById(id);

            if (booking == null) {
                model.addAttribute("error", "Booking not found");
                return "error";
            }

            model.addAttribute("booking", booking);
            return "bookingDetails";

        } catch (SQLException e) {
            model.addAttribute("error", "Database error");
            return "error";
        }
    }


    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable int id, Model model) {
        try {
            boolean deleted = bookingService.deleteBooking(id);

            if (!deleted) {
                model.addAttribute("error", "Could not delete booking");
                return "error";
            }

            return "redirect:/bookings";

        } catch (SQLException e) {
            model.addAttribute("error", "Database error");
            return "error";
        }
    }

    @GetMapping("/form")
    public String addForm(Model model) {
        model.addAttribute("booking", new Bookings());
        return "bookingForm";
    }

    @PostMapping
    public String addBooking(@ModelAttribute("booking") Bookings booking, Model model) {
        try {
            boolean added = bookingService.addBooking(booking);
            if (!added) {
                model.addAttribute("error", "Could not add booking");
                return "booking-form";
            }

            return "redirect:/bookings";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "booking-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        try {
            Bookings booking = bookingService.getBookingById(id);

            if (booking == null) {
                model.addAttribute("error", "Booking doesnt exist");
                return "error";
            }
            model.addAttribute("booking", booking);
            return "edit-booking";

        } catch (SQLException e) {
            model.addAttribute("error", "Database error");
            return "error";
        }

    }

//    @PostMapping("/booking/save")
//    public String saveBooking(@ModelAttribute Bookings booking) {
//
//        bookingService.save(booking);
//
//        return "redirect:/booking/myBookings";
//    }
}