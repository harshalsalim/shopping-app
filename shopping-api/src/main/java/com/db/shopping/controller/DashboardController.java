package com.db.shopping.controller;

import com.db.shopping.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final Long MOCK_USER_ID = 1L;

    @GetMapping("/liked")
    public ResponseEntity<?> getLikedItems() {
        return ResponseEntity.ok(dashboardService.getLikedItems(MOCK_USER_ID));
    }

    @GetMapping("/discounts")
    public ResponseEntity<?> getDiscounts() {
        return ResponseEntity.ok(dashboardService.getActiveDiscounts());
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopular() {
        return ResponseEntity.ok(dashboardService.getPopularProducts());
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<?> getNewArrivals() {
        return ResponseEntity.ok(dashboardService.getNewArrivals());
    }
}