package com.example.iimt4601_backend.dto;

import com.example.iimt4601_backend.enums.UserRoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

// 회원가입 Dto
@Getter
public class SignupRequestDto {
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    // password 복잡도 설정
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*[\\d@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "비밀번호는 8-16자의 영문, 숫자, 특수문자 중 2가지 이상 조합이어야 합니다."
    )
    private String password;

    @Pattern(regexp = "^[a-zA-Z0-9]{4,16}$", message = "아이디는 4-16자의 영문자와 숫자만 허용됩니다.")
    private String userName;

    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10-11자리 숫자여야 합니다.")
    private String phoneNumber;

    private String name;

    private String shippingAddress;

    private Boolean isSex;

    private Integer birthYear;

    private Integer birthMonth;

    private Integer birthDay;

    private UserRoleEnum role;

}