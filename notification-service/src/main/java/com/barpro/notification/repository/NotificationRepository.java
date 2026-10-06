package com.barpro.notification.repository;

import com.barpro.notification.entity.Notification;
import com.barpro.notification.entity.NotificationStatus;
import com.barpro.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status);

    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    List<Notification> findByStatusAndTypeOrderByCreatedAtDesc(NotificationStatus status,
                                                              NotificationType type);
}
