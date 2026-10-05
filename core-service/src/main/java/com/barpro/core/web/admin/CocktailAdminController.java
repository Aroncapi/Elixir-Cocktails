package com.barpro.core.web.admin;

import com.barpro.core.dto.CocktailRequest;
import com.barpro.core.dto.CocktailResponse;
import com.barpro.core.service.CocktailService;
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
@RequestMapping("/api/cocktails")
@PreAuthorize("hasRole('ADMIN')")
public class CocktailAdminController {

    private final CocktailService cocktailService;

    public CocktailAdminController(CocktailService cocktailService) {
        this.cocktailService = cocktailService;
    }

    @GetMapping
    public List<CocktailResponse> list() {
        return cocktailService.listAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CocktailResponse create(@Valid @RequestBody CocktailRequest request) {
        return cocktailService.create(request);
    }

    @PutMapping("/{id}")
    public CocktailResponse update(@PathVariable Long id, @Valid @RequestBody CocktailRequest request) {
        return cocktailService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public CocktailResponse deactivate(@PathVariable Long id) {
        return cocktailService.deactivate(id);
    }
}
