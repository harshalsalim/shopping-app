package com.db.shopping.repository;

import com.db.shopping.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findTop10ByOrderByViewsDesc();

    List<Product> findTop10ByOrderByCreatedAtDesc();
}