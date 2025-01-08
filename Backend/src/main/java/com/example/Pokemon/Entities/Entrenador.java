package com.example.Pokemon.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "entrenador")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entrenador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Long idPokemon1;
    private Long idPokemon2;
    private Long idPokemon3;
}
