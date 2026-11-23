package ar.edu.turnos.dobles;

import ar.edu.turnos.dominio.modelo.DetalleReprogramacion;
import ar.edu.turnos.dominio.puerto.RegistroDetalleReprogramacion;
import java.util.ArrayList;
import java.util.List;

/** Doble append-only del registro de detalle de reprogramaciones (D-C02-11). */
public class RegistroDetalleReprogramacionEnMemoria implements RegistroDetalleReprogramacion {

    private final List<DetalleReprogramacion> detalles = new ArrayList<>();

    @Override
    public void registrar(DetalleReprogramacion detalle) {
        detalles.add(detalle);
    }

    @Override
    public List<DetalleReprogramacion> listarPorTurno(String turnoId) {
        return detalles.stream().filter(d -> d.turnoId().equals(turnoId)).toList();
    }

    public List<DetalleReprogramacion> todos() {
        return List.copyOf(detalles);
    }
}
