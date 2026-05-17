package com.example.DATN.repository;

import com.example.DATN.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant,Long> {
    Optional<Participant> findByConversationIdAndUserId(Long conversationId, Long userId);

    List<Participant> findByConversationId(Long conversationId);

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    /** Lấy userId của tất cả participant trong conversation */
    @Query("SELECT p.user.id FROM Participant p WHERE p.conversation.id = :convId")
    List<Long> findUserIdsByConversationId(@Param("convId") Long conversationId);
}
