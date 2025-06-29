package com.example.Pokemon.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "efecto")
public class Efecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    public enum tipoEfecto {
        DANO_CONTINUO,
        SUBIR_VIDA,
        BAJAR_ATAQUE_RIVAL,
        BAJAR_DEFENSA_RIVAL,
        SUBIR_ATAQUE_PROPIO,
        SUBIR_DEFENSA_PROPIO,
        // Tipo legacy para compatibilidad - será migrado
        SUBIR_VELOCIDAD_PROPIO,
        BAJAR_VELOCIDAD_RIVAL
    }
    @Enumerated(EnumType.STRING)
    private tipoEfecto tipoEfecto;
    private double multiplicador;
    private String descripcion;

    public Efecto() {

    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public tipoEfecto getTipoEfecto() {
        return tipoEfecto;
    }
    public void setTipoEfecto(tipoEfecto tipoEfecto) {
        this.tipoEfecto = tipoEfecto;
    }
    public double getMultiplicador() {
        return multiplicador;
    }
    public void setMultiplicador(double multiplicador) {
        this.multiplicador = multiplicador;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
