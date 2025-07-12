package com.example.Pokemon.Controllers;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.DTO.CpuActionRequest;
import com.example.Pokemon.DTO.CpuActionResponse;
import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Services.AtaqueService;
import com.example.Pokemon.Services.BatallaService;
import com.example.Pokemon.Services.CpuPlayerService;
import com.example.Pokemon.Services.EfectoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cpu")
@CrossOrigin(origins = "http://localhost:5173")
public class CpuController {

    @Autowired
    private CpuPlayerService cpuPlayerService;
    
    @Autowired
    private BatallaService batallaService;
    
    @Autowired
    private AtaqueService ataqueService;
    
    @Autowired
    private EfectoService efectoService;

    /**
     * Endpoint para obtener la decisión de la CPU
     */
    @PostMapping("/action")
    public ResponseEntity<CpuActionResponse> getCpuAction(@RequestBody CpuActionRequest request) {
        try {
            // Validaciones básicas
            if (request.getBatalla() == null) {
                CpuActionResponse errorResponse = new CpuActionResponse();
                errorResponse.setSuccess(false);
                errorResponse.setReasoning("Estado de batalla no proporcionado");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getDifficulty() == null || request.getDifficulty().trim().isEmpty()) {
                request.setDifficulty("NORMAL"); // Valor por defecto
            }

            // Llamar al servicio para obtener la decisión
            CpuActionResponse response = cpuPlayerService.decideBestAction(
                request.getBatalla(),
                request.getDifficulty(),
                request.isCpuIsTeam1()
            );

            // Verificar si la respuesta es exitosa
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            System.err.println("Error en CpuController: " + e.getMessage());
            e.printStackTrace();

            CpuActionResponse errorResponse = new CpuActionResponse();
            errorResponse.setSuccess(false);
            errorResponse.setReasoning("Error interno del servidor: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint de prueba para verificar que el controlador funciona
     */
    @GetMapping("/test")
    public ResponseEntity<String> testCpuController() {
        return ResponseEntity.ok("CPU Controller funcionando correctamente");
    }

    /**
     * Endpoint para ejecutar turno completo de CPU (decisión + combate)
     */
    @PostMapping("/cpu-turn")
    public ResponseEntity<BatallaDTO> ejecutarTurnoCPU(@RequestBody CpuActionRequest request) {
        try {
            // Validaciones básicas
            if (request.getBatalla() == null) {
                return ResponseEntity.badRequest().build();
            }

            if (request.getDifficulty() == null || request.getDifficulty().trim().isEmpty()) {
                request.setDifficulty("NORMAL");
            }

            System.out.println("=== EJECUTANDO TURNO CPU ===");
            
            // 1. Obtener decisión de la CPU
            CpuActionResponse cpuDecision = cpuPlayerService.decideBestAction(
                request.getBatalla(),
                request.getDifficulty(),
                request.isCpuIsTeam1()
            );

            if (!cpuDecision.isSuccess()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            System.out.println("Decisión CPU: " + cpuDecision.getReasoning());
            System.out.println("Usar efecto: " + cpuDecision.isUseEffect());

            // 2. Configurar la batalla con la decisión de la CPU
            BatallaDTO batalla = request.getBatalla();
            
            if (request.isCpuIsTeam1()) {
                // CPU es equipo 1
                batalla.setUsarEfectoE1(cpuDecision.isUseEffect());
                
                if (cpuDecision.isUseEffect()) {
                    // Buscar efecto del Pokémon atacante
                    Pokemon atacante = batalla.getEntrenador1().get(cpuDecision.getAttackerIndex());
                    if (atacante.getIdEfecto() != null) {
                        try {
                            Efecto efecto = efectoService.findEfectoById(atacante.getIdEfecto());
                            batalla.setEfectoE1(efecto);
                        } catch (Exception e) {
                            System.err.println("Error obteniendo efecto: " + e.getMessage());
                        }
                    }
                } else {
                    // Buscar ataque del Pokémon atacante
                    Pokemon atacante = batalla.getEntrenador1().get(cpuDecision.getAttackerIndex());
                    Long idAtaque = cpuDecision.getAttackIndex() == 0 ? atacante.getIdAtaque1() : atacante.getIdAtaque2();
                    if (idAtaque != null) {
                        try {
                            Ataque ataque = ataqueService.getAtaqueById(idAtaque);
                            batalla.setataqueE1(ataque);
                        } catch (Exception e) {
                            System.err.println("Error obteniendo ataque: " + e.getMessage());
                        }
                    }
                }
            } else {
                // CPU es equipo 2
                batalla.setUsarEfectoE2(cpuDecision.isUseEffect());
                
                if (cpuDecision.isUseEffect()) {
                    // Buscar efecto del Pokémon atacante
                    Pokemon atacante = batalla.getEntrenador2().get(cpuDecision.getAttackerIndex());
                    if (atacante.getIdEfecto() != null) {
                        try {
                            Efecto efecto = efectoService.findEfectoById(atacante.getIdEfecto());
                            batalla.setEfectoE2(efecto);
                        } catch (Exception e) {
                            System.err.println("Error obteniendo efecto: " + e.getMessage());
                        }
                    }
                } else {
                    // Buscar ataque del Pokémon atacante
                    Pokemon atacante = batalla.getEntrenador2().get(cpuDecision.getAttackerIndex());
                    Long idAtaque = cpuDecision.getAttackIndex() == 0 ? atacante.getIdAtaque1() : atacante.getIdAtaque2();
                    if (idAtaque != null) {
                        try {
                            Ataque ataque = ataqueService.getAtaqueById(idAtaque);
                            batalla.setataqueE2(ataque);
                        } catch (Exception e) {
                            System.err.println("Error obteniendo ataque: " + e.getMessage());
                        }
                    }
                }
            }

            // 3. Ejecutar el combate con las posiciones determinadas por la CPU
            BatallaDTO resultado = batallaService.combatir(
                batalla,
                cpuDecision.getAttackerIndex(),
                cpuDecision.getTargetIndex()
            );

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            System.err.println("Error ejecutando turno CPU: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
