package com.db.shopping.service;

import com.db.shopping.dto.*;
import com.db.shopping.entity.*;
import com.db.shopping.exception.*;
import com.db.shopping.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartReservationRepository cartRepo;
    private final InventoryRepository inventoryRepo;
    private final ProductRepository productRepo;
    private final StringRedisTemplate redisTemplate;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartReservation> reservations = cartRepo.findByUserIdAndStatus(userId, "ACTIVE");
        List<CartItemResponse> items = reservations.stream().map(this::mapToCartItemResponse).toList();

        // Updated to use record accessors: i.product().price() and i.quantity()
        BigDecimal total = items.stream()
                .map(i -> i.product().price().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder().items(items).cartTotal(total).build();
    }

    @Transactional
    public CartItemResponse addItem(Long userId, AddCartItemRequest request) {
        // Updated to use request.productId()
        Product product = productRepo.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId().toString()));

        Inventory inventory = inventoryRepo.findByProductId(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", product.getId().toString()));

        try {
            // Updated to use request.quantity()
            inventory.reserve(request.quantity());
        } catch (IllegalStateException e) {
            throw new InsufficientStockException(product.getId(), request.quantity(), inventory.getAvailableQuantity());
        }

        inventoryRepo.save(inventory);

        CartReservation reservation = cartRepo.findByUserIdAndProductId(userId, product.getId())
                .orElse(CartReservation.builder().userId(userId).product(product).build());

        if ("ACTIVE".equals(reservation.getStatus()) && reservation.getQuantity() != null) {
            reservation.setQuantity(reservation.getQuantity() + request.quantity());
        } else {
            reservation.setQuantity(request.quantity());
            reservation.setStatus("ACTIVE");
        }

        reservation.setReservedAt(LocalDateTime.now());
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        cartRepo.save(reservation);

        redisTemplate.opsForValue().set("cart:reservation:" + userId + ":" + product.getId(), "ACTIVE", 15, TimeUnit.MINUTES);

        return mapToCartItemResponse(reservation);
    }

    @Transactional
    public void removeItem(Long userId, Long productId, Integer quantityToRemove) {
        CartReservation reservation = cartRepo.findByUserIdAndProductIdAndStatus(userId, productId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", productId.toString()));

        // If requested removal quantity exceeds current reserved quantity, throw exception
        if (quantityToRemove != null && quantityToRemove > reservation.getQuantity()) {
            throw new ResourceNotFoundException("CartItem",
                    "Exceeded reserved quantity. Item quantity in cart: " + reservation.getQuantity());
        }

        Inventory inventory = inventoryRepo.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId.toString()));

        // If quantity is not specified or matches exact reserved quantity, release all and deactivate
        if (quantityToRemove == null || quantityToRemove.equals(reservation.getQuantity())) {
            inventory.release(reservation.getQuantity());
            reservation.setStatus("RELEASED");
            redisTemplate.delete("cart:reservation:" + userId + ":" + productId);
        } else {
            // Partial release: decrement quantity in cart and return reserved stock back to available inventory
            inventory.release(quantityToRemove);
            reservation.setQuantity(reservation.getQuantity() - quantityToRemove);
        }

        inventoryRepo.save(inventory);
        cartRepo.save(reservation);
    }

    private CartItemResponse mapToCartItemResponse(CartReservation res) {
        ProductResponse pr = ProductResponse.builder().id(res.getProduct().getId())
                .name(res.getProduct().getName()).price(res.getProduct().getPrice()).build();
        return CartItemResponse.builder().product(pr).quantity(res.getQuantity()).expiresAt(res.getExpiresAt()).build();
    }
}