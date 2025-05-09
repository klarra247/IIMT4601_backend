package com.example.iimt4601_backend.mapper;

import com.example.iimt4601_backend.dto.ProductRequestDto;
import com.example.iimt4601_backend.dto.ProductResponseDto;
import com.example.iimt4601_backend.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Product cake = new Product();
        cake.setProductName(dto.getProductName());
        cake.setPrice(dto.getPrice());
        cake.setDescription(dto.getDescription());
        cake.setSku(dto.getSku());
        cake.setCategory(dto.getCategory());
        cake.setTags(dto.getTags());
        cake.setOptions(dto.getOptions());
        cake.setNutritionInfo(dto.getNutritionInfo());
        cake.setSize(dto.getSize());
        cake.setIngredients(dto.getIngredients());
        cake.setIsAvailable(dto.getIsAvailable());
        cake.setDiscountPercentage(dto.getDiscountPercentage());
        cake.setOriginalPrice(dto.getOriginalPrice());
        cake.setImages(dto.getImages());
        cake.setThumbnail(dto.getThumbnail());
        cake.setIsNew(dto.getIsNew());
        cake.setIsCustomizable(dto.getIsCustomizable());

        return cake;
    }

    public ProductResponseDto toDto(Product cake) {
        if (cake == null) {
            return null;
        }

        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(cake.getId());
        dto.setProductName(cake.getProductName());
        dto.setPrice(cake.getPrice());
        dto.setDescription(cake.getDescription());
        dto.setSku(cake.getSku());
        dto.setCreatedAt(cake.getCreatedAt());
        dto.setUpdatedAt(cake.getUpdatedAt());
        dto.setCategory(cake.getCategory());
        dto.setTags(cake.getTags());
        dto.setOptions(cake.getOptions());
        dto.setNutritionInfo(cake.getNutritionInfo());
        dto.setSize(cake.getSize());
        dto.setIngredients(cake.getIngredients());
        dto.setIsAvailable(cake.getIsAvailable());
        dto.setDiscountPercentage(cake.getDiscountPercentage());
        dto.setOriginalPrice(cake.getOriginalPrice());
        dto.setImages(cake.getImages());
        dto.setThumbnail(cake.getThumbnail());
        dto.setRating(cake.getRating());
        dto.setReviewCount(cake.getReviewCount());
//        dto.setRelatedCakes(cake.getRelatedCakes());
        dto.setIsNew(cake.getIsNew());

        // 계산된 필드 - 할인된 가격 계산
        if (cake.getDiscountPercentage() != null && cake.getDiscountPercentage() > 0) {
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(cake.getDiscountPercentage() / 100));
            dto.setFinalPrice(cake.getPrice().multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP));
        } else {
            dto.setFinalPrice(cake.getPrice());
        }

        return dto;
    }

    public List<ProductResponseDto> toDtoList(List<Product> cakes) {
        return cakes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void updateProductFromDto(ProductRequestDto dto, Product cake) {
        if (dto.getProductName() != null) {
            cake.setProductName(dto.getProductName());
        }
        if (dto.getPrice() != null) {
            cake.setPrice(dto.getPrice());
        }
        if (dto.getDescription() != null) {
            cake.setDescription(dto.getDescription());
        }
        if (dto.getSku() != null) {
            cake.setSku(dto.getSku());
        }
        if (dto.getCategory() != null) {
            cake.setCategory(dto.getCategory());
        }
        if (dto.getTags() != null) {
            cake.setTags(dto.getTags());
        }
        if (dto.getOptions() != null) {
            cake.setOptions(dto.getOptions());
        }
        if (dto.getNutritionInfo() != null) {
            cake.setNutritionInfo(dto.getNutritionInfo());
        }
        if (dto.getSize() != null) {
            cake.setSize(dto.getSize());
        }
        if (dto.getIngredients() != null) {
            cake.setIngredients(dto.getIngredients());
        }
        if (dto.getIsAvailable() != null) {
            cake.setIsAvailable(dto.getIsAvailable());
        }
        if (dto.getDiscountPercentage() != null) {
            cake.setDiscountPercentage(dto.getDiscountPercentage());
        }
        if (dto.getOriginalPrice() != null) {
            cake.setOriginalPrice(dto.getOriginalPrice());
        }
        if (dto.getImages() != null) {
            cake.setImages(dto.getImages());
        }
        if (dto.getThumbnail() != null) {
            cake.setThumbnail(dto.getThumbnail());
        }
        if (dto.getIsNew() != null) {
            cake.setIsNew(dto.getIsNew());
        }
        if (dto.getIsCustomizable() != null) {
            cake.setIsCustomizable(dto.getIsCustomizable());
        }
    }
}