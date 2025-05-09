package com.example.iimt4601_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cakes")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cake_name", nullable = false)
    private String productName;

    @Column(name = "cake_price", nullable = false)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 케이크 코드
    @Column(name = "sku", unique = true)
    private String sku;

    // 케이크 등록일
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 케이크 수정일
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 카테고리 관계 추가
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    // 검색 및 필터링용 태그
    @ElementCollection
    @CollectionTable(name = "cake_tags", joinColumns = @JoinColumn(name = "cake_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // 옵션 (크기, 맛 등)
    @ElementCollection
    @CollectionTable(name = "cake_options", joinColumns = @JoinColumn(name = "cake_id"))
    @Column(name = "option_value")
    private Set<String> options = new HashSet<>();

    // 영양 정보 및 알레르기 정보
    @Column(name = "nutrition_info", columnDefinition = "TEXT")
    private String nutritionInfo; // JSON 형태로 저장

    // 케이크 크기(호수)
    @Column(name = "size")
    private String size;  // 1호, 2호, 3호 등

    // 주요 재료
    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients;

    // 판매 가능 여부
    @Column(name = "is_available")
    private Boolean isAvailable = true;

    // 할인율
    @Column(name = "discount_percentage")
    private Double discountPercentage;

    // 정가
    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cake_images", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

//    // 연관 케이크 ID 배열
//    @ElementCollection
//    @CollectionTable(name = "related_cakes", joinColumns = @JoinColumn(name = "cake_id"))
//    @Column(name = "related_cake_id")
//    private Set<Long> relatedCakes = new HashSet<>();

    // 신상품 여부
    @Column(name = "is_new")
    private Boolean isNew = false;

    // 커스터마이징 가능 여부
    @Column(name = "is_customizable")
    private Boolean isCustomizable = false;

    @Column(name = "sold_count")
    private Integer soldCount = 0; // 판매량

    @Column(name = "view_count")
    private Integer viewCount = 0; // 조회수

    @Column(name = "recommended")
    private Boolean recommended = false; // 추천 상품 여부

    @Column(name = "search_keywords", columnDefinition = "TEXT")
    private String searchKeywords; // 검색 키워드
}