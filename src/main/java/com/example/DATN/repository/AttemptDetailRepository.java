package com.example.DATN.repository;

import com.example.DATN.entity.document.AttemptDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AttemptDetailRepository extends MongoRepository<AttemptDetail,String> {
    Optional<AttemptDetail> findByAttemptId(Long attemptId);
    List<AttemptDetail> findByQuizIdAndUserId(Long quizId, Long userId);
}
