package com.filazero.notificationservice.controller;

public enum BatchType {
    NOTIFICACAO_INICIAL,
    EXPIRAR_APPOINTMENTS,
    REPORT_GENERATION;

    public static BatchType fromString(String value) {
        for (BatchType type : BatchType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown batch type: " + value);
    }
}
