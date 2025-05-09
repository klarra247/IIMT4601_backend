package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.Order;
import com.example.iimt4601_backend.entity.OrderItem;
import com.example.iimt4601_backend.enums.OrderStatusEnum;
import com.example.iimt4601_backend.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    
    public OrderListResponseDto getOrders(int page, int size, String status, String sort,
                                          LocalDate startDate, LocalDate endDate, String search) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 검색 조건 구성
        Specification<Order> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), OrderStatusEnum.valueOf(status)));
        }

        if (startDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), start));
        }

        if (endDate != null) {
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("orderDate"), end));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("orderNumber"), "%" + search + "%"),
                            cb.like(root.get("user").get("userName"), "%" + search + "%"),
                            cb.like(root.get("user").get("email"), "%" + search + "%")
                    )
            );
        }

        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);

        List<OrderDto> orderDtos = ordersPage.getContent().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());

        OrderListResponseDto response = new OrderListResponseDto();
        response.setOrders(orderDtos);
        response.setCurrentPage(ordersPage.getNumber());
        response.setTotalPages(ordersPage.getTotalPages());
        response.setTotalElements(ordersPage.getTotalElements());

        return response;
    }

    
    public OrderDetailDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        return mapToOrderDetailDto(order);
    }

    
    public OrderDetailDto updateOrderStatus(Long id, OrderStatusUpdateDto statusDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        order.setStatus(OrderStatusEnum.valueOf(statusDto.getStatus()));

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderDetailDto(updatedOrder);
    }

    
    public OrderStatsDto getOrderStats(String period, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // 총 매출
        BigDecimal totalSales = orderRepository.sumTotalAmountByOrderDateBetween(start, end);

        // 총 주문 수
        Integer totalOrders = (int) orderRepository.countByOrderDateBetween(start, end);

        // 평균 주문 금액
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 상태별 주문 수
        Map<String, Integer> ordersByStatus = new HashMap<>();
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            long count = orderRepository.countByStatusAndOrderDateBetween(status, start, end);
            ordersByStatus.put(status.getDisplayName(), (int) count);
        }

        // 시계열 데이터
        List<Object[]> timeSeriesData;
        switch (period.toLowerCase()) {
            case "daily":
                timeSeriesData = orderRepository.findDailyOrderStatsBetween(start, end);
                break;
            case "weekly":
                timeSeriesData = orderRepository.findWeeklyOrderStatsBetween(start, end);
                break;
            case "monthly":
                timeSeriesData = orderRepository.findMonthlyOrderStatsBetween(start, end);
                break;
            case "yearly":
                timeSeriesData = orderRepository.findYearlyOrderStatsBetween(start, end);
                break;
            default:
                timeSeriesData = orderRepository.findDailyOrderStatsBetween(start, end);
        }

        List<OrderTimeSeriesDto> timeSeries = timeSeriesData.stream()
                .map(row -> new OrderTimeSeriesDto(
                        row[0].toString(),
                        (BigDecimal) row[1],
                        ((Number) row[2]).intValue()
                ))
                .collect(Collectors.toList());

        OrderStatsDto result = new OrderStatsDto();
        result.setTotalSales(totalSales);
        result.setTotalOrders(totalOrders);
        result.setAverageOrderValue(averageOrderValue);
        result.setOrdersByStatus(ordersByStatus);
        result.setTimeSeriesData(timeSeries);

        return result;
    }

    // 매핑 메서드
    private OrderDto mapToOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setPickupDate(order.getPickupDate());
        dto.setPickupTime(order.getPickupTime());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().getDisplayName());
        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentMethod(order.getPaymentMethod());
        return dto;
    }

    private OrderDetailDto mapToOrderDetailDto(Order order) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());

        UserMinimalDto userDto = new UserMinimalDto();
        userDto.setId(order.getUser().getId());
        userDto.setUserName(order.getUser().getUserName());
        userDto.setEmail(order.getUser().getEmail());
        userDto.setPhoneNumber(order.getUser().getPhoneNumber());
        dto.setUser(userDto);

        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().getDisplayName());
        dto.setOrderDate(order.getOrderDate());
        dto.setPaymentMethod(order.getPaymentMethod());

        List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    private OrderItemDto mapToOrderItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setThumbnail(item.getProduct().getThumbnail());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setDiscountPercentage(item.getDiscountPercentage());
        dto.setOptions(item.getOptions());
        return dto;
    }
}