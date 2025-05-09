package com.example.iimt4601_backend.repository;

import com.example.iimt4601_backend.entity.Order;
import com.example.iimt4601_backend.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    // 기존 메서드들
    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndOrderDateBetween(OrderStatusEnum status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    BigDecimal sumTotalAmountByOrderDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate >= :date")
    BigDecimal calculateRevenueAfterDate(@Param("date") LocalDateTime date);

    @Query("SELECT CAST(o.orderDate AS date) as date, SUM(o.totalAmount) as sales " +
            "FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 GROUP BY CAST(o.orderDate AS date) " +
            "ORDER BY date")
    List<Object[]> findDailySalesBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT FUNCTION('YEARWEEK', o.orderDate) as week, SUM(o.totalAmount) as sales " +
            "FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 GROUP BY FUNCTION('YEARWEEK', o.orderDate) " +
            "ORDER BY week")
    List<Object[]> findWeeklySalesBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') as month, SUM(o.totalAmount) as sales " +
            "FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') " +
            "ORDER BY month")
    List<Object[]> findMonthlySalesBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT YEAR(o.orderDate) as year, SUM(o.totalAmount) as sales " +
            "FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 GROUP BY YEAR(o.orderDate) " +
            "ORDER BY year")
    List<Object[]> findYearlySalesBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT CAST(o.orderDate AS date) as date, SUM(o.totalAmount) as sales, COUNT(o) as count " +
            "FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 GROUP BY CAST(o.orderDate AS date) " +
            "ORDER BY date")
    List<Object[]> findDailyOrderStatsBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT FUNCTION('YEARWEEK', o.orderDate) as week, SUM(o.totalAmount) as sales, COUNT(o) as count " +
            "FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 GROUP BY FUNCTION('YEARWEEK', o.orderDate) " +
            "ORDER BY week")
    List<Object[]> findWeeklyOrderStatsBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') as month, SUM(o.totalAmount) as sales, COUNT(o) as count " +
            "FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') " +
            "ORDER BY month")
    List<Object[]> findMonthlyOrderStatsBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT YEAR(o.orderDate) as year, SUM(o.totalAmount) as sales, COUNT(o) as count " +
            "FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 GROUP BY YEAR(o.orderDate) " +
            "ORDER BY year")
    List<Object[]> findYearlyOrderStatsBetween(LocalDateTime start, LocalDateTime end);

    // 추가 메서드들

    // 사용자별 주문 수 조회
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    // 사용자별 총 주문 금액 조회
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.id = :userId")
    BigDecimal sumTotalAmountByUserId(@Param("userId") Long userId);

    // 사용자별 최근 주문 조회 (주문일 기준 내림차순)
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    // EntityGraph를 사용한 findAll 오버라이드 (N+1 쿼리 문제 방지)
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    @Override
    List<Order> findAll();

    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    @Override
    List<Order> findAll(Specification<Order> spec);

    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    // 주문 상태별 개수 조회
    long countByStatus(OrderStatusEnum status);

    // 전체 주문 총액 계산
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal calculateTotalRevenue();


    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 특정 기간 동안의 총 매출 조회
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 일별 주문 수 조회 (특정 기간)
    @Query("SELECT DATE(o.createdAt) as date, COUNT(o) as count FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(o.createdAt) ORDER BY date")
    List<Object[]> getDailyOrderCounts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 일별 매출 조회 (특정 기간)
    @Query("SELECT DATE(o.createdAt) as date, SUM(o.totalAmount) as total FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(o.createdAt) ORDER BY date")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}