package com.hrapp.repository;

import com.hrapp.entity.PasswordResetToken;
import com.hrapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
