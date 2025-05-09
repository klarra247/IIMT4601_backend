package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.SignupRequestDto;
import com.example.iimt4601_backend.enums.UserRoleEnum;
import com.example.iimt4601_backend.security.JwtUtil;
import com.example.iimt4601_backend.security.UserDetailsImpl;
import com.example.iimt4601_backend.service.RefreshTokenService;
import com.example.iimt4601_backend.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        // 예외는 GlobalExceptionHandler에서 처리하므로, 여기서는 성공 응답만 반환합니다.
        userService.signup(requestDto);

        // 성공 응답
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "회원가입 성공!");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 Refresh Token 가져오기
        String refreshToken = jwtUtil.getTokenFromCookie(request, "refreshToken");

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        Claims claims = jwtUtil.getUserInfoFromToken(refreshToken);
        String username = claims.getSubject();
        UserRoleEnum role = UserRoleEnum.valueOf(claims.get(JwtUtil.AUTHORIZATION_KEY).toString());

        // 사용자 에이전트 확인
        String userAgent = request.getHeader("User-Agent");
        if (!refreshTokenService.isValidTokenForUserAgent(username, refreshToken, userAgent)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
        }

        // 새 Access Token 생성
        String newAccessToken = jwtUtil.createAccessToken(username, role);

        // Access Token을 쿠키에 설정
        Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(request.isSecure());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtUtil.getAccessTokenExpiration() / 1000));
        response.addCookie(accessTokenCookie);

        // SameSite 설정 추가
        String sameSitePolicy = "Lax";
        response.setHeader("Set-Cookie", accessTokenCookie.getName() + "=" + accessTokenCookie.getValue()
                + "; HttpOnly; SameSite=" + sameSitePolicy + "; Path=/; Max-Age=" + accessTokenCookie.getMaxAge()
                + (request.isSecure() ? "; Secure" : ""));

        return ResponseEntity.ok().body(Map.of("message", "토큰이 갱신되었습니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 인증된 사용자가 있는 경우 DB에서 토큰 삭제
        if (userDetails != null) {
            String username = userDetails.getUsername();
            jwtUtil.logout(username);
        }

        // 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(request.isSecure());
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(request.isSecure());
        response.addCookie(refreshTokenCookie);

        // SameSite 설정 추가 (액세스 토큰)
        String sameSitePolicy = "Lax";
        response.setHeader("Set-Cookie", accessTokenCookie.getName() + "=" + accessTokenCookie.getValue()
                + "; HttpOnly; SameSite=" + sameSitePolicy + "; Path=/; Max-Age=0"
                + (request.isSecure() ? "; Secure" : ""));

        // 세션 무효화
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }

        // 보안 컨텍스트 정리 (항상 실행)
        SecurityContextHolder.clearContext();


        // 응답 처리
        if (userDetails != null) {
            return ResponseEntity.ok().body(Map.of("message", "로그아웃 성공"));
        } else {
            // 인증된 사용자가 없어도 쿠키는 삭제하고 200 OK 반환
            // 프론트엔드에서는 어차피 인증 상태를 초기화할 것이므로
            return ResponseEntity.ok().body(Map.of("message", "이미 로그아웃된 상태입니다"));
        }
    }

}