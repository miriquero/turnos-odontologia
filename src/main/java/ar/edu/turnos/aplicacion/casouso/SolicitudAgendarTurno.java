package ar.edu.turnos.aplicacion.casouso;

import java.time.LocalDateTime;

/** Entrada del alta. El unico dato temporal es el inicio: el fin se deriva de la prestacion (RN-AG-01). */
public record SolicitudAgendarTurno(String pacienteId, String profesionalId, String sillonId, String prestacionId,
        LocalDateTime inicio, String usuario) {
}
