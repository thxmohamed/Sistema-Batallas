package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Entrenador;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Repositories.EntrenadorRepository;
import com.example.Pokemon.Repositories.PokemonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EntrenadorService {
    @Autowired
    EntrenadorRepository entrenadorRepository;

    @Autowired
    PokemonRepository pokemonRepository;

    public List<Entrenador> findAll() {
        return entrenadorRepository.findAll();
    }
    public Entrenador findById(Long id) {
        Optional<Entrenador> entrenador = entrenadorRepository.findById(id);
        return entrenador.orElse(null);
    }

    public Entrenador createEntrenador(Entrenador entrenador) {
        return entrenadorRepository.save(entrenador);
    }

    public List<Pokemon> getPokemonByEntrenador(Entrenador entrenador) {
        Optional<Pokemon> pokemon1 = pokemonRepository.findById(entrenador.getIdPokemon1());
        Optional<Pokemon> pokemon2 = pokemonRepository.findById(entrenador.getIdPokemon2());
        Optional<Pokemon> pokemon3 = pokemonRepository.findById(entrenador.getIdPokemon3());

        List<Pokemon> pokemons = new ArrayList<>();
        pokemon1.ifPresent(pokemons::add);
        pokemon2.ifPresent(pokemons::add);
        pokemon3.ifPresent(pokemons::add);
        return pokemons;

    }
}
