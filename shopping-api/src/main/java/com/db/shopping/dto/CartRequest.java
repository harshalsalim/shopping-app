package com.db.shopping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CartRequest(
        @NotNull Long productId,
        @Min(1) Integer quantity
) {
}

