package com.example.Pokemon.Controllers;

import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Services.AtaqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ataque")
@CrossOrigin("*")
public class AtaqueController {
    @Autowired
    AtaqueService ataqueService;

    @GetMapping("/")
    public ResponseEntity<List<Ataque>> getAll() {
        List<Ataque> ataques = ataqueService.getAllAtaques();
        return ResponseEntity.ok(ataques);
    }

    @PostMapping("/create")
    public ResponseEntity<Ataque> createAtaque(@RequestBody Ataque ataque) {
        System.out.println("Datos recibidos: " + ataque.getTipoAtaque());
        Ataque newAtaque = ataqueService.createAtaque(ataque);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAtaque);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ataque> getAtaqueById(@PathVariable Long id) {
        Ataque ataque = ataqueService.getAtaqueById(id);
        if (ataque != null) {
            return ResponseEntity.ok(ataque);
        }
        return ResponseEntity.notFound().build();
    }
}
