package com.example.DATN.repository;

import com.example.DATN.entity.TeacherApplication;
import com.example.DATN.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TeacherApplicationRepository extends JpaRepository<TeacherApplication, Long> {
    boolean existsByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);

    // Admin xem pending
    List<TeacherApplication> findByStatusOrderByAppliedAtDesc(ApplicationStatus status);

    // User xem đơn pending của mình
    Optional<TeacherApplication> findByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);

    boolean existsByApplicantIdAndStatusIn(Long userId, List<ApplicationStatus> pending);
}