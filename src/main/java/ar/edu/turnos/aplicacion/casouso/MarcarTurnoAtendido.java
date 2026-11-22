package ar.edu.turnos.aplicacion.casouso;

import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.RegistroAuditoria;
import ar.edu.turnos.dominio.puerto.TurnoRepositorio;
import java.time.Clock;
import java.time.LocalDateTime;

/** Registra la atención de un turno agendado, sin exigir que haya llegado el inicio (C-05). */
public class MarcarTurnoAtendido {

    private final TurnoRepositorio turnos;
    private final RegistroAuditoria auditoria;
    private final Clock reloj;

    public MarcarTurnoAtendido(TurnoRepositorio turnos, RegistroAuditoria auditoria, Clock reloj) {
        this.turnos = turnos;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public Turno ejecutar(SolicitudMarcarAtendido solicitud) {
        if (solicitud.usuario() == null || solicitud.usuario().isBlank()) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AU_01,
                    "No se puede identificar al usuario responsable");
        }
        Turno turno = turnos.buscarPorId(solicitud.turnoId())
                .orElseThrow(() -> new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02,
                        "No existe el turno " + solicitud.turnoId()));
        if (!turno.estado().puedeTransicionarA(EstadoTurno.ATENDIDO)) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02,
                    "El turno ya esta en un estado terminal");
        }
        Turno cerrado = turno.marcarAtendido();
        LocalDateTime ahora = LocalDateTime.now(reloj);
        turnos.guardar(cerrado);
        auditoria.registrar(new EventoAuditoria(cerrado.id(), AccionAuditoria.MARCADO_ATENDIDO,
                solicitud.usuario(), ahora));
        return cerrado;
    }
}
