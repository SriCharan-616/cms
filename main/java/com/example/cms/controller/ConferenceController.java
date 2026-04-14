package com.example.cms.controller;

import com.example.cms.entity.*;
import com.example.cms.service.ConferenceService;
import com.example.cms.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/organizer")
@RequiredArgsConstructor
public class ConferenceController {

    private final ConferenceService conferenceService;
    private final SessionUtil sessionUtil;

    private boolean isOrganizer(HttpSession session) {
        User user = sessionUtil.getUser(session);
        return user != null && (user.getRole() == User.Role.ORGANIZER || user.getRole() == User.Role.ADMIN);
    }

    // ============================
    // CREATE CONFERENCE
    // ============================

    @GetMapping("/conferences/new")
    public String newConferencePage(HttpSession session, Model model) {
        if (!isOrganizer(session)) return "redirect:/login";
        model.addAttribute("user", sessionUtil.getUser(session));
        return "organizer/conference-form";
    }

    @PostMapping("/conferences/new")
    public String createConference(@RequestParam String title,
                                    @RequestParam String description,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    @RequestParam String location,
                                    @RequestParam(defaultValue = "0") BigDecimal fee,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!isOrganizer(session)) return "redirect:/login";
        User organizer = sessionUtil.getUser(session);

        try {
            conferenceService.createConference(title, description, date, location, fee, organizer);
            redirectAttributes.addFlashAttribute("success", "Conference created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create conference: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ============================
    // EDIT CONFERENCE
    // ============================

    @GetMapping("/conferences/{id}/edit")
    public String editConferencePage(@PathVariable Long id, HttpSession session, Model model) {
        if (!isOrganizer(session)) return "redirect:/login";

        Optional<Conference> confOpt = conferenceService.findById(id);
        if (confOpt.isEmpty()) return "redirect:/dashboard";

        model.addAttribute("conference", confOpt.get());
        model.addAttribute("user", sessionUtil.getUser(session));
        return "organizer/conference-form";
    }

    @PostMapping("/conferences/{id}/edit")
    public String updateConference(@PathVariable Long id,
                                    @RequestParam String title,
                                    @RequestParam String description,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    @RequestParam String location,
                                    @RequestParam(defaultValue = "0") BigDecimal fee,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!isOrganizer(session)) return "redirect:/login";

        try {
            conferenceService.updateConference(id, title, description, date, location, fee);
            redirectAttributes.addFlashAttribute("success", "Conference updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ============================
    // DELETE CONFERENCE
    // ============================

    @PostMapping("/conferences/{id}/delete")
    public String deleteConference(@PathVariable Long id, HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!isOrganizer(session)) return "redirect:/login";

        try {
            conferenceService.deleteConference(id);
            redirectAttributes.addFlashAttribute("success", "Conference deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ============================
    // VIEW REGISTRATIONS FOR CONFERENCE
    // ============================

    @GetMapping("/conferences/{id}/registrations")
    public String viewRegistrations(@PathVariable Long id, HttpSession session, Model model) {
        if (!isOrganizer(session)) return "redirect:/login";

        Optional<Conference> confOpt = conferenceService.findById(id);
        if (confOpt.isEmpty()) return "redirect:/dashboard";

        List<Registration> registrations = conferenceService.getRegistrationsByConference(id);
        model.addAttribute("conference", confOpt.get());
        model.addAttribute("registrations", registrations);
        model.addAttribute("user", sessionUtil.getUser(session));
        return "organizer/registrations";
    }

    // ============================
    // VIEW SUBMISSIONS FOR CONFERENCE
    // ============================

    @GetMapping("/conferences/{id}/submissions")
    public String viewSubmissions(@PathVariable Long id, HttpSession session, Model model) {
        if (!isOrganizer(session)) return "redirect:/login";

        Optional<Conference> confOpt = conferenceService.findById(id);
        if (confOpt.isEmpty()) return "redirect:/dashboard";

        List<Submission> submissions = conferenceService.getSubmissionsByConference(id);
        model.addAttribute("conference", confOpt.get());
        model.addAttribute("submissions", submissions);
        model.addAttribute("user", sessionUtil.getUser(session));
        return "organizer/submissions";
    }

    // ============================
    // REVIEW SUBMISSION (Approve/Reject)
    // ============================

    @PostMapping("/submissions/{subId}/review")
    public String reviewSubmission(@PathVariable Long subId,
                                    @RequestParam String status,
                                    @RequestParam Long conferenceId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!isOrganizer(session)) return "redirect:/login";

        try {
            Submission.Status newStatus = Submission.Status.valueOf(status.toUpperCase());
            conferenceService.updateSubmissionStatus(subId, newStatus);
            redirectAttributes.addFlashAttribute("success", "Submission " + status.toLowerCase() + "d.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update submission: " + e.getMessage());
        }
        return "redirect:/organizer/conferences/" + conferenceId + "/submissions";
    }
}
