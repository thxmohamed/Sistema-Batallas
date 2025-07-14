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
import java.util.Map;
import java.util.HashMap;

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

    @Autowired
    AtaqueService ataqueService;

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
     * Actualiza los contadores de turnos sin atacar basándose en si se usó efecto o ataque
     */
    private void updateTurnosSinAtacar(BatallaDTO batalla, boolean esEquipo1) {
        boolean usoEfecto = esEquipo1 ? batalla.isUsarEfectoE1() : batalla.isUsarEfectoE2();
        
        if (esEquipo1) {
            if (usoEfecto) {
                // Incrementar contador de turnos sin atacar para equipo 1
                batalla.setTurnosSinAtacarEquipo1(batalla.getTurnosSinAtacarEquipo1() + 1);
            } else {
                // Resetear contador si atacó
                batalla.setTurnosSinAtacarEquipo1(0);
            }
        } else {
            if (usoEfecto) {
                // Incrementar contador de turnos sin atacar para equipo 2
                batalla.setTurnosSinAtacarEquipo2(batalla.getTurnosSinAtacarEquipo2() + 1);
            } else {
                // Resetear contador si atacó
                batalla.setTurnosSinAtacarEquipo2(0);
            }
        }
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
     * Selecciona 3 Pokémon para CPU en dificultad difícil que tengan ventaja contra el equipo humano
     * Implementación mejorada que evita vulnerabilidades compartidas y prioriza diversidad
     */
    public List<Pokemon> seleccionarEquipoAntiHumano(List<Pokemon> todosLosPokemon, List<Pokemon> equipoHumano) {
        List<Pokemon> equipoOptimizado = new ArrayList<>();
        List<Pokemon> pokemonDisponibles = new ArrayList<>(todosLosPokemon);
        Collections.shuffle(pokemonDisponibles);
        
        System.out.println("=== SELECCIONANDO EQUIPO CPU DIFÍCIL CONTRA HUMANO ===");
        System.out.println("Equipo humano a contrarrestar:");
        equipoHumano.forEach(p -> {
            try {
                Ataque att1 = ataqueService.getAtaqueById(p.getIdAtaque1());
                Ataque att2 = ataqueService.getAtaqueById(p.getIdAtaque2());
                System.out.println("  - " + p.getNombre() + " (" + p.getTipoPokemon() + ") - Ataques: " + 
                    (att1 != null ? att1.getTipoAtaque() : "?") + "/" + 
                    (att2 != null ? att2.getTipoAtaque() : "?"));
            } catch (Exception e) {
                System.out.println("  - " + p.getNombre() + " (" + p.getTipoPokemon() + ")");
            }
        });
        System.out.println();
        
        // FASE 1: Analizar amenazas críticas del equipo humano
        List<Ataque.TipoAtaque> amenazasCriticas = identificarAmenazasCriticas(equipoHumano);
        System.out.println("🚨 AMENAZAS CRÍTICAS DETECTADAS: " + amenazasCriticas);
        
        // FASE 2: Selección inteligente evitando vulnerabilidades compartidas
        for (int i = 0; i < 3; i++) {
            System.out.println("--- Seleccionando Pokémon #" + (i+1) + " ---");
            Pokemon mejorCounter = encontrarMejorCounterPokemonConDiversidad(pokemonDisponibles, equipoHumano, equipoOptimizado, amenazasCriticas);
            if (mejorCounter != null) {
                equipoOptimizado.add(crearCopiaPokemon(mejorCounter));
                // Remover para evitar duplicados
                pokemonDisponibles.removeIf(p -> p.getId().equals(mejorCounter.getId()));
                System.out.println("✅ Seleccionado " + mejorCounter.getNombre() + " (" + mejorCounter.getTipoPokemon() + ") como counter #" + (i+1));
                
                // Verificar vulnerabilidades del equipo actual
                verificarVulnerabilidadesEquipo(equipoOptimizado, equipoHumano);
            } else {
                System.out.println("⚠️ No se encontró counter óptimo para posición #" + (i+1));
            }
            System.out.println();
        }
        
        // Si no se pudieron encontrar 3 counters, completar con Pokémon aleatorios balanceados
        while (equipoOptimizado.size() < 3) {
            Pokemon fallback = encontrarFallbackPokemon(pokemonDisponibles, equipoOptimizado, equipoHumano);
            if (fallback != null) {
                equipoOptimizado.add(crearCopiaPokemon(fallback));
                pokemonDisponibles.removeIf(p -> p.getId().equals(fallback.getId()));
                System.out.println("🔧 Completando equipo con " + fallback.getNombre() + " (fallback)");
            } else {
                // Última opción: cualquier Pokémon disponible
                for (Pokemon pokemon : pokemonDisponibles) {
                    if (equipoOptimizado.size() >= 3) break;
                    
                    boolean yaEstaEnEquipo = equipoOptimizado.stream()
                        .anyMatch(p -> p.getId().equals(pokemon.getId()));
                    
                    if (!yaEstaEnEquipo) {
                        equipoOptimizado.add(crearCopiaPokemon(pokemon));
                        System.out.println("🎲 Completando equipo con " + pokemon.getNombre() + " (aleatorio)");
                        break;
                    }
                }
            }
            // Prevenir bucle infinito
            if (pokemonDisponibles.size() < 3) break;
        }
        
        // Análisis final del equipo seleccionado
        System.out.println("=== ANÁLISIS DEL EQUIPO CPU OPTIMIZADO ===");
        equipoOptimizado.forEach(p -> System.out.println("  - " + p.getNombre() + " (" + p.getTipoPokemon() + ")"));
        
        analizarCoberturaEquipo(equipoOptimizado, equipoHumano);
        
        return equipoOptimizado;
    }
    
    /**
     * Encuentra el mejor Pokémon counter contra el equipo humano
     */
    private Pokemon encontrarMejorCounterPokemon(List<Pokemon> candidatos, List<Pokemon> equipoHumano, List<Pokemon> yaSeleccionados) {
        Pokemon mejorCounter = null;
        double mejorPuntuacion = 0.0;
        
        for (Pokemon candidato : candidatos) {
            // Evitar duplicados
            boolean yaEstaSeleccionado = yaSeleccionados.stream()
                .anyMatch(p -> p.getId().equals(candidato.getId()));
            if (yaEstaSeleccionado) continue;
            
            double puntuacion = evaluarCounterEffectiveness(candidato, equipoHumano);
            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejorCounter = candidato;
            }
        }
        
        return mejorCounter;
    }
    
    /**
     * Identifica los tipos de ataque más peligrosos del equipo humano
     */
    private List<Ataque.TipoAtaque> identificarAmenazasCriticas(List<Pokemon> equipoHumano) {
        Map<Ataque.TipoAtaque, Integer> conteoAmenazas = new HashMap<>();
        
        for (Pokemon humano : equipoHumano) {
            try {
                Ataque attack1 = ataqueService.getAtaqueById(humano.getIdAtaque1());
                Ataque attack2 = ataqueService.getAtaqueById(humano.getIdAtaque2());
                
                if (attack1 != null) {
                    conteoAmenazas.put(attack1.getTipoAtaque(), 
                        conteoAmenazas.getOrDefault(attack1.getTipoAtaque(), 0) + 1);
                }
                if (attack2 != null) {
                    conteoAmenazas.put(attack2.getTipoAtaque(), 
                        conteoAmenazas.getOrDefault(attack2.getTipoAtaque(), 0) + 1);
                }
            } catch (Exception e) {
                System.err.println("Error identificando amenazas de " + humano.getNombre());
            }
        }
        
        // Retornar tipos de ataque que aparecen más frecuentemente (amenazas críticas)
        return conteoAmenazas.entrySet().stream()
            .filter(entry -> entry.getValue() >= 1) // Al menos 1 ataque de este tipo
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Encuentra el mejor counter considerando diversidad de tipos y evitando vulnerabilidades compartidas
     */
    private Pokemon encontrarMejorCounterPokemonConDiversidad(List<Pokemon> candidatos, List<Pokemon> equipoHumano, 
                                                             List<Pokemon> yaSeleccionados, List<Ataque.TipoAtaque> amenazasCriticas) {
        Pokemon mejorCounter = null;
        double mejorPuntuacion = 0.0;
        
        for (Pokemon candidato : candidatos) {
            // Evitar duplicados
            boolean yaEstaSeleccionado = yaSeleccionados.stream()
                .anyMatch(p -> p.getId().equals(candidato.getId()));
            if (yaEstaSeleccionado) continue;
            
            double puntuacion = evaluarCounterEffectivenessConDiversidad(candidato, equipoHumano, yaSeleccionados, amenazasCriticas);
            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejorCounter = candidato;
            }
        }
        
        return mejorCounter;
    }
    
    /**
     * Evaluación mejorada que penaliza vulnerabilidades compartidas y premia diversidad
     */
    private double evaluarCounterEffectivenessConDiversidad(Pokemon candidato, List<Pokemon> equipoHumano, 
                                                           List<Pokemon> yaSeleccionados, List<Ataque.TipoAtaque> amenazasCriticas) {
        double puntuacion = 0.0;
        
        try {
            // Obtener ataques del candidato
            Ataque attack1 = ataqueService.getAtaqueById(candidato.getIdAtaque1());
            Ataque attack2 = ataqueService.getAtaqueById(candidato.getIdAtaque2());
            
            System.out.println("Evaluando " + candidato.getNombre() + " (" + candidato.getTipoPokemon() + "):");
            
            // 1. PENALIZACIÓN CRÍTICA POR VULNERABILIDADES COMPARTIDAS
            int vulnerabilidadesCompartidas = contarVulnerabilidadesCompartidas(candidato, yaSeleccionados, amenazasCriticas);
            if (vulnerabilidadesCompartidas > 0) {
                double penalizacion = vulnerabilidadesCompartidas * 200.0; // Penalización muy fuerte
                puntuacion -= penalizacion;
                System.out.println("  ⚠️ VULNERABILIDADES COMPARTIDAS: -" + penalizacion + " puntos (" + vulnerabilidadesCompartidas + " amenazas)");
            }
            
            // 2. BONUS GIGANTE POR DIVERSIDAD DE TIPOS
            boolean esTipoNuevo = yaSeleccionados.stream()
                .noneMatch(p -> p.getTipoPokemon().equals(candidato.getTipoPokemon()));
            if (esTipoNuevo) {
                puntuacion += 300.0; // Bonus masivo por diversidad
                System.out.println("  ⭐ DIVERSIDAD DE TIPOS: +300 puntos (tipo nuevo)");
            } else {
                puntuacion -= 150.0; // Penalización por repetir tipo
                System.out.println("  ⚠️ TIPO REPETIDO: -150 puntos");
            }
            
            // 3. ANÁLISIS OFENSIVO (reducido en importancia)
            int pokemonCountereables = 0;
            double danoOfensivoTotal = 0.0;
            
            for (Pokemon humano : equipoHumano) {
                if (attack1 != null) {
                    double efectividad1 = calcularEfectividadTipo(attack1.getTipoAtaque(), humano.getTipoPokemon());
                    if (efectividad1 >= 2.0) {
                        pokemonCountereables++;
                        puntuacion += 80.0; // Reducido de 120
                        danoOfensivoTotal += efectividad1 * attack1.getPotencia();
                        System.out.println("  ✓ " + attack1.getNombre() + " SUPER EFECTIVO vs " + humano.getNombre() + " (x" + efectividad1 + ")");
                    } else if (efectividad1 > 1.0) {
                        puntuacion += 20.0;
                        danoOfensivoTotal += efectividad1 * attack1.getPotencia();
                    }
                }
                
                if (attack2 != null) {
                    double efectividad2 = calcularEfectividadTipo(attack2.getTipoAtaque(), humano.getTipoPokemon());
                    if (efectividad2 >= 2.0) {
                        pokemonCountereables++;
                        puntuacion += 80.0;
                        danoOfensivoTotal += efectividad2 * attack2.getPotencia();
                        System.out.println("  ✓ " + attack2.getNombre() + " SUPER EFECTIVO vs " + humano.getNombre() + " (x" + efectividad2 + ")");
                    } else if (efectividad2 > 1.0) {
                        puntuacion += 20.0;
                        danoOfensivoTotal += efectividad2 * attack2.getPotencia();
                    }
                }
            }
            
            // 4. ANÁLISIS DEFENSIVO MEJORADO
            int ataquesSuperEfectivosRecibidos = 0;
            int ataquesResistidos = 0;
            
            for (Pokemon humano : equipoHumano) {
                try {
                    Ataque humanoAttack1 = ataqueService.getAtaqueById(humano.getIdAtaque1());
                    Ataque humanoAttack2 = ataqueService.getAtaqueById(humano.getIdAtaque2());
                    
                    if (humanoAttack1 != null) {
                        double efectividadContra = calcularEfectividadTipo(humanoAttack1.getTipoAtaque(), candidato.getTipoPokemon());
                        if (efectividadContra >= 2.0) {
                            ataquesSuperEfectivosRecibidos++;
                            puntuacion -= 100.0; // Aumentado de 80
                            System.out.println("  ✗ VULNERABLE a " + humanoAttack1.getNombre() + " de " + humano.getNombre() + " (x" + efectividadContra + ")");
                        } else if (efectividadContra <= 0.5) {
                            ataquesResistidos++;
                            puntuacion += 90.0; // Aumentado de 70
                            System.out.println("  ✓ RESISTE " + humanoAttack1.getNombre() + " de " + humano.getNombre() + " (x" + efectividadContra + ")");
                        }
                    }
                    
                    if (humanoAttack2 != null) {
                        double efectividadContra = calcularEfectividadTipo(humanoAttack2.getTipoAtaque(), candidato.getTipoPokemon());
                        if (efectividadContra >= 2.0) {
                            ataquesSuperEfectivosRecibidos++;
                            puntuacion -= 100.0;
                            System.out.println("  ✗ VULNERABLE a " + humanoAttack2.getNombre() + " de " + humano.getNombre() + " (x" + efectividadContra + ")");
                        } else if (efectividadContra <= 0.5) {
                            ataquesResistidos++;
                            puntuacion += 90.0;
                            System.out.println("  ✓ RESISTE " + humanoAttack2.getNombre() + " de " + humano.getNombre() + " (x" + efectividadContra + ")");
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores
                }
            }
            
            // 5. PENALIZACIÓN EXTRA POR MÚLTIPLES VULNERABILIDADES
            if (ataquesSuperEfectivosRecibidos >= 2) {
                puntuacion -= 200.0; // Aumentado de 120
                System.out.println("  🚨 MUY VULNERABLE: -200 puntos adicionales");
            }
            
            // 6. BONUS POR COBERTURA Y RESISTENCIAS
            if (pokemonCountereables >= 2) {
                puntuacion += 100.0;
                System.out.println("  ⭐ EXCELENTE COBERTURA: +" + pokemonCountereables + " counters");
            }
            
            if (ataquesResistidos >= 2) {
                puntuacion += 80.0;
                System.out.println("  🛡️ BUENA SUPERVIVENCIA: +" + ataquesResistidos + " resistencias");
            }
            
            // 7. FACTOR ALEATORIO REDUCIDO
            double factorAleatorio = (Math.random() - 0.5) * 10.0; // Reducido
            puntuacion += factorAleatorio;
            
            System.out.println("  📊 PUNTUACIÓN FINAL: " + String.format("%.1f", puntuacion));
            
        } catch (Exception e) {
            System.err.println("Error evaluando counter effectiveness para " + candidato.getNombre() + ": " + e.getMessage());
        }
        
        return puntuacion;
    }
    
    /**
     * Cuenta cuántas vulnerabilidades críticas comparte este Pokémon con el equipo actual
     */
    private int contarVulnerabilidadesCompartidas(Pokemon candidato, List<Pokemon> yaSeleccionados, List<Ataque.TipoAtaque> amenazasCriticas) {
        int vulnerabilidadesCompartidas = 0;
        
        for (Ataque.TipoAtaque amenaza : amenazasCriticas) {
            double efectividadCandidato = calcularEfectividadTipoContraDefensor(amenaza, candidato.getTipoPokemon());
            
            if (efectividadCandidato >= 2.0) { // Candidato es vulnerable a esta amenaza
                // Verificar si algún Pokémon ya seleccionado también es vulnerable
                for (Pokemon yaSeleccionado : yaSeleccionados) {
                    double efectividadYaSeleccionado = calcularEfectividadTipoContraDefensor(amenaza, yaSeleccionado.getTipoPokemon());
                    if (efectividadYaSeleccionado >= 2.0) {
                        vulnerabilidadesCompartidas++;
                        break; // Solo contar una vez por amenaza
                    }
                }
            }
        }
        
        return vulnerabilidadesCompartidas;
    }
    
    /**
     * Calcula efectividad de un tipo de ataque contra un tipo defensor (versión helper)
     */
    private double calcularEfectividadTipoContraDefensor(Ataque.TipoAtaque tipoAtaque, Pokemon.TipoPokemon tipoDefensor) {
        String tipoAtaqueStr = String.valueOf(tipoAtaque);
        String tipoDefensorStr = String.valueOf(tipoDefensor);
        
        return switch (tipoAtaqueStr) {
            case "AGUA" -> {
                if (tipoDefensorStr.equals("FUEGO")) yield 2.0;
                else if (tipoDefensorStr.equals("ELECTRICO") || tipoDefensorStr.equals("AGUA")) yield 0.5;
                else yield 1.0;
            }
            case "FUEGO" -> {
                if (tipoDefensorStr.equals("PLANTA")) yield 2.0;
                else if (tipoDefensorStr.equals("AGUA") || tipoDefensorStr.equals("FUEGO")) yield 0.5;
                else yield 1.0;
            }
            case "PLANTA" -> {
                if (tipoDefensorStr.equals("TIERRA")) yield 2.0;
                else if (tipoDefensorStr.equals("FUEGO") || tipoDefensorStr.equals("PLANTA")) yield 0.5;
                else yield 1.0;
            }
            case "TIERRA" -> {
                if (tipoDefensorStr.equals("ELECTRICO")) yield 2.0;
                else if (tipoDefensorStr.equals("PLANTA") || tipoDefensorStr.equals("TIERRA")) yield 0.5;
                else yield 1.0;
            }
            case "ELECTRICO" -> {
                if (tipoDefensorStr.equals("AGUA")) yield 2.0;
                else if (tipoDefensorStr.equals("TIERRA") || tipoDefensorStr.equals("ELECTRICO")) yield 0.5;
                else yield 1.0;
            }
            case "NORMAL" -> 1.0;
            default -> 1.0;
        };
    }
    
    /**
     * Verifica las vulnerabilidades del equipo actual y muestra advertencias
     */
    private void verificarVulnerabilidadesEquipo(List<Pokemon> equipoActual, List<Pokemon> equipoHumano) {
        System.out.println("🔍 VERIFICANDO VULNERABILIDADES DEL EQUIPO ACTUAL:");
        
        for (Pokemon humano : equipoHumano) {
            try {
                Ataque attack1 = ataqueService.getAtaqueById(humano.getIdAtaque1());
                Ataque attack2 = ataqueService.getAtaqueById(humano.getIdAtaque2());
                
                int vulnerablesA1 = 0, vulnerablesA2 = 0;
                
                if (attack1 != null) {
                    for (Pokemon cpu : equipoActual) {
                        double efectividad = calcularEfectividadTipo(attack1.getTipoAtaque(), cpu.getTipoPokemon());
                        if (efectividad >= 2.0) vulnerablesA1++;
                    }
                }
                
                if (attack2 != null) {
                    for (Pokemon cpu : equipoActual) {
                        double efectividad = calcularEfectividadTipo(attack2.getTipoAtaque(), cpu.getTipoPokemon());
                        if (efectividad >= 2.0) vulnerablesA2++;
                    }
                }
                
                if (vulnerablesA1 >= 2 || vulnerablesA2 >= 2) {
                    String ataque = vulnerablesA1 >= 2 ? attack1.getNombre() : attack2.getNombre();
                    int count = Math.max(vulnerablesA1, vulnerablesA2);
                    System.out.println("  🚨 ALERTA: " + humano.getNombre() + " puede derrotar " + count + " CPU con " + ataque);
                }
                
            } catch (Exception e) {
                // Ignorar errores
            }
        }
    }
    
    /**
     * Evalúa qué tan efectivo es un Pokémon como counter contra el equipo humano
     * Implementación mejorada que considera resistencias/debilidades más estratégicamente
     */
    private double evaluarCounterEffectiveness(Pokemon candidato, List<Pokemon> equipoHumano) {
        double puntuacion = 0.0;
        
        try {
            // Obtener ataques del candidato
            Ataque attack1 = ataqueService.getAtaqueById(candidato.getIdAtaque1());
            Ataque attack2 = ataqueService.getAtaqueById(candidato.getIdAtaque2());
            
            System.out.println("Evaluando " + candidato.getNombre() + " (" + candidato.getTipoPokemon() + "):");
            
            // 1. ANÁLISIS OFENSIVO: ¿Puede hacer daño super efectivo?
            int pokemonContrarrestablesConAtaque1 = 0;
            int pokemonContrarrestablesConAtaque2 = 0;
            double danoOfensivoTotal = 0.0;
            
            for (Pokemon humano : equipoHumano) {
                if (attack1 != null) {
                    double efectividad1 = calcularEfectividadTipo(attack1.getTipoAtaque(), humano.getTipoPokemon());
                    if (efectividad1 >= 2.0) {
                        pokemonContrarrestablesConAtaque1++;
                        puntuacion += 120.0; // Prioridad alta: puede hacer super efectivo
                        danoOfensivoTotal += efectividad1 * attack1.getPotencia();
                        System.out.println("  ✓ " + attack1.getNombre() + " SUPER EFECTIVO vs " + humano.getNombre() + " (x" + efectividad1 + ")");
                    } else if (efectividad1 > 1.0) {
                        puntuacion += 30.0;
                        danoOfensivoTotal += efectividad1 * attack1.getPotencia();
                    } else if (efectividad1 < 1.0) {
                        puntuacion -= 15.0; // Penalizar ataques no muy efectivos
                    }
                }
                
                if (attack2 != null) {
                    double efectividad2 = calcularEfectividadTipo(attack2.getTipoAtaque(), humano.getTipoPokemon());
                    if (efectividad2 >= 2.0) {
                        pokemonContrarrestablesConAtaque2++;
                        puntuacion += 120.0;
                        danoOfensivoTotal += efectividad2 * attack2.getPotencia();
                        System.out.println("  ✓ " + attack2.getNombre() + " SUPER EFECTIVO vs " + humano.getNombre() + " (x" + efectividad2 + ")");
                    } else if (efectividad2 > 1.0) {
                        puntuacion += 30.0;
                        danoOfensivoTotal += efectividad2 * attack2.getPotencia();
                    } else if (efectividad2 < 1.0) {
                        puntuacion -= 15.0;
                    }
                }
            }
            
            // 2. BONUS POR COBERTURA OFENSIVA: Premiar Pokémon que pueden contrarrestar múltiples enemigos
            int pokemonCountereables = Math.max(pokemonContrarrestablesConAtaque1, pokemonContrarrestablesConAtaque2);
            if (pokemonCountereables >= 2) {
                puntuacion += 150.0; // Excelente cobertura: puede contrarrestar múltiples enemigos
                System.out.println("  ⭐ EXCELENTE COBERTURA: Puede contrarrestar " + pokemonCountereables + " Pokémon");
            } else if (pokemonCountereables == 1) {
                puntuacion += 80.0; // Buena cobertura
                System.out.println("  ⚡ BUENA COBERTURA: Puede contrarrestar " + pokemonCountereables + " Pokémon");
            }
            
            // 3. ANÁLISIS DEFENSIVO: ¿Puede resistir los ataques enemigos?
            int ataquesSuperEfectivosRecibidos = 0;
            int ataquesResistidos = 0;
            double danoDefensivoRecibido = 0.0;
            
            for (Pokemon humano : equipoHumano) {
                try {
                    Ataque humanoAttack1 = ataqueService.getAtaqueById(humano.getIdAtaque1());
                    Ataque humanoAttack2 = ataqueService.getAtaqueById(humano.getIdAtaque2());
                    
                    if (humanoAttack1 != null) {
                        double efectividadContra = calcularEfectividadTipo(humanoAttack1.getTipoAtaque(), candidato.getTipoPokemon());
                        if (efectividadContra >= 2.0) {
                            ataquesSuperEfectivosRecibidos++;
                            puntuacion -= 80.0; // Penalizar MUCHO si es vulnerable
                            danoDefensivoRecibido += efectividadContra * humanoAttack1.getPotencia();
                            System.out.println("  ✗ VULNERABLE a " + humanoAttack1.getNombre() + " de " + humano.getNombre() + " (x" + efectividadContra + ")");
                        } else if (efectividadContra <= 0.5) {
                            ataquesResistidos++;
                            puntuacion += 70.0; // Bonus grande por resistencia
                            System.out.println("  ✓ RESISTE " + humanoAttack1.getNombre() + " de " + humano.getNombre() + " (x" + efectividadContra + ")");
                        } else if (efectividadContra < 1.0) {
                            puntuacion += 20.0; // Bonus menor por daño reducido
                        }
                    }
                    
                    if (humanoAttack2 != null) {
                        double efectividadContra = calcularEfectividadTipo(humanoAttack2.getTipoAtaque(), candidato.getTipoPokemon());
                        if (efectividadContra >= 2.0) {
                            ataquesSuperEfectivosRecibidos++;
                            puntuacion -= 80.0;
                            danoDefensivoRecibido += efectividadContra * humanoAttack2.getPotencia();
                            System.out.println("  ✗ VULNERABLE a " + humanoAttack2.getNombre() + " de " + humano.getNombre() + " (x" + efectividadContra + ")");
                        } else if (efectividadContra <= 0.5) {
                            System.out.println("  ✓ RESISTE " + humanoAttack2.getNombre() + " de " + humano.getNombre() + " (x" + efectividadContra + ")");
                        } else if (efectividadContra < 1.0) {
                            puntuacion += 20.0;
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores al obtener ataques humanos
                }
            }
            
            // 4. BONUS POR SUPERVIVENCIA: Premiar Pokémon que pueden resistir múltiples ataques
            if (ataquesResistidos >= 3) {
                puntuacion += 100.0; // Excelente supervivencia
                System.out.println("  🛡️ EXCELENTE SUPERVIVENCIA: Resiste " + ataquesResistidos + " ataques");
            } else if (ataquesResistidos >= 2) {
                puntuacion += 60.0;
                System.out.println("  🛡️ BUENA SUPERVIVENCIA: Resiste " + ataquesResistidos + " ataques");
            }
            
            // 5. PENALIZACIÓN POR VULNERABILIDAD CRÍTICA
            if (ataquesSuperEfectivosRecibidos >= 2) {
                puntuacion -= 120.0; // Muy vulnerable, penalización extra
                System.out.println("  ⚠️ MUY VULNERABLE: Recibe " + ataquesSuperEfectivosRecibidos + " ataques super efectivos");
            }
            
            // 6. FACTOR DE EQUILIBRIO: Ratio daño ofensivo vs defensivo
            if (danoOfensivoTotal > 0 && danoDefensivoRecibido > 0) {
                double ratio = danoOfensivoTotal / danoDefensivoRecibido;
                if (ratio > 1.5) {
                    puntuacion += 50.0; // Puede hacer más daño del que recibe
                    System.out.println("  ⚖️ RATIO FAVORABLE: Da más daño del que recibe (" + String.format("%.1f", ratio) + ":1)");
                } else if (ratio < 0.7) {
                    puntuacion -= 30.0; // Recibe más daño del que puede hacer
                }
            }
            
            // 7. BONUS POR STATS SUPERIORES (peso reducido para mantener foco en tipos)
            for (Pokemon humano : equipoHumano) {
                if (candidato.getAtaque() > humano.getAtaque()) puntuacion += 8.0;
                if (candidato.getDefensa() > humano.getDefensa()) puntuacion += 8.0;
                if (candidato.getVida() > humano.getVida()) puntuacion += 5.0;
            }
            
            // 8. FACTOR DE RANDOMIZACIÓN: Añadir pequeña variación para evitar patrones predecibles
            double factorAleatorio = (Math.random() - 0.5) * 20.0; // ±10 puntos aleatorios
            puntuacion += factorAleatorio;
            
            System.out.println("  📊 PUNTUACIÓN FINAL: " + String.format("%.1f", puntuacion));
            System.out.println("    - Cobertura ofensiva: " + pokemonCountereables + " Pokémon");
            System.out.println("    - Ataques resistidos: " + ataquesResistidos);
            System.out.println("    - Vulnerabilidades: " + ataquesSuperEfectivosRecibidos);
            
        } catch (Exception e) {
            System.err.println("Error evaluando counter effectiveness para " + candidato.getNombre() + ": " + e.getMessage());
        }
        
        return puntuacion;
    }
    
    /**
     * Calcula la efectividad de un tipo de ataque contra un tipo de Pokémon
     */
    private double calcularEfectividadTipo(Ataque.TipoAtaque tipoAtaque, Pokemon.TipoPokemon tipoDefensor) {
        String tipoAtaqueStr = String.valueOf(tipoAtaque);
        String tipoDefensorStr = String.valueOf(tipoDefensor);
        
        return switch (tipoAtaqueStr) {
            case "AGUA" -> {
                if (tipoDefensorStr.equals("FUEGO")) yield 2.0;
                else if (tipoDefensorStr.equals("ELECTRICO") || tipoDefensorStr.equals("AGUA")) yield 0.5;
                else yield 1.0;
            }
            case "FUEGO" -> {
                if (tipoDefensorStr.equals("PLANTA")) yield 2.0;
                else if (tipoDefensorStr.equals("AGUA") || tipoDefensorStr.equals("FUEGO")) yield 0.5;
                else yield 1.0;
            }
            case "PLANTA" -> {
                if (tipoDefensorStr.equals("TIERRA")) yield 2.0;
                else if (tipoDefensorStr.equals("FUEGO") || tipoDefensorStr.equals("PLANTA")) yield 0.5;
                else yield 1.0;
            }
            case "TIERRA" -> {
                if (tipoDefensorStr.equals("ELECTRICO")) yield 2.0;
                else if (tipoDefensorStr.equals("PLANTA") || tipoDefensorStr.equals("TIERRA")) yield 0.5;
                else yield 1.0;
            }
            case "ELECTRICO" -> {
                if (tipoDefensorStr.equals("AGUA")) yield 2.0;
                else if (tipoDefensorStr.equals("TIERRA") || tipoDefensorStr.equals("ELECTRICO")) yield 0.5;
                else yield 1.0;
            }
            case "NORMAL" -> 1.0;
            default -> 1.0;
        };
    }

    /**
     * Crea una batalla permitiendo especificar dificultad y equipo humano para optimizar CPU
     */
    public BatallaDTO crearBatallaAleatoriaConDificultad(String modo, String difficulty, List<Pokemon> equipoHumano) {
        // Obtener todos los Pokémon disponibles
        List<Pokemon> todosLosPokemon = pokemonService.getAllPokemon();
        
        if (todosLosPokemon.size() < 3) {
            throw new RuntimeException("Se necesitan al menos 3 Pokémon en la base de datos para crear una batalla aleatoria");
        }
        
        // Crear copias de los Pokémon para la batalla (para no modificar los originales)
        List<Pokemon> equipo1, equipo2;
        
        // Si hay equipoHumano y dificultad es HARD, optimizar el equipo CPU
        if (equipoHumano != null && "HARD".equalsIgnoreCase(difficulty)) {
            System.out.println("=== CREANDO BATALLA DIFÍCIL CON EQUIPO OPTIMIZADO ===");
            equipo1 = equipoHumano; // El humano ya tiene su equipo
            equipo2 = seleccionarEquipoAntiHumano(todosLosPokemon, equipoHumano); // CPU optimizado
        } else {
            // Usar selección normal basándose en el modo
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
        }
        
        // Crear BatallaDTO
        BatallaDTO batallaOptimizada = new BatallaDTO();
        
        // Configurar equipos
        batallaOptimizada.setEntrenador1(equipo1);
        batallaOptimizada.setEntrenador2(equipo2);
        
        // Nombres específicos para batalla difícil
        if ("HARD".equalsIgnoreCase(difficulty)) {
            batallaOptimizada.setNombreEquipo1("Retador");
            batallaOptimizada.setNombreEquipo2("Elite CPU");
        } else {
            batallaOptimizada.setNombreEquipo1(NOMBRES_EQUIPO_1[(int)(Math.random() * NOMBRES_EQUIPO_1.length)]);
            batallaOptimizada.setNombreEquipo2(NOMBRES_EQUIPO_2[(int)(Math.random() * NOMBRES_EQUIPO_2.length)]);
        }
        
        // Configuración inicial
        batallaOptimizada.setTurno(1);
        batallaOptimizada.setUsarEfectoE1(false);
        batallaOptimizada.setUsarEfectoE2(false);
        
        // Configurar flags iniciales
        batallaOptimizada.setAtaqueReducidoEquipo1(false);
        batallaOptimizada.setAtaqueReducidoEquipo2(false);
        batallaOptimizada.setDefensaReducidaEquipo1(false);
        batallaOptimizada.setDefensaReducidaEquipo2(false);
        
        // Configurar efectos continuos iniciales
        batallaOptimizada.setEfectoContinuoEquipo1(null);
        batallaOptimizada.setEfectoContinuoEquipo2(null);
        
        // Contadores de turnos sin atacar para factor de agresividad
        batallaOptimizada.setTurnosSinAtacarEquipo1(0);
        batallaOptimizada.setTurnosSinAtacarEquipo2(0);
        
        return batallaOptimizada;
    }
    
    /**
     * Encuentra un Pokémon fallback cuando no se puede encontrar un counter óptimo
     * Prioriza Pokémon con buenas estadísticas y resistencias defensivas
     */
    private Pokemon encontrarFallbackPokemon(List<Pokemon> pokemonDisponibles, List<Pokemon> yaSeleccionados, List<Pokemon> equipoHumano) {
        Pokemon mejorFallback = null;
        double mejorPuntuacion = 0.0;
        
        for (Pokemon candidato : pokemonDisponibles) {
            // Evitar duplicados
            boolean yaEstaSeleccionado = yaSeleccionados.stream()
                .anyMatch(p -> p.getId().equals(candidato.getId()));
            if (yaEstaSeleccionado) continue;
            
            double puntuacion = evaluarFallbackPokemon(candidato, equipoHumano);
            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejorFallback = candidato;
            }
        }
        
        return mejorFallback;
    }
    
    /**
     * Evalúa un Pokémon como opción fallback basándose en supervivencia y stats
     */
    private double evaluarFallbackPokemon(Pokemon candidato, List<Pokemon> equipoHumano) {
        double puntuacion = 0.0;
        
        try {
            // 1. FACTOR DE SUPERVIVENCIA: Priorizar resistencias
            int resistenciasDefensivas = 0;
            int vulnerabilidadesCriticas = 0;
            
            for (Pokemon humano : equipoHumano) {
                try {
                    Ataque humanoAttack1 = ataqueService.getAtaqueById(humano.getIdAtaque1());
                    Ataque humanoAttack2 = ataqueService.getAtaqueById(humano.getIdAtaque2());
                    
                    if (humanoAttack1 != null) {
                        double efectividad = calcularEfectividadTipo(humanoAttack1.getTipoAtaque(), candidato.getTipoPokemon());
                        if (efectividad <= 0.5) {
                            resistenciasDefensivas++;
                            puntuacion += 60.0;
                        } else if (efectividad >= 2.0) {
                            vulnerabilidadesCriticas++;
                            puntuacion -= 50.0;
                        }
                    }
                    
                    if (humanoAttack2 != null) {
                        double efectividad = calcularEfectividadTipo(humanoAttack2.getTipoAtaque(), candidato.getTipoPokemon());
                        if (efectividad <= 0.5) {
                            resistenciasDefensivas++;
                            puntuacion += 60.0;
                        } else if (efectividad >= 2.0) {
                            vulnerabilidadesCriticas++;
                            puntuacion -= 50.0;
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores al obtener ataques
                }
            }
            
            // 2. BONUS POR MÚLTIPLES RESISTENCIAS
            if (resistenciasDefensivas >= 3) {
                puntuacion += 100.0; // Excelente tanque
            } else if (resistenciasDefensivas >= 2) {
                puntuacion += 50.0; // Buen tanque
            }
            
            // 3. PENALIZACIÓN POR MÚLTIPLES VULNERABILIDADES
            if (vulnerabilidadesCriticas >= 2) {
                puntuacion -= 80.0; // Muy frágil
            }
            
            // 4. STATS GENERALES (peso moderado)
            double statTotal = candidato.getVida() + candidato.getAtaque() + candidato.getDefensa();
            puntuacion += statTotal * 0.1; // Factor de stats reducido
            
            // 5. BONUS POR VIDA ALTA (supervivencia)
            if (candidato.getVida() >= 100) {
                puntuacion += 40.0;
            } else if (candidato.getVida() >= 80) {
                puntuacion += 20.0;
            }
            
            // 6. BONUS POR DEFENSA ALTA
            if (candidato.getDefensa() >= 80) {
                puntuacion += 30.0;
            } else if (candidato.getDefensa() >= 60) {
                puntuacion += 15.0;
            }
            
            // 7. FACTOR ALEATORIO PEQUEÑO
            puntuacion += (Math.random() - 0.5) * 10.0;
            
        } catch (Exception e) {
            System.err.println("Error evaluando fallback para " + candidato.getNombre() + ": " + e.getMessage());
            // Puntuación base mínima si hay error
            puntuacion = candidato.getVida() + candidato.getDefensa();
        }
        
        return puntuacion;
    }
    
    /**
     * Analiza la cobertura final del equipo CPU contra el equipo humano
     */
    private void analizarCoberturaEquipo(List<Pokemon> equipoCPU, List<Pokemon> equipoHumano) {
        System.out.println("📊 ANÁLISIS DE COBERTURA DEL EQUIPO CPU:");
        
        // Contar counters por cada Pokémon humano
        Map<String, Integer> countersDisponibles = new HashMap<>();
        
        for (Pokemon humano : equipoHumano) {
            String nombreHumano = humano.getNombre();
            int counters = 0;
            
            for (Pokemon cpu : equipoCPU) {
                boolean esCounter = esCounterEfectivo(cpu, humano);
                if (esCounter) {
                    counters++;
                }
            }
            
            countersDisponibles.put(nombreHumano, counters);
            
            if (counters >= 2) {
                System.out.println("  ✅ " + nombreHumano + " tiene " + counters + " counters (EXCELENTE)");
            } else if (counters == 1) {
                System.out.println("  ⚡ " + nombreHumano + " tiene " + counters + " counter (BUENO)");
            } else {
                System.out.println("  ⚠️ " + nombreHumano + " NO tiene counters efectivos (RIESGO)");
            }
        }
        
        // Estadísticas generales
        int totalCounters = countersDisponibles.values().stream().mapToInt(Integer::intValue).sum();
        double promedioCounters = (double) totalCounters / equipoHumano.size();
        
        System.out.println("\n📈 ESTADÍSTICAS DE COBERTURA:");
        System.out.println("  - Total de relaciones counter: " + totalCounters);
        System.out.println("  - Promedio de counters por Pokémon humano: " + String.format("%.1f", promedioCounters));
        
        if (promedioCounters >= 1.5) {
            System.out.println("  ⭐ COBERTURA EXCELENTE: El equipo CPU tiene ventaja estratégica");
        } else if (promedioCounters >= 1.0) {
            System.out.println("  ✓ COBERTURA BUENA: El equipo CPU está bien preparado");
        } else if (promedioCounters >= 0.5) {
            System.out.println("  ⚡ COBERTURA MODERADA: El equipo CPU tiene algunas ventajas");
        } else {
            System.out.println("  ⚠️ COBERTURA BAJA: El equipo CPU puede tener dificultades");
        }
        
        // Análisis de tipos del equipo CPU
        System.out.println("\n🏷️ DIVERSIDAD DE TIPOS CPU:");
        Map<Pokemon.TipoPokemon, Integer> tiposCPU = new HashMap<>();
        for (Pokemon cpu : equipoCPU) {
            tiposCPU.put(cpu.getTipoPokemon(), tiposCPU.getOrDefault(cpu.getTipoPokemon(), 0) + 1);
        }
        
        tiposCPU.forEach((tipo, cantidad) -> {
            System.out.println("  - " + tipo + ": " + cantidad + " Pokémon");
        });
        
        if (tiposCPU.size() == 3) {
            System.out.println("  ✅ DIVERSIDAD PERFECTA: 3 tipos diferentes");
        } else if (tiposCPU.size() == 2) {
            System.out.println("  ⚡ DIVERSIDAD BUENA: 2 tipos diferentes");
        } else {
            System.out.println("  ⚠️ DIVERSIDAD BAJA: Todos del mismo tipo");
        }
    }
    
    /**
     * Determina si un Pokémon CPU es un counter efectivo contra un Pokémon humano
     */
    private boolean esCounterEfectivo(Pokemon cpu, Pokemon humano) {
        try {
            // Verificar si CPU puede hacer daño super efectivo
            Ataque cpuAttack1 = ataqueService.getAtaqueById(cpu.getIdAtaque1());
            Ataque cpuAttack2 = ataqueService.getAtaqueById(cpu.getIdAtaque2());
            
            boolean tieneSuperEfectivo = false;
            
            if (cpuAttack1 != null) {
                double efectividad1 = calcularEfectividadTipo(cpuAttack1.getTipoAtaque(), humano.getTipoPokemon());
                if (efectividad1 >= 2.0) {
                    tieneSuperEfectivo = true;
                }
            }
            
            if (cpuAttack2 != null) {
                double efectividad2 = calcularEfectividadTipo(cpuAttack2.getTipoAtaque(), humano.getTipoPokemon());
                if (efectividad2 >= 2.0) {
                    tieneSuperEfectivo = true;
                }
            }
            
            // Verificar si CPU resiste ataques del humano
            boolean resisteAtaques = false;
            
            Ataque humanoAttack1 = ataqueService.getAtaqueById(humano.getIdAtaque1());
            Ataque humanoAttack2 = ataqueService.getAtaqueById(humano.getIdAtaque2());
            
            if (humanoAttack1 != null) {
                double resistencia1 = calcularEfectividadTipo(humanoAttack1.getTipoAtaque(), cpu.getTipoPokemon());
                if (resistencia1 <= 0.5) {
                    resisteAtaques = true;
                }
            }
            
            if (humanoAttack2 != null) {
                double resistencia2 = calcularEfectividadTipo(humanoAttack2.getTipoAtaque(), cpu.getTipoPokemon());
                if (resistencia2 <= 0.5) {
                    resisteAtaques = true;
                }
            }
            
            // Es counter si puede hacer super efectivo O resiste ataques
            return tieneSuperEfectivo || resisteAtaques;
            
        } catch (Exception e) {
            System.err.println("Error verificando counter effectiveness: " + e.getMessage());
            return false;
        }
    }
}
