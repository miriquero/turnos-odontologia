package ar.edu.turnos.dobles;

import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.puerto.RegistroAuditoria;
import java.util.ArrayList;
import java.util.List;

public class RegistroAuditoriaEnMemoria implements RegistroAuditoria {

    private final List<EventoAuditoria> eventos = new ArrayList<>();

    @Override
    public void registrar(EventoAuditoria evento) {
        eventos.add(evento);
    }

    @Override
    public List<EventoAuditoria> listarPorTurno(String turnoId) {
        return eventos.stream().filter(e -> e.turnoId().equals(turnoId)).toList();
    }

    public List<EventoAuditoria> todos() {
        return List.copyOf(eventos);
    }
}
