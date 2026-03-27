package com.ecommerce.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String to;
    private String subject;
    private String body;
    private NotificationType type;
    private String referenceId;  // orderId, userId, etc.
    private LocalDateTime requestedAt;

    public enum NotificationType {
        ORDER_PLACED, ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED,
        ORDER_CANCELLED, WELCOME, PASSWORD_RESET
    }
}
