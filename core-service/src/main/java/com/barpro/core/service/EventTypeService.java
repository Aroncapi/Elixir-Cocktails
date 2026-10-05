package com.barpro.core.service;

import com.barpro.core.dto.EventTypeRequest;
import com.barpro.core.dto.EventTypeResponse;
import com.barpro.core.entity.EventType;
import com.barpro.core.error.ConflictException;
import com.barpro.core.error.NotFoundException;
import com.barpro.core.repository.EventTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventTypeService {

    private final EventTypeRepository eventTypes;

    public EventTypeService(EventTypeRepository eventTypes) {
        this.eventTypes = eventTypes;
    }

    public List<EventTypeResponse> listAdmin() {
        return eventTypes.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(EventTypeResponse::from)
                .toList();
    }

    public List<EventTypeResponse> listPublic() {
        return eventTypes.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(EventTypeResponse::from)
                .toList();
    }

    @Transactional
    public EventTypeResponse create(EventTypeRequest request) {
        if (eventTypes.existsByCodeIgnoreCase(request.code())) {
            throw new ConflictException("Ya existe un tipo de evento con el codigo " + request.code());
        }
        EventType eventType = new EventType();
        apply(eventType, request);
        return EventTypeResponse.from(eventTypes.save(eventType));
    }

    @Transactional
    public EventTypeResponse update(Long id, EventTypeRequest request) {
        EventType eventType = eventTypes.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de evento no encontrado: " + id));
        eventTypes.findByCodeIgnoreCase(request.code())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Ya existe un tipo de evento con el codigo " + request.code());
                });
        apply(eventType, request);
        return EventTypeResponse.from(eventTypes.save(eventType));
    }

    @Transactional
    public EventTypeResponse deactivate(Long id) {
        EventType eventType = eventTypes.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de evento no encontrado: " + id));
        eventType.setActive(false);
        return EventTypeResponse.from(eventTypes.save(eventType));
    }

    private void apply(EventType eventType, EventTypeRequest request) {
        eventType.setCode(request.code().trim().toUpperCase());
        eventType.setName(request.name().trim());
        eventType.setDescription(request.description());
        eventType.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        eventType.setActive(request.active() == null || request.active());
    }
}
