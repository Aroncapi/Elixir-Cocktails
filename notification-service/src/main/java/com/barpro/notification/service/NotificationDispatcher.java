package com.barpro.notification.service;

import com.barpro.notification.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationDispatcher {

    private final List<NotificationSender> senders;

    public NotificationDispatcher(List<NotificationSender> senders) {
        this.senders = senders;
    }

    public String dispatch(Notification notification) throws Exception {
        NotificationSender sender = senders.stream()
                .filter(candidato -> candidato.supports(notification.getChannel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay transportador para el canal " + notification.getChannel()));
        sender.send(notification);
        return sender.transport();
    }
}
