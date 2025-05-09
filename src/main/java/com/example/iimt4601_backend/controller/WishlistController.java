package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.WishlistResponseDto;
import com.example.iimt4601_backend.security.UserDetailsImpl;
import com.example.iimt4601_backend.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @Autowired
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponseDto>> getWishlist(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<WishlistResponseDto> wishlist = wishlistService.getWishlist(userDetails.getUser().getId());
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<String> addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String response = wishlistService.addToWishlist(productId, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove/{productId}")
    public Void removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        wishlistService.removeFromWishlist(userDetails.getUser().getId(), productId);
        return null;
    }
}