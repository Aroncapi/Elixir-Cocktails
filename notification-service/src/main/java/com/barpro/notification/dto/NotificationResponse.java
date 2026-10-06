package com.barpro.notification.dto;

import com.barpro.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String channel,
        String recipient,
        String subject,
        String body,
        Long referenceId,
        String status,
        String transport,
        String error,
        LocalDateTime createdAt,
        LocalDateTime sentAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getChannel().name(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody(),
                notification.getReferenceId(),
                notification.getStatus().name(),
                notification.getTransport(),
                notification.getError(),
                notification.getCreatedAt(),
                notification.getSentAt());
    }
}
