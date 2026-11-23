package ar.edu.turnos.aplicacion.casouso;

/** Entrada para cerrar un turno por atención realizada. */
public record SolicitudMarcarAtendido(String turnoId, String usuario) {
}
