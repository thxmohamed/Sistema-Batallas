package com.example.Pokemon.DTO;

import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;

public class AplicarEfectoRequest {
    Pokemon usuario;
    Pokemon rival;
    Efecto efecto;

    public Pokemon getUsuario(){
        return usuario;
    }

    public void setUsuario(Pokemon usuario){
        this.usuario = usuario;
    }

    public Pokemon getRival(){
        return rival;
    }

    public void setRival(Pokemon rival){
        this.rival = rival;
    }

    public Efecto getEfecto(){
        return efecto;
    }

    public void setEfecto(Efecto efecto){
        this.efecto = efecto;
    }
}
