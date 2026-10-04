package com.db.shopping.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final Integer requested;
    private final Integer available;

    public InsufficientStockException(Long productId, Integer requested, Integer available) {
        super(String.format("Insufficient stock for product ID %d. Requested: %d, Available: %d", productId, requested, available));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }
}