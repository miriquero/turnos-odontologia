package ar.edu.turnos.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Foto inmutable de los datos de un turno que pueden cambiar al reprogramarlo (D-C02-3).
 * Sirve a la vez como detalle de auditoria (RN-AU-03), como origen del intervalo a validar
 * y como comparacion para decidir si la solicitud es no-op.
 */
public record SnapshotTurno(String profesionalId, String sillonId, String prestacionId, int duracionMinutos,
        LocalDateTime inicio, LocalDateTime fin) {

    public SnapshotTurno {
        Objects.requireNonNull(profesionalId, "profesionalId");
        Objects.requireNonNull(sillonId, "sillonId");
        Objects.requireNonNull(prestacionId, "prestacionId");
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fin, "fin");
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("fin debe ser estrictamente posterior a inicio");
        }
    }

    /** Captura el estado actual de un turno, conservando su duracion vigente (RN-AG-01). */
    public static SnapshotTurno desde(Turno turno) {
        Objects.requireNonNull(turno, "turno");
        return new SnapshotTurno(turno.profesionalId(), turno.sillonId(), turno.prestacionId(),
                turno.duracionMinutos(), turno.intervalo().inicio(), turno.intervalo().fin());
    }

    public Intervalo intervalo() {
        return new Intervalo(inicio, fin);
    }
}
