package com.ecommerce.notification.service;

import com.ecommerce.notification.model.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Async
    public void sendNotification(NotificationRequest request) {
        log.info("Sending {} notification to: {}", request.getType(), request.getTo());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            message.setFrom("noreply@ecommerce.com");

            mailSender.send(message);
            log.info("Notification sent successfully to: {}", request.getTo());
        } catch (Exception e) {
            log.error("Failed to send notification to {}: {}", request.getTo(), e.getMessage());
            // In production: store in DB and retry with a scheduler
        }
    }

    public NotificationRequest buildOrderPlacedNotification(String email, String orderNumber, double total) {
        return NotificationRequest.builder()
                .to(email)
                .subject("Order Placed - " + orderNumber)
                .body(String.format(
                    "Dear Customer,\n\n" +
                    "Your order %s has been placed successfully.\n" +
                    "Total Amount: $%.2f\n\n" +
                    "We will notify you once it is confirmed.\n\n" +
                    "Thank you for shopping with us!\n" +
                    "E-Commerce Team", orderNumber, total))
                .type(NotificationRequest.NotificationType.ORDER_PLACED)
                .referenceId(orderNumber)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public NotificationRequest buildOrderStatusNotification(String email, String orderNumber, String status) {
        return NotificationRequest.builder()
                .to(email)
                .subject("Order Update - " + orderNumber)
                .body(String.format(
                    "Dear Customer,\n\n" +
                    "Your order %s status has been updated to: %s\n\n" +
                    "Thank you for shopping with us!\n" +
                    "E-Commerce Team", orderNumber, status))
                .type(NotificationRequest.NotificationType.ORDER_CONFIRMED)
                .referenceId(orderNumber)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public NotificationRequest buildWelcomeNotification(String email, String username) {
        return NotificationRequest.builder()
                .to(email)
                .subject("Welcome to E-Commerce!")
                .body(String.format(
                    "Dear %s,\n\n" +
                    "Welcome to our E-Commerce platform!\n" +
                    "Your account has been created successfully.\n\n" +
                    "Happy Shopping!\n" +
                    "E-Commerce Team", username))
                .type(NotificationRequest.NotificationType.WELCOME)
                .requestedAt(LocalDateTime.now())
                .build();
    }
}
