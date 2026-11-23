package ar.edu.turnos.aplicacion.casouso;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entrada de la reprogramacion. El unico dato temporal es el nuevo inicio: el fin se deriva
 * de la prestacion resultante (RN-AG-01, D-C02-4). Los recursos nulos significan
 * "conservar el actual". No se valida aqui el usuario: el caso de uso lo rechaza con RN-AU-01.
 */
public record SolicitudReprogramarTurno(String turnoId, LocalDateTime inicio, String profesionalId,
        String sillonId, String prestacionId, String usuario) {

    public SolicitudReprogramarTurno {
        if (turnoId == null || turnoId.isBlank()) {
            throw new IllegalArgumentException("turnoId obligatorio");
        }
        Objects.requireNonNull(inicio, "inicio");
    }
}
