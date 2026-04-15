package com.example.cms.controller;

import com.example.cms.entity.Conference;
import com.example.cms.entity.User;
import com.example.cms.service.ConferenceService;
import com.example.cms.service.UserService;
import com.example.cms.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ConferenceService conferenceService;
    private final SessionUtil sessionUtil;

    private boolean isAdmin(HttpSession session) {
        User user = sessionUtil.getUser(session);
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    // ============================
    // ADMIN DASHBOARD
    // ============================

    @GetMapping
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        List<User> users = userService.findAll();
        List<Conference> conferences = conferenceService.findAll();

        model.addAttribute("users", users);
        model.addAttribute("conferences", conferences);
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalConferences", conferences.size());
        model.addAttribute("user", sessionUtil.getUser(session));
        return "admin/dashboard";
    }

    // ============================
    // MANAGE USERS
    // ============================

    @GetMapping("/users")
    public String manageUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("user", sessionUtil.getUser(session));
        model.addAttribute("roles", User.Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id,
                                  @RequestParam String role,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";

        try {
            User.Role newRole = User.Role.valueOf(role.toUpperCase());
            userService.updateRole(id, newRole);
            redirectAttributes.addFlashAttribute("success", "User role updated to " + role + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update role: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";

        User currentUser = sessionUtil.getUser(session);
        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account.");
            return "redirect:/admin/users";
        }

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ============================
    // MANAGE CONFERENCES
    // ============================

    @GetMapping("/conferences")
    public String manageConferences(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        List<Conference> conferences = conferenceService.findAll();
        model.addAttribute("conferences", conferences);
        model.addAttribute("user", sessionUtil.getUser(session));
        return "admin/conferences";
    }

    @PostMapping("/conferences/{id}/delete")
    public String deleteConference(@PathVariable Long id, HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/login";

        try {
            conferenceService.deleteConference(id);
            redirectAttributes.addFlashAttribute("success", "Conference deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete conference: " + e.getMessage());
        }
        return "redirect:/admin/conferences";
    }
}
