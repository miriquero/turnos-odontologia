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

/** Registra la ausencia de un turno agendado cuyo inicio ya llegó (C-05). */
public class RegistrarAusencia {

    private final TurnoRepositorio turnos;
    private final RegistroAuditoria auditoria;
    private final Clock reloj;

    public RegistrarAusencia(TurnoRepositorio turnos, RegistroAuditoria auditoria, Clock reloj) {
        this.turnos = turnos;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public Turno ejecutar(SolicitudRegistrarAusencia solicitud) {
        if (solicitud.usuario() == null || solicitud.usuario().isBlank()) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AU_01,
                    "No se puede identificar al usuario responsable");
        }
        Turno turno = turnos.buscarPorId(solicitud.turnoId())
                .orElseThrow(() -> new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02,
                        "No existe el turno " + solicitud.turnoId()));
        if (!turno.estado().puedeTransicionarA(EstadoTurno.AUSENTE)) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02,
                    "El turno ya esta en un estado terminal");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (ahora.isBefore(turno.intervalo().inicio())) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02,
                    "No se puede registrar una ausencia antes del inicio");
        }
        Turno cerrado = turno.marcarAusente(solicitud.motivo());
        turnos.guardar(cerrado);
        auditoria.registrar(new EventoAuditoria(cerrado.id(), AccionAuditoria.MARCADO_AUSENTE,
                solicitud.usuario(), ahora));
        return cerrado;
    }
}
