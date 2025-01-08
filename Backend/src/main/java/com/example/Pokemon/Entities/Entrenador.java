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
    public Long getIdPokemon1() {
        return idPokemon1;
    }
    public void setIdPokemon1(Long idPokemon1) {
        this.idPokemon1 = idPokemon1;
    }
    public Long getIdPokemon2() {
        return idPokemon2;
    }
    public void setIdPokemon2(Long idPokemon2) {
        this.idPokemon2 = idPokemon2;
    }
    public Long getIdPokemon3() {
        return idPokemon3;
    }
    public void setIdPokemon3(Long idPokemon3) {
        this.idPokemon3 = idPokemon3;
    }
}
