package com.barpro.notification.service;

import com.barpro.notification.dto.NotificationRequest;
import com.barpro.notification.dto.NotificationResponse;
import com.barpro.notification.entity.Notification;
import com.barpro.notification.entity.NotificationChannel;
import com.barpro.notification.entity.NotificationType;
import com.barpro.notification.error.NotFoundException;
import com.barpro.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private NotificationRepository notifications;
    @Mock
    private NotificationDispatcher dispatcher;
    @InjectMocks
    private NotificationService service;

    @Test
    void unaNotificacionValidaQuedaEnviada() throws Exception {
        when(dispatcher.dispatch(any())).thenReturn("CONSOLA");
        when(notifications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse respuesta = service.crear(solicitudCreada());

        assertEquals("ENVIADA", respuesta.status());
        assertEquals("CONSOLA", respuesta.transport());
        assertEquals("admin@velvet.mx", respuesta.recipient());
        assertNotNull(respuesta.sentAt());
    }

    @Test
    void siElTransportadorFallaQuedaRegistradaComoFallida() throws Exception {
        when(dispatcher.dispatch(any())).thenThrow(new IllegalStateException("sin transportador"));
        when(notifications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse respuesta = service.crear(solicitudCreada());

        assertEquals("FALLIDA", respuesta.status());
        assertNotNull(respuesta.error());
    }

    @Test
    void unTipoInvalidoSeRechaza() {
        NotificationRequest invalida = new NotificationRequest(
                "DEMASIADO", "EMAIL", "admin@velvet.mx", "asunto", "cuerpo", 1L);

        assertThrows(IllegalArgumentException.class, () -> service.crear(invalida));
    }

    @Test
    void reenviarUnaNotificacionInexistenteDa404() {
        when(notifications.findById(77L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.reenviar(77L));
    }

    @Test
    void reenviarReintentaElEnvio() throws Exception {
        Notification guardada = notificacion();
        guardada.setStatus(com.barpro.notification.entity.NotificationStatus.FALLIDA);
        guardada.setError("fallo anterior");
        when(notifications.findById(5L)).thenReturn(Optional.of(guardada));
        when(dispatcher.dispatch(any())).thenReturn("CONSOLA");
        when(notifications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse respuesta = service.reenviar(5L);

        assertEquals("ENVIADA", respuesta.status());
        assertNotNull(respuesta.transport());
        verify(dispatcher).dispatch(any());
    }

    @Test
    void listarSinFiltrosDevuelveTodo() {
        when(notifications.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(notificacion()));

        assertEquals(1, service.listar(null, "").size());
    }

    private Notification notificacion() {
        Notification notificacion = new Notification();
        notificacion.setId(5L);
        notificacion.setType(NotificationType.SOLICITUD_CREADA);
        notificacion.setChannel(NotificationChannel.EMAIL);
        notificacion.setRecipient("admin@velvet.mx");
        notificacion.setSubject("Nueva solicitud REQ-001");
        notificacion.setBody("Hay una solicitud nueva.");
        notificacion.setReferenceId(1L);
        return notificacion;
    }

    private NotificationRequest solicitudCreada() {
        return new NotificationRequest(
                "SOLICITUD_CREADA",
                "EMAIL",
                "admin@velvet.mx",
                "Nueva solicitud REQ-001",
                "Hay una solicitud nueva.",
                1L);
    }
}
