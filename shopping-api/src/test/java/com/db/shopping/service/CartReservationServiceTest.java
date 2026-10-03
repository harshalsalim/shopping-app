package com.db.shopping.service;

import com.db.shopping.dto.CartRequest;
import com.db.shopping.entity.Product;
import com.db.shopping.exception.InsufficientStockException;
import com.db.shopping.repository.CartItemRepository;
import com.db.shopping.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartReservationServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CartReservationService cartService;

    @Test
    void addItem_shouldThrowException_whenStockExhausted() {
        Long userId = 1L;
        CartRequest request = new CartRequest(100L, 5);
        Product product = Product.builder().id(100L).availableStock(2).version(1L).build();

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> cartService.addItemToCart(userId, request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void addItem_shouldReserveStock_whenSuccessful() {
        Long userId = 1L;
        CartRequest request = new CartRequest(100L, 2);
        Product product = Product.builder().id(100L).availableStock(10).version(1L).build();

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cartService.addItemToCart(userId, request);

        verify(productRepository).save(argThat(p -> p.getAvailableStock() == 8));
        verify(cartItemRepository).save(any());
        verify(valueOperations).set(eq("cart:reservation:1:100"), eq("2"), eq(15L), any());
    }
}