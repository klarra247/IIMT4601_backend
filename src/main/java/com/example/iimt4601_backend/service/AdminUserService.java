package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.Order;
import com.example.iimt4601_backend.entity.User;
import com.example.iimt4601_backend.enums.UserRoleEnum;
import com.example.iimt4601_backend.repository.OrderRepository;
import com.example.iimt4601_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * 사용자 목록을 페이징, 필터링, 정렬하여 조회합니다.
     */
    public UserListResponseDto getUsers(int page, int size, Boolean status, String sort, String search) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 검색 조건 구성
        Specification<User> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), status));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("userName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("phoneNumber")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        Page<User> usersPage = userRepository.findAll(spec, pageable);

        List<UserDto> userDtos = usersPage.getContent().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());

        UserListResponseDto response = new UserListResponseDto();
        response.setUsers(userDtos);
        response.setCurrentPage(usersPage.getNumber());
        response.setTotalPages(usersPage.getTotalPages());
        response.setTotalElements(usersPage.getTotalElements());

        return response;
    }

    /**
     * ID로 사용자 상세 정보를 조회합니다.
     */
    public UserDetailDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + id));

        return mapToUserDetailDto(user);
    }

    /**
     * 사용자의 활성화 상태를 업데이트합니다.
     */
    @Transactional
    public UserDetailDto updateUserStatus(Long id, UserStatusUpdateDto statusDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + id));

        // 현재 로그인한 관리자 체크 (관리자가 자신의 상태를 비활성화하지 못하도록)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("현재 로그인한 사용자 정보를 찾을 수 없습니다."));

        if (currentUser.getId().equals(user.getId()) && Boolean.FALSE.equals(statusDto.getIsActive())) {
            throw new AccessDeniedException("자신의 계정을 비활성화할 수 없습니다.");
        }

        user.setIsActive(statusDto.getIsActive());
        User updatedUser = userRepository.save(user);

        return mapToUserDetailDto(updatedUser);
    }

    /**
     * 사용자 통계 정보를 조회합니다.
     */
    public UserStatsDto getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long inactiveUsers = totalUsers - activeUsers;

        UserStatsDto stats = new UserStatsDto();
        stats.setTotalUsers((int) totalUsers);
        stats.setActiveUsers((int) activeUsers);
        stats.setInactiveUsers((int) inactiveUsers);

        // 최근 30일 내 가입한 신규 사용자 수
        long newUsers = userRepository.countByCreatedAtAfter(java.time.LocalDateTime.now().minusDays(30));
        stats.setNewUsersLast30Days((int) newUsers);

        return stats;
    }

    // 매핑 메서드
    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginDate(user.getLastLoginDate());

        // 주문 정보 추가
        Long orderCount = orderRepository.countByUserId(user.getId());
        dto.setOrderCount(orderCount != null ? orderCount.intValue() : 0);

        BigDecimal totalSpent = orderRepository.sumTotalAmountByUserId(user.getId());
        dto.setTotalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO);

        return dto;
    }

    private UserDetailDto mapToUserDetailDto(User user) {
        UserDetailDto dto = new UserDetailDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setShippingAddress(user.getShippingAddress());
        dto.setEmailNotifications(user.getEmailNotifications());
        dto.setMarketingConsent(user.getMarketingConsent());

        // 최근 주문 내역 조회
        List<Order> recentOrders = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId(), PageRequest.of(0, 5));

        List<OrderMinimalDto> orderDtos = recentOrders.stream()
                .map(this::mapToOrderMinimalDto)
                .collect(Collectors.toList());

        dto.setRecentOrders(orderDtos);

        return dto;
    }

    private OrderMinimalDto mapToOrderMinimalDto(Order order) {
        OrderMinimalDto dto = new OrderMinimalDto();
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().getDisplayName());
        dto.setOrderDate(order.getOrderDate());
        return dto;
    }

    /**
     * 사용자 통계 정보를 담는 DTO 클래스
     */
    public static class UserStatsDto {
        private int totalUsers;
        private int activeUsers;
        private int inactiveUsers;
        private int newUsersLast30Days;

        // Getters and Setters
        public int getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }

        public int getActiveUsers() {
            return activeUsers;
        }

        public void setActiveUsers(int activeUsers) {
            this.activeUsers = activeUsers;
        }

        public int getInactiveUsers() {
            return inactiveUsers;
        }

        public void setInactiveUsers(int inactiveUsers) {
            this.inactiveUsers = inactiveUsers;
        }

        public int getNewUsersLast30Days() {
            return newUsersLast30Days;
        }

        public void setNewUsersLast30Days(int newUsersLast30Days) {
            this.newUsersLast30Days = newUsersLast30Days;
        }
    }
}