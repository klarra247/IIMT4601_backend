package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class RecentOrderDto {
    private String id;           // 주문 번호
    private String customer;     // 고객 이름
    private BigDecimal amount;       // 주문 금액
    private String status;       // 주문 상태
    private String date;         // 주문 일자
}
