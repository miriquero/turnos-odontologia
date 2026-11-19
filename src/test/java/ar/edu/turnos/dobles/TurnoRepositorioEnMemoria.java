package ar.edu.turnos.dobles;

import ar.edu.turnos.dominio.modelo.Intervalo;
import ar.edu.turnos.dominio.modelo.RecursoAgenda;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.TurnoRepositorio;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TurnoRepositorioEnMemoria implements TurnoRepositorio {

    private final Map<String, Turno> turnos = new LinkedHashMap<>();

    @Override
    public Optional<Turno> buscarPorId(String id) {
        return Optional.ofNullable(turnos.get(id));
    }

    @Override
    public Turno guardar(Turno turno) {
        turnos.put(turno.id(), turno);
        return turno;
    }

    @Override
    public List<Turno> buscarVigentesSolapados(RecursoAgenda recurso, Intervalo intervalo, String excluirTurnoId) {
        List<Turno> resultado = new ArrayList<>();
        for (Turno t : turnos.values()) {
            boolean delRecurso = switch (recurso.tipo()) {
                case PROFESIONAL -> t.profesionalId().equals(recurso.id());
                case SILLON -> t.sillonId().equals(recurso.id());
            };
            if (delRecurso
                    && t.estado().ocupaAgenda()
                    && t.intervalo().seSolapaCon(intervalo)
                    && !t.id().equals(excluirTurnoId)) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    public List<Turno> todos() {
        return List.copyOf(turnos.values());
    }
}
