package com.example.Pokemon.Services;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.Entities.*;
import com.example.Pokemon.Repositories.BatallaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private boolean esPrimeroEntrenador1(int turno) {
        return turno % 2 == 1; // Turnos impares = Entrenador 1, Turnos pares = Entrenador 2
    }

    private boolean estaDebilitado(Pokemon pokemon){
        return pokemon.getVida() == 0;
    }

    private void actualizarPokemon(List<Pokemon> equipo, int posicion, Pokemon actualizado) {
        equipo.set(posicion, actualizado);
    }

    private boolean efectoRequiereRival(Efecto efecto) {
        String tipoEfecto = String.valueOf(efecto.getTipoEfecto());
        return tipoEfecto.equals("DANO_CONTINUO") || 
               tipoEfecto.equals("BAJAR_ATAQUE_RIVAL") || 
               tipoEfecto.equals("BAJAR_DEFENSA_RIVAL");
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

        int turnoActual = batalla.getTurno();

        if (esPrimeroEntrenador1(turnoActual)) {
            // Entrenador 1 ataca primero
            if (batalla.isUsarEfectoE1()) {
                // Usar efecto
                Efecto efectoE1 = batalla.getEfectoE1();
                Pokemon pokemonAfectado = pokemonService.aplicarEfecto(atacanteE1, agredidoE2, efectoE1);
                // El efecto puede afectar al usuario o al rival dependiendo del tipo
                if (efectoRequiereRival(efectoE1)) {
                    actualizarPokemon(entrenador2, posicionE2, pokemonAfectado);
                } else {
                    actualizarPokemon(entrenador1, posicionE1, pokemonAfectado);
                }
            } else {
                // Usar ataque
                agredidoE2 = pokemonService.atacar(atacanteE1, agredidoE2, ataqueE1);
                actualizarPokemon(entrenador2, posicionE2, agredidoE2);
            }
            
            // Si el pokemon objetivo no está debilitado, el entrenador 2 puede atacar
            if (!estaDebilitado(agredidoE2)) {
                if (batalla.isUsarEfectoE2()) {
                    // Usar efecto
                    Efecto efectoE2 = batalla.getEfectoE2();
                    Pokemon pokemonAfectado = pokemonService.aplicarEfecto(atacanteE2, agredidoE1, efectoE2);
                    if (efectoRequiereRival(efectoE2)) {
                        actualizarPokemon(entrenador1, posicionE4, pokemonAfectado);
                    } else {
                        actualizarPokemon(entrenador2, posicionE3, pokemonAfectado);
                    }
                } else {
                    // Usar ataque
                    agredidoE1 = pokemonService.atacar(atacanteE2, agredidoE1, ataqueE2);
                    actualizarPokemon(entrenador1, posicionE4, agredidoE1);
                }
            }
        } else {
            // Entrenador 2 ataca primero
            if (batalla.isUsarEfectoE2()) {
                // Usar efecto
                Efecto efectoE2 = batalla.getEfectoE2();
                Pokemon pokemonAfectado = pokemonService.aplicarEfecto(atacanteE2, agredidoE1, efectoE2);
                if (efectoRequiereRival(efectoE2)) {
                    actualizarPokemon(entrenador1, posicionE4, pokemonAfectado);
                } else {
                    actualizarPokemon(entrenador2, posicionE3, pokemonAfectado);
                }
            } else {
                // Usar ataque
                agredidoE1 = pokemonService.atacar(atacanteE2, agredidoE1, ataqueE2);
                actualizarPokemon(entrenador1, posicionE4, agredidoE1);
            }
            
            // Si el pokemon objetivo no está debilitado, el entrenador 1 puede atacar
            if (!estaDebilitado(agredidoE1)) {
                if (batalla.isUsarEfectoE1()) {
                    // Usar efecto
                    Efecto efectoE1 = batalla.getEfectoE1();
                    Pokemon pokemonAfectado = pokemonService.aplicarEfecto(atacanteE1, agredidoE2, efectoE1);
                    if (efectoRequiereRival(efectoE1)) {
                        actualizarPokemon(entrenador2, posicionE2, pokemonAfectado);
                    } else {
                        actualizarPokemon(entrenador1, posicionE1, pokemonAfectado);
                    }
                } else {
                    // Usar ataque
                    agredidoE2 = pokemonService.atacar(atacanteE1, agredidoE2, ataqueE1);
                    actualizarPokemon(entrenador2, posicionE2, agredidoE2);
                }
            }
        }

        // Actualizar el DTO
        batalla.setEntrenador1(entrenador1);
        batalla.setEntrenador2(entrenador2);
        batalla.setataqueE1(null);
        batalla.setataqueE2(null);
        batalla.setEfectoE1(null);
        batalla.setEfectoE2(null);
        batalla.setUsarEfectoE1(false);
        batalla.setUsarEfectoE2(false);
        batalla.setTurno(batalla.getTurno() + 1);

        return batalla;
    }

}
