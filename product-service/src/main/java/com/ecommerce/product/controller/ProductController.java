package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto.Response> createProduct(@Valid @RequestBody ProductDto.Request request) {
        log.info("POST /api/products - Creating product");
        ProductDto.Response response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> getProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

//    @GetMapping
//    public ResponseEntity<Page<ProductDto.Response>> getAllProducts(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "id") String sortBy,
//            @RequestParam(defaultValue = "asc") String sortDir) {

    @GetMapping
    public ResponseEntity<Page<ProductDto.Response>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto.Response>> searchProducts(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productService.searchProducts(name, category, minPrice, maxPrice, pageable)
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductDto.Response>> getByCategory(
            @PathVariable("category") String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategory(category, pageable));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/bulk")
    public ResponseEntity<List<ProductDto.Response>> getProductsByIds(
            @RequestParam(name = "ids") List<Long> ids) {

        return ResponseEntity.ok(productService.getProductsByIds(ids));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.Response> updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductDto.Request request) {

        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(
            @PathVariable("id") Long id,
            @RequestBody ProductDto.StockUpdateRequest request) {

        productService.updateStock(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") Long id) {

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
