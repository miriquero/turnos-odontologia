package ar.edu.turnos.dominio.servicio;

import ar.edu.turnos.dominio.modelo.Intervalo;
import java.util.Objects;

/** Consulta de disponibilidad. Sin datos de sobreturno: la regla informa siempre el estado real (D-05). */
public record SolicitudDisponibilidad(String profesionalId, String sillonId, Intervalo intervalo,
        String excluirTurnoId) {
    public SolicitudDisponibilidad {
        Objects.requireNonNull(profesionalId, "profesionalId");
        Objects.requireNonNull(sillonId, "sillonId");
        Objects.requireNonNull(intervalo, "intervalo");
    }
}
