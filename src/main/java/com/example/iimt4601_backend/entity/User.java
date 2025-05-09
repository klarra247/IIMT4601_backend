package com.example.iimt4601_backend.entity;

import com.example.iimt4601_backend.enums.UserRoleEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Setter
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    @Column(name = "is_sex")
    private Boolean isSex;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone_number")
    private String phoneNumber;

    // 계정 활성화 상태
    @Column(name = "is_account_active")
    private Boolean isActive = true;

    // 마지막 로그인 날짜
    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    // 계정 생성 시간
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 계정 정보 수정 시간
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 배송 주소 관련 (기본 주소 하나만 저장하는 경우)
    @Column(name = "shipping_address")
    private String shippingAddress;

    // 장바구니
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Cart> carts = new ArrayList<>();

    // 알림 설정
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "marketing_consent")
    private Boolean marketingConsent = false;

    @Column(name = "order_count")
    private Integer orderCount = 0; // 총 주문 수

    @Column(name = "total_spent")
    private BigDecimal totalSpent = BigDecimal.ZERO; // 총 지출액

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate; // 마지막 주문 일자

    @Column(name = "average_order_value")
    private BigDecimal averageOrderValue = BigDecimal.ZERO; // 평균 주문 금액

}