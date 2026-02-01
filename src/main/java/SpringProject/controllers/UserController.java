package SpringProject.controllers;

import SpringProject.dtos.User;
import SpringProject.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Home -> your frontend login/register page
    @GetMapping("/")
    public String home() {
        return "redirect:/frontend/user_index.html";
    }

    // Register form POST
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpSession session) {
        try {
            boolean ok = userService.register(username, password, email);

            if (ok) {
                // store session like CA3
                session.setAttribute("loggedInUser", username);

                // If you want userType in session:
                User u = userService.getUserByUsername(username);
                if (u != null) session.setAttribute("userType", u.getUserType());

                return "redirect:/frontend/registerSuccess.html";
            }

            return "redirect:/frontend/registerFailed.html";

        } catch (Exception e) {
            // You can also redirect with message if you want later
            return "redirect:/frontend/registerFailed.html";
        }
    }

    // Login form POST
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        try {
            boolean ok = userService.login(username, password);

            if (ok) {
                session.setAttribute("loggedInUser", username);

                // Load userType (optional)
                User u = userService.getUserByUsername(username);
                if (u != null) session.setAttribute("userType", u.getUserType());

                return "redirect:/frontend/loginSuccessful.html";
            }

            return "redirect:/frontend/loginFailed.html";

        } catch (Exception e) {
            return "redirect:/frontend/loginFailed.html";
        }
    }

    // Logout
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/frontend/user_index.html";
    }
}
