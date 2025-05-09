package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.Category;
import com.example.iimt4601_backend.mapper.CategoryMapper;
import com.example.iimt4601_backend.entity.Product;
import com.example.iimt4601_backend.exception.ResourceNotFoundException;
import com.example.iimt4601_backend.mapper.ProductMapper;
import com.example.iimt4601_backend.repository.CategoryRepository;
import com.example.iimt4601_backend.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
        this.categoryMapper = new CategoryMapper();
    }

    @Transactional(readOnly = true)
    public List<GalleryItemDto> getGalleryItems() {
        // Get the top viewed or featured products for gallery items
        List<Product> featuredProducts = productRepository.findTop3ByOrderByViewCountDesc();

        return featuredProducts.stream()
                .map(this::convertToGalleryItemDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaginationResponse<ProductDto> getProducts(Long categoryId, Integer limit, Integer page, String sort) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        if (sortField.equals("created_at")) {
            sortField = "createdAt";
        }

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sortField));
        Page<Product> productPage;

        if (categoryId != null) {
            try {
                // 카테고리 존재 여부 확인
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
                // Use the category object in the query
                productPage = productRepository.findByCategory(category, pageable);

            } catch (ResourceNotFoundException e) {
                // If category is not found, log it and return all products
                productPage = Page.empty(pageable);
            }
        } else {
            // If no category is specified, return all products
            productPage = productRepository.findAll(pageable);
        }

        // Convert the product list to ProductDto
        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());

        // Pagination info
        PaginationDto paginationInfo = new PaginationDto(
                productPage.getTotalElements(),
                page,
                limit,
                productPage.getTotalPages()
        );

        // Return the response
        return new PaginationResponse<>(productDtos, paginationInfo);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String query, Long categoryId, String sortBy, Integer limit) {
        // 정렬 방식 설정
        Sort sort;
        switch (sortBy) {
            case "price_low":
                sort = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "price_high":
                sort = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "newest":
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case "relevance":
            default:
                // 관련성 정렬은 쿼리에서 CASE WHEN으로 처리됨
                sort = Sort.unsorted();
                break;
        }

        Pageable pageable = PageRequest.of(0, limit, sort);
        List<Product> products;

        // 카테고리 필터 적용 여부에 따른 분기
        if (categoryId != null) {
            // relevance 정렬인 경우 특별 처리
            if (sortBy.equals("relevance")) {
                products = productRepository.findBySearchQueryAndCategoryIdWithRelevance(query, categoryId, pageable);
            } else {
                products = productRepository.findBySearchQueryAndCategoryId(query, categoryId, pageable);
            }
        } else {
            // 카테고리 필터 없이 전체 검색
            if (sortBy.equals("relevance")) {
                products = productRepository.findBySearchQueryWithRelevance(query, pageable);
            } else {
                products = productRepository.findBySearchQuery(query, pageable);
            }
        }

        return products.stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getNewArrivals(Integer limit) {
        List<Product> newProducts = productRepository.findByIsNewTrueOrderByCreatedAtDesc(PageRequest.of(0, limit));

        return newProducts.stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    private GalleryItemDto convertToGalleryItemDto(Product product) {
        GalleryItemDto galleryItemDto = new GalleryItemDto();
        galleryItemDto.setId(product.getId());
        galleryItemDto.setTitle(product.getProductName());
        galleryItemDto.setDesc(product.getDescription() != null ?
                product.getDescription().substring(0, Math.min(product.getDescription().length(), 50)) + "..." :
                "More details");
        galleryItemDto.setImage(product.getThumbnail() != null ?
                product.getThumbnail() :
                (!product.getImages().isEmpty() ? product.getImages().get(0) : "/images/default.jpg"));

        return galleryItemDto;
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        ProductDetailDto dto = new ProductDetailDto();
        dto.setId(product.getId());
        dto.setName(product.getProductName());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setCategory(product.getCategory().getName());

        // Calculate discount percentage
        if (product.getOriginalPrice() != null && product.getPrice() != null) {
            BigDecimal discountAmount = product.getOriginalPrice().subtract(product.getPrice());
            BigDecimal discountPercentage = discountAmount.multiply(new BigDecimal(100))
                    .divide(product.getOriginalPrice(), 0, RoundingMode.HALF_UP);
            dto.setDiscount(discountPercentage.toString() + "%");
        }

        dto.setDescription(product.getDescription());
        dto.setOptions(product.getOptions());
        dto.setImages(product.getImages());
        dto.setIsNew(product.getIsNew());

        // Assuming isBest is based on rating or soldCount
        dto.setIsBest(product.getRecommended());

        return dto;
    }

    public RelatedProductResponseDto getRelatedProducts(Long productId) {
        List<Product> relatedProducts = productRepository.findRelatedProducts(productId);

        List<RelatedProductDto> relatedProductDtos = relatedProducts.stream()
                .map(product -> new RelatedProductDto(
                        product.getId(),
                        product.getProductName(),
                        product.getPrice().toString(),
                        product.getThumbnail()))
                .collect(Collectors.toList());

        return new RelatedProductResponseDto(relatedProductDtos);
    }

    private ProductDto convertToProductDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setTitle(product.getProductName());
        productDto.setPrice("HK$" + product.getPrice().toString());
        productDto.setImage(product.getThumbnail() != null ?
                product.getThumbnail() :
                (!product.getImages().isEmpty() ? product.getImages().get(0) : "/images/default.jpg"));
        productDto.setCategory(categoryMapper.toDto(product.getCategory()));
        productDto.setIsNew(product.getIsNew());

        return productDto;
    }

    // 제품 ID로 조회
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    // 제품 추가
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productDto) {
        Product product = productMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    // 제품 정보 수정
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productMapper.updateProductFromDto(productDto, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        return productMapper.toDto(updatedProduct);
    }

    // 제품 삭제
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    // 태그로 상품 검색
    public List<ProductResponseDto> findProductsByTag(String tag) {
        List<Product> products = productRepository.findByTagsContaining(tag);
        return productMapper.toDtoList(products);
    }

    // 상품 이름으로 검색
    public List<ProductResponseDto> findProductsByNameContaining(String keyword) {
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword);
        return productMapper.toDtoList(products);
    }

    // 상품 목록 조회 (페이징, 필터링, 정렬, 검색 지원)
    public ProductListResponseDto getProducts(int page, int size, String category, String status, String sort, String search) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 검색 조건 구성
        Specification<Product> spec = Specification.where(null);

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }

        if (status != null && !status.isEmpty()) {
            if (status.equals("low-stock")) {
                // 재고 부족 상품 (재고가 minStockLevel 이하인 상품)
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("stockQuantity"), root.get("minStockLevel")));
            } else if (status.equals("out-of-stock")) {
                // 품절 상품
                spec = spec.and((root, query, cb) -> cb.equal(root.get("stockQuantity"), 0));
            } else if (status.equals("in-stock")) {
                // 재고 있는 상품
                spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0));
            } else if (status.equals("available")) {
                // 판매 가능 상품
                spec = spec.and((root, query, cb) -> cb.equal(root.get("isAvailable"), true));
            } else if (status.equals("unavailable")) {
                // 판매 불가능 상품
                spec = spec.and((root, query, cb) -> cb.equal(root.get("isAvailable"), false));
            }
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("productName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("sku")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        Page<Product> productsPage = productRepository.findAll(spec, pageable);

        List<ProductResponseDto> productDtos = productsPage.getContent().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        ProductListResponseDto response = new ProductListResponseDto();
        response.setProducts(productDtos);
        response.setCurrentPage(productsPage.getNumber());
        response.setTotalPages(productsPage.getTotalPages());
        response.setTotalElements(productsPage.getTotalElements());

        return response;
    }

    // 상품 이미지 업로드
    public String uploadProductImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("업로드된 파일이 비어 있습니다.");
            }

            // 원본 파일명에서 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 고유한 파일명 생성
            String filename = UUID.randomUUID().toString() + extension;

            // 파일 저장 경로 설정
            Path uploadDir = Paths.get("uploads/products");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 파일 저장
            Path destination = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // 웹에서 접근 가능한 URL 반환
            return "/uploads/products/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 상품 부분 업데이트
    @Transactional
    public ProductResponseDto partialUpdateProduct(Long id, Map<String, Object> updates) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id));

        // 업데이트할 필드들 처리
        if (updates.containsKey("productName")) {
            product.setProductName((String) updates.get("productName"));
        }

        if (updates.containsKey("price")) {
            Object priceObj = updates.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(new BigDecimal(priceObj.toString()));
            } else if (priceObj instanceof String) {
                product.setPrice(new BigDecimal((String) priceObj));
            }
        }

        if (updates.containsKey("originalPrice")) {
            Object priceObj = updates.get("originalPrice");
            if (priceObj instanceof Number) {
                product.setOriginalPrice(new BigDecimal(priceObj.toString()));
            } else if (priceObj instanceof String) {
                product.setOriginalPrice(new BigDecimal((String) priceObj));
            }
        }

        if (updates.containsKey("description")) {
            product.setDescription((String) updates.get("description"));
        }

