package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.DetalleReprogramacion;
import java.util.List;

/**
 * Registro append-only del detalle de las reprogramaciones (RN-AU-03, D-C02-2).
 *
 * <p>Es la segunda fuente que {@code ConsultarHistorialDeTurno} (C-08) debe combinar con
 * {@link RegistroAuditoria}: el evento dice QUE paso; este registro dice QUE valores
 * cambiaron. No expone modificacion ni borrado.
 */
public interface RegistroDetalleReprogramacion {

    void registrar(DetalleReprogramacion detalle);

    List<DetalleReprogramacion> listarPorTurno(String turnoId);
}
