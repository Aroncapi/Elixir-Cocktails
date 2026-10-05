package com.barpro.core.service;

import com.barpro.core.dto.ClientRequest;
import com.barpro.core.dto.ClientResponse;
import com.barpro.core.entity.Client;
import com.barpro.core.entity.ClientTier;
import com.barpro.core.error.ConflictException;
import com.barpro.core.error.NotFoundException;
import com.barpro.core.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ClientService {

    private final ClientRepository clients;

    public ClientService(ClientRepository clients) {
        this.clients = clients;
    }

    public List<ClientResponse> list(String search, String tier) {
        String needle = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String tierFilter = tier == null ? "" : tier.trim().toUpperCase(Locale.ROOT);
        return clients.findAll().stream()
                .filter(client -> tierFilter.isEmpty() || client.getTier().name().equals(tierFilter))
                .filter(client -> needle.isEmpty()
                        || contains(client.getName(), needle)
                        || contains(client.getEmail(), needle)
                        || contains(client.getLocation(), needle)
                        || contains(client.getLastEvent(), needle))
                .sorted(Comparator.comparing(Client::getCreatedAt).reversed())
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        validateEmailIsFree(request.email(), null);
        Client client = new Client();
        apply(client, request);
        return ClientResponse.from(clients.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findById(id);
        validateEmailIsFree(request.email(), id);
        apply(client, request);
        return ClientResponse.from(clients.save(client));
    }

    @Transactional
    public ClientResponse deactivate(Long id) {
        Client client = findById(id);
        client.setActive(false);
        return ClientResponse.from(clients.save(client));
    }

    private Client findById(Long id) {
        return clients.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + id));
    }

    private void validateEmailIsFree(String email, Long ignoreId) {
        if (email == null || email.isBlank()) {
            return;
        }
        clients.findByEmailIgnoreCase(email.trim())
                .filter(other -> ignoreId == null || !other.getId().equals(ignoreId))
                .ifPresent(other -> {
                    throw new ConflictException("Ya existe un cliente con el correo " + email);
                });
    }

    private void apply(Client client, ClientRequest request) {
        client.setName(request.name().trim());
        client.setEmail(blankToNull(request.email()));
        client.setWhatsapp(blankToNull(request.whatsapp()));
        client.setLocation(blankToNull(request.location()));
        client.setTier(parseTier(request.tier()));
        client.setTotalSpent(request.totalSpent() == null ? BigDecimal.ZERO : request.totalSpent());
        if (request.active() != null) {
            client.setActive(request.active());
        }
    }

    private ClientTier parseTier(String raw) {
        try {
            return ClientTier.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Tier invalido: " + raw + " (use VIP, CORPORATIVO o PARTICULAR)");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean contains(String source, String needle) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(needle);
    }
}
