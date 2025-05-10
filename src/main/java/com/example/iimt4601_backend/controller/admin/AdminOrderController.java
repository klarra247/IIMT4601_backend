package com.example.iimt4601_backend.controller.admin;

import com.example.iimt4601_backend.service.AdminOrderService;
import com.example.iimt4601_backend.dto.OrderDetailDto;
import com.example.iimt4601_backend.dto.OrderListResponseDto;
import com.example.iimt4601_backend.dto.OrderStatsDto;
import com.example.iimt4601_backend.dto.OrderStatusUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService orderService;

    @GetMapping
    public ResponseEntity<OrderListResponseDto> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "orderDate,desc") String sort,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {
        OrderListResponseDto orders = orderService.getOrders(page, size, status, sort, startDate, endDate, search);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailDto> getOrderById(@PathVariable Long id) {
        OrderDetailDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDetailDto> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDto statusDto) {
        OrderDetailDto updatedOrder = orderService.updateOrderStatus(id, statusDto);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/stats")
    public ResponseEntity<OrderStatsDto> getOrderStats(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        OrderStatsDto stats = orderService.getOrderStats(period, startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}