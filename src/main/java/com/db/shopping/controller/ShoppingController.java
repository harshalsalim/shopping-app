package com.db.shopping.controller;

import com.db.shopping.dto.*;
import com.db.shopping.service.CartService;
import com.db.shopping.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShoppingController {

    private final DashboardService dashboardService;
    private final CartService cartService;

    @GetMapping("/dashboards/popular")
    public ResponseEntity<List<ProductResponse>> getPopularProducts(
            @RequestParam(defaultValue = "20") int limit) {
        List<ProductResponse> products = dashboardService.getPopularProducts(limit);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .body(products);
    }

    @GetMapping("/dashboards/discounts")
    public ResponseEntity<DiscountsDashboardResponse> getDiscounts() {
        DiscountsDashboardResponse discounts = dashboardService.getActiveDiscounts();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(discounts);
    }

    @GetMapping("/dashboards/liked")
    public ResponseEntity<List<LikedItemResponse>> getLikedItems(
            @RequestHeader("X-User-Id") Long userId) {
        List<LikedItemResponse> likedItems = dashboardService.getLikedItems(userId);
        return ResponseEntity.ok(likedItems);
    }

    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<CartItemResponse> addCartItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddCartItemRequest request) {
        CartItemResponse response = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId,
            @RequestParam(required = false) Integer quantity) {
        cartService.removeItem(userId, productId, quantity);
        return ResponseEntity.noContent().build();
    }
}