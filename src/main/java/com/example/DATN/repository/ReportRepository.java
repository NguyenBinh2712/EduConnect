package com.example.DATN.repository;

import com.example.DATN.entity.Post;
import com.example.DATN.entity.Report;
import com.example.DATN.entity.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByPostAndReporterId(Post post, Long reporterId);
    List<Report> findByStatusOrderByReportedAtDesc(ReportStatus status);
}