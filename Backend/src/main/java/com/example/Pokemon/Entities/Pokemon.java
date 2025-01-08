package com.example.Pokemon.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pokemon")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pokemon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    enum TipoPokemon {
        AGUA,
        FUEGO,
        PLANTA,
        TIERRA,
        ELECTRICO
    }
    @Enumerated(EnumType.STRING)

    private TipoPokemon tipoAtaque;

    private Long vida;
    private Long ataque;
    private Long defensa;
    private Long velocidad;

    private Long idAtaque1;
    private Long idAtaque2;

    private Long idEfecto;

}
