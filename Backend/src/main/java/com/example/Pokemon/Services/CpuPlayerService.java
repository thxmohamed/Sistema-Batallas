package com.example.Pokemon.Services;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.DTO.CpuActionResponse;
import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Repositories.AtaqueRepository;
import com.example.Pokemon.Repositories.EfectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

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

            // Encontrar un Pokémon vivo de la CPU para atacar
            int attackerIndex = findRandomAlivePokemon(cpuTeam);
            if (attackerIndex == -1) {
                return createErrorResponse("No hay Pokémon vivos en el equipo CPU");
            }

            // Decidir acción basándose en la dificultad
            return switch (difficulty.toUpperCase()) {
                case "EASY" -> decideEasyAction(attackerIndex, cpuTeam, enemyTeam);
                case "HARD" -> decideHardActionWithThreatEvaluation(attackerIndex, cpuTeam, enemyTeam, batalla);
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
     * Sobrecarga para incluir información de la batalla (turnos sin atacar)
     */
    public CpuActionResponse decideHardActionWithThreatEvaluation(int attackerIndex, List<Pokemon> cpuTeam, 
                                                                   List<Pokemon> enemyTeam, BatallaDTO batalla) {
        return decideNormalAction(attackerIndex, cpuTeam, enemyTeam, batalla);
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
                double damage1 = calculateDamage(attacker, target, attack1);
                System.out.println("  - " + attack1.getNombre() + ": " + String.format("%.1f", damage1) + " daño");
                
                if (damage1 > maxDamage) {
                    maxDamage = damage1;
                    bestResult = new BestAttackResult(pokemonIndex, 0, damage1, attacker.getNombre(), attack1.getNombre());
                }
            }
            
            // Evaluar ataque 2
            if (attack2 != null) {
                double damage2 = calculateDamage(attacker, target, attack2);
                System.out.println("  - " + attack2.getNombre() + ": " + String.format("%.1f", damage2) + " daño");
                
                if (damage2 > maxDamage) {
                    maxDamage = damage2;
                    bestResult = new BestAttackResult(pokemonIndex, 1, damage2, attacker.getNombre(), attack2.getNombre());
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
        Long ataqueEfectivo = attacker.getAtaqueEfectivo() != null ? attacker.getAtaqueEfectivo() : attacker.getAtaque();
        Long defensaEfectiva = defender.getDefensaEfectiva() != null ? defender.getDefensaEfectiva() : defender.getDefensa();
        
        // Usar el método del PokemonService
        Long dano = pokemonService.calcularDano(ataqueEfectivo, defensaEfectiva, stab, efectividad, attack.getPotencia());
        
        return dano.doubleValue();
    }
    
    /**
     * Calcula la efectividad de tipos (copiado de PokemonService para mantener consistencia)
     */
    private double calculateTypeEffectiveness(Ataque.TipoAtaque tipoAtaque, Pokemon.TipoPokemon tipoDefensor) {
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
            if (attack1 == null) return 1;
            if (attack2 == null) return 0;
            
            // Calcular daño de cada ataque
            double damage1 = calculateDamage(attacker, target, attack1);
            double damage2 = calculateDamage(attacker, target, attack2);
            
            // Información de debug
            System.out.println(String.format("Evaluando ataques contra %s:", target.getNombre()));
            System.out.println(String.format("  - %s: %.1f daño", attack1.getNombre(), damage1));
            System.out.println(String.format("  - %s: %.1f daño", attack2.getNombre(), damage2));
            
            // Seleccionar el ataque que haga más daño
            int selectedAttack = damage1 >= damage2 ? 0 : 1;
            String selectedName = selectedAttack == 0 ? attack1.getNombre() : attack2.getNombre();
            double selectedDamage = selectedAttack == 0 ? damage1 : damage2;
            
            System.out.println(String.format("  -> Seleccionado: %s (%.1f daño)", selectedName, selectedDamage));
            
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
}
