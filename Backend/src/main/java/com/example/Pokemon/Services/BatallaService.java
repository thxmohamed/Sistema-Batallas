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

    @Autowired
    EfectoService efectoService;

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
                             int posicionAtacante,
                             int posicionReceptor) {
        List<Pokemon> entrenador1 = batalla.getEntrenador1();
        List<Pokemon> entrenador2 = batalla.getEntrenador2();
        
        int turnoActual = batalla.getTurno();
        boolean esEquipo1 = esPrimeroEntrenador1(turnoActual);
        
        // PRIMERO: Inicializar estadísticas base SOLO si no existen
        for (Pokemon p : entrenador1) {
            p.inicializarSoloEstadisticasBase();
        }
        for (Pokemon p : entrenador2) {
            p.inicializarSoloEstadisticasBase();
        }
        
        // SEGUNDO: APLICAR DAÑO CONTINUO AL EQUIPO QUE VA A ATACAR (ANTES DE TODO)
        if (esEquipo1 && batalla.getEfectoContinuoEquipo1() != null && batalla.getTurnosRestantesEquipo1() > 0) {
            // El equipo 1 tiene efecto continuo activo y le toca atacar
            try {
                Efecto efecto = efectoService.findEfectoById(batalla.getEfectoContinuoEquipo1());
                if (efecto != null && efecto.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                    System.out.println("Aplicando daño continuo al equipo 1 antes de su turno");
                    pokemonService.aplicarDanoContinuoEquipo(entrenador1, efecto);
                }
                
                // Reducir turnos restantes
                batalla.setTurnosRestantesEquipo1(batalla.getTurnosRestantesEquipo1() - 1);
                
                // Si no quedan turnos, limpiar el efecto
                if (batalla.getTurnosRestantesEquipo1() <= 0) {
                    batalla.setEfectoContinuoEquipo1(null);
                }
            } catch (Exception e) {
                System.err.println("Error aplicando daño continuo al equipo 1: " + e.getMessage());
            }
        }
        
        if (!esEquipo1 && batalla.getEfectoContinuoEquipo2() != null && batalla.getTurnosRestantesEquipo2() > 0) {
            // El equipo 2 tiene efecto continuo activo y le toca atacar
            try {
                Efecto efecto = efectoService.findEfectoById(batalla.getEfectoContinuoEquipo2());
                if (efecto != null && efecto.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                    System.out.println("Aplicando daño continuo al equipo 2 antes de su turno");
                    pokemonService.aplicarDanoContinuoEquipo(entrenador2, efecto);
                }
                
                // Reducir turnos restantes
                batalla.setTurnosRestantesEquipo2(batalla.getTurnosRestantesEquipo2() - 1);
                
                // Si no quedan turnos, limpiar el efecto
                if (batalla.getTurnosRestantesEquipo2() <= 0) {
                    batalla.setEfectoContinuoEquipo2(null);
                }
            } catch (Exception e) {
                System.err.println("Error aplicando daño continuo al equipo 2: " + e.getMessage());
            }
        }
        
        // TERCERO: VERIFICAR SI ALGUIEN HA GANADO POR ENVENENAMIENTO
        boolean equipoEntrenador1Derrotado = entrenador1.stream().allMatch(p -> p.getVida() <= 0);
        boolean equipoEntrenador2Derrotado = entrenador2.stream().allMatch(p -> p.getVida() <= 0);
        
        if (equipoEntrenador1Derrotado || equipoEntrenador2Derrotado) {
            // La batalla ha terminado por envenenamiento
            System.out.println("¡Batalla terminada por daño continuo!");
            // Actualizar los datos y devolver el resultado
            batalla.setEntrenador1(entrenador1);
            batalla.setEntrenador2(entrenador2);
            batalla.setTurno(turnoActual + 1); // Incrementar turno para el frontend
            return batalla;
        }
        
        Pokemon atacante, receptor;
        List<Pokemon> equipoAtacante, equipoReceptor;
        int posAtacanteEnEquipo, posReceptorEnEquipo;
        
        if (esEquipo1) {
            // Equipo 1 ataca al equipo 2
            atacante = entrenador1.get(posicionAtacante);
            receptor = entrenador2.get(posicionReceptor);
            equipoAtacante = entrenador1;
            equipoReceptor = entrenador2;
            posAtacanteEnEquipo = posicionAtacante;
            posReceptorEnEquipo = posicionReceptor;
        } else {
            // Equipo 2 ataca al equipo 1
            atacante = entrenador2.get(posicionAtacante);
            receptor = entrenador1.get(posicionReceptor);
            equipoAtacante = entrenador2;
            equipoReceptor = entrenador1;
            posAtacanteEnEquipo = posicionAtacante;
            posReceptorEnEquipo = posicionReceptor;
        }
        
        // Verificar que el atacante no esté debilitado (DESPUÉS del daño continuo)
        if (estaDebilitado(atacante)) {
            throw new RuntimeException("El Pokémon atacante está debilitado y no puede atacar");
        }
        
        // Ejecutar la acción (ataque o efecto)
        if (esEquipo1 && batalla.isUsarEfectoE1() || !esEquipo1 && batalla.isUsarEfectoE2()) {
            // Usar efecto
            Efecto efecto = esEquipo1 ? batalla.getEfectoE1() : batalla.getEfectoE2();
            if (efecto != null) {
                // Verificar si es un efecto DANO_CONTINUO para aplicarlo a nivel de equipo
                if (efecto.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                    // Configurar efecto continuo para el equipo contrario
                    if (esEquipo1) {
                        // Equipo 1 aplica efecto al equipo 2
                        batalla.setEfectoContinuoEquipo2(efecto.getId());
                        batalla.setTurnosRestantesEquipo2(4); // 4 turnos de duración
                        System.out.println("Efecto DANO_CONTINUO configurado para equipo 2 por " + batalla.getTurnosRestantesEquipo2() + " turnos");
                    } else {
                        // Equipo 2 aplica efecto al equipo 1
                        batalla.setEfectoContinuoEquipo1(efecto.getId());
                        batalla.setTurnosRestantesEquipo1(4); // 4 turnos de duración
                        System.out.println("Efecto DANO_CONTINUO configurado para equipo 1 por " + batalla.getTurnosRestantesEquipo1() + " turnos");
                    }
                } else {
                    // Para otros efectos, aplicar normalmente
                    Pokemon pokemonAfectado = pokemonService.aplicarEfecto(atacante, receptor, efecto);
                    
                    // Determinar a quién afecta el efecto
                    if (efectoRequiereRival(efecto)) {
                        // El efecto afecta al receptor
                        actualizarPokemon(equipoReceptor, posReceptorEnEquipo, pokemonAfectado);
                    } else {
                        // El efecto afecta al atacante (auto-buff)
                        actualizarPokemon(equipoAtacante, posAtacanteEnEquipo, pokemonAfectado);
                    }
                }
            }
        } else {
            // Usar ataque
            Ataque ataque = esEquipo1 ? batalla.getataqueE1() : batalla.getataqueE2();
            if (ataque != null) {
                Pokemon receptorDañado = pokemonService.atacar(atacante, receptor, ataque);
                actualizarPokemon(equipoReceptor, posReceptorEnEquipo, receptorDañado);
            }
        }
        
        // Actualizar el DTO con los equipos modificados
        batalla.setEntrenador1(entrenador1);
        batalla.setEntrenador2(entrenador2);
        
        // Limpiar las acciones del turno
        batalla.setataqueE1(null);
        batalla.setataqueE2(null);
        batalla.setEfectoE1(null);
        batalla.setEfectoE2(null);
        batalla.setUsarEfectoE1(false);
        batalla.setUsarEfectoE2(false);
        
        // Avanzar al siguiente turno
        batalla.setTurno(turnoActual + 1);
        
        return batalla;
    }

}
