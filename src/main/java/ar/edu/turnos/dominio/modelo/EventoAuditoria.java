package ar.edu.turnos.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/** Evento del registro de auditoria append-only (RN-AU-01, RN-AU-02). */
public record EventoAuditoria(String turnoId, AccionAuditoria accion, String usuario, LocalDateTime timestamp) {
    public EventoAuditoria {
        Objects.requireNonNull(turnoId, "turnoId");
        Objects.requireNonNull(accion, "accion");
        Objects.requireNonNull(timestamp, "timestamp");
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("usuario obligatorio");
        }
    }
}
