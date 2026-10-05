package com.barpro.core.web.admin;

import com.barpro.core.dto.EventTypeRequest;
import com.barpro.core.dto.EventTypeResponse;
import com.barpro.core.service.EventTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event-types")
@PreAuthorize("hasRole('ADMIN')")
public class EventTypeAdminController {

    private final EventTypeService eventTypeService;

    public EventTypeAdminController(EventTypeService eventTypeService) {
        this.eventTypeService = eventTypeService;
    }

    @GetMapping
    public List<EventTypeResponse> list() {
        return eventTypeService.listAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventTypeResponse create(@Valid @RequestBody EventTypeRequest request) {
        return eventTypeService.create(request);
    }

    @PutMapping("/{id}")
    public EventTypeResponse update(@PathVariable Long id, @Valid @RequestBody EventTypeRequest request) {
        return eventTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public EventTypeResponse deactivate(@PathVariable Long id) {
        return eventTypeService.deactivate(id);
    }
}
