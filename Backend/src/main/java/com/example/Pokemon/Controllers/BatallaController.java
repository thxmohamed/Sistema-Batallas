package com.example.Pokemon.Controllers;

import com.example.Pokemon.Entities.Batalla;
import com.example.Pokemon.Services.BatallaService;
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
}
