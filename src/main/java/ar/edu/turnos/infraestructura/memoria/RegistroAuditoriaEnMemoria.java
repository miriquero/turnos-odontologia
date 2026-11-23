package ar.edu.turnos.infraestructura.memoria;

import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.puerto.RegistroAuditoria;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro de auditoria append-only en memoria (RN-AU-02): expone registrar y leer, nunca
 * modificar ni borrar.
 */
public final class RegistroAuditoriaEnMemoria implements RegistroAuditoria {

    private final List<EventoAuditoria> eventos = new ArrayList<>();

    @Override
    public synchronized void registrar(EventoAuditoria evento) {
        eventos.add(evento);
    }

    @Override
    public synchronized List<EventoAuditoria> listarPorTurno(String turnoId) {
        return eventos.stream().filter(e -> e.turnoId().equals(turnoId)).toList();
    }

    public synchronized List<EventoAuditoria> todos() {
        return List.copyOf(eventos);
    }

    public synchronized void vaciar() {
        eventos.clear();
    }
}
