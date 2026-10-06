package com.barpro.notification.service;

import com.barpro.notification.entity.Notification;
import com.barpro.notification.entity.NotificationChannel;

public interface NotificationSender {

    boolean supports(NotificationChannel channel);

    String transport();

    void send(Notification notification) throws Exception;
}
