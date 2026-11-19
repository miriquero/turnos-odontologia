package ar.edu.turnos.dominio.modelo;

import java.util.Objects;

public record Sillon(String id, String nombre, boolean activo) {
    public Sillon {
        Objects.requireNonNull(id, "id");
    }
}
