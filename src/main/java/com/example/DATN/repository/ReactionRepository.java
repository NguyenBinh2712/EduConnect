package com.example.DATN.repository;

import com.example.DATN.entity.Reaction;
import com.example.DATN.entity.enums.ReactionTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction,Long> {
    Optional<Reaction> findByTargetIdAndTargetTypeAndUserId(
            Long targetId,
            ReactionTargetType targetType,
            Long userId
    );

    List<Reaction> findByTargetIdAndTargetType(
            Long targetId,
            ReactionTargetType targetType
    );
    void deleteByTargetIdAndTargetType(Long postId,ReactionTargetType type);
}
