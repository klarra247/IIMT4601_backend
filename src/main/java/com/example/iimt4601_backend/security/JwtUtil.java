package com.example.iimt4601_backend.security;

import com.example.iimt4601_backend.enums.UserRoleEnum;
import com.example.iimt4601_backend.repository.RefreshTokenRepository;
import com.example.iimt4601_backend.service.RefreshTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";

    // Access Token 만료 시간
    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    // Refresh Token 만료 시간
    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtUtil(RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // 초기화 메소드: secretKey를 바탕으로 암호화 키 초기화
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // Access Token 생성
    public String createAccessToken(String username, UserRoleEnum role) {
        Date date = new Date();

        return Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .claim("token_type", "access_token") // 토큰 유형 추가
                        .setExpiration(new Date(date.getTime() + accessTokenExpiration)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String username, UserRoleEnum role) {
        Date date = new Date();

        return Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                .claim("token_type", "refresh") // 토큰 유형 추가
                .setExpiration(new Date(date.getTime() + refreshTokenExpiration)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // Refresh Token을 DB에 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRefreshToken(String username, String token, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIp(request);
        refreshTokenService.saveRefreshToken(username, token, userAgent, ipAddress, refreshTokenExpiration);
    }

    // 클라이언트 IP 주소 가져오기
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    // 로그아웃 처리: 사용자에 대한 모든 Refresh Token 삭제
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logout(String username) {
        try {
            refreshTokenService.removeRefreshToken(username); // Refresh Token 삭제
            log.info("User {} has been logged out, all tokens removed.", username);

        } catch (Exception e) {
            log.error("Error during logout for user: {}", username, e);
            throw new RuntimeException("Logout failed", e);
        }
    }

    // 로그아웃 시 쿠키 삭제 메소드
    public void addClearCookies(HttpServletResponse response) {
        // Access Token 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);
    }
    // 요청에서 JWT 토큰을 추출하는 메소드
    public String getTokenFromRequest(HttpServletRequest req, String tokenType) {
        String bearerToken = req.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(7).trim(); // 공백 제거
            log.debug("Extracted JWT Token: {}", token);
            return token;
        }
        return null;
    }

    // 쿠키에서 토큰 추출 메소드
    public String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            // JWT 토큰을 서명된 키로 검증
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 유효한 토큰일 경우 true 반환
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token. Attempting to refresh...");
            return false; // 만료된 토큰일 경우 false 반환
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false; // 잘못된 토큰일 경우 false 반환
        } catch (Exception e) {
            log.error("Error validating token", e); // 기타 오류 처리
            return false;
        }
    }

    // 토큰에서 사용자 정보 추출
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    // 토큰 유형 확인
    public boolean isAccessToken(String token) {
        try {
            Claims claims = getUserInfoFromToken(token);
            return "access".equals(claims.get("token_type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getUserInfoFromToken(token);
            return "refresh".equals(claims.get("token_type"));
        } catch (Exception e) {
            return false;
        }
    }
}