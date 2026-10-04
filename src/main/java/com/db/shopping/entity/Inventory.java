package com.db.shopping.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", unique = true, nullable = false)
    private Product product;

    private Integer availableQuantity;
    private Integer reservedQuantity;

    @Builder.Default
    private String location = "LONDON";

    @Version
    private Long version;

    public void reserve(int quantity) {
        if (this.availableQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }
}