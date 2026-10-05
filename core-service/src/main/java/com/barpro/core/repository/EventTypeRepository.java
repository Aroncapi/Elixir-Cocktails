package com.barpro.core.repository;

import com.barpro.core.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {

    List<EventType> findByActiveTrue();

    List<EventType> findByActiveTrueOrderBySortOrderAsc();

    Optional<EventType> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
