package com.ecommerce.product.dto;

import com.ecommerce.product.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Product name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        @NotNull(message = "Stock quantity is required")
        private Integer stockQuantity;

        @NotBlank(message = "Category is required")
        private String category;

        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private String category;
        private String imageUrl;
        private Product.ProductStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockUpdateRequest {
        @NotNull
        private Integer quantity;
        private String operation; // ADD or SUBTRACT
    }
}
