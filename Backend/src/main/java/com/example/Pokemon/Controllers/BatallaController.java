package com.example.Pokemon.Controllers;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.Entities.Batalla;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Services.BatallaService;
import com.example.Pokemon.Services.EntrenadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/batalla")
@CrossOrigin("*")
public class BatallaController {
    @Autowired
    BatallaService batallaService;

    @Autowired
    EntrenadorService entrenadorService;

    @GetMapping("/")
    public ResponseEntity<List<Batalla>> findAll() {
        List<Batalla> batallas = batallaService.getBatallas();
        return new ResponseEntity<>(batallas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Batalla> findById(@PathVariable Long id) {
        Batalla batalla = batallaService.getBatallaById(id);
        return new ResponseEntity<>(batalla, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Batalla> createBatalla(@RequestBody Batalla batalla) {
        Batalla createdBatalla = batallaService.createBatalla(batalla);
        return new ResponseEntity<>(createdBatalla, HttpStatus.CREATED);
    }

    @PostMapping("/combate/{posicionAtacante}/{posicionReceptor}")
    public ResponseEntity<BatallaDTO> combatir(@RequestBody BatallaDTO batallaDTO,
                                               @PathVariable int posicionAtacante,
                                               @PathVariable int posicionReceptor) {
        batallaDTO = batallaService.combatir(batallaDTO, posicionAtacante, posicionReceptor);
        return new ResponseEntity<>(batallaDTO, HttpStatus.OK);
    }

    @PostMapping("/batalla-aleatoria")
    public ResponseEntity<BatallaDTO> crearBatallaAleatoria() {
        BatallaDTO batallaAleatoria = batallaService.crearBatallaAleatoria();
        return new ResponseEntity<>(batallaAleatoria, HttpStatus.CREATED);
    }
    
    @PostMapping("/batalla-aleatoria/{modo}")
    public ResponseEntity<BatallaDTO> crearBatallaAleatoriaConModo(@PathVariable String modo) {
        try {
            BatallaDTO batallaAleatoria = batallaService.crearBatallaAleatoriaConModo(modo);
            return new ResponseEntity<>(batallaAleatoria, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error al crear batalla aleatoria con modo " + modo + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/batalla-dificultad/{modo}/{difficulty}")
    public ResponseEntity<BatallaDTO> crearBatallaConDificultad(@PathVariable String modo, @PathVariable String difficulty, @RequestBody(required = false) List<Pokemon> equipoHumano) {
        try {
            BatallaDTO batallaOptimizada = batallaService.crearBatallaAleatoriaConDificultad(modo, difficulty, equipoHumano);
            return new ResponseEntity<>(batallaOptimizada, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error al crear batalla con dificultad " + difficulty + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/batalla-cpu-hard")
    public ResponseEntity<BatallaDTO> crearBatallaCpuHard(@RequestBody List<Pokemon> equipoHumano) {
        try {
            System.out.println("=== CREANDO BATALLA CPU DIFÍCIL ===");
            System.out.println("Equipo humano recibido: " + equipoHumano.size() + " Pokémon");
            
            BatallaDTO batallaHard = batallaService.crearBatallaAleatoriaConDificultad("TOTAL", "HARD", equipoHumano);
            
            System.out.println("Batalla CPU Hard creada exitosamente");
            return new ResponseEntity<>(batallaHard, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error al crear batalla CPU Hard: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
