package com.db.shopping.service;

import com.db.shopping.dto.*;
import com.db.shopping.entity.*;
import com.db.shopping.exception.*;
import com.db.shopping.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartReservationService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final StringRedisTemplate redisTemplate;

    private static final int RESERVATION_MINUTES = 15;
    private static final String REDIS_KEY_PREFIX = "cart:reservation:";

    private String buildRedisKey(Long userId, Long productId) {
        return REDIS_KEY_PREFIX + userId + ":" + productId;
    }

    @Transactional
    public void addItemToCart(Long userId, CartRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getAvailableStock() < request.quantity()) {
            throw new InsufficientStockException("Not enough stock available");
        }

        // Deduct stock (Optimistic Locking will trigger if concurrently modified)
        product.setAvailableStock(product.getAvailableStock() - request.quantity());
        productRepository.save(product);

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, request.productId())
                .orElse(CartItem.builder()
                        .userId(userId)
                        .product(product)
                        .quantity(0)
                        .build());

        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        cartItem.setExpiresAt(Instant.now().plus(RESERVATION_MINUTES, ChronoUnit.MINUTES));
        cartItemRepository.save(cartItem);

        // Track in Redis
        redisTemplate.opsForValue().set(
                buildRedisKey(userId, request.productId()),
                String.valueOf(request.quantity()),
                RESERVATION_MINUTES,
                TimeUnit.MINUTES
        );
    }

    @Transactional
    public void removeCartItem(Long userId, Long productId) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not in cart"));

        Product product = item.getProduct();
        product.setAvailableStock(product.getAvailableStock() + item.getQuantity());
        productRepository.save(product);

        cartItemRepository.delete(item);
        redisTemplate.delete(buildRedisKey(userId, productId));
    }

    @Transactional(readOnly = true)
    public CartResponse viewCart(Long userId) {
        List<CartItemDto> dtos = cartItemRepository.findByUserId(userId).stream()
                .map(item -> {
                    String key = buildRedisKey(userId, item.getProduct().getId());
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    long finalTtl = (ttl != null && ttl > 0) ? ttl : 0L;

                    return new CartItemDto(
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            item.getQuantity(),
                            finalTtl
                    );
                }).collect(Collectors.toList());

        return new CartResponse(userId, dtos);
    }

    // Runs every minute to clean up orphaned reservations
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredReservations() {
        log.info("Starting expired cart reservation cleanup...");
        List<CartItem> expiredItems = cartItemRepository.findByExpiresAtBefore(Instant.now());

        for (CartItem item : expiredItems) {
            Product product = item.getProduct();
            product.setAvailableStock(product.getAvailableStock() + item.getQuantity());
            productRepository.save(product);
            cartItemRepository.delete(item);

            log.info("Released {} units of product {} back to inventory from user {}",
                    item.getQuantity(), product.getId(), item.getUserId());
        }
    }
}