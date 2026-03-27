package com.ecommerce.notification.service;

import com.ecommerce.notification.model.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        log.info("Received notification request for: {}", request.getTo());
        notificationService.sendNotification(request);
        return ResponseEntity.ok("Notification queued successfully");
    }

    @PostMapping("/order-placed")
    public ResponseEntity<String> orderPlaced(
            @RequestParam("email") String email,
            @RequestParam("orderNumber") String orderNumber,
            @RequestParam("total") double total) {
        NotificationRequest req = notificationService.buildOrderPlacedNotification(email, orderNumber, total);
        notificationService.sendNotification(req);
        return ResponseEntity.ok("Order placed notification sent");
    }

    @PostMapping("/order-status")
    public ResponseEntity<String> orderStatus(
            @RequestParam("email") String email,
            @RequestParam("orderNumber") String orderNumber,
            @RequestParam("status") String status) {
        NotificationRequest req = notificationService.buildOrderStatusNotification(email, orderNumber, status);
        notificationService.sendNotification(req);
        return ResponseEntity.ok("Order status notification sent");
    }

    @PostMapping("/welcome")
    public ResponseEntity<String> welcome(
            @RequestParam("email") String email,
            @RequestParam("username") String username) {
        NotificationRequest req = notificationService.buildWelcomeNotification(email, username);
        notificationService.sendNotification(req);
        return ResponseEntity.ok("Welcome notification sent");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
