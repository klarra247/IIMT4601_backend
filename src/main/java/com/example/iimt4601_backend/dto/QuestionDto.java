package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private UserMinimalDto user;
    private String title;
    private String content;
    private Boolean isPrivate;
    private String createdAt;
    private String answer;
    private String answeredAt;
}

