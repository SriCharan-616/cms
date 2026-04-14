package com.example.cms.service;

import com.example.cms.entity.*;
import com.example.cms.repository.*;
import com.example.cms.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConferenceService {

    private final ConferenceRepository conferenceRepository;
    private final RegistrationRepository registrationRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final FileUploadUtil fileUploadUtil;

    // ============================
    // CONFERENCE OPERATIONS
    // ============================

    public Conference createConference(String title, String description, LocalDate date,
                                       String location, BigDecimal fee, User organizer) {
        Conference conf = new Conference();
        conf.setTitle(title);
        conf.setDescription(description);
        conf.setDate(date);
        conf.setLocation(location);
        conf.setFee(fee != null ? fee : BigDecimal.ZERO);
        conf.setCreatedBy(organizer);
        return conferenceRepository.save(conf);
    }

    public Conference updateConference(Long id, String title, String description,
                                        LocalDate date, String location, BigDecimal fee) {
        Conference conf = conferenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + id));
        conf.setTitle(title);
        conf.setDescription(description);
        conf.setDate(date);
        conf.setLocation(location);
        conf.setFee(fee != null ? fee : BigDecimal.ZERO);
        return conferenceRepository.save(conf);
    }

    public void deleteConference(Long id) {
        conferenceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Conference> findById(Long id) {
        return conferenceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Conference> findAll() {
        return conferenceRepository.findAllByOrderByDateAsc();
    }

    @Transactional(readOnly = true)
    public List<Conference> searchConferences(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        return conferenceRepository.searchByKeyword(keyword);
    }

    @Transactional(readOnly = true)
    public List<Conference> findByOrganizer(User organizer) {
        return conferenceRepository.findByCreatedBy(organizer);
    }

    // ============================
    // REGISTRATION OPERATIONS
    // ============================

    public Registration registerForConference(User user, Long conferenceId) {
        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found"));

        if (registrationRepository.existsByUserAndConference(user, conference)) {
            throw new IllegalStateException("Already registered for this conference");
        }

        Registration reg = new Registration();
        reg.setUser(user);
        reg.setConference(conference);
        reg.setStatus(Registration.Status.CONFIRMED);
        return registrationRepository.save(reg);
    }

    public void cancelRegistration(Long registrationId, Long userId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        if (!reg.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized to cancel this registration");
        }
        registrationRepository.delete(reg);
    }

    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsByUser(User user) {
        return registrationRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsByConference(Long conferenceId) {
        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found"));
        return registrationRepository.findByConference(conference);
    }

    @Transactional(readOnly = true)
    public boolean isUserRegistered(User user, Conference conference) {
        return registrationRepository.existsByUserAndConference(user, conference);
    }

    // ============================
    // SUBMISSION OPERATIONS
    // ============================

    public Submission submitPaper(User user, Long conferenceId, String title,
                                   String abstrakt, MultipartFile file) throws IOException {
        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found"));

        String filePath = null;
        if (file != null && !file.isEmpty()) {
            filePath = fileUploadUtil.uploadFile(file, user.getId());
        }

        Submission submission = new Submission();
        submission.setTitle(title);
        submission.setAbstrakt(abstrakt);
        submission.setFilePath(filePath);
        submission.setStatus(Submission.Status.PENDING);
        submission.setUser(user);
        submission.setConference(conference);
        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByUser(User user) {
        return submissionRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByConference(Long conferenceId) {
        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found"));
        return submissionRepository.findByConference(conference);
    }

    public Submission updateSubmissionStatus(Long submissionId, Submission.Status status) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        sub.setStatus(status);
        return submissionRepository.save(sub);
    }
}
