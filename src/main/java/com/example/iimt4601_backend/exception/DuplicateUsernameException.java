package com.example.iimt4601_backend.exception;

// 사용자명 중복 예외
public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
