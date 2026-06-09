package com.example.DATN.repository;

import com.example.DATN.entity.QuizAttempt;
import com.example.DATN.entity.enums.AttemptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt,Long> {
    // Lần làm gần nhất của user với quiz
    Optional<QuizAttempt> findTopByQuizIdAndUserIdOrderByAttemptNumberDesc(Long quizId, Long userId);

    // Tất cả lần làm của user với quiz (để hiện lịch sử)
    List<QuizAttempt> findByQuizIdAndUserIdOrderByAttemptNumberAsc(Long quizId, Long userId);

    // Đang làm dở (IN_PROGRESS) — dùng khi resume hoặc check timeout
    Optional<QuizAttempt> findByQuizIdAndUserIdAndStatus(Long quizId, Long userId, AttemptStatus status);

    // Bài thi đang làm dở chưa nộp (để scheduler auto-submit)
    @Query("""
        SELECT a FROM QuizAttempt a
        WHERE a.status = 'IN_PROGRESS'
        AND a.startAt < :deadline
        """)
    List<QuizAttempt> findExpiredAttempts(@Param("deadline") java.time.LocalDateTime deadline);

    // Giáo viên xem tất cả bài nộp của một đề
    Page<QuizAttempt> findByQuizIdAndStatusIn(Long quizId, List<AttemptStatus> statuses, Pageable pageable);

    // Điểm cao nhất của user trong một đề
    @Query("SELECT MAX(a.score) FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.user.id = :userId")
    Optional<Double> findBestScore(@Param("quizId") Long quizId, @Param("userId") Long userId);

    // Số lần đã làm
    int countByQuizIdAndUserId(Long quizId, Long userId);
}
