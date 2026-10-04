package com.db.shopping.repository;

import com.db.shopping.entity.CartReservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartReservationRepository extends JpaRepository<CartReservation, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<CartReservation> findByUserIdAndStatus(Long userId, String status);

    Optional<CartReservation> findByUserIdAndProductIdAndStatus(Long userId, Long productId, String status);


    Optional<CartReservation> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT c FROM CartReservation c WHERE c.status = 'ACTIVE' AND c.expiresAt < :now")
    List<CartReservation> findExpiredReservations(@Param("now") LocalDateTime now);
}