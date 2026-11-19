package ar.edu.turnos.aplicacion.casouso;

import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.NotificadorPaciente;
import ar.edu.turnos.dominio.puerto.PacienteRepositorio;
import ar.edu.turnos.dominio.puerto.PrestacionRepositorio;
import ar.edu.turnos.dominio.puerto.ProfesionalRepositorio;
import ar.edu.turnos.dominio.puerto.RegistroAuditoria;
import ar.edu.turnos.dominio.puerto.SillonRepositorio;
import ar.edu.turnos.dominio.puerto.TurnoRepositorio;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import ar.edu.turnos.dominio.servicio.SolicitudDisponibilidad;
import java.time.Clock;
import java.time.LocalDateTime;

/** Alta de un turno sin solapamientos (C-01). */
public class AgendarTurno {

    private final TurnoRepositorio turnos;
    private final PacienteRepositorio pacientes;
    private final ProfesionalRepositorio profesionales;
    private final SillonRepositorio sillones;
    private final PrestacionRepositorio prestaciones;
    private final ServicioDisponibilidad disponibilidad;
    private final RegistroAuditoria auditoria;
    private final NotificadorPaciente notificador;
    private final Clock reloj;

    public AgendarTurno(TurnoRepositorio turnos, PacienteRepositorio pacientes, ProfesionalRepositorio profesionales,
            SillonRepositorio sillones, PrestacionRepositorio prestaciones, ServicioDisponibilidad disponibilidad,
            RegistroAuditoria auditoria, NotificadorPaciente notificador, Clock reloj) {
        this.turnos = turnos;
        this.pacientes = pacientes;
        this.profesionales = profesionales;
        this.sillones = sillones;
        this.prestaciones = prestaciones;
        this.disponibilidad = disponibilidad;
        this.auditoria = auditoria;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    public Turno ejecutar(SolicitudAgendarTurno solicitud) {
        if (solicitud.usuario() == null || solicitud.usuario().isBlank()) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AU_01,
                    "No se puede identificar al usuario responsable");
        }
        Paciente paciente = pacientes.buscarPorId(solicitud.pacienteId())
                .orElseThrow(() -> noExiste("paciente", solicitud.pacienteId()));
        Profesional profesional = profesionales.buscarPorId(solicitud.profesionalId())
                .orElseThrow(() -> noExiste("profesional", solicitud.profesionalId()));
        Sillon sillon = sillones.buscarPorId(solicitud.sillonId())
                .orElseThrow(() -> noExiste("sillon", solicitud.sillonId()));
        Prestacion prestacion = prestaciones.buscarPorId(solicitud.prestacionId())
                .orElseThrow(() -> noExiste("prestacion", solicitud.prestacionId()));
        if (!profesional.activo()) {
            throw inactivo("profesional");
        }
        if (!sillon.activo()) {
            throw inactivo("sillon");
        }
        if (!prestacion.activa()) {
            throw inactivo("prestacion");
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (!solicitud.inicio().isAfter(ahora)) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AG_08, "El inicio debe ser estrictamente futuro");
        }

        Turno turno = Turno.agendar(paciente, profesional, sillon, prestacion, solicitud.inicio());
        disponibilidad.verificarDisponibilidad(new SolicitudDisponibilidad(
                profesional.id(), sillon.id(), turno.intervalo(), null));

        turnos.guardar(turno);
        auditoria.registrar(new EventoAuditoria(turno.id(), AccionAuditoria.CREADO, solicitud.usuario(), ahora));
        try {
            notificador.notificar(turno, AccionAuditoria.CREADO);
        } catch (RuntimeException fallo) {
            // RN-NO-03: un fallo del aviso no invalida el alta.
        }
        return turno;
    }

    private static ReglaDeNegocioViolada noExiste(String recurso, String id) {
        return new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AG_08, "No existe " + recurso + " " + id);
    }

    private static ReglaDeNegocioViolada inactivo(String recurso) {
        return new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AG_08, "El " + recurso + " esta inactivo");
    }
}
