package com.example.iimt4601_backend.controller.admin;

import com.example.iimt4601_backend.dto.DashboardStatsDto;
import com.example.iimt4601_backend.dto.RecentOrderListDto;
import com.example.iimt4601_backend.dto.SalesChartDto;
import com.example.iimt4601_backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

//    @GetMapping("/stats")
//    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
//        DashboardStatsDto stats = dashboardService.getDashboardStats();
//        return ResponseEntity.ok(stats);
//    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats(
            @RequestParam(defaultValue = "monthly") String period) {
        try {
            DashboardStatsDto stats = dashboardService.getDashboardStats(period);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // 로깅 추가
            log.error("대시보드 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DashboardStatsDto.builder()
                            .error("서버 오류가 발생했습니다: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<RecentOrderListDto> getRecentOrders() {
        RecentOrderListDto orders = dashboardService.getRecentOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/sales-chart")
    public ResponseEntity<SalesChartDto> getSalesChartData(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SalesChartDto chartData = dashboardService.getSalesChartData(period, startDate, endDate);
        return ResponseEntity.ok(chartData);
    }
}