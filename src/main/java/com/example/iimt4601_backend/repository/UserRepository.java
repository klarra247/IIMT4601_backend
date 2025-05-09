package com.example.iimt4601_backend.repository;

import com.example.iimt4601_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

//    Optional<User> findByAppleId(String appleId);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    Page<User> findByUserNameContaining(String query, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countUsersRegisteredBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 일별 가입자 수 조회
    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count FROM User u " +
            "WHERE u.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(u.createdAt) ORDER BY date")
    List<Object[]> getDailyRegistrations(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u WHERE u.lastLoginDate < :date")
    List<User> findInactiveUsersSince(@Param("date") LocalDateTime date);

    long countByIsActiveTrue();

    long countByCreatedAtAfter(LocalDateTime date);

    // EntityGraph를 적용한 findAll 메서드 추가
    @EntityGraph(attributePaths = {"shippingAddress"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"shippingAddress"})
    @Override
    List<User> findAll();
}