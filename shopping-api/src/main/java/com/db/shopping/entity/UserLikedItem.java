package com.db.shopping.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_liked_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLikedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}