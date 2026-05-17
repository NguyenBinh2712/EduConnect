package com.example.DATN.repository;

import com.example.DATN.entity.Group;
import com.example.DATN.entity.GroupMembership;
import com.example.DATN.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership,Long> {
    Optional<GroupMembership> findByGroupAndUser(Group group, User user);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    List<GroupMembership> findByGroupId(Long groupId);
    long countByGroupId(Long groupId);
    List<GroupMembership> findByUserId(Long userId);
    Optional<GroupMembership> findByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);

}
