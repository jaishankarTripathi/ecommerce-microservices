package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.exception.InvalidOrderStateException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//    private final ProductClient productClient;
//
//    @CircuitBreaker(name = "product-service", fallbackMethod = "createOrderFallback")
//    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
//        log.info("Creating order for user: {}", request.getUserId());
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        BigDecimal totalAmount = BigDecimal.ZERO;
//
//        // Validate products and build order items
//        for (OrderDto.OrderItemRequest itemRequest : request.getItems()) {
//            ProductClient.ProductResponse product = productClient.getProductById(itemRequest.getProductId());
//
//            if (product.getStockQuantity() < itemRequest.getQuantity()) {
//                throw new InvalidOrderStateException(
//                    "Insufficient stock for product: " + product.getName() +
//                    ". Available: " + product.getStockQuantity() +
//                    ", Requested: " + itemRequest.getQuantity()
//                );
//            }
//
//            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
//            totalAmount = totalAmount.add(subtotal);
//
//            OrderItem item = OrderItem.builder()
//                    .productId(product.getId())
//                    .productName(product.getName())
//                    .quantity(itemRequest.getQuantity())
//                    .unitPrice(product.getPrice())
//                    .subtotal(subtotal)
//                    .build();
//
//            orderItems.add(item);
//        }
//
//        // Build and save order
//        Order order = Order.builder()
//                .orderNumber(generateOrderNumber())
//                .userId(request.getUserId())
//                .totalAmount(totalAmount)
//                .shippingAddress(request.getShippingAddress())
//                .paymentMethod(request.getPaymentMethod())
//                .notes(request.getNotes())
//                .status(Order.OrderStatus.PENDING)
//                .build();
//
//        orderItems.forEach(item -> item.setOrder(order));
//        order.setItems(orderItems);
//
//        Order savedOrder = orderRepository.save(order);
//
//        // Deduct stock from product-service
//        for (OrderDto.OrderItemRequest itemRequest : request.getItems()) {
//            productClient.updateStock(itemRequest.getProductId(),
//                    ProductClient.StockUpdateRequest.builder()
//                            .quantity(itemRequest.getQuantity())
//                            .operation("SUBTRACT")
//                            .build());
//        }
//
//        log.info("Order created: {} for user: {}", savedOrder.getOrderNumber(), request.getUserId());
//        return mapToResponse(savedOrder);
//    }
//
//    public OrderDto.Response createOrderFallback(OrderDto.CreateRequest request, Exception ex) {
//        log.error("Circuit breaker triggered for createOrder: {}", ex.getMessage());
//        throw new RuntimeException("Order service temporarily unavailable. Please try again later.");
//    }
//
//    @Transactional(readOnly = true)
//    public OrderDto.Response getOrderById(Long id) {
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new OrderNotFoundException(id));
//        return mapToResponse(order);
//    }
//
//    @Transactional(readOnly = true)
//    public OrderDto.Response getOrderByNumber(String orderNumber) {
//        Order order = orderRepository.findByOrderNumber(orderNumber)
//                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
//        return mapToResponse(order);
//    }
//
//    @Transactional(readOnly = true)
//    public List<OrderDto.Response> getOrdersByUserId(Long userId) {
//        return orderRepository.findByUserId(userId).stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public Page<OrderDto.Response> getOrdersByUserIdPaged(Long userId, Pageable pageable) {
//        return orderRepository.findByUserId(userId, pageable).map(this::mapToResponse);
//    }
//
//    @Transactional(readOnly = true)
//    public Page<OrderDto.Response> getAllOrders(Pageable pageable) {
//        return orderRepository.findAll(pageable).map(this::mapToResponse);
//    }
//
//    public OrderDto.Response updateOrderStatus(Long id, OrderDto.StatusUpdateRequest request) {
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new OrderNotFoundException(id));
//
//        validateStatusTransition(order.getStatus(), request.getStatus());
//        order.setStatus(request.getStatus());
//
//        // Restore stock if cancelled
//        if (request.getStatus() == Order.OrderStatus.CANCELLED) {
//            restoreStock(order);
//        }
//
//        Order updated = orderRepository.save(order);
//        log.info("Order {} status updated to {}", id, request.getStatus());
//        return mapToResponse(updated);
//    }
//
//    public void cancelOrder(Long id) {
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new OrderNotFoundException(id));
//
//        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
//            order.getStatus() == Order.OrderStatus.DELIVERED) {
//            throw new InvalidOrderStateException("Cannot cancel order in status: " + order.getStatus());
//        }
//
//        order.setStatus(Order.OrderStatus.CANCELLED);
//        restoreStock(order);
//        orderRepository.save(order);
//        log.info("Order cancelled: {}", id);
//    }
//
//    @Transactional(readOnly = true)
//    public List<OrderDto.Response> getOrdersByStatus(Order.OrderStatus status) {
//        return orderRepository.findByStatus(status).stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<OrderDto.Response> getOrdersBetweenDates(LocalDateTime start, LocalDateTime end) {
//        return orderRepository.findByCreatedAtBetween(start, end).stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    private void restoreStock(Order order) {
//        order.getItems().forEach(item -> {
//            try {
//                productClient.updateStock(item.getProductId(),
//                        ProductClient.StockUpdateRequest.builder()
//                                .quantity(item.getQuantity())
//                                .operation("ADD")
//                                .build());
//            } catch (Exception e) {
//                log.error("Failed to restore stock for product {}: {}", item.getProductId(), e.getMessage());
//            }
//        });
//    }
//
//    private void validateStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
//        boolean valid = switch (current) {
//            case PENDING -> next == Order.OrderStatus.CONFIRMED || next == Order.OrderStatus.CANCELLED;
//            case CONFIRMED -> next == Order.OrderStatus.PROCESSING || next == Order.OrderStatus.CANCELLED;
//            case PROCESSING -> next == Order.OrderStatus.SHIPPED || next == Order.OrderStatus.CANCELLED;
//            case SHIPPED -> next == Order.OrderStatus.DELIVERED;
//            case DELIVERED -> next == Order.OrderStatus.REFUNDED;
//            default -> false;
//        };
//        if (!valid) {
//            throw new InvalidOrderStateException(
//                    "Invalid status transition from " + current + " to " + next);
//        }
//    }
//
//    private String generateOrderNumber() {
//        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//    }
//
//    private OrderDto.Response mapToResponse(Order order) {
//        List<OrderDto.OrderItemResponse> itemResponses = order.getItems().stream()
//                .map(item -> OrderDto.OrderItemResponse.builder()
//                        .id(item.getId())
//                        .productId(item.getProductId())
//                        .productName(item.getProductName())
//                        .quantity(item.getQuantity())
//                        .unitPrice(item.getUnitPrice())
//                        .subtotal(item.getSubtotal())
//                        .build())
//                .collect(Collectors.toList());
//
//        return OrderDto.Response.builder()
//                .id(order.getId())
//                .orderNumber(order.getOrderNumber())
//                .userId(order.getUserId())
//                .items(itemResponses)
//                .totalAmount(order.getTotalAmount())
//                .status(order.getStatus())
//                .shippingAddress(order.getShippingAddress())
//                .paymentMethod(order.getPaymentMethod())
//                .notes(order.getNotes())
//                .createdAt(order.getCreatedAt())
//                .updatedAt(order.getUpdatedAt())
//                .build();
//    }
//}









