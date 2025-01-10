package com.example.Pokemon.DTO;

import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Pokemon;

public class AtaqueRequest {
    private Pokemon usuario;
    private Pokemon rival;
    private Ataque ataque;
    public Pokemon getUsuario() {
        return usuario;
    }

    public void setUsuario(Pokemon usuario) {
        this.usuario = usuario;
    }

    public Pokemon getRival() {
        return rival;
    }

    public void setRival(Pokemon rival) {
        this.rival = rival;
    }

    public Ataque getAtaque() {
        return ataque;
    }

    public void setAtaque(Ataque ataque) {
        this.ataque = ataque;
    }
}
