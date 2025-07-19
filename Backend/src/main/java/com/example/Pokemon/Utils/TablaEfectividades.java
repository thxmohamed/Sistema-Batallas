package com.example.Pokemon.Utils;

import com.example.Pokemon.Entities.Pokemon;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.Pokemon.Entities.Pokemon.TipoPokemon.*;

@Component
public class TablaEfectividades {
    private static final Map<Pokemon.TipoPokemon, Map<Pokemon.TipoPokemon, Double>> efectividades = new HashMap<>();

    static {
        inicializarEfectividades();
    }

    private static void inicializarEfectividades() {
        // NORMAL
        agregar(NORMAL, ROCA, 0.5);
        agregar(NORMAL, FANTASMA, 0.0);
        agregar(NORMAL, ACERO, 0.5);

        // FUEGO
        agregar(FUEGO, FUEGO, 0.5);
        agregar(FUEGO, AGUA, 0.5);
        agregar(FUEGO, PLANTA, 2.0);
        agregar(FUEGO, HIELO, 2.0);
        agregar(FUEGO, BICHO, 2.0);
        agregar(FUEGO, ROCA, 0.5);
        agregar(FUEGO, DRAGON, 0.5);
        agregar(FUEGO, ACERO, 2.0);

        // AGUA
        agregar(AGUA, FUEGO, 2.0);
        agregar(AGUA, AGUA, 0.5);
        agregar(AGUA, PLANTA, 0.5);
        agregar(AGUA, TIERRA, 2.0);
        agregar(AGUA, ROCA, 2.0);
        agregar(AGUA, DRAGON, 0.5);

        // ELECTRICO
        agregar(ELECTRICO, AGUA, 2.0);
        agregar(ELECTRICO, ELECTRICO, 0.5);
        agregar(ELECTRICO, PLANTA, 0.5);
        agregar(ELECTRICO, TIERRA, 0.0);
        agregar(ELECTRICO, VOLADOR, 2.0);
        agregar(ELECTRICO, DRAGON, 0.5);

        // PLANTA
        agregar(PLANTA, FUEGO, 0.5);
        agregar(PLANTA, AGUA, 2.0);
        agregar(PLANTA, PLANTA, 0.5);
        agregar(PLANTA, VENENO, 0.5);
        agregar(PLANTA, TIERRA, 2.0);
        agregar(PLANTA, VOLADOR, 0.5);
        agregar(PLANTA, BICHO, 0.5);
        agregar(PLANTA, ROCA, 2.0);
        agregar(PLANTA, DRAGON, 0.5);
        agregar(PLANTA, ACERO, 0.5);

        // HIELO
        agregar(HIELO, FUEGO, 0.5);
        agregar(HIELO, AGUA, 0.5);
        agregar(HIELO, PLANTA, 2.0);
        agregar(HIELO, HIELO, 0.5);
        agregar(HIELO, TIERRA, 2.0);
        agregar(HIELO, VOLADOR, 2.0);
        agregar(HIELO, DRAGON, 2.0);
        agregar(HIELO, ACERO, 0.5);

        // LUCHA
        agregar(LUCHA, NORMAL, 2.0);
        agregar(LUCHA, HIELO, 2.0);
        agregar(LUCHA, VENENO, 0.5);
        agregar(LUCHA, VOLADOR, 0.5);
        agregar(LUCHA, PSIQUICO, 0.5);
        agregar(LUCHA, BICHO, 0.5);
        agregar(LUCHA, ROCA, 2.0);
        agregar(LUCHA, FANTASMA, 0.0);
        agregar(LUCHA, SINIESTRO, 2.0);
        agregar(LUCHA, ACERO, 2.0);
        agregar(LUCHA, HADA, 0.5);

        // VENENO
        agregar(VENENO, PLANTA, 2.0);
        agregar(VENENO, VENENO, 0.5);
        agregar(VENENO, TIERRA, 0.5);
        agregar(VENENO, ROCA, 0.5);
        agregar(VENENO, FANTASMA, 0.5);
        agregar(VENENO, ACERO, 0.0);
        agregar(VENENO, HADA, 2.0);

        // TIERRA
        agregar(TIERRA, FUEGO, 2.0);
        agregar(TIERRA, ELECTRICO, 2.0);
        agregar(TIERRA, PLANTA, 0.5);
        agregar(TIERRA, VENENO, 2.0);
        agregar(TIERRA, VOLADOR, 0.0);
        agregar(TIERRA, BICHO, 0.5);
        agregar(TIERRA, ROCA, 2.0);
        agregar(TIERRA, ACERO, 2.0);

        // VOLADOR
        agregar(VOLADOR, ELECTRICO, 0.5);
        agregar(VOLADOR, PLANTA, 2.0);
        agregar(VOLADOR, LUCHA, 2.0);
        agregar(VOLADOR, BICHO, 2.0);
        agregar(VOLADOR, ROCA, 0.5);
        agregar(VOLADOR, ACERO, 0.5);

        // PSIQUICO
        agregar(PSIQUICO, LUCHA, 2.0);
        agregar(PSIQUICO, VENENO, 2.0);
        agregar(PSIQUICO, PSIQUICO, 0.5);
        agregar(PSIQUICO, SINIESTRO, 0.0);
        agregar(PSIQUICO, ACERO, 0.5);

        // BICHO
        agregar(BICHO, FUEGO, 0.5);
        agregar(BICHO, PLANTA, 2.0);
        agregar(BICHO, LUCHA, 0.5);
        agregar(BICHO, VENENO, 0.5);
        agregar(BICHO, VOLADOR, 0.5);
        agregar(BICHO, PSIQUICO, 2.0);
        agregar(BICHO, FANTASMA, 0.5);
        agregar(BICHO, SINIESTRO, 2.0);
        agregar(BICHO, ACERO, 0.5);
        agregar(BICHO, HADA, 0.5);

        // ROCA
        agregar(ROCA, FUEGO, 2.0);
        agregar(ROCA, HIELO, 2.0);
        agregar(ROCA, LUCHA, 0.5);
        agregar(ROCA, TIERRA, 0.5);
        agregar(ROCA, VOLADOR, 2.0);
        agregar(ROCA, BICHO, 2.0);
        agregar(ROCA, ACERO, 0.5);

        // FANTASMA
        agregar(FANTASMA, NORMAL, 0.0);
        agregar(FANTASMA, PSIQUICO, 2.0);
        agregar(FANTASMA, FANTASMA, 2.0);
        agregar(FANTASMA, SINIESTRO, 0.5);

        // DRAGON
        agregar(DRAGON, DRAGON, 2.0);
        agregar(DRAGON, ACERO, 0.5);
        agregar(DRAGON, HADA, 0.0);

        // SINIESTRO
        agregar(SINIESTRO, LUCHA, 0.5);
        agregar(SINIESTRO, PSIQUICO, 2.0);
        agregar(SINIESTRO, FANTASMA, 2.0);
        agregar(SINIESTRO, SINIESTRO, 0.5);
        agregar(SINIESTRO, HADA, 0.5);

        // ACERO
        agregar(ACERO, FUEGO, 0.5);
        agregar(ACERO, AGUA, 0.5);
        agregar(ACERO, ELECTRICO, 0.5);
        agregar(ACERO, HIELO, 2.0);
        agregar(ACERO, ROCA, 2.0);
        agregar(ACERO, ACERO, 0.5);
        agregar(ACERO, HADA, 2.0);

        // HADA
        agregar(HADA, FUEGO, 0.5);
        agregar(HADA, VENENO, 0.5);
        agregar(HADA, LUCHA, 2.0);
        agregar(HADA, DRAGON, 2.0);
        agregar(HADA, SINIESTRO, 2.0);
        agregar(HADA, ACERO, 0.5);
    }

