package com.example.DATN.repository;

import com.example.DATN.entity.ActiveCode;
import com.example.DATN.entity.User;
import com.example.DATN.entity.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActiveCodeRepository extends JpaRepository<ActiveCode,Long> {
    Optional<ActiveCode> findByUserAndOtpAndType(User user, String otp, OtpType type);
    void deleteByUserAndType(User user, OtpType type);
}
