package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import java.util.List;

/** Registro append-only (RN-AU-02): solo se agrega y se lee; no hay modificar ni borrar. */
public interface RegistroAuditoria {
    void registrar(EventoAuditoria evento);

    List<EventoAuditoria> listarPorTurno(String turnoId);
}
