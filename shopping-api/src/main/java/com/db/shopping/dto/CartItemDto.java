package com.db.shopping.dto;

public record CartItemDto(
        Long productId,
        String productName,
        Integer quantity,
        Long remainingTtlSeconds
) {
}