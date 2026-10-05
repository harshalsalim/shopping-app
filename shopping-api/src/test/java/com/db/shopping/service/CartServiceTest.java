package com.db.shopping.service;

import com.db.shopping.dto.AddCartItemRequest;
import com.db.shopping.dto.CartItemResponse;
import com.db.shopping.dto.CartResponse;
import com.db.shopping.entity.CartReservation;
import com.db.shopping.entity.Inventory;
import com.db.shopping.entity.Product;
import com.db.shopping.exception.InsufficientStockException;
import com.db.shopping.exception.ResourceNotFoundException;
import com.db.shopping.repository.CartReservationRepository;
import com.db.shopping.repository.InventoryRepository;
import com.db.shopping.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartReservationRepository cartRepo;
    @Mock private InventoryRepository inventoryRepo;
    @Mock private ProductRepository productRepo;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CartService cartService;

    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cartService, "shopLocation", "LONDON");

        product = Product.builder().id(1L).name("Test Product").price(BigDecimal.valueOf(100.00)).sku("SKU-100").currency("GBP").build();
        inventory = Inventory.builder().id(1L).product(product).availableQuantity(10).reservedQuantity(0).location("LONDON").build();
    }

    @Test
    void getCart_ReturnsCartWithCalculatedTotal() {
        CartReservation reservation = CartReservation.builder()
                .id(1L).userId(100L).product(product).quantity(2).status("ACTIVE").expiresAt(LocalDateTime.now().plusMinutes(15)).build();

        when(cartRepo.findByUserIdAndStatus(100L, "ACTIVE")).thenReturn(List.of(reservation));

        CartResponse cart = cartService.getCart(100L);

        assertNotNull(cart);
        assertEquals(1, cart.items().size());
        assertEquals(BigDecimal.valueOf(200.00), cart.cartTotal());
        assertEquals(2, cart.items().get(0).quantity());
    }

    @Test
    void addItem_Success_NewReservation() {
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);

        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepo.findByProductIdAndLocation(1L, "LONDON")).thenReturn(Optional.of(inventory));
        when(cartRepo.findByUserIdAndProductId(100L, 1L)).thenReturn(Optional.empty());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CartItemResponse response = cartService.addItem(100L, request);

        assertEquals(8, inventory.getAvailableQuantity());
        assertEquals(2, inventory.getReservedQuantity());
        assertEquals(2, response.quantity());
        verify(cartRepo, times(1)).save(any(CartReservation.class));
        verify(valueOperations, times(1)).set(eq("cart:reservation:100:1"), eq("ACTIVE"), eq(15L), eq(TimeUnit.MINUTES));
    }

    @Test
    void addItem_Success_UpdatesExistingActiveReservationQuantity() {
        AddCartItemRequest request = new AddCartItemRequest(1L, 3);
        CartReservation existingReservation = CartReservation.builder()
                .id(10L).userId(100L).product(product).quantity(2).status("ACTIVE").build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepo.findByProductIdAndLocation(1L, "LONDON")).thenReturn(Optional.of(inventory));
        when(cartRepo.findByUserIdAndProductId(100L, 1L)).thenReturn(Optional.of(existingReservation));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CartItemResponse response = cartService.addItem(100L, request);

        assertEquals(5, existingReservation.getQuantity());
        assertEquals(5, response.quantity());
        verify(cartRepo, times(1)).save(existingReservation);
    }

    @Test
    void addItem_ProductNotFound_ThrowsException() {
        AddCartItemRequest request = new AddCartItemRequest(99L, 1);
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(100L, request));
    }

    @Test
    void addItem_InventoryNotFound_ThrowsException() {
        AddCartItemRequest request = new AddCartItemRequest(1L, 1);
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepo.findByProductIdAndLocation(1L, "LONDON")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(100L, request));
    }

    @Test
    void addItem_InsufficientStock_ThrowsException() {
        AddCartItemRequest request = new AddCartItemRequest(1L, 20);

        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepo.findByProductIdAndLocation(1L, "LONDON")).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> cartService.addItem(100L, request));
        verify(cartRepo, never()).save(any());
    }

    @Test
    void updateItem_Success_IncreaseQuantity() {
        CartReservation reservation = CartReservation.builder()
                .id(1L).userId(100L).product(product).quantity(2).status("ACTIVE").build();
        inventory.setReservedQuantity(2);
        inventory.setAvailableQuantity(8);

        when(cartRepo.findByUserIdAndProductIdAndStatus(100L, 1L, "ACTIVE")).thenReturn(Optional.of(reservation));
        when(inventoryRepo.findByProductIdAndLocation(1L, "LONDON")).thenReturn(Optional.of(inventory));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CartItemResponse response = cartService.updateItem(100L, 1L, 5);

        assertEquals(5, reservation.getQuantity());
        assertEquals(5, inventory.getReservedQuantity());
        assertEquals(5, inventory.getAvailableQuantity());
        assertEquals(5, response.quantity());
    }

    @Test
    void updateItem_Success_DecreaseQuantity() {
        CartReservation reservation = CartReservation.builder()
                .id(1L).userId(100L).product(product).quantity(5).status("ACTIVE").build();
        inventory.setReservedQuantity(5);
        inventory.setAvailableQuantity(5);

        when(cartRepo.findByUserIdAndProductIdAndStatus(100L, 1L, "ACTIVE")).thenReturn(Optional.of(reservation));
        when(inventoryRepo.findByProductIdAndLocation(1L, "LONDON")).thenReturn(Optional.of(inventory));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CartItemResponse response = cartService.updateItem(100L, 1L, 2);

        assertEquals(2, reservation.getQuantity());
        assertEquals(2, inventory.getReservedQuantity());
        assertEquals(8, inventory.getAvailableQuantity());
        assertEquals(2, response.quantity());
    }

    @Test
    void updateItem_InsufficientStock_ThrowsException() {
        CartReservation reservation = CartReservation.builder()
                .id(1L).userId(100L).product(product).quantity(2).status("ACTIVE").build();

        when(cartRepo.findByUserIdAndProductIdAndStatus(100L, 1L, "ACTIVE")).thenReturn(Optional.of(reservation));
        when(inventoryRepo.findByProductIdAndLocation(1L, "LONDON")).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> cartService.updateItem(100L, 1L, 20));
    }

    @Test
    void removeItem_Success() {
        CartReservation reservation = CartReservation.builder()
                .id(1L).userId(100L).product(product).quantity(2).status("ACTIVE").build();
        inventory.setReservedQuantity(2);
        inventory.setAvailableQuantity(8);

        when(cartRepo.findByUserIdAndProductIdAndStatus(100L, 1L, "ACTIVE")).thenReturn(Optional.of(reservation));
        when(inventoryRepo.findByProductIdAndLocation(1L, "LONDON")).thenReturn(Optional.of(inventory));

        cartService.removeItem(100L, 1L, null);

        assertEquals("RELEASED", reservation.getStatus());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(10, inventory.getAvailableQuantity());
        verify(redisTemplate, times(1)).delete("cart:reservation:100:1");
    }

    @Test
    void removeItem_ExceededQuantity_ThrowsException() {
        CartReservation reservation = CartReservation.builder()
                .id(1L).userId(100L).product(product).quantity(2).status("ACTIVE").build();

        when(cartRepo.findByUserIdAndProductIdAndStatus(100L, 1L, "ACTIVE")).thenReturn(Optional.of(reservation));

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(100L, 1L, 5));
    }

    @Test
    void removeItem_NotFound_ThrowsException() {
        when(cartRepo.findByUserIdAndProductIdAndStatus(100L, 1L, "ACTIVE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(100L, 1L, null));
    }
}