package com.example.iimt4601_backend.security;

import com.example.iimt4601_backend.enums.UserRoleEnum;
import com.example.iimt4601_backend.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;


@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenService refreshTokenService;
    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    // HTTP 요청을 필터링하고 JWT 인증을 처리하는 메소드
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("=== JWT Authorization Filter 시작 ===");
        log.debug("Request URL: {}", req.getRequestURL());
        log.debug("X-Forwarded-Proto: {}", req.getHeader("X-Forwarded-Proto"));
        log.debug("isSecure(): {}", req.isSecure());

        // 쿠키 디버깅
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.debug("Cookie: {} = {}", cookie.getName(),
                        cookie.getValue().substring(0, Math.min(cookie.getValue().length(), 10)) + "...");
            }
        } else {
            log.debug("No cookies found");
        }

        // 쿠키에서 토큰 가져오기
        String accessToken = jwtUtil.getTokenFromCookie(req, "accessToken");
        String refreshToken = jwtUtil.getTokenFromCookie(req, "refreshToken");

        if (StringUtils.hasText(accessToken)) {
            try {
                // Access Token이 유효하지 않으면 Refresh Token을 통해 인증 시도
                if (!jwtUtil.validateToken(accessToken)) {
                    if (refreshToken == null || !handleRefreshToken(req, res, refreshToken)) {
                        // Refresh Token이 없거나 유효하지 않으면 자동 로그아웃
                        handleExpiredSession(req, res);
                        return;
                    }
                } else {
                    // Access Token이 유효하면 인증 처리
                    Claims accessTokenInfo = jwtUtil.getUserInfoFromToken(accessToken);
                    setAuthentication(accessTokenInfo.getSubject());
                }
            } catch (Exception e) {
                // 인증 오류 시 자동 로그아웃 처리
                log.error("Authentication error", e);
                handleExpiredSession(req, res);
                return;
            }
        }
        filterChain.doFilter(req, res);
    }

    private boolean handleRefreshToken(HttpServletRequest req, HttpServletResponse res, String refreshToken)
            throws ServletException, IOException {
        if (!StringUtils.hasText(refreshToken)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            Claims refreshTokenInfo = jwtUtil.getUserInfoFromToken(refreshToken);
            String username = refreshTokenInfo.getSubject();
            String currentUserAgent = req.getHeader("User-Agent");

            if (!refreshTokenService.isValidTokenForUserAgent(username, refreshToken, currentUserAgent)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            if (refreshTokenService.isValidToken(username, refreshToken)) {
                UserRoleEnum role = UserRoleEnum.valueOf(
                        refreshTokenInfo.get(JwtUtil.AUTHORIZATION_KEY, String.class)
                );
                setAuthentication(username);

                // 새 Access Token 발급
                String newAccessToken = jwtUtil.createAccessToken(username, role);

                // 환경별 SameSite 정책 결정
                boolean isSecure = req.isSecure() || "https".equals(req.getHeader("X-Forwarded-Proto"));
                String sameSitePolicy = isSecure ? "None" : "Lax";

                // 새로운 Access Token 쿠키 설정
                String accessTokenHeader = String.format("%s=%s; HttpOnly; SameSite=%s; Path=/; Max-Age=%d%s",
                        "accessToken", newAccessToken,
                        sameSitePolicy, (jwtUtil.getAccessTokenExpiration() / 1000),
                        isSecure ? "; Secure" : "");

                res.setHeader("Set-Cookie", accessTokenHeader);
                return true;
            }
        } catch (Exception e) {
            log.error("Error handling refresh token", e);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return false;
    }

    // 사용자 인증 정보 설정
    public void setAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // 인증 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        // SecurityContext에 인증 객체 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // 세션 만료 시 처리 메소드
    private void handleExpiredSession(HttpServletRequest req, HttpServletResponse res) {
        // 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        res.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        res.addCookie(refreshTokenCookie);

        // 사용자 이름을 SecurityContext에서 가져올 수 있으면 로그아웃 이벤트 발행
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String username = userDetails.getUsername();

        }

        // 보안 컨텍스트 정리
        SecurityContextHolder.clearContext();

        // 401 Unauthorized 상태 코드 반환
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}