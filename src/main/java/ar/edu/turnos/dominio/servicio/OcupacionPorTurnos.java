package ar.edu.turnos.dominio.servicio;

import ar.edu.turnos.dominio.modelo.Intervalo;
import ar.edu.turnos.dominio.modelo.RecursoAgenda;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.FuenteDeOcupacion;
import ar.edu.turnos.dominio.puerto.TurnoRepositorio;
import java.util.List;

/** Unica fuente de ocupacion de este change: los turnos vigentes (el filtro de vigencia vive en el repositorio). */
public class OcupacionPorTurnos implements FuenteDeOcupacion {

    private final TurnoRepositorio turnos;

    public OcupacionPorTurnos(TurnoRepositorio turnos) {
        this.turnos = turnos;
    }

    @Override
    public List<Intervalo> intervalosOcupados(RecursoAgenda recurso, Intervalo franja, String excluirTurnoId) {
        return turnos.buscarVigentesSolapados(recurso, franja, excluirTurnoId).stream()
                .map(Turno::intervalo).toList();
    }
}
