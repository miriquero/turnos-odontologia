package ar.edu.turnos.infraestructura.memoria;

import ar.edu.turnos.dominio.modelo.DetalleReprogramacion;
import ar.edu.turnos.dominio.puerto.RegistroDetalleReprogramacion;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro append-only del detalle de las reprogramaciones (RN-AU-03). Es la segunda fuente
 * que hay que combinar con la auditoria para saber QUE valores cambiaron, porque
 * {@code EventoAuditoria} quedo congelado en C-01 con cuatro campos.
 */
public final class RegistroDetalleReprogramacionEnMemoria implements RegistroDetalleReprogramacion {

    private final List<DetalleReprogramacion> detalles = new ArrayList<>();

    @Override
    public synchronized void registrar(DetalleReprogramacion detalle) {
        detalles.add(detalle);
    }

    @Override
    public synchronized List<DetalleReprogramacion> listarPorTurno(String turnoId) {
        return detalles.stream().filter(d -> d.turnoId().equals(turnoId)).toList();
    }

    public synchronized List<DetalleReprogramacion> todos() {
        return List.copyOf(detalles);
    }

    public synchronized void vaciar() {
        detalles.clear();
    }
}
