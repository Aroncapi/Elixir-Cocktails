package com.barpro.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CocktailRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank String category,
        @Size(max = 30) String spirit,
        @NotBlank @Size(max = 2000) String ingredients,
        @Size(max = 500) String imageUrl,
        Boolean active,
        List<Long> eventTypeIds
) {
}
