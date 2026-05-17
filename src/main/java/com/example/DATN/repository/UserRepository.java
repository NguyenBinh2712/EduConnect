package com.example.DATN.repository;

import com.example.DATN.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long>{
    boolean existsByEmail(String email);
    public Optional<User> findUserByEmail(String email);

}
