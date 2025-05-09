package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.entity.RefreshToken;
import com.example.iimt4601_backend.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    // Refresh Token을 저장하거나 업데이트하는 메소드
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRefreshToken(String username, String token, String userAgent, String ipAddress, long expirationTimeInMillis) {
        try {
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plus(expirationTimeInMillis, ChronoUnit.MILLIS);

            // 기존 토큰 삭제
            refreshTokenRepository.deleteByUsername(username);

            // 새 토큰 생성 및 저장
            RefreshToken refreshToken = new RefreshToken(username, token, userAgent, ipAddress, issuedAt, expiresAt);
            refreshTokenRepository.save(refreshToken);

            log.info("Token updated for user: {}", username);
        } catch (Exception e) {
            log.error("Error managing refresh token for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to manage refresh token", e);
        }
    }

    // 사용자 이름과 토큰이 유효한지 확인
    @Transactional(readOnly = true)
    public boolean isValidToken(String username, String token) {
        return refreshTokenRepository.findByUsernameAndToken(username, token)
                .map(rt -> !rt.isExpired())
                .orElse(false);
    }

    // 사용자 이름, 토큰, User-Agent를 모두 확인하여 유효성 검사
    @Transactional(readOnly = true)
    public boolean isValidTokenForUserAgent(String username, String token, String userAgent) {
        return refreshTokenRepository.findByUsernameAndToken(username, token)
                .map(rt -> {
                    if (rt.isExpired()) {
                        log.info("Token expired for user: {}", username);
                        return false;  // 만료된 토큰이면 false
                    }
                    if (!rt.getUserAgent().equals(userAgent)) {
                        log.warn("User agent mismatch for user: {}. Expected: {}, Actual: {}",
                                username, rt.getUserAgent(), userAgent);
                        return false;  // User-Agent가 일치하지 않으면 false
                    }
                    return true;  // 유효한 토큰이면 true
                })
                .orElse(false);  // 존재하지 않으면 false
    }

    // 사용자의 Refresh Token을 삭제
    @Transactional
    public void removeRefreshToken(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    // 만료된 토큰 정리 - 스케줄러로 주기적으로 실행
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();  // 현재 시간
        // 만료된 토큰을 DB에서 삭제
        int deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(now);
        log.info("Cleaned up {} expired refresh tokens", deletedCount);  // 삭제된 토큰 개수 로그 출력
    }
}