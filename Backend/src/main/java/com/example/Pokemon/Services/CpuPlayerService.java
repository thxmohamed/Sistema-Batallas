package com.example.Pokemon.Services;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.DTO.CpuActionResponse;
import com.example.Pokemon.Entities.Pokemon;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class CpuPlayerService {

    private final Random random = new Random();

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
                case "NORMAL" -> decideNormalAction(attackerIndex, cpuTeam, enemyTeam);
                case "HARD" -> decideHardAction(attackerIndex, cpuTeam, enemyTeam);
                default -> decideNormalAction(attackerIndex, cpuTeam, enemyTeam);
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
    private CpuActionResponse decideNormalAction(int attackerIndex, List<Pokemon> cpuTeam, List<Pokemon> enemyTeam) {
        // Buscar el objetivo con menos vida
        int targetIndex = findWeakestTarget(enemyTeam);
        
        // 70% ataques, 30% efectos
        boolean useEffect = random.nextDouble() < 0.3;
        
        if (useEffect) {
            CpuActionResponse response = new CpuActionResponse(attackerIndex, targetIndex, true, 0, 
                "CPU Normal: Usar efecto en objetivo débil");
            response.setSuccess(true);
            return response;
        } else {
            int attackIndex = random.nextInt(2); // Por ahora aleatorio
            CpuActionResponse response = new CpuActionResponse(attackerIndex, targetIndex, false, attackIndex, 
                "CPU Normal: Atacar al más débil");
            response.setSuccess(true);
            return response;
        }
    }

    /**
     * Lógica DIFÍCIL: Más inteligente (por ahora igual que normal, la mejoraremos)
     */
    private CpuActionResponse decideHardAction(int attackerIndex, List<Pokemon> cpuTeam, List<Pokemon> enemyTeam) {
        // Por ahora igual que normal, después añadiremos la evaluación de amenazas
        return decideNormalAction(attackerIndex, cpuTeam, enemyTeam);
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
