package com.filazero.notificationservice.enums;

public enum CriticalityLevel {
    BAIXA(1),
    NORMAL(2),
    ALTA(3),
    URGENTE(4);

    private final int peso;

    CriticalityLevel(int peso) {
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }
}
