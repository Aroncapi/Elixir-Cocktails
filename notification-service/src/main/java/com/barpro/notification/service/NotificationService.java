package com.barpro.notification.service;

import com.barpro.notification.dto.NotificationRequest;
import com.barpro.notification.dto.NotificationResponse;
import com.barpro.notification.entity.Notification;
import com.barpro.notification.entity.NotificationChannel;
import com.barpro.notification.entity.NotificationStatus;
import com.barpro.notification.entity.NotificationType;
import com.barpro.notification.error.NotFoundException;
import com.barpro.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final NotificationDispatcher dispatcher;

    public NotificationService(NotificationRepository notifications,
                               NotificationDispatcher dispatcher) {
        this.notifications = notifications;
        this.dispatcher = dispatcher;
    }

    @Transactional
    public NotificationResponse crear(NotificationRequest request) {
        Notification notificacion = new Notification();
        notificacion.setType(parseType(request.type()));
        notificacion.setChannel(parseChannel(request.channel()));
        notificacion.setRecipient(request.recipient().trim());
        notificacion.setSubject(request.subject().trim());
        notificacion.setBody(request.body().trim());
        notificacion.setReferenceId(request.referenceId());
        enviar(notifications.save(notificacion));
        return NotificationResponse.from(notifications.save(notificacion));
    }

    @Transactional
    public NotificationResponse reenviar(Long id) {
        Notification notificacion = notifications.findById(id)
                .orElseThrow(() -> new NotFoundException("Notificacion no encontrada: " + id));
        notificacion.setStatus(NotificationStatus.PENDIENTE);
        notificacion.setError(null);
        notificacion.setTransport(null);
        notificacion.setSentAt(null);
        enviar(notifications.save(notificacion));
        return NotificationResponse.from(notifications.save(notificacion));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listar(String status, String type) {
        NotificationStatus estado = status == null || status.isBlank() ? null : parseStatus(status);
        NotificationType tipo = type == null || type.isBlank() ? null : parseType(type);
        List<Notification> base;
        if (estado != null && tipo != null) {
            base = notifications.findByStatusAndTypeOrderByCreatedAtDesc(estado, tipo);
        } else if (estado != null) {
            base = notifications.findByStatusOrderByCreatedAtDesc(estado);
        } else if (tipo != null) {
            base = notifications.findByTypeOrderByCreatedAtDesc(tipo);
        } else {
            base = notifications.findAllByOrderByCreatedAtDesc();
        }
        return base.stream().map(NotificationResponse::from).toList();
    }

    private void enviar(Notification notificacion) {
        try {
            notificacion.setTransport(dispatcher.dispatch(notificacion));
            notificacion.setStatus(NotificationStatus.ENVIADA);
            notificacion.setSentAt(LocalDateTime.now());
            notificacion.setError(null);
        } catch (Exception ex) {
            String mensaje = ex.getMessage() == null
                    ? ex.getClass().getSimpleName()
                    : ex.getMessage();
            notificacion.setStatus(NotificationStatus.FALLIDA);
            notificacion.setError(mensaje.substring(0, Math.min(mensaje.length(), 500)));
            LOG.warn("No se pudo enviar la notificacion {}: {}", notificacion.getId(), mensaje);
        }
    }

    private NotificationType parseType(String raw) {
        try {
            return NotificationType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Tipo invalido: " + raw + " (use SOLICITUD_CREADA o ESTADO_CAMBIADO)");
        }
    }

    private NotificationChannel parseChannel(String raw) {
        try {
            return NotificationChannel.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Canal invalido: " + raw + " (use EMAIL o WHATSAPP)");
        }
    }

    private NotificationStatus parseStatus(String raw) {
        try {
            return NotificationStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Estado invalido: " + raw + " (use PENDIENTE, ENVIADA o FALLIDA)");
        }
    }
}
