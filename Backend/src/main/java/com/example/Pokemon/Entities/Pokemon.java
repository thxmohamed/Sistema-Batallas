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

    // Estadísticas base (se mantienen para calcular efectos)
    private Long vidaBase;
    private Long ataqueBase;
    private Long defensaBase;

    // Efectos activos
    private Long ataqueModificado; // Para efectos de ataque
    private Long defensaModificada; // Para efectos de defensa
    private boolean tieneEfectoContinuo; // Para efectos como veneno
    private int turnosEfectoContinuo; // Contador de turnos para efectos continuos
    private Long idEfectoActivo; // ID del efecto activo

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

    // Getters y setters para campos de estadísticas base
    public Long getVidaBase() {
        return vidaBase;
    }
    public void setVidaBase(Long vidaBase) {
        this.vidaBase = vidaBase;
    }
    public Long getAtaqueBase() {
        return ataqueBase;
    }
    public void setAtaqueBase(Long ataqueBase) {
        this.ataqueBase = ataqueBase;
    }
    public Long getDefensaBase() {
        return defensaBase;
    }
    public void setDefensaBase(Long defensaBase) {
        this.defensaBase = defensaBase;
    }

    // Getters y setters para efectos activos
    public Long getAtaqueModificado() {
        return ataqueModificado;
    }
    public void setAtaqueModificado(Long ataqueModificado) {
        this.ataqueModificado = ataqueModificado;
    }
    public Long getDefensaModificada() {
        return defensaModificada;
    }
    public void setDefensaModificada(Long defensaModificada) {
        this.defensaModificada = defensaModificada;
    }
    public boolean isTieneEfectoContinuo() {
        return tieneEfectoContinuo;
    }
    public void setTieneEfectoContinuo(boolean tieneEfectoContinuo) {
        this.tieneEfectoContinuo = tieneEfectoContinuo;
    }
    public int getTurnosEfectoContinuo() {
        return turnosEfectoContinuo;
    }
    public void setTurnosEfectoContinuo(int turnosEfectoContinuo) {
        this.turnosEfectoContinuo = turnosEfectoContinuo;
    }
    public Long getIdEfectoActivo() {
        return idEfectoActivo;
    }
    public void setIdEfectoActivo(Long idEfectoActivo) {
        this.idEfectoActivo = idEfectoActivo;
    }

    // Métodos utilitarios para efectos
    public void inicializarEstadisticasBase() {
        if (vidaBase == null) vidaBase = vida;
        if (ataqueBase == null) ataqueBase = ataque;
        if (defensaBase == null) defensaBase = defensa;
    }

    public void resetearModificadores() {
        ataqueModificado = null;
        defensaModificada = null;
        
        // Restaurar estadísticas base solo si no son efectos permanentes
        if (ataqueBase != null) ataque = ataqueBase;
        if (defensaBase != null) defensa = defensaBase;
    }

    public Long getAtaqueEfectivo() {
        return ataqueModificado != null ? ataqueModificado : ataque;
    }

    public Long getDefensaEfectiva() {
        return defensaModificada != null ? defensaModificada : defensa;
    }
}
