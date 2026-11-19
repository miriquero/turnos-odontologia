package ar.edu.turnos.dominio.modelo;

import java.util.Objects;

public record Profesional(String id, String nombre, boolean activo) {
    public Profesional {
        Objects.requireNonNull(id, "id");
    }
}
