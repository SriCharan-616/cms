package com.example.cms.controller;

import com.example.cms.entity.User;
import com.example.cms.service.UserService;
import com.example.cms.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SessionUtil sessionUtil;

    // ============================
    // LOGIN
    // ============================

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (sessionUtil.isLoggedIn(session)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.login(email, password);
        if (userOpt.isPresent()) {
            sessionUtil.setUser(session, userOpt.get());
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password.");
            return "redirect:/login";
        }
    }

    // ============================
    // REGISTER
    // ============================

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (sessionUtil.isLoggedIn(session)) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(defaultValue = "USER") String role,
                           RedirectAttributes redirectAttributes) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            // Prevent self-registration as ADMIN
            if (userRole == User.Role.ADMIN) userRole = User.Role.USER;
            userService.register(name, email, password, userRole);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // ============================
    // LOGOUT
    // ============================

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        sessionUtil.invalidate(session);
        redirectAttributes.addFlashAttribute("success", "Logged out successfully.");
        return "redirect:/login";
    }

    // ============================
    // ROOT REDIRECT
    // ============================

    @GetMapping("/")
    public String root(HttpSession session) {
        if (sessionUtil.isLoggedIn(session)) return "redirect:/dashboard";
        return "redirect:/login";
    }
}
