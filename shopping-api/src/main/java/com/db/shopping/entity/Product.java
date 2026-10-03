package com.db.shopping.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long views;
    private Instant createdAt;

    // We store stock directly on the product for this challenge's simplicity,
    // though a separate Inventory entity could also be used.
    private Integer availableStock;

    @Version
    private Long version; // Optimistic locking
}
