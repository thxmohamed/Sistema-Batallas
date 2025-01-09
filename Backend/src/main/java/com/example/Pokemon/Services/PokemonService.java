package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Repositories.AtaqueRepository;
import com.example.Pokemon.Repositories.EfectoRepository;
import com.example.Pokemon.Repositories.PokemonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class PokemonService {
    @Autowired
    PokemonRepository pokemonRepository;

    @Autowired
    AtaqueRepository ataqueRepository;

    @Autowired
    EfectoRepository efectoRepository;

    public List<Pokemon> getAllPokemon() {
        return pokemonRepository.findAll();
    }

    public Pokemon getPokemonById(Long id) {
        Optional<Pokemon> pokemon = pokemonRepository.findById(id);
        return pokemon.orElse(null);
    }

    public Pokemon createPokemon(Pokemon pokemon) {
        return pokemonRepository.save(pokemon);
    }

    public Long calcularDano(Pokemon atacante, Pokemon enemigo, Ataque golpe) {
        Long ataque = atacante.getAtaque();
        Long defensa = enemigo.getDefensa();
        long stab = 1L;
        long efectividad = 1L;

        String tipoPokemon = String.valueOf(atacante.getTipoPokemon());
        String tipoAtaque = String.valueOf(golpe.getTipoAtaque());

        if(tipoPokemon.equals(tipoAtaque)){
            stab = (long) 1.5;
        }

        Random random = new Random();
        int danoAleatorio = random.nextInt(31) -15;

        double danoBase = (double) (ataque * stab * efectividad) /(1 + ((double) defensa /30));
        long danoTotal = (long) Math.floor(danoBase + danoAleatorio);
        return Math.max(1,danoTotal);
    }

    public List<Ataque> getPokemonAtaques(Pokemon pokemon) {
        List<Ataque> ataques = new ArrayList<>();
        Optional<Ataque> ataque1 =  ataqueRepository.findById(pokemon.getIdAtaque1());
        Optional<Ataque> ataque2 =  ataqueRepository.findById(pokemon.getIdAtaque2());
        if(ataque1.isPresent() && ataque2.isPresent()){
            ataques.add(ataque1.get());
            ataques.add(ataque2.get());
            return ataques;
        }
        return ataques;
    }

    public Efecto getPokemonEfecto(Pokemon pokemon) {
        Optional<Efecto> efecto = efectoRepository.findById(pokemon.getIdEfecto());
        return efecto.orElse(null);
    }


}
