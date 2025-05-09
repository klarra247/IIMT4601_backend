package com.example.iimt4601_backend.exception;

import com.example.iimt4601_backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 사용자명 중복 예외 처리
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(DuplicateUsernameException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code("DUPLICATE_USERNAME")
                .message(ex.getMessage())
                .field("userName")
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 이메일 중복 예외 처리
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code("DUPLICATE_EMAIL")
                .message(ex.getMessage())
                .field("email")
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 회원가입 처리 예외 처리
    @ExceptionHandler(SignupProcessingException.class)
    public ResponseEntity<ErrorResponse> handleSignupProcessing(SignupProcessingException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code("SIGNUP_ERROR")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 유효성 검증 예외 처리 (@Valid 관련)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        // 첫 번째 필드 오류만 반환
        FieldError fieldError = fieldErrors.get(0);
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(fieldError.getDefaultMessage())
                .field(fieldError.getField())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code("SERVER_ERROR")
                .message("서버 오류가 발생했습니다: " + ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}