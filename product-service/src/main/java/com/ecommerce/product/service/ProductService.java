package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.exception.InsufficientStockException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDto.Response createProduct(ProductDto.Request request) {
        log.info("Creating product: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductDto.Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto.Response> searchProducts(String name, String category,
                                                     BigDecimal minPrice, BigDecimal maxPrice,
                                                     Pageable pageable) {
        return productRepository.searchProducts(name, category, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto.Response> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable).map(this::mapToResponse);
    }

    public ProductDto.Response updateProduct(Long id, ProductDto.Request request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", id);
        return mapToResponse(updated);
    }

    public void updateStock(Long id, ProductDto.StockUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if ("SUBTRACT".equals(request.getOperation())) {
            if (product.getStockQuantity() < request.getQuantity()) {
                throw new InsufficientStockException(id, product.getStockQuantity(), request.getQuantity());
            }
            product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
            if (product.getStockQuantity() == 0) {
                product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
            }
        } else {
            product.setStockQuantity(product.getStockQuantity() + request.getQuantity());
            if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK) {
                product.setStatus(Product.ProductStatus.ACTIVE);
            }
        }

        productRepository.save(product);
        log.info("Stock updated for product {}: {} {}", id, request.getOperation(), request.getQuantity());
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setStatus(Product.ProductStatus.INACTIVE);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Transactional(readOnly = true)
    public List<ProductDto.Response> getProductsByIds(List<Long> ids) {
        return productRepository.findByIdIn(ids).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductDto.Response mapToResponse(Product product) {
        return ProductDto.Response.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
