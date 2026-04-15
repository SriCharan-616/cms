package com.example.cms.repository;

import com.example.cms.entity.Conference;
import com.example.cms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {

    List<Conference> findByCreatedBy(User user);

    @Query("SELECT c FROM Conference c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Conference> searchByKeyword(@Param("keyword") String keyword);

    List<Conference> findAllByOrderByDateAsc();
}
