package com.example.Pokemon.Controllers;

import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Services.EfectoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/efecto")
@CrossOrigin("*")
public class EfectoController {
    @Autowired
    EfectoService efectoService;

    @GetMapping("/")
    public ResponseEntity<List<Efecto>> getAllEfectos(){
        List<Efecto> efectos = efectoService.getAllEfectos();
        return new ResponseEntity<>(efectos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Efecto> getEfectoById(@PathVariable Long id){
        Efecto efecto = efectoService.findEfectoById(id);
        if(efecto == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(efecto, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Efecto> createEfecto(@RequestBody Efecto efecto){
        Efecto newEfecto = efectoService.createEfecto(efecto);
        return new ResponseEntity<>(newEfecto, HttpStatus.CREATED);
    }
}
