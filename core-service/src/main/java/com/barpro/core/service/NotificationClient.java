package com.barpro.core.service;

import com.barpro.core.entity.Request;
import com.barpro.core.entity.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificationClient {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient client;
    private final String publicUrl;
    private final String adminEmail;

    public NotificationClient(
            @Value("${app.notifications.url:http://localhost:8083/api/notifications}") String url,
            @Value("${app.public-url:http://localhost:4200}") String publicUrl,
            @Value("${app.admin-email:admin@velvet.mx}") String adminEmail) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.client = RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .build();
        this.publicUrl = publicUrl;
        this.adminEmail = adminEmail;
    }

    public void solicitudCreada(Request solicitud) {
        String cuerpo = """
                Nueva solicitud %s.

                Cliente: %s <%s>
                Evento: %s el %s%s
                Lugar: %s
                Invitados: %d | Duracion: %d h | Nivel: %s
                Total: $%s

                Seguimiento: %s
                """.formatted(
                solicitud.getFolio(),
                solicitud.getClientName(),
                valor(solicitud.getClientEmail(), "sin correo"),
                solicitud.getEventType().getName(),
                solicitud.getEventDate(),
                solicitud.getEventTime() == null ? "" : " " + solicitud.getEventTime(),
                valor(solicitud.getLocation(), "sin lugar"),
                solicitud.getGuests(),
                solicitud.getDurationHours(),
                solicitud.getLevel(),
                solicitud.getTotalAmount().toPlainString(),
                enlace(solicitud));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "SOLICITUD_CREADA");
        payload.put("channel", "EMAIL");
        payload.put("recipient", adminEmail);
        payload.put("subject", "Nueva solicitud " + solicitud.getFolio());
        payload.put("body", cuerpo);
        payload.put("referenceId", solicitud.getId());
        despuesDeConfirmar(() -> enviar(payload));
    }

    public void estadoCambiado(Request solicitud, RequestStatus anterior, RequestStatus nuevo) {
        String destino = valor(solicitud.getClientEmail(), adminEmail);
        String cuerpo = """
                Hola %s,

                tu solicitud %s cambio de estado.

                Antes: %s
                Ahora: %s
                Evento: %s el %s
                Total: $%s

                Seguimiento: %s
                """.formatted(
                solicitud.getClientName(),
                solicitud.getFolio(),
                anterior,
                nuevo,
                solicitud.getEventType().getName(),
                solicitud.getEventDate(),
                solicitud.getTotalAmount().toPlainString(),
                enlace(solicitud));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "ESTADO_CAMBIADO");
        payload.put("channel", "EMAIL");
        payload.put("recipient", destino);
        payload.put("subject", "Tu solicitud " + solicitud.getFolio() + " ahora esta " + etiqueta(nuevo));
        payload.put("body", cuerpo);
        payload.put("referenceId", solicitud.getId());
        despuesDeConfirmar(() -> enviar(payload));
    }

    private void enviar(Map<String, Object> payload) {
        try {
            client.post()
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            LOG.warn("No se pudo registrar la notificacion de {}: {}",
                    payload.get("type"), ex.getMessage());
        }
    }

    private void despuesDeConfirmar(Runnable tarea) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    tarea.run();
                }
            });
        } else {
            tarea.run();
        }
    }

    private String enlace(Request solicitud) {
        return publicUrl + "/solicitud/" + solicitud.getPublicToken();
    }

    private String etiqueta(RequestStatus status) {
        return switch (status) {
            case CONFIRMADO -> "confirmada";
            case CANCELADO -> "cancelada";
            case PENDIENTE -> "pendiente";
        };
    }

    private String valor(String dato, String respaldo) {
        return dato == null || dato.isBlank() ? respaldo : dato;
    }
}
