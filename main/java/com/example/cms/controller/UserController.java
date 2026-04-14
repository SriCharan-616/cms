package com.example.cms.controller;

import com.example.cms.entity.*;
import com.example.cms.service.ConferenceService;
import com.example.cms.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final ConferenceService conferenceService;
    private final SessionUtil sessionUtil;

    // ============================
    // DASHBOARD
    // ============================

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!sessionUtil.isLoggedIn(session)) return "redirect:/login";
        User user = sessionUtil.getUser(session);
        model.addAttribute("user", user);

        if (user.getRole() == User.Role.ORGANIZER) {
            List<Conference> myConferences = conferenceService.findByOrganizer(user);
            model.addAttribute("myConferences", myConferences);
        } else if (user.getRole() == User.Role.USER) {
            List<Registration> registrations = conferenceService.getRegistrationsByUser(user);
            List<Submission> submissions = conferenceService.getSubmissionsByUser(user);
            model.addAttribute("registrations", registrations);
            model.addAttribute("submissions", submissions);
        }

        return "dashboard";
    }

    // ============================
    // CONFERENCE LIST & SEARCH
    // ============================

    @GetMapping("/conferences")
    public String conferenceList(@RequestParam(required = false) String search,
                                  HttpSession session, Model model) {
        if (!sessionUtil.isLoggedIn(session)) return "redirect:/login";
        User user = sessionUtil.getUser(session);

        List<Conference> conferences = conferenceService.searchConferences(search);
        model.addAttribute("conferences", conferences);
        model.addAttribute("search", search);
        model.addAttribute("user", user);
        return "conference-list";
    }

    // ============================
    // CONFERENCE DETAILS
    // ============================

    @GetMapping("/conferences/{id}")
    public String conferenceDetails(@PathVariable Long id, HttpSession session, Model model) {
        if (!sessionUtil.isLoggedIn(session)) return "redirect:/login";
        User user = sessionUtil.getUser(session);

        Optional<Conference> confOpt = conferenceService.findById(id);
        if (confOpt.isEmpty()) return "redirect:/conferences";

        Conference conference = confOpt.get();
        boolean isRegistered = conferenceService.isUserRegistered(user, conference);

        model.addAttribute("conference", conference);
        model.addAttribute("isRegistered", isRegistered);
        model.addAttribute("user", user);
        return "conference-details";
    }

    // ============================
    // REGISTER FOR CONFERENCE
    // ============================

    @PostMapping("/conferences/{id}/register")
    public String registerForConference(@PathVariable Long id, HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        if (!sessionUtil.isLoggedIn(session)) return "redirect:/login";
        User user = sessionUtil.getUser(session);

        try {
            conferenceService.registerForConference(user, id);
            redirectAttributes.addFlashAttribute("success", "Successfully registered for the conference!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/conferences/" + id;
    }

    // ============================
    // CANCEL REGISTRATION
    // ============================

    @PostMapping("/registrations/{regId}/cancel")
    public String cancelRegistration(@PathVariable Long regId, HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!sessionUtil.isLoggedIn(session)) return "redirect:/login";
        User user = sessionUtil.getUser(session);

        try {
            conferenceService.cancelRegistration(regId, user.getId());
            redirectAttributes.addFlashAttribute("success", "Registration cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-registrations";
    }

    // ============================
    // MY REGISTRATIONS
    // ============================

    @GetMapping("/my-registrations")
    public String myRegistrations(HttpSession session, Model model) {
        if (!sessionUtil.isLoggedIn(session)) return "redirect:/login";
        User user = sessionUtil.getUser(session);

        List<Registration> registrations = conferenceService.getRegistrationsByUser(user);
        model.addAttribute("registrations", registrations);
        model.addAttribute("user", user);
        return "my-registrations";
    }

    // ============================
    // SUBMIT PAPER
    // ============================

    @GetMapping("/submit-paper")
    public String submitPaperPage(HttpSession session, Model model) {
        if (!sessionUtil.isLoggedIn(session)) return "redirect:/login";
        User user = sessionUtil.getUser(session);

        List<Conference> conferences = conferenceService.findAll();
        model.addAttribute("conferences", conferences);
        model.addAttribute("user", user);
        return "submit-paper";
    }

    @PostMapping("/submit-paper")
    public String submitPaper(@RequestParam String title,
                               @RequestParam String abstrakt,
                               @RequestParam Long conferenceId,
                               @RequestParam(required = false) MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!sessionUtil.isLoggedIn(session)) return "redirect:/login";
        User user = sessionUtil.getUser(session);

        try {
            conferenceService.submitPaper(user, conferenceId, title, abstrakt, file);
            redirectAttributes.addFlashAttribute("success", "Paper submitted successfully! Status: Pending review.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Submission failed: " + e.getMessage());
        }
        return "redirect:/my-registrations";
    }
}
