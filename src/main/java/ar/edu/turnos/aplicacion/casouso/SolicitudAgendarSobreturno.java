package ar.edu.turnos.aplicacion.casouso;

import java.time.LocalDateTime;

/**
 * Datos de entrada para el alta de un sobreturno autorizado (C-04).
 * El único dato temporal es {@code inicio}: el fin se deriva de la prestación (RN-AG-01).
 */
public record SolicitudAgendarSobreturno(
        String pacienteId,
        String profesionalId,
        String sillonId,
        String prestacionId,
        LocalDateTime inicio,
        boolean esSobreturno,
        String autorizadoPor,
        RolAutorizador rolAutorizador,
        String motivo) {
}
