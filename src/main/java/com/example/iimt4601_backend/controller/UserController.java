package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.UserResponseDto;
import com.example.iimt4601_backend.security.UserDetailsImpl;
import com.example.iimt4601_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;


    // 현재 인증된 사용자의 정보를 반환하는 엔드포인트
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 인증된 사용자가 없으면 401 Unauthorized 반환
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        UserResponseDto userResponseDto = userService.getCurrentUserInfo(userDetails.getUser());

        if (userResponseDto == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(userResponseDto);
    }
}