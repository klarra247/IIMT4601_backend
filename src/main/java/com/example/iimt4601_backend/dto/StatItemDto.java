package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StatItemDto {
    private String title;
    private String value;
    private String change;
    private Boolean isUp;
}
