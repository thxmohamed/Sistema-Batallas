package com.example.Pokemon.Services;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.Entities.*;
import com.example.Pokemon.Repositories.BatallaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BatallaService {
    @Autowired
    BatallaRepository batallaRepository;

    @Autowired
    EntrenadorService entrenadorService;

    @Autowired
    PokemonService pokemonService;

    public List<Batalla> getBatallas() {
        return batallaRepository.findAll();
    }

    public Batalla getBatallaById(Long id) {
        Optional<Batalla> batalla = batallaRepository.findById(id);
        return batalla.orElse(null);
    }

    public Batalla createBatalla(Batalla batalla) {
        return batallaRepository.save(batalla);
    }

    private boolean esPrimeroAtacante1(Pokemon atacanteE1, Pokemon atacanteE2) {
        return atacanteE1.getVelocidad() > atacanteE2.getVelocidad();
    }

    private boolean estaDebilitado(Pokemon pokemon){
        return pokemon.getVida() == 0;
    }

    private void actualizarPokemon(List<Pokemon> equipo, int posicion, Pokemon actualizado) {
        equipo.set(posicion, actualizado);
    }

    public BatallaDTO combatir(BatallaDTO batalla,
                                         int posicionE1,
                                         int posicionE2,
                                         int posicionE3,
                                         int posicionE4) {
        // E1 ataca a E2, E3 ataca a E4
        List<Pokemon> entrenador1 = batalla.getEntrenador1();
        List<Pokemon> entrenador2 = batalla.getEntrenador2();

        Pokemon atacanteE1 = entrenador1.get(posicionE1);
        Pokemon agredidoE2 = entrenador2.get(posicionE2);

        Pokemon atacanteE2 = entrenador2.get(posicionE3);
        Pokemon agredidoE1 = entrenador1.get(posicionE4);

        Ataque ataqueE1 = batalla.getataqueE1();
        Ataque ataqueE2 = batalla.getataqueE2();

        Long velocidadAtacante1 = atacanteE1.getVelocidad();
        Long velocidadAtacante2 = atacanteE2.getVelocidad();

        if (esPrimeroAtacante1(atacanteE1, atacanteE2)) {
            agredidoE2 = pokemonService.atacar(atacanteE1, agredidoE2, ataqueE1);
            if (estaDebilitado(agredidoE2)) {
                System.out.println(agredidoE2.getNombre() + " ha sido debilitado.");
            } else {
                agredidoE1 = pokemonService.atacar(atacanteE2, agredidoE1, ataqueE2);
            }
        } else {
            agredidoE1 = pokemonService.atacar(atacanteE2, agredidoE1, ataqueE2);
            if (estaDebilitado(agredidoE1)) {
                System.out.println(agredidoE1.getNombre() + " ha sido debilitado.");
            } else {
                agredidoE2 = pokemonService.atacar(atacanteE1, agredidoE2, ataqueE1);
            }
        }

        //actualizar equipos
        actualizarPokemon(entrenador1, posicionE4, agredidoE1);
        actualizarPokemon(entrenador2, posicionE2, agredidoE2);

        // Actualizar el DTO
        batalla.setEntrenador1(entrenador1);
        batalla.setEntrenador2(entrenador2);
        batalla.setataqueE1(null);
        batalla.setataqueE2(null);
        batalla.setTurno(batalla.getTurno() + 1);

        return batalla;
    }

}
