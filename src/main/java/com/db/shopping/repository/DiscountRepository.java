package com.db.shopping.repository;

import com.db.shopping.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    @Query("SELECT d FROM Discount d WHERE d.active = true AND d.startsAt <= :now AND (d.endsAt IS NULL OR d.endsAt >= :now)")
    List<Discount> findActiveDiscounts(@Param("now") LocalDateTime now);
}