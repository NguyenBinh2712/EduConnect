package com.example.DATN.repository;

import com.example.DATN.entity.document.ContentQuiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuizContentRepository extends MongoRepository<ContentQuiz,String> {
    Optional<ContentQuiz> findByQuizId(Long quizId);
    void deleteByQuizId(Long quizId);
}
