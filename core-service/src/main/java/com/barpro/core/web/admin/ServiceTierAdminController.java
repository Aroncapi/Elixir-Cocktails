package com.barpro.core.web.admin;

import com.barpro.core.dto.ServiceTierRequest;
import com.barpro.core.dto.ServiceTierResponse;
import com.barpro.core.service.ServiceTierService;
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
@RequestMapping("/api/service-tiers")
@PreAuthorize("hasRole('ADMIN')")
public class ServiceTierAdminController {

    private final ServiceTierService serviceTierService;

    public ServiceTierAdminController(ServiceTierService serviceTierService) {
        this.serviceTierService = serviceTierService;
    }

    @GetMapping
    public List<ServiceTierResponse> list() {
        return serviceTierService.listAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceTierResponse create(@Valid @RequestBody ServiceTierRequest request) {
        return serviceTierService.create(request);
    }

    @PutMapping("/{id}")
    public ServiceTierResponse update(@PathVariable Long id, @Valid @RequestBody ServiceTierRequest request) {
        return serviceTierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ServiceTierResponse deactivate(@PathVariable Long id) {
        return serviceTierService.deactivate(id);
    }
}
