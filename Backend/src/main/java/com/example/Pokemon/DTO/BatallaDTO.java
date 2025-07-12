package com.example.Pokemon.DTO;

import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;

import java.util.List;

public class BatallaDTO {
    List<Pokemon> entrenador1;
    List<Pokemon> entrenador2;
    
    // Nombres de los equipos para batallas aleatorias
    private String nombreEquipo1;
    private String nombreEquipo2;
    
    Ataque ataqueE1;
    Ataque ataqueE2;
    Efecto efectoE1;
    Efecto efectoE2;
    // Campos para indicar si usar ataque o efecto
    boolean usarEfectoE1; // true = usar efecto, false = usar ataque
    boolean usarEfectoE2; // true = usar efecto, false = usar ataque
    int turno;
    
    // Nuevos campos para efectos de equipo (DANO_CONTINUO)
    private Long efectoContinuoEquipo1; // ID del efecto continuo activo en equipo 1
    private Long efectoContinuoEquipo2; // ID del efecto continuo activo en equipo 2
    private int turnosRestantesEquipo1; // Turnos restantes del efecto en equipo 1
    private int turnosRestantesEquipo2; // Turnos restantes del efecto en equipo 2

    // Nuevos campos para efectos de reducción de estadísticas aplicados
    private boolean ataqueReducidoEquipo1; // Indica si el ataque del equipo 1 fue reducido este turno
    private boolean ataqueReducidoEquipo2; // Indica si el ataque del equipo 2 fue reducido este turno
    private boolean defensaReducidaEquipo1; // Indica si la defensa del equipo 1 fue reducida este turno
    private boolean defensaReducidaEquipo2; // Indica si la defensa del equipo 2 fue reducida este turno

    // Campos para el factor de agresividad - turnos consecutivos sin atacar
    private int turnosSinAtacarEquipo1; // Turnos consecutivos que el equipo 1 ha usado efectos en lugar de atacar
    private int turnosSinAtacarEquipo2; // Turnos consecutivos que el equipo 2 ha usado efectos en lugar de atacar

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
    
    public boolean isUsarEfectoE1(){
        return usarEfectoE1;
    }
    public void setUsarEfectoE1(boolean usarEfectoE1){
        this.usarEfectoE1 = usarEfectoE1;
    }
    public boolean isUsarEfectoE2(){
        return usarEfectoE2;
    }
    public void setUsarEfectoE2(boolean usarEfectoE2){
        this.usarEfectoE2 = usarEfectoE2;
    }

    public int getTurno(){
        return turno;
    }
    public void setTurno(int turno){
        this.turno = turno;
    }
    
    // Getters y setters para efectos de equipo
    public Long getEfectoContinuoEquipo1() {
        return efectoContinuoEquipo1;
    }
    
    public void setEfectoContinuoEquipo1(Long efectoContinuoEquipo1) {
        this.efectoContinuoEquipo1 = efectoContinuoEquipo1;
    }
    
    public Long getEfectoContinuoEquipo2() {
        return efectoContinuoEquipo2;
    }
    
    public void setEfectoContinuoEquipo2(Long efectoContinuoEquipo2) {
        this.efectoContinuoEquipo2 = efectoContinuoEquipo2;
    }
    
    public int getTurnosRestantesEquipo1() {
        return turnosRestantesEquipo1;
    }
    
    public void setTurnosRestantesEquipo1(int turnosRestantesEquipo1) {
        this.turnosRestantesEquipo1 = turnosRestantesEquipo1;
    }
    
    public int getTurnosRestantesEquipo2() {
        return turnosRestantesEquipo2;
    }
    
    public void setTurnosRestantesEquipo2(int turnosRestantesEquipo2) {
        this.turnosRestantesEquipo2 = turnosRestantesEquipo2;
    }
    
    // Getters y setters para efectos de reducción de estadísticas
    public boolean isAtaqueReducidoEquipo1() {
        return ataqueReducidoEquipo1;
    }
    
    public void setAtaqueReducidoEquipo1(boolean ataqueReducidoEquipo1) {
        this.ataqueReducidoEquipo1 = ataqueReducidoEquipo1;
    }
    
    public boolean isAtaqueReducidoEquipo2() {
        return ataqueReducidoEquipo2;
    }
    
    public void setAtaqueReducidoEquipo2(boolean ataqueReducidoEquipo2) {
        this.ataqueReducidoEquipo2 = ataqueReducidoEquipo2;
    }
    
    public boolean isDefensaReducidaEquipo1() {
        return defensaReducidaEquipo1;
    }
    
    public void setDefensaReducidaEquipo1(boolean defensaReducidaEquipo1) {
        this.defensaReducidaEquipo1 = defensaReducidaEquipo1;
    }
    
    public boolean isDefensaReducidaEquipo2() {
        return defensaReducidaEquipo2;
    }
    
    public void setDefensaReducidaEquipo2(boolean defensaReducidaEquipo2) {
        this.defensaReducidaEquipo2 = defensaReducidaEquipo2;
    }
    
    // Getters y setters para nombres de equipos
    public String getNombreEquipo1() {
        return nombreEquipo1;
    }
    
    public void setNombreEquipo1(String nombreEquipo1) {
        this.nombreEquipo1 = nombreEquipo1;
    }
    
    public String getNombreEquipo2() {
        return nombreEquipo2;
    }
    
    public void setNombreEquipo2(String nombreEquipo2) {
        this.nombreEquipo2 = nombreEquipo2;
    }
    
    // Getters y setters para turnos sin atacar
    public int getTurnosSinAtacarEquipo1() {
        return turnosSinAtacarEquipo1;
    }
    
    public void setTurnosSinAtacarEquipo1(int turnosSinAtacarEquipo1) {
        this.turnosSinAtacarEquipo1 = turnosSinAtacarEquipo1;
    }
    
    public int getTurnosSinAtacarEquipo2() {
        return turnosSinAtacarEquipo2;
    }
    
    public void setTurnosSinAtacarEquipo2(int turnosSinAtacarEquipo2) {
        this.turnosSinAtacarEquipo2 = turnosSinAtacarEquipo2;
    }
}
