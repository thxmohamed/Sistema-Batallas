package com.example.Pokemon.Controllers;

import com.example.Pokemon.Entities.Entrenador;
import com.example.Pokemon.Services.EntrenadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entrenador")
@CrossOrigin("*")
public class EntrenadorController {
    @Autowired
    EntrenadorService entrenadorService;

    @GetMapping("/")
    public ResponseEntity<List<Entrenador>> findAll() {
        List<Entrenador> entrenador = entrenadorService.findAll();
        return new ResponseEntity<>(entrenador, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entrenador> findById(@PathVariable Long id) {
        Entrenador entrenador = entrenadorService.findById(id);
        return new ResponseEntity<>(entrenador, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Entrenador> createEntrenador(@RequestBody Entrenador entrenador) {
        Entrenador createdEntrenador = entrenadorService.createEntrenador(entrenador);
        return new ResponseEntity<>(createdEntrenador, HttpStatus.CREATED);
    }
}
