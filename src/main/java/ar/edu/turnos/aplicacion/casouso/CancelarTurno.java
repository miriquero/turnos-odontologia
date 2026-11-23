package ar.edu.turnos.aplicacion.casouso;

import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.NotificadorPaciente;
import ar.edu.turnos.dominio.puerto.RegistroAuditoria;
import ar.edu.turnos.dominio.puerto.TurnoRepositorio;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Caso de uso: Cancelar un turno agendado con motivo obligatorio y anticipacion minima (C-03).
 *
 * <p>Reglas que aplica:
 * <ul>
 *   <li>RN-AG-05: Anticipacion minima de N horas respecto del inicio (N configurable, D-02 limite inclusivo).</li>
 *   <li>RN-TU-05: Exige motivo no vacio.</li>
 *   <li>RN-TU-02: Solo transiciona desde AGENDADO; rechaza estados terminales.</li>
 *   <li>RN-AU-01: Auditoria append-only obligatoria con usuario y timestamp.</li>
 *   <li>RN-NO-03: Notificacion al paciente tolerante a fallos.</li>
 * </ul>
 */
public class CancelarTurno {

    public static final String RN_AG_05 = "RN-AG-05";
    public static final String RN_TU_05 = "RN-TU-05";
    public static final int ANTICIPACION_MINIMA_HORAS_POR_DEFECTO = 24;

    private final TurnoRepositorio turnos;
    private final RegistroAuditoria auditoria;
    private final NotificadorPaciente notificador;
    private final Clock reloj;
    private final int anticipacionMinimaHoras;

    public CancelarTurno(TurnoRepositorio turnos, RegistroAuditoria auditoria, NotificadorPaciente notificador, Clock reloj) {
        this(turnos, auditoria, notificador, reloj, ANTICIPACION_MINIMA_HORAS_POR_DEFECTO);
    }

    public CancelarTurno(TurnoRepositorio turnos, RegistroAuditoria auditoria, NotificadorPaciente notificador, Clock reloj, int anticipacionMinimaHoras) {
        this.turnos = turnos;
        this.auditoria = auditoria;
        this.notificador = notificador;
        this.reloj = reloj;
        this.anticipacionMinimaHoras = anticipacionMinimaHoras;
    }

    public Turno ejecutar(SolicitudCancelarTurno solicitud) {
        if (solicitud.usuario() == null || solicitud.usuario().isBlank()) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AU_01,
                    "No se puede identificar al usuario responsable");
        }
        if (solicitud.motivo() == null || solicitud.motivo().isBlank()) {
            throw new ReglaDeNegocioViolada(RN_TU_05,
                    "El motivo de cancelacion no puede estar vacio");
        }

        Turno turno = turnos.buscarPorId(solicitud.turnoId())
                .orElseThrow(() -> new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02,
                        "No existe el turno " + solicitud.turnoId()));

        if (!turno.estado().puedeTransicionarA(EstadoTurno.CANCELADO)) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02,
                    "El turno ya esta en un estado terminal");
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime limite = turno.intervalo().inicio().minusHours(anticipacionMinimaHoras);

        // Si ahora > limite, faltan menos de N horas -> rechaza (RN-AG-05)
        // Si ahora == limite (exactamente N horas), limite es inclusivo (D-02) -> se acepta
        if (ahora.isAfter(limite)) {
            throw new ReglaDeNegocioViolada(RN_AG_05,
                    "No se puede cancelar con menos de " + anticipacionMinimaHoras + " horas de anticipacion");
        }

        Turno cancelado = turno.cancelar(solicitud.motivo());
        turnos.guardar(cancelado);

        auditoria.registrar(new EventoAuditoria(cancelado.id(), AccionAuditoria.CANCELADO,
                solicitud.usuario(), ahora));

        try {
            notificador.notificar(cancelado, AccionAuditoria.CANCELADO);
        } catch (Exception ignored) {
            // RN-NO-03: resiliencia ante caida del canal de notificacion
        }

        return cancelado;
    }
}
