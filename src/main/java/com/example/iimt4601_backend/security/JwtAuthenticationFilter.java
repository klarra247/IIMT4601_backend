package com.example.iimt4601_backend.security;

import com.example.iimt4601_backend.dto.LoginRequestDto;
import com.example.iimt4601_backend.dto.LoginRequestDto;
import com.example.iimt4601_backend.dto.UserResponseDto;
import com.example.iimt4601_backend.entity.User;
import com.example.iimt4601_backend.enums.UserRoleEnum;
import com.example.iimt4601_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.example.iimt4601_backend.security.JwtUtil.BEARER_PREFIX;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        setFilterProcessesUrl("/login");
    }

    // 로그인 시도 시 인증 처리
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUserName(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // 인증 성공 후 호출되는 메소드
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        try {
            // 인증된 사용자 정보 가져오기
            UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
            String username = userDetails.getUsername();
            UserRoleEnum role = userDetails.getUser().getRole();

            // Access Token과 Refresh Token 생성
            String accessToken = jwtUtil.createAccessToken(username, role);
            String refreshToken = jwtUtil.createRefreshToken(username, role);

            // 생성된 refreshToken을 저장
            jwtUtil.saveRefreshToken(username, refreshToken, request);

//            // 헤더에 토큰 추가
//            response.addHeader("Authorization", BEARER_PREFIX + accessToken);
//            response.addHeader("Refresh-Token", BEARER_PREFIX + refreshToken);

            // Access Token을 HttpOnly 쿠키로 설정
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true); // HTTPS인 경우만 Secure 설정
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) (jwtUtil.getAccessTokenExpiration() / 1000)); // 초 단위로 변환
            response.addCookie(accessTokenCookie);

            // Refresh Token을 HttpOnly 쿠키로 설정
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(request.isSecure());
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpiration() / 1000));
            response.addCookie(refreshTokenCookie);

            // 환경별 SameSite 정책 결정
            boolean isSecure = request.isSecure() || "https".equals(request.getHeader("X-Forwarded-Proto"));
            String sameSitePolicy = isSecure ? "None" : "Lax";

            // Access Token 쿠키 설정
            String accessTokenHeader = String.format("%s=%s; HttpOnly; SameSite=%s; Path=/; Max-Age=%d%s",
                    "accessToken", accessToken,
                    sameSitePolicy, (jwtUtil.getAccessTokenExpiration() / 1000),
                    isSecure ? "; Secure" : "");

            // Refresh Token 쿠키 설정
            String refreshTokenHeader = String.format("%s=%s; HttpOnly; SameSite=%s; Path=/; Max-Age=%d%s",
                    "refreshToken", refreshToken,
                    sameSitePolicy, (jwtUtil.getRefreshTokenExpiration() / 1000),
                    isSecure ? "; Secure" : "");

            // Set-Cookie 헤더로 설정 (addCookie 대신 사용)
            response.setHeader("Set-Cookie", accessTokenHeader);
            response.addHeader("Set-Cookie", refreshTokenHeader);


            UserResponseDto userResponseDto = new UserResponseDto();
            userResponseDto.setUserId(userDetails.getUser().getId());
            userResponseDto.setUserName(userDetails.getUser().getUserName());
            userResponseDto.setName(userDetails.getUser().getName());
            userResponseDto.setEmail(userDetails.getUser().getEmail());
            userResponseDto.setPhoneNumber(userDetails.getUser().getPhoneNumber());
            userResponseDto.setLastLoginDate(userDetails.getUser().getLastLoginDate());
            userResponseDto.setCreatedAt(userDetails.getUser().getCreatedAt());
            userResponseDto.setActive(userDetails.getUser().getIsActive());
            userResponseDto.setRole(userDetails.getUser().getRole().toString());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "로그인 성공!");
            responseData.put("role", role.toString());
            responseData.put("user", userResponseDto);
            // 토큰은 이제 응답 본문에 포함하지 않고 쿠키로만 전송
//            responseData.put("accessToken", accessToken);
//            responseData.put("refreshToken", refreshToken);

            // 마지막 로그인 시간 업데이트
            User user = userDetails.getUser();
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.writeValue(response.getWriter(), responseData);        } catch (Exception e) {
            log.error("Authentication error: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Authentication failed: " + e.getMessage());
        }
    }

    // 인증 실패 시 호출되는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}