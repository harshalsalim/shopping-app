package com.db.shopping.service;

import com.db.shopping.entity.CartReservation;
import com.db.shopping.entity.Inventory;
import com.db.shopping.repository.CartReservationRepository;
import com.db.shopping.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartCleanupWorker {
    private final CartReservationRepository cartRepo;
    private final InventoryRepository inventoryRepo;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredReservations() {
        List<CartReservation> expired = cartRepo.findExpiredReservations(LocalDateTime.now());
        for (CartReservation res : expired) {
            Inventory inv = inventoryRepo.findByProductId(res.getProduct().getId()).orElseThrow();
            inv.release(res.getQuantity());
            inventoryRepo.save(inv);

            res.setStatus("EXPIRED");
            cartRepo.save(res);

            redisTemplate.delete("cart:reservation:" + res.getUserId() + ":" + res.getProduct().getId());
            log.info("Released {} expired items for product {}", res.getQuantity(), res.getProduct().getId());
        }
    }
}