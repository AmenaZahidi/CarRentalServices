package SpringProject.controllers;
import SpringProject.dtos.User;
import SpringProject.services.UserService;
import SpringProject.utils.PasswordValidator;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Object username = session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        model.addAttribute("username", username.toString());
        model.addAttribute("userType", session.getAttribute("userType"));
        return "dashboard";
    }
    @GetMapping("/contact")
    public String showContactPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        return "contact"; // Opens contact.html
    }

    @GetMapping("/locations")
    public String showLocationsPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        return "locations"; // Opens locations.html
    }
    @GetMapping("/manageBookings")
    public String showManageBookingsPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        return "redirect:/bookings";
    }


    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes ra
    ) {
        try {
            username = username == null ? "" : username.trim();
            email = email == null ? "" : email.trim();

            if (!PasswordValidator.isValidPassword(password)) {
                ra.addFlashAttribute("error",
                        "Password must be 8+ chars with uppercase, lowercase, number, and symbol.");
                return "redirect:/register";
            }

            boolean ok = userService.register(username, password, email);

            if (ok) {
                ra.addFlashAttribute("success", "Account created! Please login.");
                return "redirect:/login";
            }

            ra.addFlashAttribute("error", "Register failed. Username/email may already exist.");
            return "redirect:/register";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Register failed. Username/email may already exist.");
            return "redirect:/register";
        }
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes ra
    ) {
        try {
            username = username == null ? "" : username.trim();

            boolean ok = userService.login(username, password);

            if (!ok) {
                ra.addFlashAttribute("error", "Login failed. Check username/password.");
                return "redirect:/login";
            }

            session.setAttribute("loggedInUser", username);

            User u = userService.getUserByUsername(username);
            if (u != null) {
                session.setAttribute("userId", u.getUserId());
                session.setAttribute("userType", u.getUserType());
            }

            return "redirect:/dashboard";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Login failed. Please try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
