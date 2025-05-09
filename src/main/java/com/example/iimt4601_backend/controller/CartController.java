package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.CartRequestDto;
import com.example.iimt4601_backend.dto.CartResponseDto;
import com.example.iimt4601_backend.entity.Cart;
import com.example.iimt4601_backend.entity.CartItem;
import com.example.iimt4601_backend.entity.User;
import com.example.iimt4601_backend.repository.CartRepository;
import com.example.iimt4601_backend.security.UserDetailsImpl;
import com.example.iimt4601_backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponseDto> addCart(@RequestBody CartRequestDto cartRequestDto,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(cartService.addToCart(cartRequestDto, userId));
    }

}
