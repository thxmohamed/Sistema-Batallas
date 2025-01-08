package com.example.Pokemon.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "batalla")
public class Batalla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idEntrenador1;
    private Long idEntrenador2;
    private int turno;

    public Batalla() {

    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getIdEntrenador1() {
        return idEntrenador1;
    }
    public void setIdEntrenador1(Long idEntrenador1) {
        this.idEntrenador1 = idEntrenador1;
    }
    public Long getIdEntrenador2() {
        return idEntrenador2;
    }
    public void setIdEntrenador2(Long idEntrenador2) {
        this.idEntrenador2 = idEntrenador2;
    }
    public int getTurno() {
        return turno;
    }
    public void setTurno(int turno) {
        this.turno = turno;
    }
}
