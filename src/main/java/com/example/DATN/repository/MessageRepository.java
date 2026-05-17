package com.example.DATN.repository;

import com.example.DATN.entity.document.Messages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Messages,String> {
    Page<Messages> findByConversationIdOrderByTimestampDesc(Long conversationId,Pageable pageable);

    @Query(value = "{ " +
            "'conversationId': ?0, " +
            "'senderId': { $ne: ?1 }, " +
            "$or: [ " +
            "   { 'seenBy': { $exists: false } }, " +
            "   { 'seenBy': { $nin: [?1] } } " +
            "] " +
            "}",
            count = true)
    long countUnreadByConversationIdAndUserId(Long conversationId, Long userId);

    List<Messages> findByConversationIdAndIsPendingTrue(Long conversationId);


    void deleteByConversationId(Long conversationId);


    @Query("""
            {
              'conversationId': ?0,
              'content': { $regex: ?1, $options: 'i' },
              $or: [
                { 'deletedFor': { $exists: false } },
                { 'deletedFor': { $nin: [?2] } }
              ]
            }
            """)
    List<Messages> searchInConversation(Long conversationId, String keywordRegex, Long userId,Pageable pageable);


    @Query(value = """
            {
              'conversationId': ?0,
              'content': { $regex: ?1, $options: 'i' },
              $or: [
                { 'deletedFor': { $exists: false } },
                { 'deletedFor': { $nin: [?2] } }
              ]
            }
            """, count = true)
    long countSearchResults(Long conversationId, String keywordRegex, Long userId);

}
