package com.barpro.core.service;

import com.barpro.core.dto.RequestEventoResponse;
import com.barpro.core.dto.RequestResumenResponse;
import com.barpro.core.dto.SolicitudPageResponse;
import com.barpro.core.dto.SolicitudRequest;
import com.barpro.core.dto.SolicitudResponse;
import com.barpro.core.entity.Client;
import com.barpro.core.entity.ClientTier;
import com.barpro.core.entity.Cocktail;
import com.barpro.core.entity.EventType;
import com.barpro.core.entity.Request;
import com.barpro.core.entity.RequestLevel;
import com.barpro.core.entity.RequestStatus;
import com.barpro.core.entity.ServiceTier;
import com.barpro.core.error.ConflictException;
import com.barpro.core.error.NotFoundException;
import com.barpro.core.repository.ClientRepository;
import com.barpro.core.repository.CocktailRepository;
import com.barpro.core.repository.EventTypeRepository;
import com.barpro.core.repository.RequestRepository;
import com.barpro.core.repository.ServiceTierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SolicitudService {

    private static final Map<RequestStatus, Set<RequestStatus>> TRANSICIONES =
            new EnumMap<>(RequestStatus.class);

    static {
        TRANSICIONES.put(RequestStatus.PENDIENTE,
                EnumSet.of(RequestStatus.CONFIRMADO, RequestStatus.CANCELADO));
        TRANSICIONES.put(RequestStatus.CONFIRMADO,
                EnumSet.of(RequestStatus.PENDIENTE, RequestStatus.CANCELADO));
        TRANSICIONES.put(RequestStatus.CANCELADO, EnumSet.of(RequestStatus.PENDIENTE));
    }


    private final RequestRepository requests;
    private final EventTypeRepository eventTypes;
    private final CocktailRepository cocktails;
    private final ServiceTierRepository tiers;
    private final ClientRepository clients;
    private final NotificationClient notificaciones;

    public SolicitudService(RequestRepository requests,
                            EventTypeRepository eventTypes,
                            CocktailRepository cocktails,
                            ServiceTierRepository tiers,
                            ClientRepository clients,
                            NotificationClient notificaciones) {
        this.requests = requests;
        this.eventTypes = eventTypes;
        this.cocktails = cocktails;
        this.tiers = tiers;
        this.clients = clients;
        this.notificaciones = notificaciones;
    }

    @Transactional
    public SolicitudResponse create(SolicitudRequest request) {
        EventType eventType = eventTypes.findById(request.eventTypeId())
                .orElseThrow(() -> new NotFoundException("Tipo de evento no encontrado: " + request.eventTypeId()));
        if (!eventType.isActive()) {
            throw new IllegalArgumentException("El tipo de evento ya no esta disponible");
        }

        RequestLevel level = parseLevel(request.level());
        LinkedHashSet<Cocktail> seleccion = resolveCocktails(request.cocktailIds());

        Request solicitud = new Request();
        solicitud.setEventType(eventType);
        solicitud.setEventDate(LocalDate.parse(request.eventDate()));
        solicitud.setEventTime(blankToNull(request.eventTime()) == null
                ? null
                : LocalTime.parse(request.eventTime()));
        solicitud.setGuests(request.guests());
        solicitud.setDurationHours(request.durationHours());
        solicitud.setLocation(blankToNull(request.location()));
        solicitud.setNotes(blankToNull(request.notes()));
        solicitud.setLevel(level);
        solicitud.setExtraBartenders(request.extraBartenders() == null ? 0 : request.extraBartenders());
        solicitud.setCocktails(seleccion);
        solicitud.setClientName(request.clientName().trim());
        solicitud.setClientEmail(request.clientEmail().trim());
        solicitud.setClientWhatsapp(request.clientWhatsapp().trim());
        solicitud.setClient(upsertClient(request));
        solicitud.setPublicToken(UUID.randomUUID().toString().replace("-", ""));
        solicitud.setFolio("TMP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        price(solicitud);

        Request guardada = requests.save(solicitud);
        guardada.setFolio("REQ-" + String.format("%03d", guardada.getId()));
        Request confirmada = requests.save(guardada);
        notificaciones.solicitudCreada(confirmada);
        return SolicitudResponse.from(confirmada);
    }

    public SolicitudResponse getByToken(String token) {
        Request solicitud = requests.findByPublicToken(token)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada"));
        return SolicitudResponse.from(solicitud);
    }

    @Transactional(readOnly = true)
    public SolicitudPageResponse pageAdmin(String status, String search, int page, int size) {
        RequestStatus estado = status == null || status.isBlank() ? null : parseStatus(status);
        String needle = search == null || search.isBlank()
                ? null
                : "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100));
        Page<Request> resultado = requests.search(estado, needle, pageable);
        return new SolicitudPageResponse(
                resultado.getContent().stream().map(SolicitudResponse::from).toList(),
                resultado.getTotalElements(),
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalPages());
    }

    @Transactional(readOnly = true)
    public RequestResumenResponse resumen() {
        LocalDate hoy = LocalDate.now();
        BigDecimal ingresos = requests.sumTotalAmountExcluding(RequestStatus.CANCELADO);
        return new RequestResumenResponse(
                requests.count(),
                ingresos == null ? BigDecimal.ZERO : ingresos,
                requests.countByStatusExcludingAndEventDateBetween(
                        RequestStatus.CANCELADO, hoy, hoy.plusDays(7)),
                requests.countByStatus(RequestStatus.PENDIENTE),
                requests.countByStatus(RequestStatus.CONFIRMADO),
                requests.countByStatus(RequestStatus.CANCELADO));
    }

    @Transactional(readOnly = true)
    public List<RequestEventoResponse> eventos(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde == null ? LocalDate.now() : desde;
        LocalDate fin = hasta == null ? inicio.plusMonths(1) : hasta;
        if (fin.isBefore(inicio)) {
            LocalDate auxiliar = inicio;
            inicio = fin;
            fin = auxiliar;
        }
        return requests.findByEventDateBetween(inicio, fin).stream()
                .map(RequestEventoResponse::from)
                .toList();
    }

    public SolicitudResponse getAdmin(Long id) {
        return SolicitudResponse.from(findById(id));
    }

    @Transactional
    public SolicitudResponse updateStatus(Long id, String statusRaw) {
        Request solicitud = findById(id);
        RequestStatus nuevo = parseStatus(statusRaw);
        RequestStatus anterior = solicitud.getStatus();
        if (anterior == nuevo) {
            return SolicitudResponse.from(solicitud);
        }
        if (!TRANSICIONES.getOrDefault(anterior, Set.of()).contains(nuevo)) {
            throw new ConflictException(
                    "Transicion no permitida: " + anterior + " -> " + nuevo);
        }

        solicitud.setStatus(nuevo);
        Client client = solicitud.getClient();
        if (client != null) {
            if (nuevo == RequestStatus.CONFIRMADO && anterior != RequestStatus.CONFIRMADO) {
                client.setTotalSpent(client.getTotalSpent().add(solicitud.getTotalAmount()));
                client.setReservations(client.getReservations() + 1);
                client.setLastEvent(descripcionEvento(solicitud));
            } else if (anterior == RequestStatus.CONFIRMADO && nuevo != RequestStatus.CONFIRMADO) {
                client.setTotalSpent(client.getTotalSpent().subtract(solicitud.getTotalAmount()));
                client.setReservations(Math.max(0, client.getReservations() - 1));
                client.setLastEvent(ultimoEventoConfirmado(client, solicitud));
            }
            clients.save(client);
        }
        Request actualizada = requests.save(solicitud);
        notificaciones.estadoCambiado(actualizada, anterior, nuevo);
        return SolicitudResponse.from(actualizada);
    }

    private String ultimoEventoConfirmado(Client client, Request excluir) {
        return requests.findByClientAndStatusOrderByEventDateDesc(client, RequestStatus.CONFIRMADO).stream()
                .filter(otra -> otra.getId() == null || !otra.getId().equals(excluir.getId()))
                .findFirst()
                .map(this::descripcionEvento)
                .orElse(null);
    }

    private Request findById(Long id) {
        return requests.findById(id)
                .orElseThrow(() -> new NotFoundException("Solicitud no encontrada: " + id));
    }

    private LinkedHashSet<Cocktail> resolveCocktails(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos un coctel para la carta del evento");
        }
        LinkedHashSet<Long> unicos = new LinkedHashSet<>(ids);
        List<Cocktail> encontrados = cocktails.findAllById(unicos);
        if (encontrados.size() != unicos.size()) {
            throw new IllegalArgumentException("Uno o mas cocteles no existen");
        }
        return new LinkedHashSet<>(encontrados);
    }

    private Client upsertClient(SolicitudRequest request) {
        String email = request.clientEmail().trim();
        return clients.findByEmailIgnoreCase(email)
                .map(client -> {
                    client.setName(request.clientName().trim());
                    client.setWhatsapp(request.clientWhatsapp().trim());
                    if (blankToNull(request.location()) != null) {
                        client.setLocation(request.location().trim());
                    }
                    if (!client.isActive()) {
                        client.setActive(true);
                    }
                    return clients.save(client);
                })
                .orElseGet(() -> {
                    Client client = new Client();
                    client.setName(request.clientName().trim());
                    client.setEmail(email);
                    client.setWhatsapp(request.clientWhatsapp().trim());
                    client.setLocation(blankToNull(request.location()));
                    client.setTier(ClientTier.PARTICULAR);
                    return clients.save(client);
                });
    }

    private void price(Request solicitud) {
        BigDecimal porPersonaHora = priceOf("BASE");
        BigDecimal porPersona = priceOf("PREMIUM");
        BigDecimal porBartenderHora = priceOf("PERSONAL");

        BigDecimal base = porPersonaHora
                .multiply(BigDecimal.valueOf(solicitud.getGuests()))
                .multiply(BigDecimal.valueOf(solicitud.getDurationHours()));
        BigDecimal premium = solicitud.getLevel() == RequestLevel.PREMIUM
                ? porPersona.multiply(BigDecimal.valueOf(solicitud.getGuests()))
                : BigDecimal.ZERO;
        BigDecimal personal = porBartenderHora
                .multiply(BigDecimal.valueOf(solicitud.getDurationHours()))
                .multiply(BigDecimal.valueOf(solicitud.getExtraBartenders()));

        solicitud.setBaseAmount(round(base));
        solicitud.setPremiumAmount(round(premium));
        solicitud.setPersonalAmount(round(personal));
        solicitud.setTotalAmount(round(base.add(premium).add(personal)));
    }

    private BigDecimal priceOf(String code) {
        return tiers.findByCodeIgnoreCase(code)
                .map(ServiceTier::getPrice)
                .filter(precio -> precio != null)
                .orElse(BigDecimal.ZERO);
    }

    private String descripcionEvento(Request solicitud) {
        String nombre = solicitud.getEventType().getName();
        if (solicitud.getEventDate() == null) {
            return nombre;
        }
        String mes = solicitud.getEventDate()
                .format(DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es", "MX")));
        return nombre + " (" + mes + ")";
    }

    private RequestLevel parseLevel(String raw) {
        try {
            return RequestLevel.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Nivel invalido: " + raw + " (use BASE o PREMIUM)");
        }
    }

    private RequestStatus parseStatus(String raw) {
        try {
            return RequestStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Estado invalido: " + raw + " (use PENDIENTE, CONFIRMADO o CANCELADO)");
        }
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
