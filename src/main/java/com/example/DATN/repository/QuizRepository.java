package com.example.DATN.repository;

import com.example.DATN.entity.Quiz;
import com.example.DATN.entity.enums.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz,Long> {
    // Đề trong group
    List<Quiz> findByGroupIdAndStatus(Long groupId, QuizStatus status);

    // Đề public đã được duyệt
    Page<Quiz> findByGroupIsNullAndStatus(QuizStatus status, Pageable pageable);

    // Đề do giáo viên tạo
    Page<Quiz> findByCreatorId(Long creatorId, Pageable pageable);

    // Kiểm tra giáo viên có phải chủ đề không
    boolean existsByIdAndCreatorId(Long quizId, Long creatorId);

    Slice<Quiz> findByStatusAndGroupIsNull(QuizStatus status, Pageable pageable);
    List<Quiz> findByGroupIdAndStatusOrderByCreateAtDesc(
            Long groupId,
            QuizStatus status
    );

    List<Quiz> findByCreator_IdOrderByCreateAtDesc(Long creatorId);
}
