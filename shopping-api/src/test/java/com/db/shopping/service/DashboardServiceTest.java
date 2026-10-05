package com.db.shopping.service;

import com.db.shopping.dto.DiscountsDashboardResponse;
import com.db.shopping.dto.LikedItemResponse;
import com.db.shopping.dto.ProductResponse;
import com.db.shopping.entity.Discount;
import com.db.shopping.entity.Product;
import com.db.shopping.entity.UserLikedItem;
import com.db.shopping.repository.DiscountRepository;
import com.db.shopping.repository.ProductRepository;
import com.db.shopping.repository.UserLikedItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private UserLikedItemRepository userLikedItemRepository;

    @InjectMocks private DashboardService dashboardService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).sku("SKU-1").name("Keyboard").price(BigDecimal.valueOf(149.50)).currency("GBP").build();
    }

    @Test
    void getPopularProducts_ReturnsMappedDTOs() {
        when(productRepository.findByActiveTrueOrderByPopularityScoreDesc(any(Pageable.class))).thenReturn(List.of(product));

        List<ProductResponse> result = dashboardService.getPopularProducts(10);

        assertEquals(1, result.size());
        assertEquals("Keyboard", result.get(0).name());
        assertEquals(BigDecimal.valueOf(149.50), result.get(0).price());
    }

    @Test
    void getNewArrivals_ReturnsMappedDTOs() {
        when(productRepository.findByActiveTrueOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(List.of(product));

        List<ProductResponse> result = dashboardService.getNewArrivals(10);

        assertEquals(1, result.size());
        assertEquals("Keyboard", result.get(0).name());
        assertEquals(BigDecimal.valueOf(149.50), result.get(0).price());
    }

    @Test
    void getActiveDiscounts_ReturnsActiveDiscounts() {
        Discount discount = Discount.builder().title("London 20%").code("LONDON20").discountType("PERCENTAGE").discountValue(BigDecimal.valueOf(20)).active(true).build();
        when(discountRepository.findActiveDiscounts(any(LocalDateTime.class))).thenReturn(List.of(discount));

        DiscountsDashboardResponse result = dashboardService.getActiveDiscounts();

        assertEquals(1, result.activeDiscounts().size());
        assertEquals("LONDON20", result.activeDiscounts().get(0).code());
    }

    @Test
    void getLikedItems_ReturnsUserLikedProducts() {
        UserLikedItem likedItem = UserLikedItem.builder().id(1L).userId(100L).product(product).likedAt(LocalDateTime.now()).build();
        when(userLikedItemRepository.findByUserId(100L)).thenReturn(List.of(likedItem));

        List<LikedItemResponse> result = dashboardService.getLikedItems(100L);

        assertEquals(1, result.size());
        assertEquals("Keyboard", result.get(0).product().name());
    }
}