@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderDto.OrderItemRequest itemRequest : request.getItems()) {
            ProductClient.ProductResponse product =
                    productClient.getProductById(itemRequest.getProductId());

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InvalidOrderStateException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStockQuantity() +
                                ", Requested: " + itemRequest.getQuantity()
                );
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(item);
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(request.getUserId())
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .status(Order.OrderStatus.PENDING)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Deduct stock
        for (OrderDto.OrderItemRequest itemRequest : request.getItems()) {
            productClient.updateStock(
                    itemRequest.getProductId(),
                    ProductClient.StockUpdateRequest.builder()
                            .quantity(itemRequest.getQuantity())
                            .operation("SUBTRACT")
                            .build()
            );
        }

        log.info("Order created: {} for user: {}",
                savedOrder.getOrderNumber(), request.getUserId());
        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto.Response getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderDto.Response getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrdersByUserIdPaged(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToResponse);
    }

    public OrderDto.Response updateOrderStatus(Long id, OrderDto.StatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        validateStatusTransition(order.getStatus(), request.getStatus());
        order.setStatus(request.getStatus());

        if (request.getStatus() == Order.OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        Order updated = orderRepository.save(order);
        log.info("Order {} status updated to {}", id, request.getStatus());
        return mapToResponse(updated);
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
                order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        restoreStock(order);
        orderRepository.save(order);
        log.info("Order cancelled: {}", id);
    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> getOrdersBetweenDates(
            LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void restoreStock(Order order) {
        order.getItems().forEach(item -> {
            try {
                productClient.updateStock(
                        item.getProductId(),
                        ProductClient.StockUpdateRequest.builder()
                                .quantity(item.getQuantity())
                                .operation("ADD")
                                .build()
                );
            } catch (Exception e) {
                log.error("Failed to restore stock for product {}: {}",
                        item.getProductId(), e.getMessage());
            }
        });
    }

    private void validateStatusTransition(
            Order.OrderStatus current, Order.OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == Order.OrderStatus.CONFIRMED ||
                    next == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> next == Order.OrderStatus.PROCESSING ||
                    next == Order.OrderStatus.CANCELLED;
            case PROCESSING -> next == Order.OrderStatus.SHIPPED ||
                    next == Order.OrderStatus.CANCELLED;
            case SHIPPED -> next == Order.OrderStatus.DELIVERED;
            case DELIVERED -> next == Order.OrderStatus.REFUNDED;
            default -> false;
        };
        if (!valid) {
            throw new InvalidOrderStateException(
                    "Invalid status transition from " + current + " to " + next);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
    }

    private OrderDto.Response mapToResponse(Order order) {
        List<OrderDto.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderDto.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderDto.Response.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}