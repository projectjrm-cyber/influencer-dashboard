package com.analytics.influencer_dashboard.controller;

import com.analytics.influencer_dashboard.config.SessionKeys;
import com.analytics.influencer_dashboard.entity.AppUser;
import com.analytics.influencer_dashboard.entity.Role;
import com.analytics.influencer_dashboard.service.AuthException;
import com.analytics.influencer_dashboard.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        Role role = Role.fromText((String) session.getAttribute(SessionKeys.ROLE));
        return role != null ? "redirect:" + role.getHome() : "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(defaultValue = "") String dashboard,
                        HttpServletRequest request,
                        Model model) {
        try {
            AppUser user = authService.authenticate(username, password);

            // The toggle is only a hint: the account's role decides access
            Role chosen = Role.fromText(dashboard);
            if (chosen != null && chosen != user.getRole()) {
                model.addAttribute("error", "This account is for the "
                        + user.getRole().getLabel() + " dashboard. Switch the toggle and try again.");
                return "login";
            }

            HttpSession session = request.getSession();
            request.changeSessionId(); // prevents session fixation
            session.setAttribute(SessionKeys.USER, user.getUsername());
            session.setAttribute(SessionKeys.ROLE, user.getRole().name());

            return "redirect:" + user.getRole().getHome();

        } catch (AuthException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String role,
                           @RequestParam String registrationCode,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        try {
            authService.register(fullName, username, password, confirmPassword, role, registrationCode);
            redirectAttributes.addFlashAttribute("success", "Account created. Please log in.");
            return "redirect:/login";
        } catch (AuthException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}