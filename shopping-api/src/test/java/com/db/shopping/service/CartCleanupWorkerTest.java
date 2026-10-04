package com.db.shopping.service;

import com.db.shopping.entity.CartReservation;
import com.db.shopping.entity.Inventory;
import com.db.shopping.entity.Product;
import com.db.shopping.repository.CartReservationRepository;
import com.db.shopping.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartCleanupWorkerTest {

    @Mock private CartReservationRepository cartRepo;
    @Mock private InventoryRepository inventoryRepo;
    @Mock private StringRedisTemplate redisTemplate;

    @InjectMocks private CartCleanupWorker cartCleanupWorker;

    @Test
    void cleanupExpiredReservations_ReleasesInventoryAndUpdatesStatus() {
        Product product = Product.builder().id(1L).build();
        CartReservation expiredRes = CartReservation.builder()
                .id(10L).userId(100L).product(product).quantity(2).status("ACTIVE").expiresAt(LocalDateTime.now().minusMinutes(1)).build();
        Inventory inventory = Inventory.builder().id(1L).product(product).availableQuantity(5).reservedQuantity(2).build();

        when(cartRepo.findExpiredReservations(any(LocalDateTime.class))).thenReturn(List.of(expiredRes));
        when(inventoryRepo.findByProductId(1L)).thenReturn(Optional.of(inventory));

        cartCleanupWorker.cleanupExpiredReservations();

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals("EXPIRED", expiredRes.getStatus());

        verify(inventoryRepo, times(1)).save(inventory);
        verify(cartRepo, times(1)).save(expiredRes);
        verify(redisTemplate, times(1)).delete("cart:reservation:100:1");
    }
}