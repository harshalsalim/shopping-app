package com.db.shopping.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    private String name;
    private String description;
    private String category;

    private BigDecimal price;

    @Builder.Default
    private String currency = "GBP";

    private Long popularityScore;

    @Builder.Default
    private Boolean active = true;

    private LocalDateTime createdAt;
}