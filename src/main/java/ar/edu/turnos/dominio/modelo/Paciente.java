package ar.edu.turnos.dominio.modelo;

import java.util.Objects;

public record Paciente(String id, String nombre, String documento, boolean activo) {
    public Paciente {
        Objects.requireNonNull(id, "id");
    }
}
