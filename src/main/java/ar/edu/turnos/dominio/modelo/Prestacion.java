package ar.edu.turnos.dominio.modelo;

import java.util.Objects;

/** Prestacion del catalogo; su duracion determina el fin del turno (RN-CA-01, RN-AG-01). */
public record Prestacion(String id, String nombre, int duracionMinutos, boolean activa) {
    public Prestacion {
        Objects.requireNonNull(id, "id");
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("duracionMinutos debe ser positiva: " + duracionMinutos);
        }
    }
}
