package ar.edu.turnos.aplicacion.casouso;

/** Entrada para cerrar un turno por ausencia del paciente. */
public record SolicitudRegistrarAusencia(String turnoId, String motivo, String usuario) {
}
