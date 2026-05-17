package com.example.DATN.repository;

import com.example.DATN.dto.chat.SearchRequest;
import com.example.DATN.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
           SELECT c FROM Conversation c
           WHERE c.isGroup = false
           AND SIZE(c.participants)=2
           AND :userId1 IN (SELECT p.user.id FROM c.participants p)
           AND :userId2 IN (SELECT p.user.id FROM c.participants p)
""")
    public Optional<Conversation> findConversationOneToOne(@Param("userId1") Long userId1,@Param("userId2")Long userId2);

   @Query("""
          SELECT DISTINCT c FROM Conversation c
          JOIN c.participants p
          WHERE p.user.id= :userId
          ORDER BY c.lastMessageAt DESC NULLS LAST
""")
    public List<Conversation> findAllConversationByUserId(@Param("userId")Long userId);

}