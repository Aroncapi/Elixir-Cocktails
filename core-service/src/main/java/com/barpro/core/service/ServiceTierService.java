package com.barpro.core.service;

import com.barpro.core.dto.ServiceTierRequest;
import com.barpro.core.dto.ServiceTierResponse;
import com.barpro.core.entity.ServiceTier;
import com.barpro.core.entity.TierType;
import com.barpro.core.error.ConflictException;
import com.barpro.core.error.NotFoundException;
import com.barpro.core.repository.ServiceTierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceTierService {

    private final ServiceTierRepository tiers;

    public ServiceTierService(ServiceTierRepository tiers) {
        this.tiers = tiers;
    }

    public List<ServiceTierResponse> listAdmin() {
        return tiers.findAll().stream().map(ServiceTierResponse::from).toList();
    }

    public List<ServiceTierResponse> listPublic() {
        return tiers.findByActiveTrue().stream().map(ServiceTierResponse::from).toList();
    }

    @Transactional
    public ServiceTierResponse create(ServiceTierRequest request) {
        if (tiers.existsByCodeIgnoreCase(request.code())) {
            throw new ConflictException("Ya existe un tier con el codigo " + request.code());
        }
        ServiceTier tier = new ServiceTier();
        apply(tier, request);
        return ServiceTierResponse.from(tiers.save(tier));
    }

    @Transactional
    public ServiceTierResponse update(Long id, ServiceTierRequest request) {
        ServiceTier tier = tiers.findById(id)
                .orElseThrow(() -> new NotFoundException("Tier no encontrado: " + id));
        tiers.findByCodeIgnoreCase(request.code())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Ya existe un tier con el codigo " + request.code());
                });
        apply(tier, request);
        return ServiceTierResponse.from(tiers.save(tier));
    }

    @Transactional
    public ServiceTierResponse deactivate(Long id) {
        ServiceTier tier = tiers.findById(id)
                .orElseThrow(() -> new NotFoundException("Tier no encontrado: " + id));
        tier.setActive(false);
        return ServiceTierResponse.from(tiers.save(tier));
    }

    private void apply(ServiceTier tier, ServiceTierRequest request) {
        tier.setCode(request.code().trim().toUpperCase());
        tier.setName(request.name().trim());
        tier.setDescription(request.description());
        tier.setTierType(parseTierType(request.tierType()));
        tier.setPrice(request.price());
        tier.setActive(request.active() == null || request.active());
    }

    private TierType parseTierType(String raw) {
        try {
            return TierType.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Tier invalido: " + raw + " (use BASE, PREMIUM o PERSONAL)");
        }
    }
}
