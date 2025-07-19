package com.example.Pokemon.Controllers;

import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Services.TipoEfectividadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tabla-efectividad")
@CrossOrigin("*")
public class TablaEfectividadController {
    @Autowired
    private TipoEfectividadService tipoEfectividadService;

    @GetMapping("/fortalezas/{tipo}")
    public List<Pokemon.TipoPokemon> getFortalezas(@PathVariable Pokemon.TipoPokemon tipo) {
        return tipoEfectividadService.obtenerFortalezas(tipo);
    }

    @GetMapping("/debilidadesO/{tipo}")
    public List<Pokemon.TipoPokemon> getDebilidades(@PathVariable Pokemon.TipoPokemon tipo) {
        return tipoEfectividadService.obtenerDebilidadesOfensivas(tipo);
    }

    @GetMapping("/debilidadesD/{tipo}")
    public List<Pokemon.TipoPokemon> getDebilidadesDefensivas(@PathVariable Pokemon.TipoPokemon tipo) {
        return tipoEfectividadService.obtenerDebilidadesDefensivas(tipo);
    }

    @GetMapping("/resistencias/{tipo}")
    public List<Pokemon.TipoPokemon> getResistencias(@PathVariable Pokemon.TipoPokemon tipo) {
        return tipoEfectividadService.obtenerResistencias(tipo);
    }

    @GetMapping("/inmunidades/{tipo}")
    public List<Pokemon.TipoPokemon> getInmunidades(@PathVariable Pokemon.TipoPokemon tipo) {
        return tipoEfectividadService.obtenerInmunidades(tipo);
    }

}
