package com.example.Pokemon.Controllers;

import com.example.Pokemon.DTO.CpuActionRequest;
import com.example.Pokemon.DTO.CpuActionResponse;
import com.example.Pokemon.Services.CpuPlayerService;
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
}
