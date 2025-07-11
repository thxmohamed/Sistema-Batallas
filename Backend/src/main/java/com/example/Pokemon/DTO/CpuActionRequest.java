package com.example.Pokemon.DTO;

public class CpuActionRequest {
    private BatallaDTO batalla;
    private String difficulty;
    private boolean cpuIsTeam1;

    public CpuActionRequest() {
    }

    public CpuActionRequest(BatallaDTO batalla, String difficulty, boolean cpuIsTeam1) {
        this.batalla = batalla;
        this.difficulty = difficulty;
        this.cpuIsTeam1 = cpuIsTeam1;
    }

    public BatallaDTO getBatalla() {
        return batalla;
    }

    public void setBatalla(BatallaDTO batalla) {
        this.batalla = batalla;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isCpuIsTeam1() {
        return cpuIsTeam1;
    }

    public void setCpuIsTeam1(boolean cpuIsTeam1) {
        this.cpuIsTeam1 = cpuIsTeam1;
    }
}
