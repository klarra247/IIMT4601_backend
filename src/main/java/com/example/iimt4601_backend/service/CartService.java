package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.CartRequestDto;
import com.example.iimt4601_backend.dto.CartResponseDto;
import com.example.iimt4601_backend.entity.Cart;
import com.example.iimt4601_backend.entity.CartItem;
import com.example.iimt4601_backend.entity.Product;
import com.example.iimt4601_backend.entity.User;
import com.example.iimt4601_backend.repository.CartItemRepository;
import com.example.iimt4601_backend.repository.CartRepository;
import com.example.iimt4601_backend.repository.ProductRepository;
import com.example.iimt4601_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartResponseDto addToCart(CartRequestDto requestDto, Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 상품 조회
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 사용자의 기존 카트 찾기 또는 새로 생성하기
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // 상품이 이미 카트에 있는지 확인
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductId(
                cart.getId(), requestDto.getProductId());

        if (existingItem.isPresent()) {
            // 기존 상품 수량 업데이트
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + requestDto.getQuantity());
            cartItemRepository.save(item);
        } else {
            // 새 카트 아이템 생성
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(requestDto.getQuantity());
            cartItemRepository.save(newItem);
        }

        // 업데이트된 카트 수량 조회
        Integer cartCount = cartItemRepository.getCartItemCount(cart.getId());
        if (cartCount == null) {
            cartCount = 0;
        }

        return new CartResponseDto(true, cart.getId(), "Product added to cart successfully", cartCount);
    }
}