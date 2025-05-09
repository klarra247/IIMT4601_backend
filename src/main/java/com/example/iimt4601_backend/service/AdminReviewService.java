package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.Review;
import com.example.iimt4601_backend.entity.User;
import com.example.iimt4601_backend.exception.ResourceNotFoundException;
import com.example.iimt4601_backend.repository.OrderRepository;
import com.example.iimt4601_backend.repository.ReviewRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public AdminReviewListResponseDto getReviews(int page, int size, Long productId, Boolean visible, String sort) {
        // 정렬 처리
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortOrder = Sort.by(direction, sortField);

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 동적 쿼리 구성
        Specification<Review> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productId != null) {
                predicates.add(criteriaBuilder.equal(root.get("product").get("id"), productId));
            }

            if (visible != null) {
                predicates.add(criteriaBuilder.equal(root.get("visible"), visible));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Review> reviewsPage = reviewRepository.findAll(spec, pageable);

        List<ReviewListItemDto> reviewDtos = reviewsPage.getContent().stream()
                .map(this::convertToListItem)
                .collect(Collectors.toList());

        return AdminReviewListResponseDto.builder()
                .content(reviewDtos)
                .page(reviewsPage.getNumber())
                .size(reviewsPage.getSize())
                .totalElements(reviewsPage.getTotalElements())
                .totalPages(reviewsPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public ReviewDetailDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        return convertToDetailDto(review);
    }

    @Transactional
    public ReviewDetailDto updateReviewVisibility(Long id, ReviewVisibilityDto visibilityDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        review.setVisible(visibilityDto.getVisible());
        Review updatedReview = reviewRepository.save(review);

        return convertToDetailDto(updatedReview);
    }

    private ReviewListItemDto convertToListItem(Review review) {
        return ReviewListItemDto.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getProductName())
                .userId(review.getUser().getId())
                .username(review.getUser().getUserName())
                .rating(review.getRating())
                .title(review.getTitle())
                .visible(review.getVisible())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private ReviewDetailDto convertToDetailDto(Review review) {
        return ReviewDetailDto.builder()
                .id(review.getId())
                .product(ProductDto.builder()
                        .id(review.getProduct().getId())
                        .title(review.getProduct().getProductName())
                        .build())
                .user(mapToUserDto(review.getUser()))
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .images(review.getImages())
                .visible(review.getVisible())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginDate(user.getLastLoginDate());

        // 주문 정보 추가
        Long orderCount = orderRepository.countByUserId(user.getId());
        dto.setOrderCount(orderCount != null ? orderCount.intValue() : 0);

        BigDecimal totalSpent = orderRepository.sumTotalAmountByUserId(user.getId());
        dto.setTotalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO);

        return dto;
    }

}