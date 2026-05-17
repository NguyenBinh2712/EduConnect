package com.example.DATN.repository;

import com.example.DATN.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia,Long> {

}
