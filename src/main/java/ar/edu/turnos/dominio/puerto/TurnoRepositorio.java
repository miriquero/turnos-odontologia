package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.Intervalo;
import ar.edu.turnos.dominio.modelo.RecursoAgenda;
import ar.edu.turnos.dominio.modelo.Turno;
import java.util.List;
import java.util.Optional;

public interface TurnoRepositorio {

    Optional<Turno> buscarPorId(String id);

    Turno guardar(Turno turno);

    /**
     * Turnos que ocupan la agenda (RN-AG-07) del recurso y que se solapan con el intervalo,
     * ignorando el turno cuyo id sea excluirTurnoId (puede ser null).
     */
    List<Turno> buscarVigentesSolapados(RecursoAgenda recurso, Intervalo intervalo, String excluirTurnoId);
}
