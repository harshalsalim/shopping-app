package com.db.shopping.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Discount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String title;
    private String discountType;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Boolean active;
    private Integer usageLimit;
    private Integer usageCount;
}