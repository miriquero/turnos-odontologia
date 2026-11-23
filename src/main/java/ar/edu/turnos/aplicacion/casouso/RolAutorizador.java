package ar.edu.turnos.aplicacion.casouso;

/**
 * Rol mínimo del usuario que solicita autorizar un sobreturno (SU-04, PA-03).
 * Los roles efectivos con seguridad real se implementan en C-15.
 */
public enum RolAutorizador {
    ODONTOLOGO_DUENIO,
    ODONTOLOGO_COLEGA,
    RECEPCION
}
