package com.db.shopping.service;

import com.db.shopping.entity.*;
import com.db.shopping.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final UserLikedItemRepository likedItemRepository;

    @Transactional(readOnly = true)
    public List<UserLikedItem> getLikedItems(Long userId) {
        return likedItemRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Discount> getActiveDiscounts() {
        return discountRepository.findByActiveTrue();
    }

    @Cacheable("popular-products")
    @Transactional(readOnly = true)
    public List<Product> getPopularProducts() {
        return productRepository.findTop10ByOrderByViewsDesc();
    }

    @Cacheable("new-arrivals")
    @Transactional(readOnly = true)
    public List<Product> getNewArrivals() {
        return productRepository.findTop10ByOrderByCreatedAtDesc();
    }
}