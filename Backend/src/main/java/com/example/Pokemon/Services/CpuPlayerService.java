package com.example.Pokemon.Services;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.DTO.CpuActionResponse;
import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Repositories.AtaqueRepository;
import com.example.Pokemon.Repositories.EfectoRepository;
import com.example.Pokemon.Utils.TablaEfectividades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CpuPlayerService {

    private final Random random = new Random();
    
    @Autowired
    private AtaqueRepository ataqueRepository;
    
    @Autowired
    private EfectoRepository efectoRepository;
    
    @Autowired
    private PokemonService pokemonService;

    /**
     * Decide la mejor acción para la CPU basándose en el estado actual de la batalla
     */
    public CpuActionResponse decideBestAction(BatallaDTO batalla, String difficulty, boolean cpuIsTeam1) {
        try {
            // Obtener equipos
            List<Pokemon> cpuTeam = cpuIsTeam1 ? batalla.getEntrenador1() : batalla.getEntrenador2();
            List<Pokemon> enemyTeam = cpuIsTeam1 ? batalla.getEntrenador2() : batalla.getEntrenador1();

            // Para dificultad HARD, usar directamente Minimax sin necesidad de attackerIndex
            if ("HARD".equalsIgnoreCase(difficulty)) {
                return decideHardActionWithMinimax(cpuTeam, enemyTeam, batalla);
            }

            // Para otras dificultades, encontrar un Pokémon vivo de la CPU para atacar
            int attackerIndex = findRandomAlivePokemon(cpuTeam);
            if (attackerIndex == -1) {
                return createErrorResponse("No hay Pokémon vivos en el equipo CPU");
            }

            // Decidir acción basándose en la dificultad
            return switch (difficulty.toUpperCase()) {
                case "EASY" -> decideEasyAction(attackerIndex, cpuTeam, enemyTeam);
                default -> decideNormalAction(attackerIndex, cpuTeam, enemyTeam, batalla);
            };

        } catch (Exception e) {
            System.err.println("Error en CPU decision: " + e.getMessage());
            return createErrorResponse("Error interno en la decisión de CPU");
        }
    }

    /**
     * Lógica FÁCIL: Completamente aleatoria
     */
    private CpuActionResponse decideEasyAction(int attackerIndex, List<Pokemon> cpuTeam, List<Pokemon> enemyTeam) {
        // 80% ataques, 20% efectos
        boolean useEffect = random.nextDouble() < 0.2;
        
        if (useEffect) {
            CpuActionResponse response = new CpuActionResponse(attackerIndex, 0, true, 0, "CPU Fácil: Usar efecto aleatorio");
            response.setSuccess(true);
            return response;
        } else {
            int targetIndex = findRandomAliveTarget(enemyTeam);
            int attackIndex = random.nextInt(2); // Ataque 0 o 1
            CpuActionResponse response = new CpuActionResponse(attackerIndex, targetIndex, false, attackIndex, 
                "CPU Fácil: Ataque aleatorio");
            response.setSuccess(true);
            return response;
        }
    }

    /**
     * Lógica NORMAL: Básica pero sensata
     */
    public CpuActionResponse decideNormalAction(int attackerIndex, List<Pokemon> cpuTeam,
                                                                  List<Pokemon> enemyTeam, BatallaDTO batalla) {
        try {
            // Determinar si la CPU es equipo 1 o 2
            boolean cpuIsTeam1 = batalla != null && cpuTeam.equals(batalla.getEntrenador1());

            // DEBUG: Información de estado actual
            System.out.println("=== DEBUG CPU EVALUACIÓN ===");
            System.out.println("CPU es equipo 1: " + cpuIsTeam1);
            if (batalla != null) {
                System.out.println("Turnos sin atacar Equipo 1: " + batalla.getTurnosSinAtacarEquipo1());
                System.out.println("Turnos sin atacar Equipo 2: " + batalla.getTurnosSinAtacarEquipo2());
                System.out.println("Turno actual: " + batalla.getTurno());
            }

            // Encontrar el Pokemon enemigo más amenazante
            int mostThreateningIndex = -1;
            double highestThreat = 0.0;

            for (int i = 0; i < enemyTeam.size(); i++) {
                Pokemon enemy = enemyTeam.get(i);
                if (enemy.getVida() <= 0) continue;

                double threat = evaluatePokemonThreat(enemy, cpuTeam);
                System.out.println("Amenaza de " + enemy.getNombre() + ": " + threat);
                if (threat > highestThreat) {
                    highestThreat = threat;
                    mostThreateningIndex = i;
                } else if (mostThreateningIndex == -1) {
                    // Si no hay amenaza específica, al menos seleccionar el primer enemigo vivo
                    mostThreateningIndex = i;
                }
            }

            // Factor de agresividad: +15 por cada turno sin atacar
            double aggressionFactor = 0.0;
            if (batalla != null) {
                int turnosSinAtacar = cpuIsTeam1 ? batalla.getTurnosSinAtacarEquipo1() : batalla.getTurnosSinAtacarEquipo2();
                aggressionFactor = turnosSinAtacar * 15.0;
                highestThreat += aggressionFactor;

                System.out.println("Factor de agresividad:");
                System.out.println("  - Turnos sin atacar: " + turnosSinAtacar);
                System.out.println("  - Multiplicador: 5.0");
                System.out.println("  - Factor total: " + aggressionFactor);
                System.out.println("Amenaza antes agresividad: " + (highestThreat - aggressionFactor));
                System.out.println("Amenaza TOTAL: " + highestThreat);
            }

            // Umbral de amenaza (ajustado para evaluación individual)
            double threatThreshold = 35.0;

            // Verificar si hay factor de agresividad que debe forzar ataque
            boolean shouldForceAttack = aggressionFactor >= threatThreshold;

            System.out.println("Evaluación de decisión:");
            System.out.println("  - Amenaza base más alta: " + (highestThreat - aggressionFactor));
            System.out.println("  - Factor de agresividad: " + aggressionFactor);
            System.out.println("  - Amenaza TOTAL: " + highestThreat);
            System.out.println("  - Umbral para atacar: " + threatThreshold);
            System.out.println("  - ¿Debe forzar ataque por agresividad?: " + shouldForceAttack);
            System.out.println("  - Objetivo disponible (mostThreateningIndex): " + mostThreateningIndex);

            // Decidir acción basándose en la mayor amenaza individual
            if ((highestThreat >= threatThreshold || shouldForceAttack) && mostThreateningIndex != -1) {
                // Amenaza alta: Buscar el MEJOR atacante y ataque de todo el equipo CPU
                BestAttackResult bestAttack = findBestAttackAgainstTarget(mostThreateningIndex, cpuTeam, enemyTeam);

                if (bestAttack != null) {
                    CpuActionResponse response = new CpuActionResponse(bestAttack.attackerIndex, mostThreateningIndex, false, bestAttack.attackIndex,
                            String.format("CPU Difícil: Amenaza alta (%.1f >= %.1f) - ATACAR a %s con %s (%.1f daño)",
                                    highestThreat, threatThreshold, enemyTeam.get(mostThreateningIndex).getNombre(),
                                    bestAttack.attackName, bestAttack.damage));
                    response.setSuccess(true);
                    System.out.println("DECISIÓN: ATACAR (amenaza alta) con " + bestAttack.attackerName + " usando " + bestAttack.attackName);
                    return response;
                } else {
                    // Fallback si no se encuentra ataque válido
                    int attackIndex = selectBestAttackAgainst(attackerIndex, mostThreateningIndex, cpuTeam, enemyTeam);
                    CpuActionResponse response = new CpuActionResponse(attackerIndex, mostThreateningIndex, false, attackIndex,
                            String.format("CPU Difícil: Amenaza alta (%.1f >= %.1f) - ATACAR a %s (fallback)",
                                    highestThreat, threatThreshold, enemyTeam.get(mostThreateningIndex).getNombre()));
                    response.setSuccess(true);
                    System.out.println("DECISIÓN: ATACAR (amenaza alta - fallback)");
                    return response;
                }
            } else {
                // Amenaza baja: situación segura, usar efecto para obtener ventaja
                int targetIndex = findBestDefensiveTarget(cpuTeam, enemyTeam);
                CpuActionResponse response = new CpuActionResponse(attackerIndex, targetIndex, true, 0,
                        String.format("CPU Difícil: Amenaza baja (%.1f < %.1f) - Usar efecto (situación segura)",
                                highestThreat, threatThreshold));
                response.setSuccess(true);
                System.out.println("DECISIÓN: USAR EFECTO (amenaza baja)");
                return response;
            }

        } catch (Exception e) {
            System.err.println("Error en evaluación de amenazas: " + e.getMessage());
            e.printStackTrace();
            // Fallback a lógica normal
            return decideEasyAction(attackerIndex, cpuTeam, enemyTeam);
        }
    }


    /**
     * Clase para almacenar el resultado del mejor ataque
     */
    private static class BestAttackResult {
        int attackerIndex;
        int attackIndex;
        double damage;
        String attackerName;
        String attackName;
        
        BestAttackResult(int attackerIndex, int attackIndex, double damage, String attackerName, String attackName) {
            this.attackerIndex = attackerIndex;
            this.attackIndex = attackIndex;
            this.damage = damage;
            this.attackerName = attackerName;
            this.attackName = attackName;
        }
    }
    
    /**
     * Encuentra el mejor atacante y ataque de todo el equipo CPU contra un objetivo específico
     */
    private BestAttackResult findBestAttackAgainstTarget(int targetIndex, List<Pokemon> cpuTeam, List<Pokemon> enemyTeam) {
        Pokemon target = enemyTeam.get(targetIndex);
        BestAttackResult bestResult = null;
        double maxDamage = 0.0;
        
        System.out.println("=== Evaluando TODOS los ataques del equipo CPU contra " + target.getNombre() + " ===");
        
        // Evaluar cada Pokémon del equipo CPU
        for (int pokemonIndex = 0; pokemonIndex < cpuTeam.size(); pokemonIndex++) {
            Pokemon attacker = cpuTeam.get(pokemonIndex);
            
            // Solo considerar Pokémon vivos
            if (attacker.getVida() <= 0) {
                continue;
            }
            
            System.out.println("Evaluando ataques de " + attacker.getNombre() + ":");
            
            // Obtener ataques del atacante
            Ataque attack1 = ataqueRepository.findById(attacker.getIdAtaque1()).orElse(null);
            Ataque attack2 = ataqueRepository.findById(attacker.getIdAtaque2()).orElse(null);
            
            // Evaluar ataque 1
            if (attack1 != null) {
                // Verificar efectividad antes de considerar el ataque
                double effectiveness1 = calculateTypeEffectiveness(attack1.getTipoAtaque(), target.getTipoPokemon());
                
                if (effectiveness1 == 0.0) {
                    System.out.println("  - " + attack1.getNombre() + " (" + attack1.getTipoAtaque() + "): SIN EFECTO contra " + target.getTipoPokemon() + " - DESCARTADO");
                } else {
                    double damage1 = calculateDamage(attacker, target, attack1);
                    System.out.println("  - " + attack1.getNombre() + " (" + attack1.getTipoAtaque() + "): " + String.format("%.1f", damage1) + " daño (efectividad: " + effectiveness1 + "x)");
                    
                    if (damage1 > maxDamage) {
                        maxDamage = damage1;
                        bestResult = new BestAttackResult(pokemonIndex, 0, damage1, attacker.getNombre(), attack1.getNombre());
                    }
                }
            }
            
            // Evaluar ataque 2
            if (attack2 != null) {
                // Verificar efectividad antes de considerar el ataque
                double effectiveness2 = calculateTypeEffectiveness(attack2.getTipoAtaque(), target.getTipoPokemon());
                
                if (effectiveness2 == 0.0) {
                    System.out.println("  - " + attack2.getNombre() + " (" + attack2.getTipoAtaque() + "): SIN EFECTO contra " + target.getTipoPokemon() + " - DESCARTADO");
                } else {
                    double damage2 = calculateDamage(attacker, target, attack2);
                    System.out.println("  - " + attack2.getNombre() + " (" + attack2.getTipoAtaque() + "): " + String.format("%.1f", damage2) + " daño (efectividad: " + effectiveness2 + "x)");
                    
                    if (damage2 > maxDamage) {
                        maxDamage = damage2;
                        bestResult = new BestAttackResult(pokemonIndex, 1, damage2, attacker.getNombre(), attack2.getNombre());
                    }
                }
            }
        }
        
        if (bestResult != null) {
            System.out.println("MEJOR OPCIÓN: " + bestResult.attackerName + " con " + bestResult.attackName + 
                             " (" + String.format("%.1f", bestResult.damage) + " daño)");
        } else {
            System.out.println("No se encontró ningún ataque válido");
        }
        
        return bestResult;
    }
    
    /**
     * Evalúa la amenaza de un Pokemon específico
     */
    private double evaluatePokemonThreat(Pokemon enemy, List<Pokemon> cpuTeam) {
        double threat = 0.0;
        
        try {
            // Obtener ataques del enemigo
            Ataque attack1 = ataqueRepository.findById(enemy.getIdAtaque1()).orElse(null);
            Ataque attack2 = ataqueRepository.findById(enemy.getIdAtaque2()).orElse(null);
            Efecto effect = efectoRepository.findById(enemy.getIdEfecto()).orElse(null);
            
            // Evaluar contra cada Pokemon de nuestro equipo
            for (Pokemon myPokemon : cpuTeam) {
                if (myPokemon.getVida() <= 0) continue;
                
                // 1. Pokemon rival puede derrotar de un golpe: +50 puntos
                if (canOneHitKill(enemy, myPokemon, attack1) || canOneHitKill(enemy, myPokemon, attack2)) {
                    threat += 50.0;
                }
                
                // 2. Pokemon rival tiene movimiento super eficaz: +40 puntos  
                // 8. Pokemon rival tiene movimiento super eficaz pero < 50% vida: -20 puntos
                boolean hasSuperEffective = isSuperEffective(attack1, myPokemon) || isSuperEffective(attack2, myPokemon);
                if (hasSuperEffective) {
                    double maxDamage = Math.max(
                        calculateDamage(enemy, myPokemon, attack1),
                        calculateDamage(enemy, myPokemon, attack2)
                    );
                    if (maxDamage >= myPokemon.getVida() * 0.5) {
                        threat += 40.0;
                    } else {
                        threat -= 20.0;
                    }
                }
                
                // 3. Pokemon rival puede quitar más del 50% de vida: +30 puntos
                double maxDamageOverall = Math.max(
                    calculateDamage(enemy, myPokemon, attack1),
                    calculateDamage(enemy, myPokemon, attack2)
                );
                if (maxDamageOverall >= myPokemon.getVida() * 0.5 && !hasSuperEffective) {
                    threat += 30.0;
                }
                
                // 9. Pokemon rival no puede quitar 50% de vida con ningún movimiento: -30 puntos
                if (maxDamageOverall < myPokemon.getVida() * 0.5) {
                    threat -= 30.0;
                }
            }
            
            // 4. Pokemon rival puede envenenar (daño continuo): +15 puntos
            if (effect != null && effect.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                threat += 15.0;
            }
            
            // 5. Pokemon rival puede subir ataque o bajar defensa: +10 puntos
            if (effect != null && (effect.getTipoEfecto() == Efecto.tipoEfecto.SUBIR_ATAQUE_PROPIO || 
                                 effect.getTipoEfecto() == Efecto.tipoEfecto.BAJAR_DEFENSA_RIVAL)) {
                threat += 10.0;
            }
            
            // 6. Pokemon rival puede subir defensa o bajar ataque: +5 puntos
            if (effect != null && (effect.getTipoEfecto() == Efecto.tipoEfecto.SUBIR_DEFENSA_PROPIO || 
                                 effect.getTipoEfecto() == Efecto.tipoEfecto.BAJAR_ATAQUE_RIVAL)) {
                threat += 5.0;
            }
            
        } catch (Exception e) {
            System.err.println("Error evaluando Pokemon " + enemy.getNombre() + ": " + e.getMessage());
        }
        
        return threat;
    }
    
    /**
     * Verifica si un ataque puede hacer one-hit kill
     */
    private boolean canOneHitKill(Pokemon attacker, Pokemon defender, Ataque attack) {
        if (attack == null) return false;
        double damage = calculateDamage(attacker, defender, attack);
        return damage >= defender.getVida();
    }
    
    /**
     * Verifica si un ataque es super efectivo (efectividad >= 2.0)
     */
    private boolean isSuperEffective(Ataque attack, Pokemon defender) {
        if (attack == null) return false;
        
        double efectividad = calculateTypeEffectiveness(attack.getTipoAtaque(), defender.getTipoPokemon());
        return efectividad >= 2.0;
    }
    
    /**
     * Calcula el daño de un ataque usando el método del PokemonService
     */
    private double calculateDamage(Pokemon attacker, Pokemon defender, Ataque attack) {
        if (attack == null) return 0.0;
        
        // Calcular STAB (Same Type Attack Bonus)
        double stab = 1.0;
        String tipoUsuario = String.valueOf(attacker.getTipoPokemon());
        String tipoAtaque = String.valueOf(attack.getTipoAtaque());
        if (tipoUsuario.equals(tipoAtaque)) {
            stab = 1.5;
        }
        
        // Calcular efectividad
        double efectividad = calculateTypeEffectiveness(attack.getTipoAtaque(), defender.getTipoPokemon());
        
        // Usar estadísticas efectivas
        Long ataqueEfectivo = attacker.getAtaqueModificado() != null ? attacker.getAtaqueModificado() : attacker.getAtaque();
        Long defensaEfectiva = defender.getDefensaModificada() != null ? defender.getDefensaModificada() : defender.getDefensa();
        
        // Usar el método del PokemonService
        Long dano = pokemonService.calcularDano(ataqueEfectivo, defensaEfectiva, stab, efectividad, attack.getPotencia());
        
        return dano.doubleValue();
    }
    
    /**
     * Calcula la efectividad de tipos usando la tabla completa de efectividades
     */
    private double calculateTypeEffectiveness(Ataque.TipoAtaque tipoAtaque, Pokemon.TipoPokemon tipoDefensor) {
        // Convertir TipoAtaque a TipoPokemon para usar con TablaEfectividades
        Pokemon.TipoPokemon tipoAtaquePokemon;
        try {
            tipoAtaquePokemon = Pokemon.TipoPokemon.valueOf(tipoAtaque.name());
        } catch (IllegalArgumentException e) {
            System.err.println("Tipo de ataque no reconocido: " + tipoAtaque);
            return 1.0; // Efectividad neutral por defecto
        }
        
        // Usar la tabla de efectividades completa
        double efectividad = TablaEfectividades.getEfectividad(tipoAtaquePokemon, tipoDefensor);
        
        // Debug para verificar las efectividades
        if (efectividad != 1.0) {
            System.out.println("DEBUG: " + tipoAtaque + " vs " + tipoDefensor + " = " + efectividad + "x");
        }
        
        return efectividad;
    }
    
    /**
     * Encuentra el mejor objetivo para efectos defensivos
     */
    private int findBestDefensiveTarget(List<Pokemon> cpuTeam, List<Pokemon> enemyTeam) {
        // Por ahora, usa el objetivo más débil
        return findWeakestTarget(enemyTeam);
    }
    
    /**
     * Selecciona el mejor ataque para usar contra un objetivo específico
     */
    private int selectBestAttackAgainst(int attackerIndex, int targetIndex, List<Pokemon> cpuTeam, List<Pokemon> enemyTeam) {
        try {
            Pokemon attacker = cpuTeam.get(attackerIndex);
            Pokemon target = enemyTeam.get(targetIndex);
            
            Ataque attack1 = ataqueRepository.findById(attacker.getIdAtaque1()).orElse(null);
            Ataque attack2 = ataqueRepository.findById(attacker.getIdAtaque2()).orElse(null);
            
            if (attack1 == null && attack2 == null) return 0;
            
            // Verificar efectividades primero
            double effectiveness1 = attack1 != null ? calculateTypeEffectiveness(attack1.getTipoAtaque(), target.getTipoPokemon()) : -1.0;
            double effectiveness2 = attack2 != null ? calculateTypeEffectiveness(attack2.getTipoAtaque(), target.getTipoPokemon()) : -1.0;
            
            // Si solo un ataque está disponible
            if (attack1 == null) {
                if (effectiveness2 == 0.0) {
                    System.out.println("ADVERTENCIA: Único ataque disponible (" + attack2.getNombre() + ") no tiene efecto");
                }
                return 1;
            }
            if (attack2 == null) {
                if (effectiveness1 == 0.0) {
                    System.out.println("ADVERTENCIA: Único ataque disponible (" + attack1.getNombre() + ") no tiene efecto");
                }
                return 0;
            }
            
            // Si uno de los ataques no tiene efecto, usar el otro
            if (effectiveness1 == 0.0 && effectiveness2 != 0.0) {
                System.out.println("EVITANDO ATAQUE SIN EFECTO: " + attack1.getNombre() + " (" + attack1.getTipoAtaque() + ") vs " + target.getTipoPokemon() + ", usando " + attack2.getNombre());
                return 1;
            }
            if (effectiveness2 == 0.0 && effectiveness1 != 0.0) {
                System.out.println("EVITANDO ATAQUE SIN EFECTO: " + attack2.getNombre() + " (" + attack2.getTipoAtaque() + ") vs " + target.getTipoPokemon() + ", usando " + attack1.getNombre());
                return 0;
            }
            
            // Si ambos no tienen efecto, elegir el primero (aunque no haga daño)
            if (effectiveness1 == 0.0 && effectiveness2 == 0.0) {
                System.out.println("PROBLEMA: Ambos ataques sin efecto contra " + target.getTipoPokemon() + ", usando " + attack1.getNombre() + " por defecto");
                return 0;
            }
            
            // Calcular daño de cada ataque
            double damage1 = calculateDamage(attacker, target, attack1);
            double damage2 = calculateDamage(attacker, target, attack2);
            
            // Información de debug mejorada
            System.out.println(String.format("Evaluando ataques contra %s (%s):", target.getNombre(), target.getTipoPokemon()));
            System.out.println(String.format("  - %s (%s): %.1f daño (efectividad: %.1fx)", attack1.getNombre(), attack1.getTipoAtaque(), damage1, effectiveness1));
            System.out.println(String.format("  - %s (%s): %.1f daño (efectividad: %.1fx)", attack2.getNombre(), attack2.getTipoAtaque(), damage2, effectiveness2));
            
            // Seleccionar el ataque que haga más daño
            int selectedAttack = damage1 >= damage2 ? 0 : 1;
            String selectedName = selectedAttack == 0 ? attack1.getNombre() : attack2.getNombre();
            double selectedDamage = selectedAttack == 0 ? damage1 : damage2;
            double selectedEffectiveness = selectedAttack == 0 ? effectiveness1 : effectiveness2;
            
            System.out.println(String.format("  -> Seleccionado: %s (%.1f daño, %.1fx efectividad)", selectedName, selectedDamage, selectedEffectiveness));
            
            return selectedAttack;
            
        } catch (Exception e) {
            System.err.println("Error seleccionando mejor ataque: " + e.getMessage());
            return random.nextInt(2); // Fallback aleatorio
        }
    }

    /**
     * Encuentra un Pokémon vivo aleatorio en el equipo
     */
    private int findRandomAlivePokemon(List<Pokemon> team) {
        // Crear lista de índices de Pokémon vivos
        var aliveIndices = new java.util.ArrayList<Integer>();
        for (int i = 0; i < team.size(); i++) {
            if (team.get(i).getVida() > 0) {
                aliveIndices.add(i);
            }
        }
        
        if (aliveIndices.isEmpty()) {
            return -1; // No hay Pokémon vivos
        }
        
        // Seleccionar uno aleatorio
        int randomIndex = random.nextInt(aliveIndices.size());
        return aliveIndices.get(randomIndex);
    }

    /**
     * Encuentra un objetivo aleatorio que esté vivo
     */
    private int findRandomAliveTarget(List<Pokemon> enemyTeam) {
        // Crear lista de índices de Pokémon vivos
        var aliveIndices = new java.util.ArrayList<Integer>();
        for (int i = 0; i < enemyTeam.size(); i++) {
            if (enemyTeam.get(i).getVida() > 0) {
                aliveIndices.add(i);
            }
        }
        
        if (aliveIndices.isEmpty()) {
            return 0; // Fallback
        }
        
        return aliveIndices.get(random.nextInt(aliveIndices.size()));
    }

    /**
     * Encuentra el objetivo con menos vida
     */
    private int findWeakestTarget(List<Pokemon> enemyTeam) {
        int weakestIndex = 0;
        long lowestHp = Long.MAX_VALUE;
        
        for (int i = 0; i < enemyTeam.size(); i++) {
            Pokemon pokemon = enemyTeam.get(i);
            if (pokemon.getVida() > 0 && pokemon.getVida() < lowestHp) {
                lowestHp = pokemon.getVida();
                weakestIndex = i;
            }
        }
        
        return weakestIndex;
    }

    // ========================================
    // MINIMAX ALGORITHM CLASSES Y METHODS
    // ========================================
    
    /**
     * Representa una acción posible en el juego (ataque o efecto)
     */
    private static class GameAction {
        int attackerIndex;
        int targetIndex;
        boolean isEffect;
        int attackIndex; // 0 o 1 para ataques, 0 para efecto
        String description;
        
        GameAction(int attackerIndex, int targetIndex, boolean isEffect, int attackIndex, String description) {
            this.attackerIndex = attackerIndex;
            this.targetIndex = targetIndex;
            this.isEffect = isEffect;
            this.attackIndex = attackIndex;
            this.description = description;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    /**
     * Representa el estado completo de la batalla para simulación
     */
    private static class GameState {
        List<Pokemon> cpuTeam;
        List<Pokemon> enemyTeam;
        int turn;
        boolean cpuTurn;
        double evaluation;
        
        GameState(List<Pokemon> cpuTeam, List<Pokemon> enemyTeam, int turn, boolean cpuTurn) {
            // Crear copias profundas para no afectar el estado original
            this.cpuTeam = deepCopyTeam(cpuTeam);
            this.enemyTeam = deepCopyTeam(enemyTeam);
            this.turn = turn;
            this.cpuTurn = cpuTurn;
            this.evaluation = 0.0;
        }
        
        /**
         * Crea una copia profunda de un equipo Pokemon
         */
        private static List<Pokemon> deepCopyTeam(List<Pokemon> originalTeam) {
            List<Pokemon> copy = new ArrayList<>();
            for (Pokemon original : originalTeam) {
                Pokemon pokemonCopy = new Pokemon();
                pokemonCopy.setId(original.getId());
                pokemonCopy.setNombre(original.getNombre());
                pokemonCopy.setVida(original.getVida());
                pokemonCopy.setAtaque(original.getAtaque());
                pokemonCopy.setDefensa(original.getDefensa());
                pokemonCopy.setAtaqueModificado(original.getAtaqueModificado());
                pokemonCopy.setDefensaModificada(original.getDefensaModificada());
                pokemonCopy.setTipoPokemon(original.getTipoPokemon());
                pokemonCopy.setIdAtaque1(original.getIdAtaque1());
                pokemonCopy.setIdAtaque2(original.getIdAtaque2());
                pokemonCopy.setIdEfecto(original.getIdEfecto());
                copy.add(pokemonCopy);
            }
            return copy;
        }
        
        /**
         * Verifica si el juego ha terminado
         */
        boolean isGameOver() {
            return getAlivePokemonCount(cpuTeam) == 0 || getAlivePokemonCount(enemyTeam) == 0;
        }
        
        /**
         * Cuenta Pokemon vivos en un equipo
         */
        private int getAlivePokemonCount(List<Pokemon> team) {
            return (int) team.stream().filter(p -> p.getVida() > 0).count();
        }
        
        /**
         * Determina el ganador del juego
         * @return 1 si gana CPU, -1 si gana enemigo, 0 si empate
         */
        int getWinner() {
            int cpuAlive = getAlivePokemonCount(cpuTeam);
            int enemyAlive = getAlivePokemonCount(enemyTeam);
            
            if (cpuAlive > 0 && enemyAlive == 0) return 1; // CPU gana
            if (enemyAlive > 0 && cpuAlive == 0) return -1; // Enemigo gana
            return 0; // Empate o juego continúa
        }
    }
    
    /**
     * Resultado del algoritmo Minimax
     */
    private static class MinimaxResult {
        GameAction bestAction;
        double evaluation;
        int nodesEvaluated;
        int pruningCuts;
        
        MinimaxResult(GameAction bestAction, double evaluation, int nodesEvaluated, int pruningCuts) {
            this.bestAction = bestAction;
            this.evaluation = evaluation;
            this.nodesEvaluated = nodesEvaluated;
            this.pruningCuts = pruningCuts;
        }
    }
    
    /**
     * Motor principal del algoritmo Minimax con poda alfa-beta
     */
    private MinimaxResult runMinimax(GameState state, int depth, double alpha, double beta, boolean isMaximizing, 
                                   long startTime, long maxTimeMs) {
        // Contadores para estadísticas
        int[] nodeCount = {1};
        int[] pruneCount = {0};
        
        // Verificar límite de tiempo
        if (System.currentTimeMillis() - startTime > maxTimeMs) {
            return new MinimaxResult(null, evaluateGameState(state), nodeCount[0], pruneCount[0]);
        }
        
        // Caso base: profundidad 0 o juego terminado
        if (depth == 0 || state.isGameOver()) {
            double eval = evaluateGameState(state);
            return new MinimaxResult(null, eval, nodeCount[0], pruneCount[0]);
        }
        
        List<GameAction> possibleActions = generatePossibleActions(state, isMaximizing);
        
        // Poda inteligente: limitar acciones basándose en la profundidad
        possibleActions = pruneActions(possibleActions, state, Math.max(5 - depth, 2));
        
        GameAction bestAction = null;
        double bestValue = isMaximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        
        for (GameAction action : possibleActions) {
            // Aplicar la acción y crear nuevo estado
            GameState newState = applyAction(state, action);
            nodeCount[0]++;
            
            // Llamada recursiva
            MinimaxResult result = runMinimax(newState, depth - 1, alpha, beta, !isMaximizing, startTime, maxTimeMs);
            
            // Actualizar contadores
            nodeCount[0] += result.nodesEvaluated;
            pruneCount[0] += result.pruningCuts;
            
            // Evaluar si es mejor opción
            if (isMaximizing) {
                if (result.evaluation > bestValue) {
                    bestValue = result.evaluation;
                    bestAction = action;
                }
                alpha = Math.max(alpha, result.evaluation);
            } else {
                if (result.evaluation < bestValue) {
                    bestValue = result.evaluation;
                    bestAction = action;
                }
                beta = Math.min(beta, result.evaluation);
            }
            
            // Poda alfa-beta
            if (beta <= alpha) {
                pruneCount[0]++;
                break; // Cortar rama
            }
        }
        
        return new MinimaxResult(bestAction, bestValue, nodeCount[0], pruneCount[0]);
    }
    
    /**
     * Función de evaluación sofisticada del estado del juego
     */
    private double evaluateGameState(GameState state) {
        double score = 0.0;
        
        // 1. Victoria/Derrota inmediata (peso máximo)
        int winner = state.getWinner();
        if (winner == 1) return 10000.0; // CPU gana
        if (winner == -1) return -10000.0; // CPU pierde
        
        // 2. Detectar situaciones de "mate" (victoria en 1 turno)
        double mateScore = evaluateMateInOne(state);
        if (mateScore != 0) {
            return mateScore; // Prioridad absoluta sobre cualquier otra consideración
        }
        
        // 3. NUEVA EVALUACIÓN DE SUPERVIVENCIA - Evaluar si la CPU está en peligro inmediato
        double survivalScore = evaluateSurvivalThreat(state);
        if (Math.abs(survivalScore) > 1000.0) {
            // Si hay una amenaza crítica de supervivencia, priorizar esto sobre todo lo demás
            return survivalScore;
        }

        // 4. Diferencia de vida total
        long cpuTotalHP = state.cpuTeam.stream().mapToLong(Pokemon::getVida).sum();
        long enemyTotalHP = state.enemyTeam.stream().mapToLong(Pokemon::getVida).sum();
        score += (cpuTotalHP - enemyTotalHP) * 3.0;
        
        // 5. Número de Pokemon vivos (factor crítico)
        long cpuAlive = state.cpuTeam.stream().filter(p -> p.getVida() > 0).count();
        long enemyAlive = state.enemyTeam.stream().filter(p -> p.getVida() > 0).count();
        score += (cpuAlive - enemyAlive) * 500.0;
        
        // 6. MEJORADO: Bonificación por Pokemon enemigos con poca vida y evaluación de amenazas
        double cpuThreatPotential = 0.0;
        double enemyThreatPotential = 0.0;
        
        for (Pokemon enemy : state.enemyTeam) {
            if (enemy.getVida() > 0) {
                // Usar vida actual como base si vidaBase es null
                Long vidaMaxima = enemy.getVidaBase() != null ? enemy.getVidaBase() : enemy.getVida();
                double healthPercent = (double) enemy.getVida() / (double) vidaMaxima;
                
                if (healthPercent < 0.3) { // Menos del 30% de vida
                    score += (1.0 - healthPercent) * 400.0; // AUMENTADO: Priorizar más eliminar enemigos débiles
                } else if (healthPercent < 0.6) {
                    score += (0.6 - healthPercent) * 200.0; // Bonificar enemigos medianamente dañados
                }
                
                // NUEVO: Evaluar qué tan peligroso es este enemigo para nosotros
                try {
                    Ataque enemyAttack1 = ataqueRepository.findById(enemy.getIdAtaque1()).orElse(null);
                    Ataque enemyAttack2 = ataqueRepository.findById(enemy.getIdAtaque2()).orElse(null);
                    
                    // Calcular daño potencial máximo que puede hacer este enemigo
                    double maxDamageToUs = 0.0;
                    for (Pokemon ourPokemon : state.cpuTeam) {
                        if (ourPokemon.getVida() <= 0) continue;
                        
                        if (enemyAttack1 != null) {
                            double damage1 = calculateDamage(enemy, ourPokemon, enemyAttack1);
                            maxDamageToUs = Math.max(maxDamageToUs, damage1);
                        }
                        if (enemyAttack2 != null) {
                            double damage2 = calculateDamage(enemy, ourPokemon, enemyAttack2);
                            maxDamageToUs = Math.max(maxDamageToUs, damage2);
                        }
                    }
                    enemyThreatPotential += maxDamageToUs * healthPercent; // Escalar por vida restante
                } catch (Exception e) {
                    // Ignorar errores
                }
            }
        }
        
        // NUEVO: Evaluar nuestro potencial ofensivo
        for (Pokemon ourPokemon : state.cpuTeam) {
            if (ourPokemon.getVida() <= 0) continue;
            
            try {
                Ataque ourAttack1 = ataqueRepository.findById(ourPokemon.getIdAtaque1()).orElse(null);
                Ataque ourAttack2 = ataqueRepository.findById(ourPokemon.getIdAtaque2()).orElse(null);
                
                // Calcular daño potencial máximo que podemos hacer
                double maxDamageByUs = 0.0;
                for (Pokemon enemy : state.enemyTeam) {
                    if (enemy.getVida() <= 0) continue;
                    
                    if (ourAttack1 != null) {
                        double damage1 = calculateDamage(ourPokemon, enemy, ourAttack1);
                        maxDamageByUs = Math.max(maxDamageByUs, damage1);
                    }
                    if (ourAttack2 != null) {
                        double damage2 = calculateDamage(ourPokemon, enemy, ourAttack2);
                        maxDamageByUs = Math.max(maxDamageByUs, damage2);
                    }
                }
                
                Long vidaMaxima = ourPokemon.getVidaBase() != null ? ourPokemon.getVidaBase() : ourPokemon.getVida();
                double healthPercent = (double) ourPokemon.getVida() / (double) vidaMaxima;
                cpuThreatPotential += maxDamageByUs * healthPercent; // Escalar por vida restante
            } catch (Exception e) {
                // Ignorar errores
            }
        }
        
        // Bonificar si tenemos más potencial ofensivo
        score += (cpuThreatPotential - enemyThreatPotential) * 2.0;
        
        // 7. Pokemon con ventaja de stats (buffs/debuffs) - PESO REDUCIDO
        for (Pokemon cpuPokemon : state.cpuTeam) {
            if (cpuPokemon.getVida() <= 0) continue;

            try {
                Efecto effect = efectoRepository.findById(cpuPokemon.getIdEfecto()).orElse(null);
                if (effect != null) {
                    switch (effect.getTipoEfecto()) {
                        case SUBIR_ATAQUE_PROPIO -> {
                            // Si tiene buff de ataque, es más valioso
                            score += 300.0;
                        }
                        case SUBIR_DEFENSA_PROPIO -> {
                            // Si tiene buff de defensa, es más valioso defensivamente
                            score += 250.0;
                        }
                        case SUBIR_VELOCIDAD_PROPIO -> {
                            // Si tiene buff de velocidad, es más valioso
                            score += 200.0;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignorar errores
            }
        }
        
        // 8. MEJORADO: Potencial de daño futuro y evaluación de estados
        int poisonedEnemies = 0;
        int poisonedAllies = 0;
        
        for (Pokemon enemy : state.enemyTeam) {
            if (enemy.getVida() <= 0) continue;

            try {
                Efecto effect = efectoRepository.findById(enemy.getIdEfecto()).orElse(null);
                if (effect != null) {
                    switch (effect.getTipoEfecto()) {
                        case BAJAR_ATAQUE_RIVAL -> {
                            // Enemigo con ataque reducido es favorable
                            score += 600.0; // AUMENTADO
                        }
                        case BAJAR_DEFENSA_RIVAL -> {
                            // Enemigo con defensa reducida es favorable
                            score += 550.0; // AUMENTADO
                        }
                        case DANO_CONTINUO -> {
                            // Enemigo envenenado es muy favorable
                            poisonedEnemies++;
                            score += 800.0; // AUMENTADO: Más valor por cada enemigo envenenado
                        }
                    }
                }
            } catch (Exception e) {
                // Ignorar errores
            }
        }
        
        // NUEVO: Evaluar nuestros Pokemon envenenados (negativo)
        for (Pokemon cpuPokemon : state.cpuTeam) {
            if (cpuPokemon.getVida() <= 0) continue;

            try {
                Efecto effect = efectoRepository.findById(cpuPokemon.getIdEfecto()).orElse(null);
                if (effect != null && effect.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                    poisonedAllies++;
                    score -= 400.0; // Penalizar si nosotros estamos envenenados
                }
            } catch (Exception e) {
                // Ignorar errores
            }
        }
        
        // NUEVO: Bonificar si tenemos ventaja en envenenamiento
        if (poisonedEnemies > 0) {
            score += poisonedEnemies * 300.0; // Bonificar por cada enemigo envenenado
            System.out.println("EVALUACIÓN VENENO: " + poisonedEnemies + " enemigos envenenados vs " + poisonedAllies + " aliados envenenados");
        }
        // 9. Agregar puntuación de supervivencia como factor secundario
        score += survivalScore;

        return score;
    }
    
    /**
     * Nueva función para evaluar amenazas críticas de supervivencia
     */
    private double evaluateSurvivalThreat(GameState state) {
        double survivalScore = 0.0;

        // Evaluar si algún Pokemon de la CPU está en peligro crítico
        for (Pokemon cpuPokemon : state.cpuTeam) {
            if (cpuPokemon.getVida() <= 0) continue;

            Long vidaMaxima = cpuPokemon.getVidaBase() != null ? cpuPokemon.getVidaBase() : cpuPokemon.getVida();
            double healthPercent = (double) cpuPokemon.getVida() / (double) vidaMaxima;

            // Verificar si el enemigo puede eliminar este Pokemon en el próximo turno
            boolean canBeKilledNextTurn = false;
            double maxEnemyDamage = 0.0;

            for (Pokemon enemy : state.enemyTeam) {
                if (enemy.getVida() <= 0) continue;

                try {
                    // Verificar ambos ataques del enemigo
                    Ataque enemyAttack1 = ataqueRepository.findById(enemy.getIdAtaque1()).orElse(null);
                    Ataque enemyAttack2 = ataqueRepository.findById(enemy.getIdAtaque2()).orElse(null);

                    if (enemyAttack1 != null) {
                        double damage = calculateDamage(enemy, cpuPokemon, enemyAttack1);
                        maxEnemyDamage = Math.max(maxEnemyDamage, damage);
                        if (damage >= cpuPokemon.getVida()) {
                            canBeKilledNextTurn = true;
                        }
                    }

                    if (enemyAttack2 != null) {
                        double damage = calculateDamage(enemy, cpuPokemon, enemyAttack2);
                        maxEnemyDamage = Math.max(maxEnemyDamage, damage);
                        if (damage >= cpuPokemon.getVida()) {
                            canBeKilledNextTurn = true;
                        }
                    }
                } catch (Exception e) {
                    // Ignorar errores en la evaluación
                }
            }

            // CRITERIO CRÍTICO MEJORADO: Si puede ser eliminado en el próximo turno
            if (canBeKilledNextTurn) {
                try {
                    Efecto healEffect = efectoRepository.findById(cpuPokemon.getIdEfecto()).orElse(null);
                    if (healEffect != null && healEffect.getTipoEfecto() == Efecto.tipoEfecto.SUBIR_VIDA) {
                        long healAmount = (long) (vidaMaxima * healEffect.getMultiplicador());
                        long healedHP = Math.min(vidaMaxima, cpuPokemon.getVida() + healAmount);

                        // Si la curación puede evitar la muerte, dar PRIORIDAD MÁXIMA
                        if (healedHP > maxEnemyDamage) {
                            survivalScore += 10000.0; // AUMENTADO DRASTICAMENTE - Mayor que cualquier ataque
                            System.out.println("CRÍTICO: " + cpuPokemon.getNombre() + " puede sobrevivir curándose (+" + healAmount + " HP vs " + (int)maxEnemyDamage + " daño enemigo)");
                        } else {
                            // Si la curación no es suficiente, aún es mejor que morir
                            survivalScore += 5000.0; // Aumentado significativamente
                            System.out.println("PRECAUCIÓN: " + cpuPokemon.getNombre() + " curación insuficiente pero necesaria");
                        }
                    } else {
                        // No tiene curación disponible y está en peligro crítico
                        survivalScore -= 6000.0; // Penalizar más fuertemente
                        System.out.println("DESESPERADO: " + cpuPokemon.getNombre() + " en peligro sin curación disponible");
                    }
                } catch (Exception e) {
                    survivalScore -= 5000.0; // Penalizar por incertidumbre
                }
            }
            // CRITERIO SECUNDARIO MEJORADO: Pokemon con vida muy baja (menos del 40%)
            else if (healthPercent < 0.4) {
                try {
                    Efecto healEffect = efectoRepository.findById(cpuPokemon.getIdEfecto()).orElse(null);
                    if (healEffect != null && healEffect.getTipoEfecto() == Efecto.tipoEfecto.SUBIR_VIDA) {
                        // Bonificar más agresivamente la curación preventiva
                        double preventiveScore = (0.4 - healthPercent) * 8000.0; // Escala basada en qué tan baja está la vida
                        survivalScore += preventiveScore;
                        System.out.println("PREVENTIVO: " + cpuPokemon.getNombre() + " considera curación preventiva (+" + String.format("%.1f", preventiveScore) + " puntos)");
                    }
                } catch (Exception e) {
                    // Ignorar errores
                }
            }
        }

        return survivalScore;
    }

    /**
     * Evalúa si existe una situación de "mate en 1" (victoria asegurada en el próximo turno)
     */
    private double evaluateMateInOne(GameState state) {
        // Verificar si CPU puede eliminar a todos los Pokemon enemigos restantes
        List<Pokemon> aliveEnemies = state.enemyTeam.stream()
            .filter(p -> p.getVida() > 0)
            .collect(Collectors.toList());
        
        if (aliveEnemies.isEmpty()) {
            return 9999.0; // Ya ganó
        }
        
        // Buscar ataques que puedan eliminar a todos los enemigos restantes
        for (Pokemon cpuPokemon : state.cpuTeam) {
            if (cpuPokemon.getVida() <= 0) continue;
            
            boolean canKillAllEnemies = true;
            
            for (Pokemon enemy : aliveEnemies) {
                boolean canKillThisEnemy = false;
                
                try {
                    // Verificar ataque 1
                    Ataque attack1 = ataqueRepository.findById(cpuPokemon.getIdAtaque1()).orElse(null);
                    if (attack1 != null) {
                        double damage1 = calculateDamage(cpuPokemon, enemy, attack1);
                        if (damage1 >= enemy.getVida()) {
                            canKillThisEnemy = true;
                        }
                    }
                    
                    // Verificar ataque 2 si el primero no es suficiente
                    if (!canKillThisEnemy) {
                        Ataque attack2 = ataqueRepository.findById(cpuPokemon.getIdAtaque2()).orElse(null);
                        if (attack2 != null) {
                            double damage2 = calculateDamage(cpuPokemon, enemy, attack2);
                            if (damage2 >= enemy.getVida()) {
                                canKillThisEnemy = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    // En caso de error, asumir que no puede matar
                    canKillThisEnemy = false;
                }
                
                if (!canKillThisEnemy) {
                    canKillAllEnemies = false;
                    break;
                }
            }
            
            if (canKillAllEnemies) {
                return 9500.0; // Victoria asegurada en 1 turno - prioridad máxima
            }
        }
        
        return 0.0; // No hay mate en 1
    }
    
    /**
     * Calcula el potencial de daño máximo de un equipo contra otro
     */
    private double calculateDamagePotential(List<Pokemon> attackers, List<Pokemon> defenders) {
        double maxDamage = 0.0;
        
        for (Pokemon attacker : attackers) {
            if (attacker.getVida() <= 0) continue;
            
            for (Pokemon defender : defenders) {
                if (defender.getVida() <= 0) continue;
                
                try {
                    Ataque attack1 = ataqueRepository.findById(attacker.getIdAtaque1()).orElse(null);
                    Ataque attack2 = ataqueRepository.findById(attacker.getIdAtaque2()).orElse(null);
                    
                    if (attack1 != null) {
                        double damage = calculateDamage(attacker, defender, attack1);
                        maxDamage = Math.max(maxDamage, damage);
                    }
                    if (attack2 != null) {
                        double damage = calculateDamage(attacker, defender, attack2);
                        maxDamage = Math.max(maxDamage, damage);
                    }
                } catch (Exception e) {
                    // Ignorar errores en simulación
                }
            }
        }
        
        return maxDamage;
    }

    /**
     * Método principal para CPU difícil usando Minimax
     */
    private CpuActionResponse decideHardActionWithMinimax(List<Pokemon> cpuTeam, List<Pokemon> enemyTeam, BatallaDTO batalla) {
        try {
            System.out.println("=== INICIANDO MINIMAX DIFICULTAD DIFÍCIL ===");
            
            // Crear estado inicial
            GameState initialState = new GameState(cpuTeam, enemyTeam, batalla != null ? batalla.getTurno() : 1, true);
            
            // Configuración de Minimax
            int maxDepth = 3; // 3 turnos de profundidad (CPU -> Jugador -> CPU)
            long maxTimeMs = 2000; // Máximo 2 segundos por decisión
            long startTime = System.currentTimeMillis();
            
            System.out.println("Profundidad: " + maxDepth + " turnos");
            System.out.println("Tiempo límite: " + maxTimeMs + "ms");
            
            // Ejecutar Minimax
            MinimaxResult result = runMinimax(initialState, maxDepth, 
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true, startTime, maxTimeMs);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Estadísticas de ejecución
            System.out.println("=== RESULTADOS MINIMAX ===");
            System.out.println("Tiempo de ejecución: " + executionTime + "ms");
            System.out.println("Nodos evaluados: " + result.nodesEvaluated);
            System.out.println("Podas realizadas: " + result.pruningCuts);
            System.out.println("Evaluación final: " + String.format("%.2f", result.evaluation));
            
            // Verificar si se encontró una acción válida
            if (result.bestAction == null) {
                System.out.println("No se encontró acción óptima, fallback a lógica normal");
                return decideNormalAction(findRandomAlivePokemon(cpuTeam), cpuTeam, enemyTeam, batalla);
            }
            
            // Convertir GameAction a CpuActionResponse
            GameAction bestAction = result.bestAction;
            CpuActionResponse response = new CpuActionResponse(
                bestAction.attackerIndex,
                bestAction.targetIndex,
                bestAction.isEffect,
                bestAction.attackIndex,
                String.format("CPU Difícil MINIMAX (eval: %.1f): %s", result.evaluation, bestAction.description)
            );
            response.setSuccess(true);
            
            System.out.println("DECISIÓN MINIMAX: " + bestAction.description);
            System.out.println("Atacante: " + cpuTeam.get(bestAction.attackerIndex).getNombre());
            System.out.println("Objetivo: " + enemyTeam.get(bestAction.targetIndex).getNombre());
            System.out.println("Tipo: " + (bestAction.isEffect ? "EFECTO" : "ATAQUE " + (bestAction.attackIndex + 1)));
            
            return response;
            
        } catch (Exception e) {
            System.err.println("Error en Minimax: " + e.getMessage());
            e.printStackTrace();
            // Fallback a lógica normal en caso de error
            return decideNormalAction(findRandomAlivePokemon(cpuTeam), cpuTeam, enemyTeam, batalla);
        }
    }
    
    /**
     * Crear respuesta de error
     */
    private CpuActionResponse createErrorResponse(String message) {
        CpuActionResponse response = new CpuActionResponse();
        response.setSuccess(false);
        response.setReasoning(message);
        response.setAttackerIndex(0);
        response.setTargetIndex(0);
        response.setUseEffect(false);
        response.setAttackIndex(0);
        return response;
    }

    /**
     * Genera todas las acciones posibles para el jugador actual
     */
    private List<GameAction> generatePossibleActions(GameState state, boolean isCpuTurn) {
        List<GameAction> actions = new ArrayList<>();
        List<Pokemon> activeTeam = isCpuTurn ? state.cpuTeam : state.enemyTeam;
        List<Pokemon> targetTeam = isCpuTurn ? state.enemyTeam : state.cpuTeam;
        
        // Para cada Pokemon vivo del equipo activo
        for (int attackerIdx = 0; attackerIdx < activeTeam.size(); attackerIdx++) {
            Pokemon attacker = activeTeam.get(attackerIdx);
            if (attacker.getVida() <= 0) continue;
            
            // Generar ataques contra cada enemigo vivo
            for (int targetIdx = 0; targetIdx < targetTeam.size(); targetIdx++) {
                Pokemon target = targetTeam.get(targetIdx);
                if (target.getVida() <= 0) continue;
                
                // Ataque 1
                actions.add(new GameAction(attackerIdx, targetIdx, false, 0, 
                    String.format("%s ataca a %s con ataque 1", attacker.getNombre(), target.getNombre())));
                
                // Ataque 2
                actions.add(new GameAction(attackerIdx, targetIdx, false, 1, 
                    String.format("%s ataca a %s con ataque 2", attacker.getNombre(), target.getNombre())));
            }
            
            // Generar uso de efecto
            if (isCpuTurn) {
                // CPU puede usar efectos contra enemigos o aliados dependiendo del tipo
                try {
                    Efecto effect = efectoRepository.findById(attacker.getIdEfecto()).orElse(null);
                    if (effect != null) {
                        switch (effect.getTipoEfecto()) {
                            case SUBIR_VIDA -> {
                                // Efecto de curación: solo se aplica al usuario que lo ejecuta
                                Long vidaMaxima = attacker.getVidaBase() != null ? attacker.getVidaBase() : attacker.getVida();
                                double healthPercent = (double) attacker.getVida() / (double) vidaMaxima;

                                String priority = healthPercent < 0.7 ? " (PRIORITARIO)" : "";
                                actions.add(new GameAction(attackerIdx, attackerIdx, true, 0,
                                    String.format("%s se cura%s", attacker.getNombre(), priority)));
                            }
                            case SUBIR_ATAQUE_PROPIO, SUBIR_DEFENSA_PROPIO, SUBIR_VELOCIDAD_PROPIO -> {
                                // Efectos de buff propio: solo se aplican al usuario que los ejecuta
                                actions.add(new GameAction(attackerIdx, attackerIdx, true, 0,
                                    String.format("%s usa buff en sí mismo", attacker.getNombre())));
                            }
                            default -> {
                                // Efectos que afectan al equipo rival: contra todos los enemigos vivos
                                for (int targetIdx = 0; targetIdx < targetTeam.size(); targetIdx++) {
                                    if (targetTeam.get(targetIdx).getVida() > 0) {
                                        actions.add(new GameAction(attackerIdx, targetIdx, true, 0,
                                            String.format("%s usa efecto en %s", attacker.getNombre(), targetTeam.get(targetIdx).getNombre())));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Fallback: generar efectos básicos contra enemigos
                    for (int targetIdx = 0; targetIdx < targetTeam.size(); targetIdx++) {
                        if (targetTeam.get(targetIdx).getVida() > 0) {
                            actions.add(new GameAction(attackerIdx, targetIdx, true, 0,
                                String.format("%s usa efecto en %s", attacker.getNombre(), targetTeam.get(targetIdx).getNombre())));
                        }
                    }
                }
            } else {
                // Jugador humano: simplificar usando efecto en primer objetivo vivo
                int firstAliveTarget = findFirstAliveIndex(targetTeam);
                if (firstAliveTarget != -1) {
                    actions.add(new GameAction(attackerIdx, firstAliveTarget, true, 0, 
                        String.format("%s usa efecto", attacker.getNombre())));
                }
            }
        }
        
        return actions;
    }
    
    /**
     * Poda inteligente de acciones para reducir el espacio de búsqueda
     */
    private List<GameAction> pruneActions(List<GameAction> actions, GameState state, int maxActions) {
        if (actions.size() <= maxActions) {
            return actions;
        }
        
        // Evaluar rápidamente cada acción y quedarse con las mejores
        List<ScoredAction> scoredActions = new ArrayList<>();
        
        for (GameAction action : actions) {
            double score = quickEvaluateAction(action, state);
            scoredActions.add(new ScoredAction(action, score));
        }
        
        // Ordenar por puntuación y tomar las mejores
        return scoredActions.stream()
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(maxActions)
            .map(sa -> sa.action)
            .collect(Collectors.toList());
    }
    
    /**
     * Evaluación rápida de una acción para priorización
     */
    private double quickEvaluateAction(GameAction action, GameState state) {
        double score = 0.0;

        List<Pokemon> attackerTeam = state.cpuTurn ? state.cpuTeam : state.enemyTeam;
        List<Pokemon> targetTeam = state.cpuTurn ? state.enemyTeam : state.cpuTeam;

        Pokemon attacker = attackerTeam.get(action.attackerIndex);
        Pokemon target = targetTeam.get(action.targetIndex);

        if (action.isEffect) {
            // Evaluar efectos más específicamente
            try {
                Efecto effect = efectoRepository.findById(attacker.getIdEfecto()).orElse(null);
                if (effect != null && effect.getTipoEfecto() == Efecto.tipoEfecto.SUBIR_VIDA) {
                    // Es una curación - evaluar si es crítica
                    Long vidaMaxima = target.getVidaBase() != null ? target.getVidaBase() : target.getVida();
                    double healthPercent = (double) target.getVida() / (double) vidaMaxima;

                    // Evaluar si el Pokemon puede ser eliminado en el próximo turno
                    boolean canBeKilledNextTurn = false;
                    double maxEnemyDamage = 0.0;

                    List<Pokemon> enemyTeam = state.cpuTurn ? state.enemyTeam : state.cpuTeam;
                    for (Pokemon enemy : enemyTeam) {
                        if (enemy.getVida() <= 0) continue;
                        try {
                            Ataque enemyAttack1 = ataqueRepository.findById(enemy.getIdAtaque1()).orElse(null);
                            Ataque enemyAttack2 = ataqueRepository.findById(enemy.getIdAtaque2()).orElse(null);

                            if (enemyAttack1 != null) {
                                double damage = calculateDamage(enemy, target, enemyAttack1);
                                maxEnemyDamage = Math.max(maxEnemyDamage, damage);
                                if (damage >= target.getVida()) {
                                    canBeKilledNextTurn = true;
                                }
                            }
                            if (enemyAttack2 != null) {
                                double damage = calculateDamage(enemy, target, enemyAttack2);
                                maxEnemyDamage = Math.max(maxEnemyDamage, damage);
                                if (damage >= target.getVida()) {
                                    canBeKilledNextTurn = true;
                                }
                            }
                        } catch (Exception e) {
                            // Ignorar errores
                        }
                    }

                    if (canBeKilledNextTurn) {
                        // Curación crítica - PRIORIDAD MÁXIMA
                        score = 12000.0;
                        System.out.println("EVALUACIÓN RÁPIDA: Curación crítica para " + target.getNombre() + " - PRIORIDAD MÁXIMA");
                    } else if (healthPercent < 0.3) {
                        // Curación muy importante
                        score = 6000.0;
                    } else if (healthPercent < 0.5) {
                        // Curación importante
                        score = 3000.0;
                    } else if (healthPercent < 0.7) {
                        // Curación preventiva
                        score = 1000.0;
                    } else {
                        // Curación innecesaria
                        score = 100.0;
                    }
                } else {
                    // Evaluar otros tipos de efectos específicamente
                    switch (effect.getTipoEfecto()) {
                        case SUBIR_ATAQUE_PROPIO -> {
                            // Danza Espada - muy valioso al inicio del combate o cuando se puede sobrevivir
                            Long vidaMaxima = attacker.getVidaBase() != null ? attacker.getVidaBase() : attacker.getVida();
                            double healthPercent = (double) attacker.getVida() / (double) vidaMaxima;

                            // NUEVO: Evaluar cuánto se ha buffeado usando ataqueModificado
                            Long ataqueBase = attacker.getAtaqueBase() != null ? attacker.getAtaqueBase() : attacker.getAtaque();
                            Long ataqueActual = attacker.getAtaqueModificado() != null ? attacker.getAtaqueModificado() : attacker.getAtaque();
                            double ataqueMultiplier = (double) ataqueActual / ataqueBase;

                            double baseScore = 0;
                            if (healthPercent > 0.6) {
                                baseScore = 2500.0;
                            } else if (healthPercent > 0.4) {
                                baseScore = 1500.0;
                            } else {
                                baseScore = 500.0;
                            }

                            // Aplicar penalización basada en cuánto se ha buffeado
                            if (ataqueMultiplier <= 1.0) {
                                // No se ha buffeado, valor completo
                                score = baseScore;
                                System.out.println("ESTRATÉGICO: " + attacker.getNombre() + " considera Danza Espada (primera vez, ataque: " + ataqueActual + ")");
                            } else if (ataqueMultiplier <= 2) {
                                // Se buffeó una vez (~2x), valor moderadamente reducido
                                score = baseScore * 0.5;
                                System.out.println("ESTRATÉGICO: " + attacker.getNombre() + " considera Danza Espada (ya buffeado 1x, ataque: " + ataqueActual + " vs base: " + ataqueBase + ")");
                            } else {
                                // Se buffeó 2+ veces (3x o más), casi inútil
                                score = 100.0;
                                System.out.println("ESTRATÉGICO: " + attacker.getNombre() + " considera Danza Espada (EXCESIVO: ataque " + ataqueActual + " vs base " + ataqueBase + ", ratio: " + String.format("%.2f", ataqueMultiplier) + "x)");
                            }

                            // Bonificar si hay enemigos con mucha vida para aprovechar el ataque aumentado
                            List<Pokemon> enemyTeam = state.cpuTurn ? state.enemyTeam : state.cpuTeam;
                            boolean enemyHighHP = enemyTeam.stream().anyMatch(enemy -> {
                                if (enemy.getVida() <= 0) return false;
                                Long enemyMaxHP = enemy.getVidaBase() != null ? enemy.getVidaBase() : enemy.getVida();
                                return (double) enemy.getVida() / enemyMaxHP > 0.7;
                            });

                            if (enemyHighHP && ataqueMultiplier <= 1.0) {
                                score *= 1.2; // Bonificar Danza Espada si hay enemigos fuertes y no se ha buffeado
                                System.out.println("ESTRATÉGICO: " + attacker.getNombre() + " bonificado por Danza Espada contra enemigos fuertes");
                            }
                        }
                        case SUBIR_DEFENSA_PROPIO -> {
                            // Aumentar defensa - valioso cuando se espera recibir ataques
                            Long vidaMaxima = attacker.getVidaBase() != null ? attacker.getVidaBase() : attacker.getVida();
                            double healthPercent = (double) attacker.getVida() / (double) vidaMaxima;

                            // NUEVO: Evaluar cuánto se ha buffeado usando defensaModificada
                            Long defensaBase = attacker.getDefensaBase() != null ? attacker.getDefensaBase() : attacker.getDefensa();
                            Long defensaActual = attacker.getDefensaModificada() != null ? attacker.getDefensaModificada() : attacker.getDefensa();
                            double defensaMultiplier = (double) defensaActual / defensaBase;

                            double baseScore = 0;
                            if (healthPercent > 0.5) {
                                baseScore = 2000.0;
                            } else {
                                baseScore = 800.0;
                            }

                            // Aplicar penalización basada en cuánto se ha buffeado
                            if (defensaMultiplier <= 1.0) {
                                // No se ha buffeado, valor completo
                                score = baseScore;
                                System.out.println("DEFENSIVO: " + attacker.getNombre() + " considera Rizo Defensa (primera vez, defensa: " + defensaActual + ")");
                            } else if (defensaMultiplier <= 2) {
                                // Se buffeó una vez (~2x), valor reducido
                                score = baseScore * 0.6;
                                System.out.println("DEFENSIVO: " + attacker.getNombre() + " considera Rizo Defensa (ya buffeado 1x, defensa: " + defensaActual + " vs base: " + defensaBase + ")");
                            } else if (defensaMultiplier <= 3) {
                                // Se buffeó dos veces (~3x), valor muy reducido
                                score = baseScore * 0.2;
                                System.out.println("DEFENSIVO: " + attacker.getNombre() + " considera Rizo Defensa (ya buffeado 2x, defensa: " + defensaActual + " vs base: " + defensaBase + ")");
                            } else {
                                // Se buffeó 3+ veces (4x o más), prácticamente inútil
                                score = 50.0;
                                System.out.println("DEFENSIVO: " + attacker.getNombre() + " considera Rizo Defensa (EXCESIVO: defensa " + defensaActual + " vs base " + defensaBase + ", ratio: " + String.format("%.2f", defensaMultiplier) + "x)");
                            }

                            // NUEVO: Penalización adicional si hay enemigos débiles
                            List<Pokemon> enemyTeam = state.cpuTurn ? state.enemyTeam : state.cpuTeam;
                            boolean enemyLowHP = enemyTeam.stream().anyMatch(enemy -> {
                                if (enemy.getVida() <= 0) return false;
                                Long enemyMaxHP = enemy.getVidaBase() != null ? enemy.getVidaBase() : enemy.getVida();
                                return (double) enemy.getVida() / enemyMaxHP < 0.4;
                            });

                            if (enemyLowHP && defensaMultiplier > 1.0) {
                                score *= 0.3; // Penalizar fuertemente buffear cuando enemigos están débiles
                                System.out.println("DEFENSIVO: " + attacker.getNombre() + " penalizado por buffear defensa con enemigos débiles");
                            }
                        }
                        case SUBIR_VELOCIDAD_PROPIO -> {
                            // Aumentar velocidad - valioso para asegurar ataques primero
                            score = 1800.0;
                            System.out.println("VELOCIDAD: " + attacker.getNombre() + " considera aumentar velocidad");
                        }
                        case DANO_CONTINUO -> {
                            // Tóxico - se aplica a TODO el equipo rival de una vez
                            // LÓGICA CORREGIDA: Si cualquier enemigo está envenenado, TODOS están envenenados
                            List<Pokemon> enemyTeam = state.cpuTurn ? state.enemyTeam : state.cpuTeam;

                            // Verificar si ALGÚN enemigo vivo ya está envenenado
                            boolean anyEnemyPoisoned = false;
                            int aliveEnemies = 0;
                            double totalEnemyHP = 0;
                            int healthyEnemies = 0;

                            for (Pokemon enemy : enemyTeam) {
                                if (enemy.getVida() <= 0) continue;
                                aliveEnemies++;

                                // Si encontramos un enemigo envenenado, significa que TODOS están envenenados
                                try {
                                    Efecto enemyEffect = efectoRepository.findById(enemy.getIdEfecto()).orElse(null);
                                    if (enemyEffect != null && enemyEffect.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                                        anyEnemyPoisoned = true;
                                        System.out.println("VENENO DETECTADO: " + enemy.getNombre() + " está envenenado, por tanto TODO el equipo enemigo está envenenado");
                                        break; // No necesitamos seguir verificando
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error verificando veneno en " + enemy.getNombre() + ": " + e.getMessage());
                                }

                                // Calcular stats para evaluación solo si no hay veneno aún
                                Long maxHP = enemy.getVidaBase() != null ? enemy.getVidaBase() : enemy.getVida();
                                if ((double) enemy.getVida() / maxHP > 0.5) {
                                    healthyEnemies++;
                                }
                                totalEnemyHP += enemy.getVida();
                            }

                            if (anyEnemyPoisoned) {
                                // Si cualquier enemigo está envenenado, TODO el equipo está envenenado
                                // Usar tóxico otra vez es COMPLETAMENTE INÚTIL
                                score = -3000.0; // PENALIZACIÓN MASIVA
                                System.out.println("TÓXICO COMPLETAMENTE INÚTIL: " + attacker.getNombre() + 
                                                 " NO debe usar veneno - TODO EL EQUIPO ENEMIGO YA ESTÁ ENVENENADO");
                            } else {
                                // Ningún enemigo está envenenado - evaluar valor del tóxico
                                double baseScore = 0;
                                if (totalEnemyHP > 300 && healthyEnemies >= 2) {
                                    // Equipo enemigo fuerte - veneno muy valioso
                                    baseScore = 4500.0; // AUMENTADO: Más valor cuando realmente es útil
                                    System.out.println("TÓXICO EXCELENTE: " + attacker.getNombre() + 
                                                     " considera veneno (equipo enemigo fuerte: " + (int)totalEnemyHP + 
                                                     " HP total, " + healthyEnemies + " enemigos sanos)");
                                } else if (totalEnemyHP > 200) {
                                    baseScore = 3500.0; // AUMENTADO
                                    System.out.println("TÓXICO BUENO: " + attacker.getNombre() + 
                                                     " considera veneno (equipo enemigo moderado: " + (int)totalEnemyHP + " HP total)");
                                } else if (totalEnemyHP > 100) {
                                    baseScore = 2000.0; // AUMENTADO
                                    System.out.println("TÓXICO ÚTIL: " + attacker.getNombre() + 
                                                     " considera veneno (equipo enemigo débil: " + (int)totalEnemyHP + " HP total)");
                                } else {
                                    baseScore = 800.0; // Equipo muy débil, veneno menos valioso
                                    System.out.println("TÓXICO MARGINAL: " + attacker.getNombre() + 
                                                     " considera veneno (equipo enemigo muy débil: " + (int)totalEnemyHP + " HP total)");
                                }

                                score = baseScore;
                                System.out.println("TÓXICO DISPONIBLE: " + attacker.getNombre() + 
                                                 " puede envenenar a TODO el equipo enemigo (" + aliveEnemies + " enemigos vivos)");
                            }
                        }
                        case BAJAR_ATAQUE_RIVAL -> {
                            // Reducir ataque - se aplica a TODO el equipo rival
                            List<Pokemon> enemyTeam = state.cpuTurn ? state.enemyTeam : state.cpuTeam;

                            boolean allEnemiesDebuffed = true;
                            boolean anyEnemyDebuffed = false;
                            double totalEnemyThreat = 0;
                            int dangerousEnemies = 0;

                            for (Pokemon enemy : enemyTeam) {
                                if (enemy.getVida() <= 0) continue;

                                // Verificar si este enemigo ya tiene debuff de ataque
                                Long ataqueBase = enemy.getAtaqueBase() != null ? enemy.getAtaqueBase() : enemy.getAtaque();
                                Long ataqueActual = enemy.getAtaqueModificado() != null ? enemy.getAtaqueModificado() : enemy.getAtaque();
                                double ataqueRatio = (double) ataqueActual / ataqueBase;

                                if (ataqueRatio < 1.0) {
                                    anyEnemyDebuffed = true;
                                } else {
                                    allEnemiesDebuffed = false;
                                }

                                // Evaluar qué tan peligroso es este enemigo
                                try {
                                    Ataque enemyAttack1 = ataqueRepository.findById(enemy.getIdAtaque1()).orElse(null);
                                    Ataque enemyAttack2 = ataqueRepository.findById(enemy.getIdAtaque2()).orElse(null);

                                    double maxEnemyDamage = 0;
                                    if (enemyAttack1 != null) {
                                        maxEnemyDamage = Math.max(maxEnemyDamage, calculateDamage(enemy, attacker, enemyAttack1));
                                    }
                                    if (enemyAttack2 != null) {
                                        maxEnemyDamage = Math.max(maxEnemyDamage, calculateDamage(enemy, attacker, enemyAttack2));
                                    }

                                    totalEnemyThreat += maxEnemyDamage;
                                    if (maxEnemyDamage > attacker.getVida() * 0.4) {
                                        dangerousEnemies++;
                                    }
                                } catch (Exception e) {
                                    // Ignorar errores
                                }
                            }

                            if (allEnemiesDebuffed) {
                                // Todo el equipo enemigo ya tiene debuff de ataque - casi inútil
                                score = 25.0;
                                System.out.println("DEBUFF ATAQUE: " + attacker.getNombre() + " NO debe debuffear ataque (TODO EL EQUIPO YA DEBUFFEADO)");
                            } else if (anyEnemyDebuffed) {
                                // Algunos ya están debuffeados - valor reducido
                                double baseScore = totalEnemyThreat > 150 ? 1500.0 : 800.0;
                                score = baseScore * 0.5;
                                System.out.println("DEBUFF ATAQUE: " + attacker.getNombre() + " considera debuff de ataque (algunos enemigos ya debuffeados, amenaza total: " + (int)totalEnemyThreat + ")");
                            } else {
                                // Nadie está debuffeado - evaluar valor completo
                                double baseScore = 0;
                                if (dangerousEnemies >= 2) {
                                    baseScore = 3000.0;
                                    System.out.println("DEBUFF ATAQUE: " + attacker.getNombre() + " considera debuff de ataque (múltiples enemigos peligrosos: " + dangerousEnemies + ")");
                                } else if (totalEnemyThreat > 120) {
                                    baseScore = 2200.0;
                                    System.out.println("DEBUFF ATAQUE: " + attacker.getNombre() + " considera debuff de ataque (amenaza total alta: " + (int)totalEnemyThreat + ")");
                                } else {
                                    baseScore = 1000.0;
                                    System.out.println("DEBUFF ATAQUE: " + attacker.getNombre() + " considera debuff de ataque (amenaza moderada: " + (int)totalEnemyThreat + ")");
                                }

                                score = baseScore;
                            }
                        }
                        case BAJAR_DEFENSA_RIVAL -> {
                            // Reducir defensa - se aplica a TODO el equipo rival
                            List<Pokemon> enemyTeam = state.cpuTurn ? state.enemyTeam : state.cpuTeam;

                            boolean allEnemiesDebuffed = true;
                            boolean anyEnemyDebuffed = false;
                            double totalEnemyHP = 0;
                            int tankEnemies = 0;

                            for (Pokemon enemy : enemyTeam) {
                                if (enemy.getVida() <= 0) continue;

                                // Verificar si este enemigo ya tiene debuff de defensa
                                Long defensaBase = enemy.getDefensaBase() != null ? enemy.getDefensaBase() : enemy.getDefensa();
                                Long defensaActual = enemy.getDefensaModificada() != null ? enemy.getDefensaModificada() : enemy.getDefensa();
                                double defensaRatio = (double) defensaActual / defensaBase;

                                if (defensaRatio < 1.0) {
                                    anyEnemyDebuffed = true;
                                } else {
                                    allEnemiesDebuffed = false;
                                }

                                totalEnemyHP += enemy.getVida();

                                // Considerar enemigos "tanque" (alta defensa)
                                if (enemy.getDefensa() > 100) {
                                    tankEnemies++;
                                }
                            }

                            if (allEnemiesDebuffed) {
                                // Todo el equipo enemigo ya tiene debuff de defensa - casi inútil
                                score = 30.0;
                                System.out.println("DEBUFF DEFENSA: " + attacker.getNombre() + " NO debe debuffear defensa (TODO EL EQUIPO YA DEBUFFEADO)");
                            } else if (anyEnemyDebuffed) {
                                // Algunos ya están debuffeados - valor reducido
                                double baseScore = totalEnemyHP > 250 ? 1200.0 : 600.0;
                                score = baseScore * 0.6;
                                System.out.println("DEBUFF DEFENSA: " + attacker.getNombre() + " considera debuff de defensa (algunos enemigos ya debuffeados)");
                            } else {
                                // Nadie está debuffeado - evaluar valor completo
                                double baseScore = 0;
                                if (tankEnemies >= 2) {
                                    baseScore = 2800.0;
                                    System.out.println("DEBUFF DEFENSA: " + attacker.getNombre() + " considera debuff de defensa (múltiples enemigos tanque: " + tankEnemies + ")");
                                } else if (totalEnemyHP > 300) {
                                    baseScore = 2000.0;
                                    System.out.println("DEBUFF DEFENSA: " + attacker.getNombre() + " considera debuff de defensa (equipo enemigo resistente: " + (int)totalEnemyHP + " HP total)");
                                } else {
                                    baseScore = 1100.0;
                                    System.out.println("DEBUFF DEFENSA: " + attacker.getNombre() + " considera debuff de defensa (equipo moderado: " + (int)totalEnemyHP + " HP total)");
                                }

                                score = baseScore;
                            }
                        }
                        default -> {
                            // Otros efectos desconocidos
                            score = 800.0;
                            System.out.println("EFECTO DESCONOCIDO: " + attacker.getNombre() + " evalúa efecto " + effect.getTipoEfecto());
                        }
                    }
                }
            } catch (Exception e) {
                score = 100.0; // Puntuación base para efectos con error
            }
        } else {
            // Para ataques, evaluar el daño potencial
            try {
                Long attackId = action.attackIndex == 0 ? attacker.getIdAtaque1() : attacker.getIdAtaque2();
                Ataque attack = ataqueRepository.findById(attackId).orElse(null);
                if (attack != null) {
                    // NUEVO: Verificar efectividad ANTES de calcular daño
                    double effectiveness = calculateTypeEffectiveness(attack.getTipoAtaque(), target.getTipoPokemon());
                    
                    // Si la efectividad es 0.0 (ataque no hace efecto), penalizar ENORMEMENTE
                    if (effectiveness == 0.0) {
                        score = -5000.0; // Penalización masiva para ataques que no hacen efecto
                        System.out.println("ATAQUE INÚTIL: " + attacker.getNombre() + " NO debe usar " + attack.getNombre() + 
                                         " (" + attack.getTipoAtaque() + ") contra " + target.getNombre() + 
                                         " (" + target.getTipoPokemon() + ") - SIN EFECTO");
                        return score;
                    }
                    
                    double damage = calculateDamage(attacker, target, attack);
                    score = damage;

                    // MEJORADO: Bonificar más agresivamente ataques super efectivos
                    if (effectiveness >= 2.0) {
                        double superEffectiveBonus = 2000.0 * effectiveness; // Escala con la efectividad
                        score += superEffectiveBonus;
                        System.out.println("SUPER EFECTIVO MEJORADO: " + attacker.getNombre() + " - " + attack.getNombre() + 
                                         " (" + attack.getTipoAtaque() + ") vs " + target.getNombre() + 
                                         " (" + target.getTipoPokemon() + ") = " + effectiveness + "x (+" + (int)superEffectiveBonus + " bonus)");
                    } else if (effectiveness >= 1.5) {
                        // STAB o efectividad moderada
                        score += 800.0;
                        System.out.println("EFECTIVO: " + attacker.getNombre() + " - " + attack.getNombre() + 
                                         " vs " + target.getNombre() + " = " + effectiveness + "x (+800 bonus)");
                    } else if (effectiveness < 1.0 && effectiveness > 0.0) {
                        // Efectividad reducida - penalizar
                        double penalty = (1.0 - effectiveness) * 1000.0;
                        score -= penalty;
                        System.out.println("POCO EFECTIVO: " + attacker.getNombre() + " - " + attack.getNombre() + 
                                         " vs " + target.getNombre() + " = " + effectiveness + "x (-" + (int)penalty + " penalty)");
                    }

                    // Bonificar si puede hacer OHKO
                    if (damage >= target.getVida()) {
                        score += 3000.0; // AUMENTADO: OHKO debe tener prioridad muy alta
                        System.out.println("OHKO DETECTADO: " + attacker.getNombre() + " puede eliminar a " + target.getNombre() + 
                                         " con " + attack.getNombre() + " (" + (int)damage + " daño vs " + target.getVida() + " HP)");
                    }

                    // Bonificar si puede quitar más del 70% de vida (casi OHKO)
                    else if (damage >= target.getVida() * 0.7) {
                        score += 1500.0;
                        System.out.println("CASI OHKO: " + attacker.getNombre() + " puede hacer " + (int)damage + " daño a " + target.getNombre() + 
                                         " (" + String.format("%.1f", (damage / target.getVida()) * 100) + "% de su vida)");
                    }
                    
                    // Debug de daño calculado
                    if (damage > 0) {
                        System.out.println("EVALUACIÓN ATAQUE: " + attacker.getNombre() + " - " + attack.getNombre() + 
                                         " vs " + target.getNombre() + " = " + (int)damage + " daño (efectividad: " + effectiveness + "x, score final: " + (int)score + ")");
                    }
                }
            } catch (Exception e) {
                score = 50.0; // Puntuación base para ataques con error
            }
        }

        return score;
    }
    
    /**
     * Aplica una acción al estado del juego y retorna el nuevo estado
     */
    private GameState applyAction(GameState state, GameAction action) {
        // Crear copia del estado
        GameState newState = new GameState(state.cpuTeam, state.enemyTeam, state.turn + 1, !state.cpuTurn);
        
        List<Pokemon> attackerTeam = state.cpuTurn ? newState.cpuTeam : newState.enemyTeam;
        List<Pokemon> targetTeam = state.cpuTurn ? newState.enemyTeam : newState.cpuTeam;
        
        Pokemon attacker = attackerTeam.get(action.attackerIndex);
        Pokemon target = targetTeam.get(action.targetIndex);
        
        if (action.isEffect) {
            // Aplicar efecto (simulación simplificada)
            applyEffectSimulation(attacker, target);
        } else {
            // Aplicar ataque
            try {
                Long attackId = action.attackIndex == 0 ? attacker.getIdAtaque1() : attacker.getIdAtaque2();
                Ataque attack = ataqueRepository.findById(attackId).orElse(null);
                if (attack != null) {
                    double damage = calculateDamage(attacker, target, attack);
                    long newHP = Math.max(0, target.getVida() - (long) damage);
                    target.setVida(newHP);
                }
            } catch (Exception e) {
                // En caso de error, no aplicar daño
            }
        }
        
        return newState;
    }
    
    /**
     * Simulación simplificada de efectos para Minimax
     */
    private void applyEffectSimulation(Pokemon attacker, Pokemon target) {
        try {
            Efecto effect = efectoRepository.findById(attacker.getIdEfecto()).orElse(null);
            if (effect == null) return;
            
            switch (effect.getTipoEfecto()) {
                case SUBIR_VIDA -> {
                    // Curación: restaurar porcentaje de vida
                    Long vidaMaxima = target.getVidaBase() != null ? target.getVidaBase() : target.getVida();
                    long healAmount = (long) (vidaMaxima * effect.getMultiplicador());
                    // Asegurar que no exceda la vida máxima
                    long newHP = Math.min(vidaMaxima, target.getVida() + healAmount);
                    target.setVida(newHP);
                }
                case SUBIR_ATAQUE_PROPIO -> {
                    Long newAttack = Math.min(attacker.getAtaque() * 2, attacker.getAtaque() + 50);
                    attacker.setAtaqueModificado(newAttack);
                }
                case SUBIR_DEFENSA_PROPIO -> {
                    Long newDefense = Math.min(attacker.getDefensa() * 2, attacker.getDefensa() + 50);
                    attacker.setDefensaModificada(newDefense);
                }
                case BAJAR_ATAQUE_RIVAL -> {
                    Long newAttack = Math.max(target.getAtaque() / 2, target.getAtaque() - 30);
                    target.setAtaqueModificado(newAttack);
                }
                case BAJAR_DEFENSA_RIVAL -> {
                    Long newDefense = Math.max(target.getDefensa() / 2, target.getDefensa() - 30);
                    target.setDefensaModificada(newDefense);
                }
                case DANO_CONTINUO -> {
                    // Simular daño por veneno (aproximado)
                    long poisonDamage = Math.max(target.getVida() / 8, 10);
                    target.setVida(Math.max(0, target.getVida() - poisonDamage));
                }
                default -> {
                    // Para otros efectos, no hacer nada en la simulación
                }
            }
        } catch (Exception e) {
            // Ignorar errores en simulación
        }
    }
    
    /**
     * Encuentra el primer Pokemon vivo en un equipo
     */
    private int findFirstAliveIndex(List<Pokemon> team) {
        for (int i = 0; i < team.size(); i++) {
            if (team.get(i).getVida() > 0) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Clase auxiliar para puntuar acciones
     */
    private static class ScoredAction {
        GameAction action;
        double score;
        
        ScoredAction(GameAction action, double score) {
            this.action = action;
            this.score = score;
        }
    }
}
