package com.example.DATN.repository;

import com.example.DATN.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {
    List<Group> findByOwnerId(Long ownerId);
    List<Group> findByNameContainingIgnoreCase(String keyword);
}
