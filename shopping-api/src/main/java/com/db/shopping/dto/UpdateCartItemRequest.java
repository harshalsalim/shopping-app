package com.db.shopping.dto;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
        @Min(1) Integer quantity
) {}