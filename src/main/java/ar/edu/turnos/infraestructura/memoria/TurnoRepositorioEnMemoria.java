package ar.edu.turnos.infraestructura.memoria;

import ar.edu.turnos.dominio.modelo.Intervalo;
import ar.edu.turnos.dominio.modelo.RecursoAgenda;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.TurnoRepositorio;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptador de persistencia en memoria. Es el unico almacenamiento de la demo: no hay JPA ni
 * base de datos en la v1 (C-09 los incorpora). Sincronizado porque el servidor HTTP de la demo
 * atiende cada pedido en su propio hilo.
 *
 * <p>El filtro de vigencia (RN-AG-07) vive aca, igual que en el doble de test: un turno
 * CANCELADO o AUSENTE no ocupa su franja.
 */
public final class TurnoRepositorioEnMemoria implements TurnoRepositorio {

    private final Map<String, Turno> turnos = new LinkedHashMap<>();

    @Override
    public synchronized Optional<Turno> buscarPorId(String id) {
        return Optional.ofNullable(turnos.get(id));
    }

    @Override
    public synchronized Turno guardar(Turno turno) {
        turnos.put(turno.id(), turno);
        return turno;
    }

    @Override
    public synchronized List<Turno> buscarVigentesSolapados(
            RecursoAgenda recurso, Intervalo intervalo, String excluirTurnoId) {
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

    /** Todos los turnos en orden de alta, vigentes o no: la demo los muestra todos. */
    public synchronized List<Turno> todos() {
        return List.copyOf(turnos.values());
    }

    public synchronized void vaciar() {
        turnos.clear();
    }
}
