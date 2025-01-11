package com.example.Pokemon.DTO;

import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;

import java.util.List;

public class BatallaDTO {
    List<Pokemon> entrenador1;
    List<Pokemon> entrenador2;
    Ataque ataqueE1;
    Ataque ataqueE2;
    //Efecto efectoE1;
    //Efecto efectoE2;
    int turno;

    public List<Pokemon> getEntrenador1(){
        return entrenador1;
    }
    public void setEntrenador1(List<Pokemon> entrenador1){
        this.entrenador1 = entrenador1;
    }
    public List<Pokemon> getEntrenador2(){
        return entrenador2;
    }
    public void setEntrenador2(List<Pokemon> entrenador2){
        this.entrenador2 = entrenador2;
    }
    public Ataque getataqueE1(){
        return ataqueE1;
    }
    public void setataqueE1(Ataque ataqueE1){
        this.ataqueE1 = ataqueE1;
    }
    public Ataque getataqueE2(){
        return ataqueE2;
    }
    public void setataqueE2(Ataque ataqueE2){
        this.ataqueE2 = ataqueE2;
    }
    /*
    public Efecto getEfectoE1(){
        return efectoE1;
    }
    public void setEfectoE1(Efecto efectoE1){
        this.efectoE1 = efectoE1;
    }
    public Efecto getEfectoE2(){
        return efectoE2;
    }
    public void setEfectoE2(Efecto efectoE2){
        this.efectoE2 = efectoE2;
    }

     */
    public int getTurno(){
        return turno;
    }
    public void setTurno(int turno){
        this.turno = turno;
    }
}
