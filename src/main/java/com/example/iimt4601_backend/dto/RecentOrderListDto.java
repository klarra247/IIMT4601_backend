package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecentOrderListDto {
    private List<RecentOrderDto> orders;
}

