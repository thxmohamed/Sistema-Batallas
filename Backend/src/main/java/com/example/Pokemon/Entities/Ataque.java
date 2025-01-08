package com.example.Pokemon.Entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ataque")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ataque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    enum TipoAtaque {
        AGUA,
        FUEGO,
        PLANTA,
        TIERRA,
        ELECTRICO
    }
    @Enumerated(EnumType.STRING)

    private TipoAtaque tipoAtaque;
    private int potencia;
    private String descripcion;

}
