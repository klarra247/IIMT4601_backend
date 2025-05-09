package com.example.iimt4601_backend.dto;

import com.example.iimt4601_backend.dto.QuestionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionListResponseDto {
    private Integer total;
    private Integer page;
    private Integer limit;
    private List<QuestionDto> questions;
}
