package com.example.iimt4601_backend.repository;

import com.example.iimt4601_backend.dto.PopularProductDto;
import com.example.iimt4601_backend.entity.Category;
import com.example.iimt4601_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.relational.core.sql.In;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 수정: String 대신 Category 객체를 받도록 변경
    Page<Product> findByCategory(Category category, Pageable pageable);

    Long countByCategoryId(Long categoryId);

    // 새 상품 조회
    List<Product> findByIsNewTrueOrderByCreatedAtDesc(Pageable pageable);

    // 조회수 상위 상품 조회
    List<Product> findTop3ByOrderByViewCountDesc();

    // 카테고리 필터 없이 검색 (관련성 기준 정렬)
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.category c " +
            "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY " +
            "CASE WHEN LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) THEN 0 " +
            "     WHEN LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1 " +
            "     WHEN LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :query, '%')) THEN 2 " +
            "     ELSE 3 END")
    List<Product> findBySearchQueryWithRelevance(@Param("query") String query, Pageable pageable);

    // 카테고리 필터 없이 검색 (외부 정렬 사용)
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.category c " +
            "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> findBySearchQuery(@Param("query") String query, Pageable pageable);

    // 카테고리 ID 필터와 함께 검색 (관련성 기준 정렬) - 수정된 버전
    @Query("SELECT p FROM Product p " +
            "WHERE p.category.id = :categoryId " +
            "AND (" +
            "   LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :query, '%'))" +
            "   OR EXISTS (SELECT 1 FROM Category c WHERE c.id = p.category.id AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))" +
            ") " +
            "ORDER BY " +
            "CASE WHEN LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) THEN 0 " +
            "     WHEN EXISTS (SELECT 1 FROM Category c WHERE c.id = p.category.id AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))) THEN 1 " +
            "     WHEN LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :query, '%')) THEN 2 " +
            "     ELSE 3 END")
    List<Product> findBySearchQueryAndCategoryIdWithRelevance(
            @Param("query") String query,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    // 카테고리 ID 필터와 함께 검색 (외부 정렬 사용) - 수정된 버전
    @Query("SELECT p FROM Product p " +
            "WHERE p.category.id = :categoryId " +
            "AND (" +
            "   LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(p.searchKeywords) LIKE LOWER(CONCAT('%', :query, '%'))" +
            "   OR EXISTS (SELECT 1 FROM Category c WHERE c.id = p.category.id AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))" +
            ")")
    List<Product> findBySearchQueryAndCategoryId(
            @Param("query") String query,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query("SELECT new com.example.iimt4601_backend.dto.PopularProductDto(p.productName, p.viewCount) " +
            "FROM Product p WHERE p.isAvailable = true ORDER BY p.viewCount DESC")
    List<PopularProductDto> findPopularProductsDto(Pageable pageable);

    default Map<String, Integer> findPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<PopularProductDto> products = findPopularProductsDto(pageable);

        Map<String, Integer> result = new HashMap<>();
        for (PopularProductDto product : products) {
            result.put(product.getName(), product.getViewCount());
        }
        return result;
    }
    // 특정 기간에 추가된 제품 수 조회
    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Long countProductsAddedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 가장 많이 팔린 제품 목록 조회
    @Query(value = "SELECT p.id, p.name, SUM(oi.quantity) as total_sold " +
            "FROM products p " +
            "JOIN order_items oi ON p.id = oi.product_id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY p.id, p.name " +
            "ORDER BY total_sold DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findBestSellingProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit);



    @Query(value = "SELECT p FROM Product p WHERE p.id != :productId ORDER BY FUNCTION('RAND') LIMIT 4", nativeQuery = true)
    List<Product> findRelatedProducts(@Param("productId") Long productId);


    // 기본 findById를 오버라이드하여 필요한 컬렉션을 함께 로딩
    @EntityGraph(attributePaths = {"tags", "options", "images"})
    @Override
    Optional<Product> findById(Long id);

    // 모든 상품 조회 시 모든 컬렉션을 함께 로딩
    @EntityGraph(attributePaths = {"tags", "options", "images"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"tags", "options", "images"})
    @Override
    List<Product> findAll();

    // 페이지네이션을 위한 메서드
    @EntityGraph(attributePaths = {"tags", "options", "images"})
    @Override
    Page<Product> findAll(Pageable pageable);

    // 태그별 조회 (태그 컬렉션은 조인 쿼리가 필요)
    @Query("SELECT DISTINCT p FROM Product p JOIN p.tags t WHERE t = :tag")
    @EntityGraph(attributePaths = {"tags", "options", "images"})
    List<Product> findByTagsContaining(@Param("tag") String tag);

    // 상품 이름으로 검색
    @EntityGraph(attributePaths = {"tags", "options", "images"})
    List<Product> findByProductNameContainingIgnoreCase(String productName);

    long countByIsAvailableTrue();

}