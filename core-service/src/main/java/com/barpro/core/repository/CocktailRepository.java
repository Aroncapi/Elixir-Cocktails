package com.barpro.core.repository;

import com.barpro.core.entity.Cocktail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CocktailRepository extends JpaRepository<Cocktail, Long> {

    List<Cocktail> findByActiveTrue();

    Optional<Cocktail> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
