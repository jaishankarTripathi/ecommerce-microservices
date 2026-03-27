package com.ecommerce.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductResponse getProductById(Long id) {
        log.error("Fallback: Product service unavailable for product id: {}", id);
        throw new RuntimeException("Product service is currently unavailable. Please try again later.");
    }

    @Override
    public void updateStock(Long id, StockUpdateRequest request) {
        log.error("Fallback: Could not update stock for product id: {}", id);
        throw new RuntimeException("Product service is currently unavailable. Stock update failed.");
    }
}
