package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.Product;
import com.example.iimt4601_backend.entity.Review;
import com.example.iimt4601_backend.entity.User;
import com.example.iimt4601_backend.exception.ResourceNotFoundException;
import com.example.iimt4601_backend.repository.ProductRepository;
import com.example.iimt4601_backend.repository.ReviewRepository;
import com.example.iimt4601_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    public ReviewListResponseDto getProductReviews(Long productId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);

        List<ReviewDto> reviewDtos = reviewPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ReviewListResponseDto(
                (int) reviewPage.getTotalElements(),
                page,
                limit,
                reviewDtos
        );
    }

    @Transactional
    public ReviewDto addReview(Long productId, ReviewRequestDto requestDto, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(requestDto.getRating());
        review.setTitle(requestDto.getTitle());
        review.setContent(requestDto.getContent());
        review.setImages(requestDto.getImages());

        reviewRepository.save(review);

        // Update product rating
        updateProductRating(product);

        return convertToDto(review);
    }

    public ReviewListResponseDto getUserReviews(Long userId, int page, int limit) {
        // 사용자 존재 여부 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Review> reviewPage = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<ReviewDto> reviewDtos = reviewPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ReviewListResponseDto(
                (int) reviewPage.getTotalElements(),
                page,
                limit,
                reviewDtos
        );
    }

    private ReviewDto convertToDto(Review review) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        UserMinimalDto userDto = new UserMinimalDto();
        userDto.setId(review.getUser().getId());
        userDto.setUserName(review.getUser().getUserName());
        userDto.setEmail(review.getUser().getEmail());
        userDto.setPhoneNumber(review.getUser().getPhoneNumber());

        return new ReviewDto(
                review.getId(),
                review.getRating(),
                review.getTitle(),
                review.getContent(),
                review.getImages(),
                review.getCreatedAt().format(formatter),
                userDto
                );
    }

    private void updateProductRating(Product product) {
        int count = reviewRepository.countByProductId(product.getId());
        if (count > 0) {
            // Calculate the average rating for all reviews of this product
            // For simplicity, we're not implementing this calculation here
            // Assume average rating is calculated elsewhere
            product.setReviewCount(count);
            productRepository.save(product);
        }
    }
    //    //제품 정보 수정
//    @Transactional
//    public ReviewResponseDto updateReview(Long id, ReviewRequestDto reviewDto) {
//        Review existingReview = reviewRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("review not found with id: " + id));
//
//        User user = userRepository.findById(reviewDto.getUserId())
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + reviewDto.getUserId()));
//        Product product = productRepository.findById(reviewDto.getProductId())
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + reviewDto.getProductId()));
//
//        reviewMapper.updateReviewFromDto(reviewDto, existingReview, user, product);
//        Review updatedReview = reviewRepository.save(existingReview);
//
//        return reviewMapper.toDto(updatedReview);
//    }
//
//    // 리뷰 삭제
//    public void deleteReview(Long id) {
//        Review review = reviewRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
//        reviewRepository.delete(review);
//    }

}
