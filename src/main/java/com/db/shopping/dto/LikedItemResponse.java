package com.db.shopping.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record LikedItemResponse(
        ProductResponse product,
        LocalDateTime likedAt
) {}