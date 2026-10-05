package com.barpro.core.service;

import com.barpro.core.dto.CocktailRequest;
import com.barpro.core.dto.CocktailResponse;
import com.barpro.core.entity.Cocktail;
import com.barpro.core.entity.CocktailCategory;
import com.barpro.core.entity.EventType;
import com.barpro.core.error.ConflictException;
import com.barpro.core.error.NotFoundException;
import com.barpro.core.repository.CocktailRepository;
import com.barpro.core.repository.EventTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CocktailService {

    private final CocktailRepository cocktails;
    private final EventTypeRepository eventTypes;

    public CocktailService(CocktailRepository cocktails, EventTypeRepository eventTypes) {
        this.cocktails = cocktails;
        this.eventTypes = eventTypes;
    }

    public List<CocktailResponse> listAdmin() {
        return cocktails.findAll().stream().map(CocktailResponse::from).toList();
    }

    public List<CocktailResponse> searchPublic(String category, String search) {
        String needle = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return cocktails.findByActiveTrue().stream()
                .filter(cocktail -> category == null || category.isBlank()
                        || cocktail.getCategory().name().equalsIgnoreCase(category.trim()))
                .filter(cocktail -> needle.isEmpty()
                        || cocktail.getName().toLowerCase(Locale.ROOT).contains(needle)
                        || cocktail.getIngredients().toLowerCase(Locale.ROOT).contains(needle))
                .map(CocktailResponse::from)
                .toList();
    }

    @Transactional
    public CocktailResponse create(CocktailRequest request) {
        if (cocktails.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Ya existe un coctel con el nombre " + request.name());
        }
        Cocktail cocktail = new Cocktail();
        apply(cocktail, request);
        return CocktailResponse.from(cocktails.save(cocktail));
    }

    @Transactional
    public CocktailResponse update(Long id, CocktailRequest request) {
        Cocktail cocktail = cocktails.findById(id)
                .orElseThrow(() -> new NotFoundException("Coctel no encontrado: " + id));
        cocktails.findByNameIgnoreCase(request.name())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Ya existe un coctel con el nombre " + request.name());
                });
        apply(cocktail, request);
        return CocktailResponse.from(cocktails.save(cocktail));
    }

    @Transactional
    public CocktailResponse deactivate(Long id) {
        Cocktail cocktail = cocktails.findById(id)
                .orElseThrow(() -> new NotFoundException("Coctel no encontrado: " + id));
        cocktail.setActive(false);
        return CocktailResponse.from(cocktails.save(cocktail));
    }

    private void apply(Cocktail cocktail, CocktailRequest request) {
        cocktail.setName(request.name().trim());
        cocktail.setCategory(parseCategory(request.category()));
        cocktail.setIngredients(request.ingredients().trim());
        cocktail.setImageUrl(request.imageUrl());
        cocktail.setActive(request.active() == null || request.active());
        cocktail.setEventTypes(resolveEventTypes(request.eventTypeIds()));
    }

    private Set<EventType> resolveEventTypes(List<Long> eventTypeIds) {
        if (eventTypeIds == null || eventTypeIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<EventType> found = eventTypes.findAllById(eventTypeIds);
        if (found.size() != new LinkedHashSet<>(eventTypeIds).size()) {
            throw new IllegalArgumentException("Uno o mas tipos de evento no existen");
        }
        return new LinkedHashSet<>(found);
    }

    private CocktailCategory parseCategory(String raw) {
        try {
            return CocktailCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Categoria invalida: " + raw + " (use SIGNATURE, CLASICOS, CITRICOS o SIN_ALCOHOL)");
        }
    }
}
