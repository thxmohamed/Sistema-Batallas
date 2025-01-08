package com.example.Pokemon.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "efecto")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Efecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    enum tipoEfecto {
        DANO_CONTINUO,
        BAJAR_ATAQUE_RIVAL,
        BAJAR_DEFENSA_RIVAL,
        BAJAR_VELOCIDAD_RIVAL,
        SUBIR_ATAQUE_PROPIO,
        SUBIR_DEFENSA_PROPIO,
        SUBIR_VELOCIDAD_PROPIO
    }
    @Enumerated(EnumType.STRING)
    private tipoEfecto tipoEfecto;
    private double multiplicador;
    private String descripcion;
}
