package com.example.Pokemon.Repositories;

import com.example.Pokemon.Entities.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
}
