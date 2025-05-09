package com.example.iimt4601_backend.repository;

import com.example.iimt4601_backend.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    int deleteByExpiresAtBefore(LocalDateTime dateTime);
    Optional<RefreshToken> findByUsernameAndToken(String username, String token);
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.username = :username")
    void deleteByUsername(@Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<RefreshToken> findByUsername(String username);
}

