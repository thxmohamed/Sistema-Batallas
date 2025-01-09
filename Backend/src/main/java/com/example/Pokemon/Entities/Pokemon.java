package com.example.Pokemon.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon")
public class Pokemon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    public enum TipoPokemon {
        AGUA,
        FUEGO,
        PLANTA,
        TIERRA,
        ELECTRICO
    }
    @Enumerated(EnumType.STRING)
    public TipoPokemon tipoPokemon;

    private Long vida;
    private Long ataque;
    private Long defensa;
    private Long velocidad;

    private Long idAtaque1;
    private Long idAtaque2;

    private Long idEfecto;
    private int estado; // 1 - primera evolucion, 2 - segunda evolucion, 3 - ultima evolucion, 4 no evoluciona

    @Lob
    private byte[] sprite;

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
    public TipoPokemon getTipoPokemon() {
        return tipoPokemon;
    }
    public void setTipoPokemon(TipoPokemon tipoPokemon) {
        this.tipoPokemon = tipoPokemon;
    }
    public Long getVida() {
        return vida;
    }
    public void setVida(Long vida) {
        this.vida = vida;
    }
    public Long getAtaque() {
        return ataque;
    }
    public void setAtaque(Long ataque) {
        this.ataque = ataque;
    }
    public Long getDefensa() {
        return defensa;
    }
    public void setDefensa(Long defensa) {
        this.defensa = defensa;
    }
    public Long getVelocidad() {
        return velocidad;
    }
    public void setVelocidad(Long velocidad) {
        this.velocidad = velocidad;
    }
    public Long getIdAtaque1() {
        return idAtaque1;
    }
    public void setIdAtaque1(Long idAtaque1) {
        this.idAtaque1 = idAtaque1;
    }
    public Long getIdAtaque2() {
        return idAtaque2;
    }
    public void setIdAtaque2(Long idAtaque2) {
        this.idAtaque2 = idAtaque2;
    }
    public Long getIdEfecto() {
        return idEfecto;
    }
    public void setIdEfecto(Long idEfecto) {
        this.idEfecto = idEfecto;
    }
    public int getEstado() {
        return estado;
    }
    public void setEstado(int estado) {
        this.estado = estado;
    }
    public byte[] getSprite() {
        return sprite;
    }
    public void setSprite(byte[] sprite) {
        this.sprite = sprite;
    }

}
