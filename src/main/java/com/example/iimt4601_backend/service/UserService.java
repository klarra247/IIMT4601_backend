package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.SignupRequestDto;
import com.example.iimt4601_backend.enums.UserRoleEnum;
import com.example.iimt4601_backend.exception.DuplicateEmailException;
import com.example.iimt4601_backend.exception.DuplicateUsernameException;
import com.example.iimt4601_backend.exception.SignupProcessingException;
import com.example.iimt4601_backend.repository.UserRepository;
import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.*;
import com.example.iimt4601_backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequestDto requestDto) {
        String email = requestDto.getEmail();
        String userName = requestDto.getUserName();
        String name = requestDto.getName();
        String phoneNumber = requestDto.getPhoneNumber();
        String password = passwordEncoder.encode(requestDto.getPassword());
        String shippingAddress = requestDto.getShippingAddress();
        Boolean isSex = requestDto.getIsSex();
        LocalDate birthDate = LocalDate.of(
                requestDto.getBirthYear(),
                requestDto.getBirthMonth(),
                requestDto.getBirthDay()
        );

        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.getRole() != null) {
            role = requestDto.getRole();
        }
        // 아이디 중복 확인
        if (userRepository.existsByUserName(userName)) {
            throw new DuplicateUsernameException("이미 존재하는 아이디입니다.");
        }
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("이미 등록된 이메일입니다.");
        }

        try {
            User user = User.builder()
                    .email(email)
                    .userName(userName)
                    .password(password)
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .shippingAddress(shippingAddress)
                    .role(role)
                    .isSex(isSex)
                    .birthDate(birthDate)
                    .isActive(true)
                    .emailNotifications(true)
                    .marketingConsent(false)
                    .build();

            userRepository.save(user);
        } catch (Exception e) {
            throw new SignupProcessingException("회원가입 처리 중 오류가 발생했습니다: ", e);
        }
    }

    public UserResponseDto getCurrentUserInfo(User user) {
        if (user == null) {
            return null;
        }

        return convertToDto(user);
    }

    public UserResponseDto getUserProfile(User user) {
        return convertToDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUserName(username);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UserResponseDto updateUserProfile(User user, String newName, String newImageUrl) {
        user.setName(newName);
        userRepository.save(user);

        return convertToDto(user);
    }

    private UserResponseDto convertToDto(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .lastLoginDate(user.getLastLoginDate())
                .createdAt(user.getCreatedAt())
                .isActive(user.getIsActive())
                .role(user.getRole().toString())
                .build();
    }


}