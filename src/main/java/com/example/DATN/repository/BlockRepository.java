package com.example.DATN.repository;

import com.example.DATN.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block,Long> {
    boolean existsByBlockerIdAndBlockedId(Long userId1,Long userId2);

    List<Block> findByBlockerId(Long blockerId);

    List<Block> findByBlockedId(Long blockedId);

    @Query("SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :blockerId")
    List<Long> findBlockedIdsByBlockerId(Long blockerId);

    @Query("SELECT b.blocker.id FROM Block b WHERE b.blocked.id = :blockedId")
    List<Long> findBlockerIdsByBlockedId(Long blockedId);

    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
