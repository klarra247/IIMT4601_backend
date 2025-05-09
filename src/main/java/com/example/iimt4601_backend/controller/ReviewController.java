package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.ReviewDto;
import com.example.iimt4601_backend.dto.ReviewListResponseDto;
import com.example.iimt4601_backend.dto.ReviewRequestDto;
import com.example.iimt4601_backend.dto.ReviewResponseDto;
import com.example.iimt4601_backend.security.UserDetailsImpl;
import com.example.iimt4601_backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/{productId}")
    public ResponseEntity<ReviewListResponseDto> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        ReviewListResponseDto reviews = reviewService.getProductReviews(productId, page, limit);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<ReviewListResponseDto> getMyReviews(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = userDetails.getUser().getId();
        ReviewListResponseDto reviews = reviewService.getUserReviews(userId, page, limit);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ReviewDto> addReview(
            @PathVariable Long productId,
            @RequestBody ReviewRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ) {
        Long userId = userDetails.getUser().getId();

        ReviewDto response = reviewService.addReview(productId, requestDto, userId);
        return ResponseEntity.ok(response);
    }


//    @PutMapping("/{id}")
//    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable Long id, @RequestBody ReviewRequestDto reviewDto) {
//        return ResponseEntity.ok(reviewService.updateReview(id, reviewDto));
//    }
//
//    //
//    @DeleteMapping("/{id}")
//    public ResponseEntity<HttpStatus> deleteReview(@PathVariable long id) {
//        reviewService.deleteReview(id);
//
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }

}
