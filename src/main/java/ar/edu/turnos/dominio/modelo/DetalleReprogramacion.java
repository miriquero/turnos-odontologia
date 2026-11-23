package ar.edu.turnos.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Detalle de una reprogramacion efectiva: estados anterior y nuevo y datos de correlacion
 * con el evento {@code REPROGRAMADO} (RN-AU-03, D-C02-8). Vive en un registro propio porque
 * {@link EventoAuditoria} quedo congelado en C-01.
 */
public record DetalleReprogramacion(String turnoId, SnapshotTurno anterior, SnapshotTurno nuevo,
        String usuario, LocalDateTime timestamp) {

    public DetalleReprogramacion {
        if (turnoId == null || turnoId.isBlank()) {
            throw new IllegalArgumentException("turnoId obligatorio");
        }
        Objects.requireNonNull(anterior, "anterior");
        Objects.requireNonNull(nuevo, "nuevo");
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("usuario obligatorio");
        }
        Objects.requireNonNull(timestamp, "timestamp");
    }
}
