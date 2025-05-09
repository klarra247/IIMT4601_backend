
package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.QuestionDto;
import com.example.iimt4601_backend.dto.QuestionListResponseDto;
import com.example.iimt4601_backend.dto.QuestionRequestDto;
import com.example.iimt4601_backend.dto.QuestionResponseDto;
import com.example.iimt4601_backend.security.UserDetailsImpl;
import com.example.iimt4601_backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/{productId}")
    public ResponseEntity<QuestionListResponseDto> getProductQuestions(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        QuestionListResponseDto questions = questionService.getProductQuestions(productId, page, limit);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<QuestionDto> addQuestion(
            @PathVariable Long productId,
            @RequestBody QuestionRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ) {
        Long userId = userDetails.getUser().getId();

        QuestionDto response = questionService.addQuestion(productId, requestDto, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-questions")
    public ResponseEntity<QuestionListResponseDto> getMyQuestions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = userDetails.getUser().getId();
        QuestionListResponseDto questions = questionService.getUserQuestions(userId, page, limit);
        return ResponseEntity.ok(questions);
    }
}