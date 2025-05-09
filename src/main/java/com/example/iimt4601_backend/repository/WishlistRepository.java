package com.example.iimt4601_backend.repository;

import com.example.iimt4601_backend.entity.Product;
import com.example.iimt4601_backend.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Find wishlist item by user ID and product ID
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    // Count wishlist items by user ID
    Long countByUserId(Long userId);

    // Delete wishlist item by user ID and product ID
    void deleteByUserIdAndProductId(Long userId, Long productId);

    List<Wishlist> findAllByUserId(Long userId);

    Long product(Product product);
}