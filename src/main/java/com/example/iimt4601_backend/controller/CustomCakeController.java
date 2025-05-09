
package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.CustomCakeRequestDto;
import com.example.iimt4601_backend.dto.CustomCakeResponseDto;
import com.example.iimt4601_backend.security.UserDetailsImpl;
import com.example.iimt4601_backend.service.CustomCakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customization")
public class CustomCakeController {

    @Autowired
    private CustomCakeService customCakeService;

    @PostMapping("/initialize")
    public ResponseEntity<CustomCakeResponseDto> initializeCustomization(
            @RequestBody CustomCakeRequestDto requestDto,
    @AuthenticationPrincipal UserDetailsImpl userDetails) {

        CustomCakeResponseDto response = customCakeService.initializeCustomization(requestDto, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }
}