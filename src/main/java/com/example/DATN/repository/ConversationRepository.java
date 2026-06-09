package com.example.DATN.repository;

import com.example.DATN.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
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
    Optional<Conversation> findConversationOneToOne(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    @Query("""
          SELECT DISTINCT c
          FROM Conversation c
          JOIN c.participants p
          WHERE p.user.id = :userId
          ORDER BY c.lastMessageAt DESC NULLS LAST
    """)
    List<Conversation> findAllConversationByUserId(
            @Param("userId") Long userId
    );

    @Query("""
          SELECT DISTINCT c
          FROM Conversation c
          JOIN c.participants p
          WHERE p.user.id = :userId
          ORDER BY c.lastMessageAt DESC NULLS LAST
    """)
    Slice<Conversation> findAllConversationByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );
    @Query("""
          SELECT c FROM Conversation c 
           JOIN c.participants p 
               WHERE p.user.id = :userId 
          AND p.deleted = false AND p.archived = false""")
    Slice<Conversation> findActiveConversationsByUserId(
            @Param("userId") Long userId, Pageable pageable);
}