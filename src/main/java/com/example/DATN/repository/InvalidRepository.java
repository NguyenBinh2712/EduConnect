package com.example.DATN.repository;

import com.example.DATN.entity.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidRepository extends JpaRepository<InvalidToken,String> {

}
