package ar.edu.turnos.aplicacion.casouso;

import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.DetalleReprogramacion;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.SnapshotTurno;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.NotificadorPaciente;
import ar.edu.turnos.dominio.puerto.PrestacionRepositorio;
import ar.edu.turnos.dominio.puerto.ProfesionalRepositorio;
import ar.edu.turnos.dominio.puerto.RegistroAuditoria;
import ar.edu.turnos.dominio.puerto.RegistroDetalleReprogramacion;
import ar.edu.turnos.dominio.puerto.SillonRepositorio;
import ar.edu.turnos.dominio.puerto.TurnoRepositorio;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import ar.edu.turnos.dominio.servicio.SolicitudDisponibilidad;
import java.time.Clock;
import java.time.LocalDateTime;

/** Reprogramacion de un turno agendado (C-02). Solo archivos nuevos: C-01 queda intacto. */
public class ReprogramarTurno {

    /** Codigo local de C-02: no se modifica ReglaDeNegocioViolada (D-C02-6). */
    public static final String RN_TU_04 = "RN-TU-04";

    private final TurnoRepositorio turnos;
    private final ProfesionalRepositorio profesionales;
    private final SillonRepositorio sillones;
    private final PrestacionRepositorio prestaciones;
    private final ServicioDisponibilidad disponibilidad;
    private final RegistroAuditoria auditoria;
    private final RegistroDetalleReprogramacion detalles;
    private final NotificadorPaciente notificador;
    private final Clock reloj;

    public ReprogramarTurno(TurnoRepositorio turnos, ProfesionalRepositorio profesionales,
            SillonRepositorio sillones, PrestacionRepositorio prestaciones,
            ServicioDisponibilidad disponibilidad, RegistroAuditoria auditoria,
            RegistroDetalleReprogramacion detalles, NotificadorPaciente notificador, Clock reloj) {
        this.turnos = turnos;
        this.profesionales = profesionales;
        this.sillones = sillones;
        this.prestaciones = prestaciones;
        this.disponibilidad = disponibilidad;
        this.auditoria = auditoria;
        this.detalles = detalles;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    public Turno ejecutar(SolicitudReprogramarTurno solicitud) {
        if (solicitud.usuario() == null || solicitud.usuario().isBlank()) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AU_01,
                    "No se puede identificar al usuario responsable");
        }
        Turno actual = turnos.buscarPorId(solicitud.turnoId())
                .orElseThrow(() -> new ReglaDeNegocioViolada(RN_TU_04,
                        "No existe el turno " + solicitud.turnoId()));
        if (actual.estado() != EstadoTurno.AGENDADO) {
            throw new ReglaDeNegocioViolada(RN_TU_04,
                    "Solo se puede reprogramar un turno AGENDADO");
        }

        String profesionalId = solicitud.profesionalId() != null
                ? solicitud.profesionalId() : actual.profesionalId();
        String sillonId = solicitud.sillonId() != null ? solicitud.sillonId() : actual.sillonId();
        String prestacionId = solicitud.prestacionId() != null
                ? solicitud.prestacionId() : actual.prestacionId();

        Profesional profesional = profesionales.buscarPorId(profesionalId)
                .orElseThrow(() -> noExiste("profesional", profesionalId));
        Sillon sillon = sillones.buscarPorId(sillonId)
                .orElseThrow(() -> noExiste("sillon", sillonId));
        Prestacion prestacion = prestaciones.buscarPorId(prestacionId)
                .orElseThrow(() -> noExiste("prestacion", prestacionId));
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
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AG_08,
                    "El inicio debe ser estrictamente futuro");
        }

        // Si la prestacion no cambia se conserva la duracion vigente al agendarse (RN-AG-01).
        int duracion = prestacion.id().equals(actual.prestacionId())
                ? actual.duracionMinutos() : prestacion.duracionMinutos();
        SnapshotTurno anterior = SnapshotTurno.desde(actual);
        SnapshotTurno nuevo = new SnapshotTurno(profesionalId, sillonId, prestacionId, duracion,
                solicitud.inicio(), solicitud.inicio().plusMinutes(duracion));

        if (anterior.equals(nuevo)) {
            return actual;
        }

        disponibilidad.verificarDisponibilidad(new SolicitudDisponibilidad(
                profesionalId, sillonId, nuevo.intervalo(), actual.id()));

        Turno resultado = Turno.reconstruir(actual.id(), actual.pacienteId(), profesionalId, sillonId,
                prestacionId, duracion, solicitud.inicio(), EstadoTurno.AGENDADO, actual.esSobreturno());
        turnos.guardar(resultado);
        detalles.registrar(new DetalleReprogramacion(actual.id(), anterior, nuevo, solicitud.usuario(), ahora));
        auditoria.registrar(new EventoAuditoria(actual.id(), AccionAuditoria.REPROGRAMADO,
                solicitud.usuario(), ahora));
        try {
            notificador.notificar(resultado, AccionAuditoria.REPROGRAMADO);
        } catch (RuntimeException fallo) {
            // RN-NO-03: un fallo del aviso no invalida la reprogramacion.
        }
        return resultado;
    }

    private static ReglaDeNegocioViolada noExiste(String recurso, String id) {
        return new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AG_08, "No existe " + recurso + " " + id);
    }

    private static ReglaDeNegocioViolada inactivo(String recurso) {
        return new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_AG_08, "El " + recurso + " esta inactivo");
    }
}
