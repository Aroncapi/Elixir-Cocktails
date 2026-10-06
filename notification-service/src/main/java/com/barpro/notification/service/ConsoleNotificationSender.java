package com.barpro.notification.service;

import com.barpro.notification.entity.Notification;
import com.barpro.notification.entity.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class ConsoleNotificationSender implements NotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleNotificationSender.class);

    @Override
    public boolean supports(NotificationChannel channel) {
        return true;
    }

    @Override
    public String transport() {
        return "CONSOLA";
    }

    @Override
    public void send(Notification notification) {
        LOG.info("[NOTIFICACION][{}] {} -> {} | {}\n{}",
                transport(),
                notification.getChannel(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody());
    }
}
