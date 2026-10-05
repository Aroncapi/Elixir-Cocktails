package com.barpro.core.repository;

import com.barpro.core.entity.ServiceTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceTierRepository extends JpaRepository<ServiceTier, Long> {

    List<ServiceTier> findByActiveTrue();

    Optional<ServiceTier> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
