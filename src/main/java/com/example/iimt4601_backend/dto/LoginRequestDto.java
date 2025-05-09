package com.example.iimt4601_backend.dto;

import lombok.Getter;

// 로그인 Dto
@Getter
public class LoginRequestDto {
    private String userName;
    private String password;
}