//        if (updates.containsKey("category")) {
//            product.setCategory((String) updates.get("category"));
//        }

        if (updates.containsKey("isAvailable")) {
            product.setIsAvailable((Boolean) updates.get("isAvailable"));
        }

        if (updates.containsKey("isNew")) {
            product.setIsNew((Boolean) updates.get("isNew"));
        }

        if (updates.containsKey("discountPercentage")) {
            Object discountObj = updates.get("discountPercentage");
            if (discountObj instanceof Number) {
                product.setDiscountPercentage(((Number) discountObj).doubleValue());
            } else if (discountObj instanceof String) {
                product.setDiscountPercentage(Double.parseDouble((String) discountObj));
            }
        }

        if (updates.containsKey("size")) {
            product.setSize((String) updates.get("size"));
        }

        if (updates.containsKey("ingredients")) {
            product.setIngredients((String) updates.get("ingredients"));
        }


        if (updates.containsKey("tags")) {
            Object tagsObj = updates.get("tags");
            if (tagsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tagsList = (List<String>) tagsObj;
                product.setTags(new HashSet<>(tagsList));
            }
        }

        if (updates.containsKey("options")) {
            Object optionsObj = updates.get("options");
            if (optionsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> optionsList = (List<String>) optionsObj;
                product.setOptions(new HashSet<>(optionsList));
            }
        }

        if (updates.containsKey("images")) {
            Object imagesObj = updates.get("images");
            if (imagesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> imagesList = (List<String>) imagesObj;
                product.setImages(imagesList);
            }
        }

        if (updates.containsKey("thumbnail")) {
            product.setThumbnail((String) updates.get("thumbnail"));
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }
}
