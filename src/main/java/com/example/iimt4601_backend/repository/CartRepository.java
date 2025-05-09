package com.example.iimt4601_backend.repository;

import com.example.iimt4601_backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    /**
     * 사용자 ID로 장바구니를 찾습니다.
     */
    Optional<Cart> findByUserId(Long userId);

}