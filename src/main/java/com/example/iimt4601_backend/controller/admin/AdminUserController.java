package com.example.iimt4601_backend.controller.admin;

import com.example.iimt4601_backend.dto.UserDetailDto;
import com.example.iimt4601_backend.dto.UserListResponseDto;
import com.example.iimt4601_backend.dto.UserStatusUpdateDto;
import com.example.iimt4601_backend.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService userService;

    @GetMapping
    public ResponseEntity<UserListResponseDto> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String search) {
        UserListResponseDto users = userService.getUsers(page, size, status, sort, search);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailDto> getUserById(@PathVariable Long id) {
        UserDetailDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDetailDto> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateDto statusDto) {
        UserDetailDto updatedUser = userService.updateUserStatus(id, statusDto);
        return ResponseEntity.ok(updatedUser);
    }
}