package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Utils.TablaEfectividades;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoEfectividadService {

    public double calcularMultiplicador(Pokemon.TipoPokemon tipoAtaque, Pokemon.TipoPokemon tipoDefensor) {
        return TablaEfectividades.getEfectividad(tipoAtaque, tipoDefensor);
    }

    public double calcularDañoConTipo(Long dañoBase, Pokemon.TipoPokemon tipoAtaque, Pokemon.TipoPokemon tipoDefensor) {
        double multiplicador = calcularMultiplicador(tipoAtaque, tipoDefensor);
        return dañoBase * multiplicador;
    }

    public List<Pokemon.TipoPokemon> obtenerFortalezas(Pokemon.TipoPokemon tipo) {
        return TablaEfectividades.getFortalezas(tipo);
    }

    public List<Pokemon.TipoPokemon> obtenerDebilidadesOfensivas(Pokemon.TipoPokemon tipo) {
        return TablaEfectividades.getDebilidades(tipo);
    }

    public List<Pokemon.TipoPokemon> obtenerDebilidadesDefensivas(Pokemon.TipoPokemon tipo) {
        return TablaEfectividades.getDebilidadesDefensivas(tipo);
    }

    public List<Pokemon.TipoPokemon> obtenerResistencias(Pokemon.TipoPokemon tipo) {
        return TablaEfectividades.getResistenciasDefensivas(tipo);
    }

    public List<Pokemon.TipoPokemon> obtenerInmunidades(Pokemon.TipoPokemon tipo) {
        return TablaEfectividades.getInmunidadesDefensivas(tipo);
    }
}