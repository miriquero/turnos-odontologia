package ar.edu.turnos.dominio.servicio;

import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.RecursoAgenda;
import ar.edu.turnos.dominio.puerto.FuenteDeOcupacion;

/**
 * Regla de disponibilidad (RN-AG-02, RN-AG-03). Congelado por R-05: dos operaciones publicas, sin busqueda de
 * huecos ni nocion de sobreturno.
 */
public class ServicioDisponibilidad {

    private final FuenteDeOcupacion ocupacion;

    public ServicioDisponibilidad(FuenteDeOcupacion ocupacion) {
        this.ocupacion = ocupacion;
    }

    /** Forma excepcional: se apoya en evaluar para que ambas formas no puedan divergir. */
    public void verificarDisponibilidad(SolicitudDisponibilidad solicitud) {
        ResultadoDisponibilidad r = evaluar(solicitud);
        if (!r.disponible()) {
            throw new ReglaDeNegocioViolada(r.codigoRegla(), mensajePara(r.codigoRegla()));
        }
    }

    /** Forma consultable. Profesional primero, despues sillon; corta en el primer fallo (D-C01-4). */
    public ResultadoDisponibilidad evaluar(SolicitudDisponibilidad s) {
        if (ocupado(RecursoAgenda.profesional(s.profesionalId()), s)) {
            return ResultadoDisponibilidad.ocupado(ReglaDeNegocioViolada.RN_AG_02);
        }
        if (ocupado(RecursoAgenda.sillon(s.sillonId()), s)) {
            return ResultadoDisponibilidad.ocupado(ReglaDeNegocioViolada.RN_AG_03);
        }
        return ResultadoDisponibilidad.libre();
    }

    private boolean ocupado(RecursoAgenda recurso, SolicitudDisponibilidad s) {
        return !ocupacion.intervalosOcupados(recurso, s.intervalo(), s.excluirTurnoId()).isEmpty();
    }

    private static String mensajePara(String codigo) {
        return ReglaDeNegocioViolada.RN_AG_02.equals(codigo)
                ? "El profesional ya tiene un turno en esa franja"
                : "El sillon ya esta ocupado en esa franja";
    }
}
