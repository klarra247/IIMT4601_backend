package com.example.iimt4601_backend.exception;

// 회원가입 처리 예외
public class SignupProcessingException extends RuntimeException {
    public SignupProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
