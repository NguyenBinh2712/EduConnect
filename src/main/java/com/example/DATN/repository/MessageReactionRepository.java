package com.example.DATN.repository;

import com.example.DATN.entity.document.MessageReaction;
import com.example.DATN.entity.document.Messages;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReactionRepository extends MongoRepository<MessageReaction,String> {
    Optional<MessageReaction> findByMessageIdAndUserId(String messageId, Long userId);

}
