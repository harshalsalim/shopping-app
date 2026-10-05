package com.db.shopping.service;

import com.db.shopping.dto.DiscountsDashboardResponse;
import com.db.shopping.dto.LikedItemResponse;
import com.db.shopping.dto.ProductResponse;
import com.db.shopping.entity.Product;
import com.db.shopping.repository.DiscountRepository;
import com.db.shopping.repository.ProductRepository;
import com.db.shopping.repository.UserLikedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepo;
    private final DiscountRepository discountRepo;
    private final UserLikedItemRepository likedItemRepo;

    @Cacheable(cacheNames = "popularProducts", key = "#limit")
    @Transactional(readOnly = true)
    public List<ProductResponse> getPopularProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepo.findByActiveTrueOrderByPopularityScoreDesc(pageable)
                .stream().map(this::mapToProductResponse).toList();
    }

    @Cacheable(cacheNames = "newArrivals", key = "#limit")
    @Transactional(readOnly = true)
    public List<ProductResponse> getNewArrivals(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepo.findByActiveTrueOrderByCreatedAtDesc(pageable)
                .stream().map(this::mapToProductResponse).toList();
    }

    @Cacheable(cacheNames = "activeDiscounts")
    @Transactional(readOnly = true)
    public DiscountsDashboardResponse getActiveDiscounts() {
        List<DiscountsDashboardResponse.DiscountDto> discounts = discountRepo.findActiveDiscounts(LocalDateTime.now())
                .stream()
                .map(d -> DiscountsDashboardResponse.DiscountDto.builder()
                        .title(d.getTitle()).code(d.getCode())
                        .type(d.getDiscountType()).value(d.getDiscountValue()).build())
                .toList();
        return DiscountsDashboardResponse.builder().activeDiscounts(discounts).build();
    }

    @Transactional(readOnly = true)
    public List<LikedItemResponse> getLikedItems(Long userId) {
        return likedItemRepo.findByUserId(userId).stream()
                .map(item -> LikedItemResponse.builder()
                        .product(mapToProductResponse(item.getProduct()))
                        .likedAt(item.getLikedAt()).build())
                .toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }
}