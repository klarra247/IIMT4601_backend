package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMinimalDto {
    private Long id;
    private String userName;
    private String email;
    private String phoneNumber;
}
