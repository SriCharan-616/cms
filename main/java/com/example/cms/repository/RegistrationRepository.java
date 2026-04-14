package com.example.cms.repository;

import com.example.cms.entity.Conference;
import com.example.cms.entity.Registration;
import com.example.cms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByUser(User user);

    List<Registration> findByConference(Conference conference);

    Optional<Registration> findByUserAndConference(User user, Conference conference);

    boolean existsByUserAndConference(User user, Conference conference);

    long countByConference(Conference conference);
}
