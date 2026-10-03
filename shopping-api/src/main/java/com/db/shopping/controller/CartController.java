package com.db.shopping.controller;

import com.db.shopping.dto.*;
import com.db.shopping.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartReservationService cartService;


    private final Long MOCK_USER_ID = 1L;

    @GetMapping
    public ResponseEntity<CartResponse> viewCart() {
        return ResponseEntity.ok(cartService.viewCart(MOCK_USER_ID));
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addItem(@Valid @RequestBody CartRequest request) {
        cartService.addItemToCart(MOCK_USER_ID, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long productId) {
        cartService.removeCartItem(MOCK_USER_ID, productId);
        return ResponseEntity.ok().build();
    }
}