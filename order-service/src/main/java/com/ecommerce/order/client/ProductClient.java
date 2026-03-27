package com.ecommerce.order.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//
////@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
//@FeignClient(name = "product-service", configuration = com.ecommerce.order.client.FeignConfig.class, fallback = ProductClientFallback.class)
//public interface ProductClient {
//
//    @GetMapping("/api/products/{id}")
//    ProductResponse getProductById(@PathVariable("id") Long id);  // ← add "id"
//
//    @PatchMapping("/api/products/{id}/stock")
//    void updateStock(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);  // ← add "id"
//
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    class ProductResponse {
//        private Long id;
//        private String name;
//        private String description;
//        private BigDecimal price;
//        private Integer stockQuantity;
//        private String category;
//        private String status;
//        private LocalDateTime createdAt;
//    }
//
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    class StockUpdateRequest {
//        private Integer quantity;
//        private String operation;
//    }
//}











@FeignClient(
        name = "product-service",
        url = "http://localhost:8081"
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);

    @PatchMapping("/api/products/{id}/stock")
    void updateStock(@PathVariable("id") Long id,
                     @RequestBody StockUpdateRequest request);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private String category;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class StockUpdateRequest {
        private Integer quantity;
        private String operation;
    }
}