package com.example.DATN.repository;

import com.example.DATN.entity.Post;
import com.example.DATN.entity.enums.Privacy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Slice<Post> findByIsHiddenFalseAndPrivacy(Privacy privacy, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId AND p.isHidden = false ORDER BY p.createdAt DESC")
    Slice<Post> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    Slice<Post> findByGroupIdAndIsHiddenFalse(Long groupId, Pageable pageable);
    List<Post> findByGroupId(Long groupId);
}
