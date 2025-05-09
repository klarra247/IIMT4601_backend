package com.example.iimt4601_backend.repository;

import com.example.iimt4601_backend.entity.Product;
import com.example.iimt4601_backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    Page<Review> findByProductId(Long productId, Pageable pageable);
    int countByProductId(Long productId);

    Page<Review> findByVisible(Boolean visible, Pageable pageable);
    Page<Review> findByProductIdAndVisible(Long productId, Boolean visible, Pageable pageable);

    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r")
    Double findAverageRating();

//    // 기존 메서드들
//    List<Review> findByProduct(Product product);
//
//    List<Review> findByProductId(Long productId);
//
//    List<Review> findByProductIdAndIsVisibleTrue(Long productId);
//
//    @Query("SELECT AVG(r.stars) FROM Review r WHERE r.product.id = :productId AND r.isVisible = true")
//    Double findAverageRatingByProductId(@Param("productId") Long productId);
//
//    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isVisible = true")
//    Integer countVisibleReviewsByProductId(@Param("productId") Long productId);
//
//    @Query("SELECT r FROM Review r WHERE r.createdAt >= :date")
//    List<Review> findRecentReviewsSince(@Param("date") LocalDateTime date, Pageable pageable);
//
//    long countByIsVisibleTrue();
//
//    long countByProductIdAndStarsAndIsVisibleTrue(Long productId, Integer rating);
//
//    // EntityGraph를 적용한 findAll 메서드 추가
//    @EntityGraph(attributePaths = {"product", "user", "images"})
//    @Override
//    Page<Review> findAll(Specification<Review> spec, Pageable pageable);
//
//    @EntityGraph(attributePaths = {"product", "user", "images"})
//    @Override
//    List<Review> findAll();
}