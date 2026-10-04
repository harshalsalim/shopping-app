package com.db.shopping.service;

import com.db.shopping.dto.*;
import com.db.shopping.entity.Product;
import com.db.shopping.repository.DiscountRepository;
import com.db.shopping.repository.ProductRepository;
import com.db.shopping.repository.UserLikedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final UserLikedItemRepository userLikedItemRepository;

    @Cacheable(cacheNames = "popularProducts", key = "#limit")
    public List<ProductResponse> getPopularProducts(int limit) {
        return productRepository.findByActiveTrueOrderByPopularityScoreDesc(PageRequest.of(0, limit))
                .stream().map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "newArrivals", key = "#limit")
    public List<ProductResponse> getNewArrivals(int limit) {
        return productRepository.findByActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream().map(this::mapToProductResponse).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "activeDiscounts")
    public DiscountsDashboardResponse getActiveDiscounts() {
        List<DiscountsDashboardResponse.DiscountDto> dtos = discountRepository.findActiveDiscounts(LocalDateTime.now())
                .stream().map(d -> DiscountsDashboardResponse.DiscountDto.builder()
                        .title(d.getTitle()).code(d.getCode())
                        .type(d.getDiscountType()).value(d.getDiscountValue()).build())
                .collect(Collectors.toList());
        return DiscountsDashboardResponse.builder().activeDiscounts(dtos).build();
    }

    public List<LikedItemResponse> getLikedItems(Long userId) {
        return userLikedItemRepository.findByUserId(userId).stream()
                .map(item -> LikedItemResponse.builder()
                        .product(mapToProductResponse(item.getProduct()))
                        .likedAt(item.getLikedAt()).build())
                .collect(Collectors.toList());
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder().id(product.getId()).sku(product.getSku())
                .name(product.getName()).price(product.getPrice()).currency(product.getCurrency()).build();
    }
}