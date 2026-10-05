package com.db.shopping.controller;

import com.db.shopping.dto.*;
import com.db.shopping.exception.InsufficientStockException;
import com.db.shopping.exception.ResourceNotFoundException;
import com.db.shopping.service.CartService;
import com.db.shopping.service.DashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoppingController.class)
class ShoppingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CartService cartService;
    @MockBean private DashboardService dashboardService;

    @Test
    void getPopularProducts_ReturnsSuccess() throws Exception {
        ProductResponse product = ProductResponse.builder().id(1L).name("Headphones").price(BigDecimal.valueOf(299.99)).build();
        when(dashboardService.getPopularProducts(20)).thenReturn(List.of(product));

        mockMvc.perform(get("/api/v1/dashboards/popular"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=30, public"))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Headphones"));
    }

    @Test
    void getNewArrivals_ReturnsSuccess() throws Exception {
        ProductResponse product = ProductResponse.builder().id(2L).name("Mechanical Keyboard").price(BigDecimal.valueOf(149.50)).build();
        when(dashboardService.getNewArrivals(20)).thenReturn(List.of(product));

        mockMvc.perform(get("/api/v1/dashboards/new-arrivals"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=30, public"))
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].name").value("Mechanical Keyboard"));
    }

    @Test
    void getDiscounts_ReturnsSuccess() throws Exception {
        DiscountsDashboardResponse.DiscountDto discount = DiscountsDashboardResponse.DiscountDto.builder()
                .title("London Sale").code("LONDON20").type("PERCENTAGE").value(BigDecimal.valueOf(20)).build();
        DiscountsDashboardResponse response = DiscountsDashboardResponse.builder().activeDiscounts(List.of(discount)).build();

        when(dashboardService.getActiveDiscounts()).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboards/discounts"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=60, public"))
                .andExpect(jsonPath("$.activeDiscounts[0].code").value("LONDON20"));
    }

    @Test
    void getLikedItems_MissingHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/liked"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void getCart_Success() throws Exception {
        CartResponse cartResponse = CartResponse.builder().items(List.of()).cartTotal(BigDecimal.ZERO).build();
        when(cartService.getCart(123L)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/cart").header("X-User-Id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartTotal").value(0));
    }

    @Test
    void addCartItem_Success() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(1L, 1);
        ProductResponse product = ProductResponse.builder().id(1L).name("Mouse").price(BigDecimal.TEN).build();
        CartItemResponse response = CartItemResponse.builder().product(product).quantity(1).expiresAt(LocalDateTime.now().plusMinutes(15)).build();

        when(cartService.addItem(eq(123L), any(AddCartItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.product.id").value(1L));
    }

    @Test
    void addCartItem_ValidationFailed_QuantityZero_ReturnsBadRequest() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(1L, 0);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void addCartItem_InsufficientStock_ReturnsConflict() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(1L, 50);

        when(cartService.addItem(eq(123L), any(AddCartItemRequest.class)))
                .thenThrow(new InsufficientStockException(1L, 50, 5));

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.requested").value(50))
                .andExpect(jsonPath("$.available").value(5));
    }

    @Test
    void updateCartItem_Success() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        ProductResponse product = ProductResponse.builder().id(1L).name("Mouse").price(BigDecimal.TEN).build();
        CartItemResponse response = CartItemResponse.builder().product(product).quantity(3).expiresAt(LocalDateTime.now().plusMinutes(15)).build();

        when(cartService.updateItem(eq(123L), eq(1L), eq(3))).thenReturn(response);

        mockMvc.perform(put("/api/v1/cart/items/1")
                        .header("X-User-Id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void updateCartItem_ValidationFailed_QuantityZero_ReturnsBadRequest() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(0);

        mockMvc.perform(put("/api/v1/cart/items/1")
                        .header("X-User-Id", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void removeCartItem_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items/1").header("X-User-Id", "123"))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).removeItem(123L, 1L, null);
    }

    @Test
    void removeCartItem_WithQuantity_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items/1")
                        .header("X-User-Id", "123")
                        .param("quantity", "2"))
                .andExpect(status().isNoContent());

        verify(cartService, times(1)).removeItem(123L, 1L, 2);
    }

    @Test
    void removeCartItem_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("CartItem", "1"))
                .when(cartService).removeItem(123L, 1L, null);

        mockMvc.perform(delete("/api/v1/cart/items/1").header("X-User-Id", "123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.resourceType").value("CartItem"));
    }

    @Test
    void removeCartItem_ExceededQuantity_ReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("CartItem", "Exceeded reserved quantity. Item quantity in cart: 2"))
                .when(cartService).removeItem(123L, 1L, 5);

        mockMvc.perform(delete("/api/v1/cart/items/1")
                        .header("X-User-Id", "123")
                        .param("quantity", "5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.resourceType").value("CartItem"));
    }
}