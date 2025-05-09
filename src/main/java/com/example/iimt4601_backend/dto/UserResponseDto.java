package com.example.iimt4601_backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String userName;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdAt;
    private boolean isActive;
    private String role;

}