package com.example.DATN.repository;

import com.example.DATN.entity.GroupJoinRequest;
import com.example.DATN.entity.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest,Long> {
    List<GroupJoinRequest> findByGroupIdAndStatus(Long groupId, JoinRequestStatus status);

    List<GroupJoinRequest> findByUserIdAndStatus(Long userId, JoinRequestStatus status);
    Optional<GroupJoinRequest> findByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);

}
