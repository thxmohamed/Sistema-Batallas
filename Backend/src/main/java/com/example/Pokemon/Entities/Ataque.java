package com.example.Pokemon.Entities;


import jakarta.persistence.*;

@Entity
@Table(name = "ataque")
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

    public Ataque() {
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
    public TipoAtaque getTipoAtaque() {
        return tipoAtaque;
    }
    public void setTipoAtaque(TipoAtaque tipoAtaque) {
        this.tipoAtaque = tipoAtaque;
    }
    public int getPotencia() {
        return potencia;
    }
    public void setPotencia(int potencia) {
        this.potencia = potencia;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
