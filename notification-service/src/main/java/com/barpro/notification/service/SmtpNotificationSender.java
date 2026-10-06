package com.barpro.notification.service;

import com.barpro.notification.entity.Notification;
import com.barpro.notification.entity.NotificationChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@ConditionalOnProperty("spring.mail.host")
public class SmtpNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpNotificationSender(JavaMailSender mailSender,
                                  @Value("${spring.mail.username:notificaciones@velvet.mx}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public String transport() {
        return "SMTP";
    }

    @Override
    public void send(Notification notification) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(from);
        mensaje.setTo(notification.getRecipient());
        mensaje.setSubject(notification.getSubject());
        mensaje.setText(notification.getBody());
        mailSender.send(mensaje);
    }
}
