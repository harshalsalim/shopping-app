package com.db.shopping.service;

import com.db.shopping.entity.Product;
import com.db.shopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    private final ProductRepository productRepo;

    @CacheEvict(cacheNames = {"popularProducts", "newArrivals", "productById"}, allEntries = true)
    @Transactional
    public Product updateProduct(Product product) {
        return productRepo.save(product);
    }
}