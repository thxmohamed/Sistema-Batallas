package com.example.Pokemon.Repositories;

import com.example.Pokemon.Entities.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
    List<Pokemon> findByTipoPokemon(Pokemon.TipoPokemon tipoPokemon);
}
