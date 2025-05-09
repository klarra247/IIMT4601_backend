package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.*;
import com.example.iimt4601_backend.enums.OrderStatusEnum;
import com.example.iimt4601_backend.repository.OrderItemRepository;
import com.example.iimt4601_backend.repository.OrderRepository;
import com.example.iimt4601_backend.repository.ProductRepository;
import com.example.iimt4601_backend.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderDto createOrder(OrderRequestDto orderRequestDto, Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 주문 생성
        Order order = new Order();

        // 주문번호 생성 (현재 날짜 + 랜덤 문자열)
        String orderNumber = "ORD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());


        // 픽업 일시 설정
        if (orderRequestDto.getPickupDate() != null && !orderRequestDto.getPickupDate().isEmpty()) {
            LocalDate pickupDate = LocalDate.parse(orderRequestDto.getPickupDate());
            order.setPickupDate(pickupDate);
        }

        if (orderRequestDto.getPickupTime() != null && !orderRequestDto.getPickupTime().isEmpty()) {
            LocalTime pickupTime = LocalTime.parse(orderRequestDto.getPickupTime());
            order.setPickupTime(pickupTime);
        }

        // 특별 지시사항
//        order.setSpecialInstructions(orderRequestDto.getSpecialInstructions());

        // 결제 정보
        order.setPaymentMethod(orderRequestDto.getPaymentMethod());
        order.setPaymentProofUrl(orderRequestDto.getPaymentProofUrl());
        order.setTotalAmount(orderRequestDto.getTotalAmount());

        // 주문 상태 설정 (기본값: 미확인)
        order.setStatus(OrderStatusEnum.PENDING);

        // 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 주문 상품 저장
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDto itemDto : orderRequestDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemDto.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(itemDto.getPrice());

            // 옵션 정보를 JSON으로 변환하여 저장
            if (itemDto.getOptions() != null && !itemDto.getOptions().isEmpty()) {
                try {
                    String optionsJson = objectMapper.writeValueAsString(itemDto.getOptions());
                    orderItem.setOptions(optionsJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to process options: " + e.getMessage());
                }
            }

            orderItems.add(orderItem);
        }

        // 주문 상품 저장
        orderItemRepository.saveAll(orderItems);

        Order completedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow(() -> new RuntimeException("Order not found after saving"));


        return convertToDto(completedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(Long userId) {
        // 사용자 존재 여부 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 사용자의 모든 주문 조회 (최신 주문이 먼저 오도록 정렬)
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);

        // 주문 목록을 DTO로 변환
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderDetails(Long orderId, Long userId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 주문이 현재 로그인한 사용자의 것인지 확인
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order");
        }

        // 주문 정보를 DTO로 변환
        return convertToDto(order);
    }

    private OrderDto convertToDto(Order order) {
        // 기본 주문 정보 설정
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setOrderNumber(order.getOrderNumber());
        orderDto.setPickupDate(order.getPickupDate());
        orderDto.setPickupTime(order.getPickupTime());
        orderDto.setTotalAmount(order.getTotalAmount());
        orderDto.setStatus(order.getStatus().toString());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setPaymentMethod(order.getPaymentMethod());

        // OrderItem 데이터를 ProductDto로 변환
        List<ProductDto> productDtos = new ArrayList<>();

        // order.getOrderItems()를 통해 해당 주문의 모든 아이템을 가져옵니다
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();

            ProductDto productDto = new ProductDto();
            productDto.setId(product.getId());
            productDto.setTitle(product.getProductName());
            productDto.setPrice(orderItem.getPrice().toString()); // 주문 시점의 가격 사용

            // 이미지 설정 - 썸네일이 있으면 썸네일 사용, 없으면 첫 번째 이미지 사용
            if (product.getThumbnail() != null && !product.getThumbnail().isEmpty()) {
                productDto.setImage(product.getThumbnail());
            } else if (product.getImages() != null && !product.getImages().isEmpty()) {
                productDto.setImage(product.getImages().get(0)); // 첫 번째 이미지
            }

            // 카테고리 정보 설정
            if (product.getCategory() != null) {
                CategoryDto categoryDto = new CategoryDto();
                categoryDto.setId(product.getCategory().getId());
                categoryDto.setName(product.getCategory().getName());
                productDto.setCategory(categoryDto);
            }

            productDto.setIsNew(product.getIsNew());
            productDto.setQuantity(orderItem.getQuantity());


            productDtos.add(productDto);
        }

        orderDto.setProduct(productDtos);

        return orderDto;
    }
}