package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.WishlistResponseDto;
import com.example.iimt4601_backend.entity.Product;
import com.example.iimt4601_backend.entity.Wishlist;
import com.example.iimt4601_backend.repository.ProductRepository;
import com.example.iimt4601_backend.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Autowired
    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    public List<WishlistResponseDto> getWishlist(Long userId) {
        List<Wishlist> wishlistItems = wishlistRepository.findAllByUserId(userId);

        return wishlistItems.stream()
                .map(item -> new WishlistResponseDto(
                        item.getProduct().getId(),
                        item.getProduct().getProductName(),
                        item.getProduct().getPrice(),
                        item.getProduct().getThumbnail()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public String addToWishlist(Long productId, Long userId) {
        // Check if product already exists in wishlist
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            return "Product is already in your wishlist";
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Wishlist wishlistItem = new Wishlist();
        wishlistItem.setUserId(userId);
        wishlistItem.setProduct(product);

        wishlistRepository.save(wishlistItem);

        return "Product added to wishlist successfully";
    }

    @Transactional
    public Void removeFromWishlist(Long userId, Long productId) {
        // Check if in wishlist
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in wishlist"));

        // Remove from wishlist
        wishlistRepository.delete(wishlist);

        return null;
    }
}