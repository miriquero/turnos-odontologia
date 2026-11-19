package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.Turno;

/**
 * Aviso al paciente; el dominio no conoce ningun proveedor de mensajeria (RN-NO-01).
 *
 * <p>El metodo es generico a proposito: la accion describe que le paso al turno,
 * reutilizando {@link AccionAuditoria}. Asi los changes que agregan operaciones sobre
 * el turno (reprogramar, cancelar, sobreturno, ausencia) notifican sin modificar este
 * puerto, que queda congelado por R-05.
 */
public interface NotificadorPaciente {
    void notificar(Turno turno, AccionAuditoria accion);
}
