package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.Order;
import com.example.iimt4601_backend.enums.OrderStatusEnum;
import com.example.iimt4601_backend.repository.OrderRepository;
import com.example.iimt4601_backend.repository.ProductRepository;
import com.example.iimt4601_backend.repository.ReviewRepository;
import com.example.iimt4601_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public DashboardStatsDto getDashboardStats(String period) {
        log.info("대시보드 통계 조회 시작: period={}", period);

        try {
            // 기간 설정
            LocalDateTime endDateTime = LocalDateTime.now();
            LocalDateTime startDateTime;
            LocalDateTime previousStartDateTime;
            LocalDateTime previousEndDateTime;
            // 30일 전 날짜 (기존 방식 호환용)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            switch (period) {
                case "daily":
                    startDateTime = LocalDate.now().atStartOfDay();
                    previousStartDateTime = LocalDate.now().minusDays(1).atStartOfDay();
                    previousEndDateTime = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);
                    break;
                case "weekly":
                    startDateTime = LocalDate.now().minusDays(6).atStartOfDay();
                    previousStartDateTime = LocalDate.now().minusDays(13).atStartOfDay();
                    previousEndDateTime = LocalDate.now().minusDays(7).atTime(LocalTime.MAX);
                    break;
                case "yearly":
                    startDateTime = LocalDate.now().minusYears(1).withDayOfYear(1).atStartOfDay();
                    previousStartDateTime = LocalDate.now().minusYears(2).withDayOfYear(1).atStartOfDay();
                    previousEndDateTime = LocalDate.now().minusYears(1).withDayOfYear(1).minusDays(1).atTime(LocalTime.MAX);
                    break;
                case "monthly":
                default:
                    startDateTime = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                    previousStartDateTime = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
                    previousEndDateTime = LocalDate.now().withDayOfMonth(1).minusDays(1).atTime(LocalTime.MAX);
                    break;
            }

            // 주문 통계
            Long currentOrders = orderRepository.countOrdersBetween(startDateTime, endDateTime);
            if (currentOrders == null) currentOrders = 0L;
            Long previousOrders = orderRepository.countOrdersBetween(previousStartDateTime, previousEndDateTime);
            if (previousOrders == null) previousOrders = 0L;
            Double orderChangePercentage = calculateChangePercentage(currentOrders, previousOrders);

            // 매출 통계
            Double currentRevenue = orderRepository.getTotalRevenueBetween(startDateTime, endDateTime);
            if (currentRevenue == null) currentRevenue = 0.0;
            Double previousRevenue = orderRepository.getTotalRevenueBetween(previousStartDateTime, previousEndDateTime);
            if (previousRevenue == null) previousRevenue = 0.0;
            Double revenueChangePercentage = calculateChangePercentage(currentRevenue, previousRevenue);

            // 제품 통계
            Long currentProducts = productRepository.countProductsAddedBetween(startDateTime, endDateTime);
            if (currentProducts == null) currentProducts = 0L;
            Long previousProducts = productRepository.countProductsAddedBetween(previousStartDateTime, previousEndDateTime);
            if (previousProducts == null) previousProducts = 0L;
            Double productChangePercentage = calculateChangePercentage(currentProducts, previousProducts);

            // 사용자 통계
            Long currentUsers = userRepository.countUsersRegisteredBetween(startDateTime, endDateTime);
            if (currentUsers == null) currentUsers = 0L;
            Long previousUsers = userRepository.countUsersRegisteredBetween(previousStartDateTime, previousEndDateTime);
            if (previousUsers == null) previousUsers = 0L;
            Double userChangePercentage = calculateChangePercentage(currentUsers, previousUsers);

            // 차트 데이터
            List<ChartDataDto> dailyOrdersChart = getDailyOrdersChart(startDateTime, endDateTime);
            List<ChartDataDto> dailyRevenueChart = getDailyRevenueChart(startDateTime, endDateTime);
            List<ChartDataDto> dailyUsersChart = getDailyUsersChart(startDateTime, endDateTime);

            // 인기 제품
            List<TopProductDto> topProducts = getTopProducts(startDateTime, endDateTime, 5);

            // 기존 방식의 데이터도 수집 (DTO 호환성 유지)
            long totalUsers = userRepository.count();
            long newUsers = userRepository.countByCreatedAtAfter(thirtyDaysAgo);

            long totalProducts = productRepository.count();
            long availableProducts = productRepository.countByIsAvailableTrue();

            long totalOrders = orderRepository.count();
            long pendingOrders = orderRepository.countByStatus(OrderStatusEnum.PENDING);

            BigDecimal totalRevenueAmount = orderRepository.calculateTotalRevenue();
            BigDecimal monthlyRevenueAmount = orderRepository.calculateRevenueAfterDate(thirtyDaysAgo);

            long totalReviews = reviewRepository.count();
            Double avgRating = reviewRepository.findAverageRating();
            if (avgRating == null) avgRating = 0.0;

            // 주문 상태별 개수
            Map<String, Long> orderStatusCounts = new HashMap<>();
            for (OrderStatusEnum status : OrderStatusEnum.values()) {
                long count = orderRepository.countByStatus(status);
                orderStatusCounts.put(status.name(), count);
            }

            // 인기 제품 카테고리
            Map<String, Integer> popularProducts = productRepository.findPopularProducts(5);

            // 최종 DTO 생성
            DashboardStatsDto result = DashboardStatsDto.builder()
                    // 새로운 방식의 데이터
                    .orderStats(StatCardDto.builder()
                            .label("Orders")
                            .currentValue(currentOrders)
                            .previousValue(previousOrders)
                            .changePercentage(orderChangePercentage)
                            .period(period)
                            .build())
                    .revenueStats(StatCardDto.builder()
                            .label("Revenue")
                            .currentValue(currentRevenue)
                            .previousValue(previousRevenue)
                            .changePercentage(revenueChangePercentage)
                            .period(period)
                            .build())
                    .productStats(StatCardDto.builder()
                            .label("New Products")
                            .currentValue(currentProducts)
                            .previousValue(previousProducts)
                            .changePercentage(productChangePercentage)
                            .period(period)
                            .build())
                    .userStats(StatCardDto.builder()
                            .label("New Users")
                            .currentValue(currentUsers)
                            .previousValue(previousUsers)
                            .changePercentage(userChangePercentage)
                            .period(period)
                            .build())
                    .dailyOrdersChart(dailyOrdersChart)
                    .dailyRevenueChart(dailyRevenueChart)
                    .dailyUsersChart(dailyUsersChart)
                    .topProducts(topProducts)
                    // 기존 방식의 데이터도 포함 (DTO 호환성 유지)
                    .totalUsers(totalUsers)
                    .newUsers(newUsers)
                    .totalProducts(totalProducts)
                    .availableProducts(availableProducts)
                    .totalOrders(totalOrders)
                    .pendingOrders(pendingOrders)
                    .totalRevenue(totalRevenueAmount)
                    .monthlyRevenue(monthlyRevenueAmount)
                    .totalReviews(totalReviews)
                    .averageRating(avgRating)
                    .orderStatusCounts(orderStatusCounts)
                    .popularProducts(popularProducts)
                    .build();

            log.info("대시보드 통계 조회 완료");
            return result;
        } catch (Exception e) {
            log.error("대시보드 통계 처리 중 오류 발생", e);

            // 오류 발생 시 기본 정보만 포함한 응답 생성
            return DashboardStatsDto.builder()
                    .error("서버 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }

    private Double calculateChangePercentage(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            return current.doubleValue() > 0 ? 100.0 : 0.0;
        }

        double change = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
        BigDecimal bd = new BigDecimal(change).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private List<ChartDataDto> getDailyOrdersChart(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            List<Object[]> dailyData = orderRepository.getDailyOrderCounts(startDateTime, endDateTime);
            return dailyData.stream()
                    .map(data -> {
                        LocalDate date;
                        if (data[0] instanceof java.sql.Date) {
                            date = ((java.sql.Date) data[0]).toLocalDate();
                        } else if (data[0] instanceof java.time.LocalDate) {
                            date = (LocalDate) data[0];
                        } else if (data[0] instanceof Date) {
                            date = ((Date) data[0]).toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                        } else if (data[0] instanceof String) {
                            date = LocalDate.parse((String) data[0]);
                        } else {
                            // 대체 방법으로 현재 날짜 사용
                            date = LocalDate.now();
                            log.warn("날짜 변환 불가: " + data[0] + " (타입: " + (data[0] != null ? data[0].getClass().getName() : "null") + ")");
                        }

                        Number value = (data[1] != null) ? (Number) data[1] : 0;

                        return ChartDataDto.builder()
                                .date(date)
                                .value(value)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("일일 주문 차트 데이터 가져오기 오류", e);
            return new ArrayList<>(); // 오류 시 빈 리스트 반환
        }
    }

    private List<ChartDataDto> getDailyRevenueChart(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            List<Object[]> dailyData = orderRepository.getDailyRevenue(startDateTime, endDateTime);
            return dailyData.stream()
                    .map(data -> {
                        LocalDate date;
                        if (data[0] instanceof java.sql.Date) {
                            date = ((java.sql.Date) data[0]).toLocalDate();
                        } else if (data[0] instanceof java.time.LocalDate) {
                            date = (LocalDate) data[0];
                        } else if (data[0] instanceof Date) {
                            date = ((Date) data[0]).toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                        } else if (data[0] instanceof String) {
                            date = LocalDate.parse((String) data[0]);
                        } else {
                            date = LocalDate.now();
                            log.warn("날짜 변환 불가: " + data[0] + " (타입: " + (data[0] != null ? data[0].getClass().getName() : "null") + ")");
                        }

                        Number value = (data[1] != null) ? (Number) data[1] : 0;

                        return ChartDataDto.builder()
                                .date(date)
                                .value(value)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("일일 매출 차트 데이터 가져오기 오류", e);
            return new ArrayList<>();
        }
    }

    private List<ChartDataDto> getDailyUsersChart(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            List<Object[]> dailyData = userRepository.getDailyRegistrations(startDateTime, endDateTime);
            return dailyData.stream()
                    .map(data -> {
                        LocalDate date;
                        if (data[0] instanceof java.sql.Date) {
                            date = ((java.sql.Date) data[0]).toLocalDate();
                        } else if (data[0] instanceof java.time.LocalDate) {
                            date = (LocalDate) data[0];
                        } else if (data[0] instanceof Date) {
                            date = ((Date) data[0]).toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                        } else if (data[0] instanceof String) {
                            date = LocalDate.parse((String) data[0]);
                        } else {
                            date = LocalDate.now();
                            log.warn("날짜 변환 불가: " + data[0] + " (타입: " + (data[0] != null ? data[0].getClass().getName() : "null") + ")");
                        }

                        Number value = (data[1] != null) ? (Number) data[1] : 0;

                        return ChartDataDto.builder()
                                .date(date)
                                .value(value)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("일일 사용자 차트 데이터 가져오기 오류", e);
            return new ArrayList<>();
        }
    }

    private List<TopProductDto> getTopProducts(LocalDateTime startDateTime, LocalDateTime endDateTime, int limit) {
        try {
            List<Object[]> products = productRepository.findBestSellingProducts(startDateTime, endDateTime);
            return products.stream()
                    .limit(limit)
                    .map(product -> {
                        try {
                            Long id = product[0] != null ? ((Number) product[0]).longValue() : 0L;
                            String name = product[1] != null ? (String) product[1] : "Unknown";
                            Long totalSold = product[2] != null ? ((Number) product[2]).longValue() : 0L;

                            return TopProductDto.builder()
                                    .id(id)
                                    .name(name)
                                    .totalSold(totalSold)
                                    .build();
                        } catch (Exception e) {
                            log.error("Top 제품 변환 중 오류", e);
                            return TopProductDto.builder()
                                    .id(0L)
                                    .name("Error converting product")
                                    .totalSold(0L)
                                    .build();
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("인기 제품 가져오기 오류", e);
            return new ArrayList<>(); // 오류 시 빈 리스트 반환
        }
    }

    public RecentOrderListDto getRecentOrders() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "orderDate"));
        List<Order> recentOrders = orderRepository.findAll(pageable).getContent();

        List<RecentOrderDto> orderDtos = recentOrders.stream()
                .map(order -> new RecentOrderDto(
                        order.getOrderNumber(),
                        order.getUser().getUserName(),
                        order.getTotalAmount(),
                        order.getStatus().getDisplayName(),
                        formatDate(order.getOrderDate())))
                .collect(Collectors.toList());

        RecentOrderListDto result = new RecentOrderListDto();
        result.setOrders(orderDtos);
        return result;
    }

    

    
    public SalesChartDto getSalesChartData(String period, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> salesData;

        switch (period.toLowerCase()) {
            case "daily":
                salesData = orderRepository.findDailySalesBetween(start, end);
                break;
            case "weekly":
                salesData = orderRepository.findWeeklySalesBetween(start, end);
                break;
            case "monthly":
                salesData = orderRepository.findMonthlySalesBetween(start, end);
                break;
            case "yearly":
                salesData = orderRepository.findYearlySalesBetween(start, end);
                break;
            default:
                salesData = orderRepository.findDailySalesBetween(start, end);
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (Object[] row : salesData) {
            labels.add(row[0].toString()); // 날짜/주/월/년 라벨
            data.add((BigDecimal) row[1]); // 매출액
        }

        SalesChartDto result = new SalesChartDto();
        result.setLabels(labels);
        result.setData(data);

        return result;
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        return formatter.format(amount) + "원";
    }

    private String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage);
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }
}