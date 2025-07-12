package com.example.Pokemon.Services;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.Entities.*;
import com.example.Pokemon.Repositories.BatallaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    // Nombres aleatorios para equipos
    private static final String[] NOMBRES_EQUIPO_1 = {
        "Equipo Fuego", "Dragones Salvajes", "Campeones", "Titanes Guardianes", "Leyendas Ardientes", "Conquistadores"
    };
    
    private static final String[] NOMBRES_EQUIPO_2 = {
        "Equipo Agua", "Leyendas Místicas", "Guardianes", "Maestros", 
        "Defensores del Sur", "Héroes Acuáticos", "Estrategas"
    };

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

    /**
     * Crea una batalla aleatoria con equipos generados automáticamente
     * Cada equipo tendrá 3 Pokémon diferentes entre sí, pero pueden repetirse entre equipos
     */
    public BatallaDTO crearBatallaAleatoria() {
        return crearBatallaAleatoriaConModo("TOTAL");
    }
    
    /**
     * Crea una batalla aleatoria con el modo especificado
     * @param modo: "TOTAL", "BALANCEADO", "EFECTOS"
     */
    public BatallaDTO crearBatallaAleatoriaConModo(String modo) {
        // Obtener todos los Pokémon disponibles
        List<Pokemon> todosLosPokemon = pokemonService.getAllPokemon();
        
        if (todosLosPokemon.size() < 3) {
            throw new RuntimeException("Se necesitan al menos 3 Pokémon en la base de datos para crear una batalla aleatoria");
        }
        
        // Crear copias de los Pokémon para la batalla (para no modificar los originales)
        List<Pokemon> equipo1, equipo2;
        
        switch (modo.toUpperCase()) {
            case "BALANCEADO":
                equipo1 = seleccionarEquipoBalanceado(todosLosPokemon);
                equipo2 = seleccionarEquipoBalanceado(todosLosPokemon);
                break;
            case "EFECTOS":
                equipo1 = seleccionarEquipoEfectosBalanceados(todosLosPokemon);
                equipo2 = seleccionarEquipoEfectosBalanceados(todosLosPokemon);
                break;
            case "TOTAL":
            default:
                equipo1 = seleccionarEquipoAleatorio(todosLosPokemon);
                equipo2 = seleccionarEquipoAleatorio(todosLosPokemon);
                break;
        }
        
        // Crear BatallaDTO
        BatallaDTO batallaAleatoria = new BatallaDTO();
        
        // Configurar equipos
        batallaAleatoria.setEntrenador1(equipo1);
        batallaAleatoria.setEntrenador2(equipo2);
        
        // Nombres aleatorios para los equipos
        batallaAleatoria.setNombreEquipo1(NOMBRES_EQUIPO_1[(int)(Math.random() * NOMBRES_EQUIPO_1.length)]);
        batallaAleatoria.setNombreEquipo2(NOMBRES_EQUIPO_2[(int)(Math.random() * NOMBRES_EQUIPO_2.length)]);
        
        // Configuración inicial de batalla
        batallaAleatoria.setTurno(1);
        batallaAleatoria.setUsarEfectoE1(false);
        batallaAleatoria.setUsarEfectoE2(false);
        
        // Flags de efectos
        batallaAleatoria.setAtaqueReducidoEquipo1(false);
        batallaAleatoria.setAtaqueReducidoEquipo2(false);
        batallaAleatoria.setDefensaReducidaEquipo1(false);
        batallaAleatoria.setDefensaReducidaEquipo2(false);
        
        // Efectos continuos
        batallaAleatoria.setEfectoContinuoEquipo1(null);
        batallaAleatoria.setEfectoContinuoEquipo2(null);
        batallaAleatoria.setTurnosRestantesEquipo1(0);
        batallaAleatoria.setTurnosRestantesEquipo2(0);
        
        // Contadores de turnos sin atacar para factor de agresividad
        batallaAleatoria.setTurnosSinAtacarEquipo1(0);
        batallaAleatoria.setTurnosSinAtacarEquipo2(0);
        
        System.out.println("=== BATALLA ALEATORIA CREADA (" + modo.toUpperCase() + ") ===");
        System.out.println("Equipo 1 (" + batallaAleatoria.getNombreEquipo1() + "):");
        equipo1.forEach(p -> System.out.println("  - " + p.getNombre() + " (" + p.getTipoPokemon() + ")"));
        System.out.println("Equipo 2 (" + batallaAleatoria.getNombreEquipo2() + "):");
        equipo2.forEach(p -> System.out.println("  - " + p.getNombre() + " (" + p.getTipoPokemon() + ")"));
        
        return batallaAleatoria;
    }
    
    /**
     * Crea una copia de un Pokémon para la batalla
     */
    private Pokemon crearCopiaPokemon(Pokemon original) {
        Pokemon copia = new Pokemon();
        
        // Copiar propiedades básicas
        copia.setId(original.getId());
        copia.setNombre(original.getNombre());
        copia.setTipoPokemon(original.getTipoPokemon());
        copia.setSprite(original.getSprite());
        copia.setEstado(original.getEstado());
        
        // Copiar estadísticas
        copia.setVida(original.getVida());
        copia.setAtaque(original.getAtaque());
        copia.setDefensa(original.getDefensa());
        
        // Inicializar estadísticas base
        copia.setVidaBase(original.getVida());
        copia.setAtaqueBase(original.getAtaque());
        copia.setDefensaBase(original.getDefensa());
        
        // Inicializar modificadores
        copia.setAtaqueModificado(original.getAtaque());
        copia.setDefensaModificada(original.getDefensa());
        
        // Copiar ataques y efectos
        copia.setIdAtaque1(original.getIdAtaque1());
        copia.setIdAtaque2(original.getIdAtaque2());
        copia.setIdEfecto(original.getIdEfecto());
        
        // Inicializar efectos como inactivos
        copia.setTieneEfectoContinuo(false);
        copia.setTurnosEfectoContinuo(0);
        copia.setIdEfectoActivo(null);
        
        return copia;
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
        
        // SEGUNDO: No aplicar daño continuo al inicio del turno
        // El daño continuo se aplicará DESPUÉS del ataque del equipo afectado
        
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
                } else if (efecto.getTipoEfecto() == Efecto.tipoEfecto.BAJAR_ATAQUE_RIVAL) {
                    // Aplicar reducción de ataque a todo el equipo rival inmediatamente
                    pokemonService.aplicarReduccionAtaqueEquipo(equipoReceptor, efecto);
                    System.out.println("Efecto BAJAR_ATAQUE_RIVAL aplicado a todo el equipo rival");
                    
                    // Marcar que el efecto fue aplicado para el frontend
                    if (esEquipo1) {
                        batalla.setAtaqueReducidoEquipo2(true); // Equipo 1 reduce ataque de equipo 2
                    } else {
                        batalla.setAtaqueReducidoEquipo1(true); // Equipo 2 reduce ataque de equipo 1
                    }
                } else if (efecto.getTipoEfecto() == Efecto.tipoEfecto.BAJAR_DEFENSA_RIVAL) {
                    // Aplicar reducción de defensa a todo el equipo rival inmediatamente
                    pokemonService.aplicarReduccionDefensaEquipo(equipoReceptor, efecto);
                    System.out.println("Efecto BAJAR_DEFENSA_RIVAL aplicado a todo el equipo rival");
                    
                    // Marcar que el efecto fue aplicado para el frontend
                    if (esEquipo1) {
                        batalla.setDefensaReducidaEquipo2(true); // Equipo 1 reduce defensa de equipo 2
                    } else {
                        batalla.setDefensaReducidaEquipo1(true); // Equipo 2 reduce defensa de equipo 1
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
        
        // APLICAR DAÑO CONTINUO DESPUÉS DEL ATAQUE (si el equipo que acaba de atacar tiene veneno)
        // Esto sigue el flujo: E1 usa tóxico -> E2 ataca -> E2 sufre tóxico
        if (esEquipo1 && batalla.getEfectoContinuoEquipo1() != null && batalla.getTurnosRestantesEquipo1() > 0) {
            // El equipo 1 acaba de atacar y tiene efecto continuo activo
            try {
                Efecto efecto = efectoService.findEfectoById(batalla.getEfectoContinuoEquipo1());
                if (efecto != null && efecto.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                    System.out.println("Aplicando daño continuo al equipo 1 después de su ataque (turno " + turnoActual + ")");
                    pokemonService.aplicarDanoContinuoEquipo(entrenador1, efecto);
                }
                
                // Reducir turnos restantes
                batalla.setTurnosRestantesEquipo1(batalla.getTurnosRestantesEquipo1() - 1);
                System.out.println("Turnos restantes para equipo 1: " + batalla.getTurnosRestantesEquipo1());
                
                // Si no quedan turnos, limpiar el efecto
                if (batalla.getTurnosRestantesEquipo1() <= 0) {
                    batalla.setEfectoContinuoEquipo1(null);
                    System.out.println("Efecto continuo del equipo 1 ha terminado");
                }
            } catch (Exception e) {
                System.err.println("Error aplicando daño continuo al equipo 1: " + e.getMessage());
            }
        }
        
        if (!esEquipo1 && batalla.getEfectoContinuoEquipo2() != null && batalla.getTurnosRestantesEquipo2() > 0) {
            // El equipo 2 acaba de atacar y tiene efecto continuo activo
            try {
                Efecto efecto = efectoService.findEfectoById(batalla.getEfectoContinuoEquipo2());
                if (efecto != null && efecto.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                    System.out.println("Aplicando daño continuo al equipo 2 después de su ataque (turno " + turnoActual + ")");
                    pokemonService.aplicarDanoContinuoEquipo(entrenador2, efecto);
                }
                
                // Reducir turnos restantes
                batalla.setTurnosRestantesEquipo2(batalla.getTurnosRestantesEquipo2() - 1);
                System.out.println("Turnos restantes para equipo 2: " + batalla.getTurnosRestantesEquipo2());
                
                // Si no quedan turnos, limpiar el efecto
                if (batalla.getTurnosRestantesEquipo2() <= 0) {
                    batalla.setEfectoContinuoEquipo2(null);
                    System.out.println("Efecto continuo del equipo 2 ha terminado");
                }
            } catch (Exception e) {
                System.err.println("Error aplicando daño continuo al equipo 2: " + e.getMessage());
            }
        }
        
        // Actualizar nuevamente el DTO después del daño continuo
        batalla.setEntrenador1(entrenador1);
        batalla.setEntrenador2(entrenador2);
        
        // VERIFICAR SI ALGUIEN HA GANADO POR DAÑO CONTINUO (después de aplicarlo)
        boolean equipoEntrenador1Derrotado = entrenador1.stream().allMatch(p -> p.getVida() <= 0);
        boolean equipoEntrenador2Derrotado = entrenador2.stream().allMatch(p -> p.getVida() <= 0);
          if (equipoEntrenador1Derrotado || equipoEntrenador2Derrotado) {
            System.out.println("¡Batalla terminada por daño continuo!");
            batalla.setTurno(turnoActual + 1); // Incrementar turno para el frontend
            return batalla;
        }

        // ACTUALIZAR CONTADORES DE TURNOS SIN ATACAR para factor de agresividad
        updateTurnosSinAtacar(batalla, esEquipo1);

        // Limpiar las acciones del turno
        batalla.setataqueE1(null);
        batalla.setataqueE2(null);
        batalla.setEfectoE1(null);
        batalla.setEfectoE2(null);
        batalla.setUsarEfectoE1(false);
        batalla.setUsarEfectoE2(false);
        
        // Limpiar flags de efectos de reducción de estadísticas
        batalla.setAtaqueReducidoEquipo1(false);
        batalla.setAtaqueReducidoEquipo2(false);
        batalla.setDefensaReducidaEquipo1(false);
        batalla.setDefensaReducidaEquipo2(false);
        
        // Avanzar al siguiente turno
        batalla.setTurno(turnoActual + 1);
        
        return batalla;
    }

    /**
     * Selecciona 3 Pokémon aleatorios diferentes para formar un equipo
     */
    private List<Pokemon> seleccionarEquipoAleatorio(List<Pokemon> todosLosPokemon) {
        // Crear una copia para no modificar la lista original
        List<Pokemon> pokemonDisponibles = new ArrayList<>(todosLosPokemon);
        
        // Mezclar la lista
        Collections.shuffle(pokemonDisponibles);
        
        // Seleccionar 3 Pokémon únicos para este equipo
        return pokemonDisponibles.stream()
            .limit(3)
            .map(this::crearCopiaPokemon)
            .collect(Collectors.toList());
    }
    
    /**
     * Selecciona 3 Pokémon de tipos diferentes para formar un equipo balanceado
     */
    private List<Pokemon> seleccionarEquipoBalanceado(List<Pokemon> todosLosPokemon) {
        List<Pokemon> equipoBalanceado = new ArrayList<>();
        List<Pokemon.TipoPokemon> tiposUsados = new ArrayList<>();
        List<Pokemon> pokemonDisponibles = new ArrayList<>(todosLosPokemon);
        Collections.shuffle(pokemonDisponibles);
        
        // Intentar seleccionar 3 Pokémon de tipos diferentes
        for (Pokemon pokemon : pokemonDisponibles) {
            if (equipoBalanceado.size() >= 3) {
                break;
            }
            
            if (!tiposUsados.contains(pokemon.getTipoPokemon())) {
                equipoBalanceado.add(crearCopiaPokemon(pokemon));
                tiposUsados.add(pokemon.getTipoPokemon());
            }
        }
        
        // Si no pudimos obtener 3 tipos diferentes, completar con Pokémon aleatorios
        while (equipoBalanceado.size() < 3) {
            for (Pokemon pokemon : pokemonDisponibles) {
                if (equipoBalanceado.size() >= 3) {
                    break;
                }
                
                // Verificar que no sea el mismo Pokémon (por ID)
                boolean yaEstaEnEquipo = equipoBalanceado.stream()
                    .anyMatch(p -> p.getId().equals(pokemon.getId()));
                
                if (!yaEstaEnEquipo) {
                    equipoBalanceado.add(crearCopiaPokemon(pokemon));
                }
            }
            
            // Prevenir bucle infinito si no hay suficientes Pokémon únicos
            if (equipoBalanceado.size() < 3 && pokemonDisponibles.size() < 3) {
                break;
            }
        }
        
        System.out.println("Equipo balanceado por tipos:");
        equipoBalanceado.forEach(p -> System.out.println("  - " + p.getNombre() + " (" + p.getTipoPokemon() + ")"));
        
        return equipoBalanceado;
    }
    
    /**
     * Selecciona 3 Pokémon con tipos de efectos diferentes para formar un equipo balanceado
     */
    private List<Pokemon> seleccionarEquipoEfectosBalanceados(List<Pokemon> todosLosPokemon) {
        List<Pokemon> equipoBalanceado = new ArrayList<>();
        List<Efecto.tipoEfecto> efectosUsados = new ArrayList<>();
        List<Pokemon> pokemonDisponibles = new ArrayList<>(todosLosPokemon);
        Collections.shuffle(pokemonDisponibles);
        
        // Intentar seleccionar 3 Pokémon con efectos diferentes
        for (Pokemon pokemon : pokemonDisponibles) {
            if (equipoBalanceado.size() >= 3) {
                break;
            }
            
            if (pokemon.getIdEfecto() != null) {
                try {
                    Efecto efecto = efectoService.findEfectoById(pokemon.getIdEfecto());
                    if (efecto != null && !efectosUsados.contains(efecto.getTipoEfecto())) {
                        equipoBalanceado.add(crearCopiaPokemon(pokemon));
                        efectosUsados.add(efecto.getTipoEfecto());
                    }
                } catch (Exception e) {
                    System.err.println("Error al obtener efecto para " + pokemon.getNombre() + ": " + e.getMessage());
                }
            }
        }
        
        // Si no pudimos obtener 3 efectos diferentes, completar con Pokémon aleatorios
        while (equipoBalanceado.size() < 3) {
            for (Pokemon pokemon : pokemonDisponibles) {
                if (equipoBalanceado.size() >= 3) {
                    break;
                }
                
                // Verificar que no sea el mismo Pokémon (por ID)
                boolean yaEstaEnEquipo = equipoBalanceado.stream()
                    .anyMatch(p -> p.getId().equals(pokemon.getId()));
                
                if (!yaEstaEnEquipo) {
                    equipoBalanceado.add(crearCopiaPokemon(pokemon));
                }
            }
            
            // Prevenir bucle infinito si no hay suficientes Pokémon únicos
            if (equipoBalanceado.size() < 3 && pokemonDisponibles.size() < 3) {
                break;
            }
        }
        
        System.out.println("Equipo balanceado por efectos:");
        equipoBalanceado.forEach(p -> {
            try {
                if (p.getIdEfecto() != null) {
                    Efecto efecto = efectoService.findEfectoById(p.getIdEfecto());
                    System.out.println("  - " + p.getNombre() + " (Efecto: " + (efecto != null ? efecto.getTipoEfecto() : "Desconocido") + ")");
                } else {
                    System.out.println("  - " + p.getNombre() + " (Sin efecto)");
                }
            } catch (Exception e) {
                System.out.println("  - " + p.getNombre() + " (Error al obtener efecto)");
            }
        });
        
        return equipoBalanceado;
    }
    
    /**
     * Actualiza los contadores de turnos sin atacar para el factor de agresividad
     * Solo actualiza el contador del equipo que está jugando en este turno
     */
    private void updateTurnosSinAtacar(BatallaDTO batalla, boolean esEquipo1) {
        if (esEquipo1) {
            // Es el turno del Equipo 1
            if (batalla.isUsarEfectoE1()) {
                // Equipo 1 usó efecto, incrementar contador
                batalla.setTurnosSinAtacarEquipo1(batalla.getTurnosSinAtacarEquipo1() + 1);
                System.out.println("Equipo 1 usó efecto. Turnos sin atacar: " + batalla.getTurnosSinAtacarEquipo1());
            } else {
                // Equipo 1 atacó, resetear contador
                batalla.setTurnosSinAtacarEquipo1(0);
                System.out.println("Equipo 1 atacó. Contador de turnos sin atacar reseteado.");
            }
        } else {
            // Es el turno del Equipo 2
            if (batalla.isUsarEfectoE2()) {
                // Equipo 2 usó efecto, incrementar contador
                batalla.setTurnosSinAtacarEquipo2(batalla.getTurnosSinAtacarEquipo2() + 1);
                System.out.println("Equipo 2 usó efecto. Turnos sin atacar: " + batalla.getTurnosSinAtacarEquipo2());
            } else {
                // Equipo 2 atacó, resetear contador
                batalla.setTurnosSinAtacarEquipo2(0);
                System.out.println("Equipo 2 atacó. Contador de turnos sin atacar reseteado.");
            }
        }
    }
}
