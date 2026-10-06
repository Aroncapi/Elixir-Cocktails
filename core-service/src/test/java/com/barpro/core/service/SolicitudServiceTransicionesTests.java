package com.barpro.core.service;

import com.barpro.core.entity.Client;
import com.barpro.core.entity.EventType;
import com.barpro.core.entity.Request;
import com.barpro.core.entity.RequestLevel;
import com.barpro.core.entity.RequestStatus;
import com.barpro.core.error.ConflictException;
import com.barpro.core.repository.ClientRepository;
import com.barpro.core.repository.CocktailRepository;
import com.barpro.core.repository.EventTypeRepository;
import com.barpro.core.repository.RequestRepository;
import com.barpro.core.repository.ServiceTierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTransicionesTests {

    @Mock
    private RequestRepository requests;
    @Mock
    private EventTypeRepository eventTypes;
    @Mock
    private CocktailRepository cocktails;
    @Mock
    private ServiceTierRepository tiers;
    @Mock
    private ClientRepository clients;
    @Mock
    private NotificationClient notificaciones;
    @InjectMocks
    private SolicitudService service;

    @Test
    void una_solicitud_cancelada_no_puede_volver_a_confirmada() {
        Request solicitud = solicitud(RequestStatus.CANCELADO, cliente());
        when(requests.findById(1L)).thenReturn(Optional.of(solicitud));

        assertThrows(ConflictException.class, () -> service.updateStatus(1L, "CONFIRMADO"));

        assertEquals(RequestStatus.CANCELADO, solicitud.getStatus());
        verify(notificaciones, never()).estadoCambiado(any(), any(), any());
    }

    @Test
    void una_solicitud_pendiente_si_puede_confirmarse() {
        Client client = cliente();
        Request solicitud = solicitud(RequestStatus.PENDIENTE, client);
        when(requests.findById(1L)).thenReturn(Optional.of(solicitud));
        when(clients.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requests.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateStatus(1L, "CONFIRMADO");

        assertEquals(0, new BigDecimal("1500.00").compareTo(client.getTotalSpent()));
        assertEquals(1, client.getReservations());
        assertNotNull(client.getLastEvent());
        verify(notificaciones).estadoCambiado(solicitud, RequestStatus.PENDIENTE, RequestStatus.CONFIRMADO);
    }

    @Test
    void cancelar_lo_que_estaba_confirmado_revierte_al_cliente() {
        Client client = cliente();
        client.setTotalSpent(new BigDecimal("1500.00"));
        client.setReservations(1);
        client.setLastEvent("Bodas (nov 2026)");
        Request solicitud = solicitud(RequestStatus.CONFIRMADO, client);
        when(requests.findById(1L)).thenReturn(Optional.of(solicitud));
        when(requests.findByClientAndStatusOrderByEventDateDesc(any(), any())).thenReturn(List.of());
        when(clients.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requests.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateStatus(1L, "CANCELADO");

        assertEquals(0, BigDecimal.ZERO.compareTo(client.getTotalSpent()));
        assertEquals(0, client.getReservations());
        assertNull(client.getLastEvent());
    }

    @Test
    void un_estado_desconocido_no_modifica_nada() {
        Request solicitud = solicitud(RequestStatus.PENDIENTE, cliente());
        when(requests.findById(1L)).thenReturn(Optional.of(solicitud));

        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, "NADA"));

        assertEquals(RequestStatus.PENDIENTE, solicitud.getStatus());
        verify(notificaciones, never()).estadoCambiado(any(), any(), any());
    }

    private Client cliente() {
        Client client = new Client();
        client.setId(9L);
        client.setName("Ana Lopez");
        client.setEmail("ana@ejemplo.mx");
        client.setWhatsapp("+525512345678");
        client.setTotalSpent(BigDecimal.ZERO);
        client.setReservations(0);
        return client;
    }

    private Request solicitud(RequestStatus estado, Client client) {
        EventType tipo = new EventType();
        tipo.setId(1L);
        tipo.setCode("BODA");
        tipo.setName("Bodas");
        tipo.setActive(true);

        Request solicitud = new Request();
        solicitud.setId(1L);
        solicitud.setFolio("REQ-001");
        solicitud.setPublicToken("token123");
        solicitud.setEventType(tipo);
        solicitud.setEventDate(LocalDate.of(2026, 11, 20));
        solicitud.setGuests(50);
        solicitud.setDurationHours(4);
        solicitud.setLevel(RequestLevel.BASE);
        solicitud.setExtraBartenders(1);
        solicitud.setBaseAmount(new BigDecimal("1500.00"));
        solicitud.setPremiumAmount(BigDecimal.ZERO);
        solicitud.setPersonalAmount(BigDecimal.ZERO);
        solicitud.setTotalAmount(new BigDecimal("1500.00"));
        solicitud.setStatus(estado);
        solicitud.setClientName("Ana Lopez");
        solicitud.setClientEmail("ana@ejemplo.mx");
        solicitud.setClientWhatsapp("+525512345678");
        solicitud.setClient(client);
        return solicitud;
    }
}
