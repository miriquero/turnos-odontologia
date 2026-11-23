package ar.edu.turnos.aplicacion.casouso;

/** Solicitud para cancelar un turno agendado con motivo obligatorio (C-03, RN-TU-05). */
public record SolicitudCancelarTurno(String turnoId, String motivo, String usuario) {
}
