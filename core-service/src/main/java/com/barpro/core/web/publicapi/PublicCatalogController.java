package com.barpro.core.web.publicapi;

import com.barpro.core.dto.CocktailResponse;
import com.barpro.core.dto.EventTypeResponse;
import com.barpro.core.dto.ServiceTierResponse;
import com.barpro.core.service.CocktailService;
import com.barpro.core.service.EventTypeService;
import com.barpro.core.service.ServiceTierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicCatalogController {

    private final CocktailService cocktailService;
    private final EventTypeService eventTypeService;
    private final ServiceTierService serviceTierService;

    public PublicCatalogController(CocktailService cocktailService,
                                   EventTypeService eventTypeService,
                                   ServiceTierService serviceTierService) {
        this.cocktailService = cocktailService;
        this.eventTypeService = eventTypeService;
        this.serviceTierService = serviceTierService;
    }

    @GetMapping("/cocktails")
    public List<CocktailResponse> cocktails(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        return cocktailService.searchPublic(category, search);
    }

    @GetMapping("/event-types")
    public List<EventTypeResponse> eventTypes() {
        return eventTypeService.listPublic();
    }

    @GetMapping("/service-tiers")
    public List<ServiceTierResponse> serviceTiers() {
        return serviceTierService.listPublic();
    }
}
