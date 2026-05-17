package com.example.DATN.repository;

import com.example.DATN.dto.friend.FriendResponse;
import com.example.DATN.dto.friend.RecommendUser;
import com.example.DATN.entity.Friendship;
import com.example.DATN.entity.User;
import com.example.DATN.entity.enums.FriendshipStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship,Long> {
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
    List<Friendship> findByFriendIdAndStatus(Long friendId, FriendshipStatus status);
    List<Friendship> findByUserIdAndStatus(Long userId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.user.id = :u1 AND f.friend.id = :u2) OR (f.user.id = :u2 AND f.friend.id = :u1)")
    Optional<Friendship> findFriendshipBetween(@Param("u1") Long u1, @Param("u2") Long u2);
    @Query("""
    SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
    FROM Friendship f
    WHERE f.status = com.example.DATN.entity.enums.FriendshipStatus.ACCEPTED
    AND (
        (f.user.id = :userId1 AND f.friend.id = :userId2)
        OR (f.user.id = :userId2 AND f.friend.id = :userId1)
    )
""")
    boolean existsFriendship(Long userId1, Long userId2);
}

