package com.example.cms.repository;

import com.example.cms.entity.Conference;
import com.example.cms.entity.Submission;
import com.example.cms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUser(User user);

    List<Submission> findByConference(Conference conference);

    List<Submission> findByUserAndConference(User user, Conference conference);

    long countByConferenceAndStatus(Conference conference, Submission.Status status);
}