    private static void agregar(Pokemon.TipoPokemon atacante, Pokemon.TipoPokemon defensor, double valor) {
        efectividades
                .computeIfAbsent(atacante, k -> new HashMap<>())
                .put(defensor, valor);
    }

    public static double getEfectividad(Pokemon.TipoPokemon tipoAtaque, Pokemon.TipoPokemon tipoDefensor) {
        return efectividades
                .getOrDefault(tipoAtaque, Map.of())
                .getOrDefault(tipoDefensor, 1.0);
    }

    // Método para obtener todas las fortalezas de un tipo
    public static List<Pokemon.TipoPokemon> getFortalezas(Pokemon.TipoPokemon tipo) {
        return efectividades.getOrDefault(tipo, Map.of())
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == 2.0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Método para obtener todas las debilidades de un tipo
    public static List<Pokemon.TipoPokemon> getDebilidades(Pokemon.TipoPokemon tipo) {
        return efectividades.getOrDefault(tipo, Map.of())
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == 0.5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static List<Pokemon.TipoPokemon> getDebilidadesDefensivas(Pokemon.TipoPokemon tipoDefensor) {
        return efectividades.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getOrDefault(tipoDefensor, 1.0) == 2.0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Tipos que le hacen x0.5 al tipo dado (resistencias defensivas)
    public static List<Pokemon.TipoPokemon> getResistenciasDefensivas(Pokemon.TipoPokemon tipoDefensor) {
        return efectividades.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getOrDefault(tipoDefensor, 1.0) == 0.5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Tipos que le hacen x0.0 al tipo dado (inmunidades defensivas)
    public static List<Pokemon.TipoPokemon> getInmunidadesDefensivas(Pokemon.TipoPokemon tipoDefensor) {
        return efectividades.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getOrDefault(tipoDefensor, 1.0) == 0.0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}