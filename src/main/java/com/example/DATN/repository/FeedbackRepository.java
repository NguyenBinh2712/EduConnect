package com.example.DATN.repository;

import com.example.DATN.entity.FeedbackTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackTeacher,Long> {
    List<FeedbackTeacher> findByAttemptIdOrderByCreateAtAsc(Long attemptId);
}
