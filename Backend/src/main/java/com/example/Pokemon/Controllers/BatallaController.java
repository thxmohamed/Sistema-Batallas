package com.example.Pokemon.Controllers;

import com.example.Pokemon.DTO.BatallaDTO;
import com.example.Pokemon.Entities.Batalla;
import com.example.Pokemon.Entities.Entrenador;
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

    @PostMapping("/combate/{posAtacanteE1}/{posAtacanteE2}/{posAgredidoE1}/{posAgredidoE2}")
    public ResponseEntity<BatallaDTO> combatir(@RequestBody BatallaDTO batallaDTO,
                                               @PathVariable int posAtacanteE1,
                                               @PathVariable int posAtacanteE2,
                                               @PathVariable int posAgredidoE1,
                                               @PathVariable int posAgredidoE2) {
        batallaDTO = batallaService.combatir(batallaDTO, posAtacanteE1, posAgredidoE2, posAtacanteE2, posAgredidoE1);
        return new ResponseEntity<>(batallaDTO, HttpStatus.OK);
    }
}
