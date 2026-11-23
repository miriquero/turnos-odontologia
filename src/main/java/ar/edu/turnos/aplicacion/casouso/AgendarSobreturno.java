package ar.edu.turnos.aplicacion.casouso;

import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.AutorizacionSobreturno;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.AutorizacionSobreturnoRepositorio;
import ar.edu.turnos.dominio.puerto.NotificadorPaciente;
import ar.edu.turnos.dominio.puerto.PacienteRepositorio;
import ar.edu.turnos.dominio.puerto.PrestacionRepositorio;
import ar.edu.turnos.dominio.puerto.ProfesionalRepositorio;
import ar.edu.turnos.dominio.puerto.RegistroAuditoria;
import ar.edu.turnos.dominio.puerto.SillonRepositorio;
import ar.edu.turnos.dominio.puerto.TurnoRepositorio;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Alta excepcional de un sobreturno autorizado (C-04, D-05, RN-AG-06).
 * La decision de omitir RN-AG-02 y RN-AG-03 vive exclusivamente en este caso de uso;
 * ServicioDisponibilidad nunca se modifica ni informa disponible una franja ocupada.
 */
public class AgendarSobreturno {

    private static final String RN_AG_06 = "RN-AG-06";

    private final TurnoRepositorio turnos;
    private final PacienteRepositorio pacientes;
    private final ProfesionalRepositorio profesionales;
    private final SillonRepositorio sillones;
    private final PrestacionRepositorio prestaciones;
    private final AutorizacionSobreturnoRepositorio autorizaciones;
    private final RegistroAuditoria auditoria;
    private final NotificadorPaciente notificador;
    private final Clock reloj;

    public AgendarSobreturno(
            TurnoRepositorio turnos,
            PacienteRepositorio pacientes,
            ProfesionalRepositorio profesionales,
            SillonRepositorio sillones,
            PrestacionRepositorio prestaciones,
            AutorizacionSobreturnoRepositorio autorizaciones,
            RegistroAuditoria auditoria,
            NotificadorPaciente notificador,
            Clock reloj) {
        this.turnos = turnos;
        this.pacientes = pacientes;
        this.profesionales = profesionales;
        this.sillones = sillones;
        this.prestaciones = prestaciones;
        this.autorizaciones = autorizaciones;
        this.auditoria = auditoria;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    public Turno ejecutar(SolicitudAgendarSobreturno solicitud) {
        validarAutorizacionSobreturno(solicitud);

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
        if (solicitud.inicio() == null || !solicitud.inicio().isAfter(ahora)) {
            throw new ReglaDeNegocioViolada(
                    ReglaDeNegocioViolada.RN_AG_08, "El inicio debe ser estrictamente futuro");
        }

        Turno turno = Turno.reconstruir(
                UUID.randomUUID().toString(),
                paciente.id(),
                profesional.id(),
                sillon.id(),
                prestacion.id(),
                prestacion.duracionMinutos(),
                solicitud.inicio(),
                EstadoTurno.AGENDADO,
                true);

        turnos.guardar(turno);
        autorizaciones.guardar(new AutorizacionSobreturno(
                turno.id(), solicitud.autorizadoPor(), solicitud.motivo(), ahora));
        auditoria.registrar(new EventoAuditoria(
                turno.id(), AccionAuditoria.SOBRETURNO_AUTORIZADO, solicitud.autorizadoPor(), ahora));
        try {
            notificador.notificar(turno, AccionAuditoria.SOBRETURNO_AUTORIZADO);
        } catch (RuntimeException fallo) {
            // RN-NO-03: un fallo del aviso no invalida el sobreturno.
        }
        return turno;
    }

    private static void validarAutorizacionSobreturno(SolicitudAgendarSobreturno solicitud) {
        if (solicitud == null || !solicitud.esSobreturno()) {
            throw new ReglaDeNegocioViolada(RN_AG_06, "El sobreturno exige marca explicita");
        }
        if (solicitud.autorizadoPor() == null || solicitud.autorizadoPor().isBlank()) {
            throw new ReglaDeNegocioViolada(RN_AG_06, "El sobreturno exige usuario responsable");
        }
        if (solicitud.motivo() == null || solicitud.motivo().isBlank()) {
            throw new ReglaDeNegocioViolada(RN_AG_06, "El sobreturno exige un motivo de autorizacion");
        }
        if (solicitud.rolAutorizador() != RolAutorizador.ODONTOLOGO_DUENIO) {
            throw new ReglaDeNegocioViolada(
                    RN_AG_06, "Solo el odontologo duenio puede autorizar un sobreturno");
        }
    }

    private static ReglaDeNegocioViolada noExiste(String recurso, String id) {
        return new ReglaDeNegocioViolada(
                ReglaDeNegocioViolada.RN_AG_08, "No existe " + recurso + " " + id);
    }

    private static ReglaDeNegocioViolada inactivo(String recurso) {
        return new ReglaDeNegocioViolada(
                ReglaDeNegocioViolada.RN_AG_08, "El " + recurso + " esta inactivo");
    }
}
