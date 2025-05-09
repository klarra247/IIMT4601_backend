
package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.CustomCakeRequestDto;
import com.example.iimt4601_backend.dto.CustomCakeResponseDto;
import com.example.iimt4601_backend.entity.CustomDesign;
import com.example.iimt4601_backend.repository.CustomDesignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomCakeService {

    @Autowired
    private CustomDesignRepository customDesignRepository;

    public CustomCakeResponseDto initializeCustomization(CustomCakeRequestDto requestDto, Long userId) {
        String customizationId = "custom" + UUID.randomUUID().toString().substring(0, 8);

        CustomDesign customDesign = new CustomDesign();
        customDesign.setDesignId(requestDto.getDesignId());
        customDesign.setDesignName(requestDto.getDesignName());
        customDesign.setBasePrice(requestDto.getBasePrice());
        customDesign.setUserId(userId);
        customDesign.setCustomizationId(customizationId);

        customDesignRepository.save(customDesign);

        String redirectUrl = "/customize/" + customizationId;

        return new CustomCakeResponseDto(true, customizationId, redirectUrl);
    }
}