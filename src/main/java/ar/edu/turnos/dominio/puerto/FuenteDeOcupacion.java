package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.Intervalo;
import ar.edu.turnos.dominio.modelo.RecursoAgenda;
import java.util.List;

/** Puerto de lectura de ocupacion: intervalos ocupados de un recurso dentro de una franja (D-C01-2). */
public interface FuenteDeOcupacion {
    List<Intervalo> intervalosOcupados(RecursoAgenda recurso, Intervalo franja, String excluirTurnoId);
}
