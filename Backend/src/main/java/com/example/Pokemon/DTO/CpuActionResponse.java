package com.example.Pokemon.DTO;

public class CpuActionResponse {
    private int attackerIndex;
    private int targetIndex;
    private boolean useEffect;
    private int attackIndex; // Cual ataque usar (0 o 1)
    private String reasoning; // Para debug/logs
    private boolean success;

    public CpuActionResponse() {
    }

    public CpuActionResponse(int attackerIndex, int targetIndex, boolean useEffect, int attackIndex, String reasoning) {
        this.attackerIndex = attackerIndex;
        this.targetIndex = targetIndex;
        this.useEffect = useEffect;
        this.attackIndex = attackIndex;
        this.reasoning = reasoning;
        this.success = true;
    }

    public int getAttackerIndex() {
        return attackerIndex;
    }

    public void setAttackerIndex(int attackerIndex) {
        this.attackerIndex = attackerIndex;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public boolean isUseEffect() {
        return useEffect;
    }

    public void setUseEffect(boolean useEffect) {
        this.useEffect = useEffect;
    }

    public int getAttackIndex() {
        return attackIndex;
    }

    public void setAttackIndex(int attackIndex) {
        this.attackIndex = attackIndex;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
