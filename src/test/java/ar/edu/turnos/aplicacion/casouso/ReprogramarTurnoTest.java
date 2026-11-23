package ar.edu.turnos.aplicacion.casouso;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dobles.Escenario;
import ar.edu.turnos.dobles.RegistroDetalleReprogramacionEnMemoria;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Intervalo;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.RegistroDetalleReprogramacion;
import ar.edu.turnos.dominio.servicio.OcupacionPorTurnos;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import ar.edu.turnos.dominio.servicio.SolicitudDisponibilidad;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ReprogramarTurnoTest {

    private static final Clock RELOJ = Clock.fixed(hora("08:00").toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final Clock RELOJ_TARDE = Clock.fixed(hora("08:30").toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final String USUARIO = "recepcion.ficticia";
    private static final String ID = "T-1";

    private final Escenario e = Escenario.vacio();
    private final RegistroDetalleReprogramacionEnMemoria detalles = new RegistroDetalleReprogramacionEnMemoria();
    private final ReprogramarTurno reprogramar = nuevo(e, detalles, RELOJ);

    private static ReprogramarTurno nuevo(Escenario e, RegistroDetalleReprogramacion registro, Clock reloj) {
        return new ReprogramarTurno(e.turnos, e.profesionales, e.sillones, e.prestaciones,
                new ServicioDisponibilidad(new OcupacionPorTurnos(e.turnos)),
                e.auditoria, registro, e.notificador, reloj);
    }

    private static SolicitudReprogramarTurno pedido(String turnoId, String inicio, String profesional,
            String sillon, String prestacion, String usuario) {
        return new SolicitudReprogramarTurno(turnoId, hora(inicio), profesional, sillon, prestacion, usuario);
    }

    private static SolicitudReprogramarTurno mover(String inicio) {
        return pedido(ID, inicio, null, null, null, USUARIO);
    }

    private static SolicitudReprogramarTurno mismosValores() {
        return pedido(ID, "10:00", "P1", "S1", "LIMPIEZA", USUARIO);
    }

    private Turno turno(String id, String profesional, String sillon, String prestacion, int duracion,
            String inicio, EstadoTurno estado) {
        Turno t = Turno.reconstruir(id, "PA1", profesional, sillon, prestacion, duracion, hora(inicio), estado, false);
        e.turnos.guardar(t);
        return t;
    }

    private Turno agendado() {
        return turno(ID, "P1", "S1", "LIMPIEZA", 30, "10:00", EstadoTurno.AGENDADO);
    }

    private void assertRechazo(SolicitudReprogramarTurno solicitud, String codigo) {
        Turno antes = e.turnos.buscarPorId(ID).orElse(null);
        assertThatThrownBy(() -> reprogramar.ejecutar(solicitud))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class, ex -> assertThat(ex.codigo()).isEqualTo(codigo));
        if (antes == null) {
            assertThat(e.turnos.buscarPorId(ID)).isEmpty();
        } else {
            assertThat(e.turnos.buscarPorId(ID)).contains(antes);
        }
        assertThat(e.auditoria.todos()).isEmpty();
        assertThat(detalles.todos()).isEmpty();
        assertThat(e.notificador.avisos).isEmpty();
    }

    private void assertCambioRegistrado(Turno resultado) {
        assertThat(e.auditoria.listarPorTurno(ID)).extracting(EventoAuditoria::accion)
                .containsExactly(AccionAuditoria.REPROGRAMADO);
        assertThat(detalles.listarPorTurno(ID)).hasSize(1);
        assertThat(e.notificador.acciones).containsExactly(AccionAuditoria.REPROGRAMADO);
        assertThat(e.notificador.avisos).containsExactly(resultado);
    }

    // Solo se reprograma un turno agendado

    @Test
    void rechazaUnTurnoCancelado() {
        turno(ID, "P1", "S1", "LIMPIEZA", 30, "10:00", EstadoTurno.CANCELADO);
        assertRechazo(mover("11:00"), ReprogramarTurno.RN_TU_04);
        assertThat(e.turnos.buscarPorId(ID).orElseThrow().estado()).isEqualTo(EstadoTurno.CANCELADO);
    }

    @Test
    void rechazaUnTurnoAtendido() {
        turno(ID, "P1", "S1", "LIMPIEZA", 30, "10:00", EstadoTurno.ATENDIDO);
        assertRechazo(mover("11:00"), ReprogramarTurno.RN_TU_04);
    }

    @Test
    void rechazaUnTurnoAusente() {
        turno(ID, "P1", "S1", "LIMPIEZA", 30, "10:00", EstadoTurno.AUSENTE);
        assertRechazo(mover("11:00"), ReprogramarTurno.RN_TU_04);
    }

    @Test
    void aceptaUnTurnoAgendado() {
        agendado();
        Turno resultado = reprogramar.ejecutar(mover("11:00"));
        assertThat(resultado.estado()).isEqualTo(EstadoTurno.AGENDADO);
    }

    // La duracion se recalcula desde la prestacion resultante

    @Test
    void moverElTurnoConservaSus30Minutos() {
        agendado();
        Turno resultado = reprogramar.ejecutar(mover("11:00"));
        assertThat(resultado.id()).isEqualTo(ID);
        assertThat(resultado.intervalo()).isEqualTo(intervalo("11:00", "11:30"));
    }

    @Test
    void cambiarLaPrestacionRecalculaElFin() {
        agendado();
        Turno resultado = reprogramar.ejecutar(pedido(ID, "10:00", null, null, "CONDUCTO", USUARIO));
        assertThat(resultado.prestacionId()).isEqualTo("CONDUCTO");
        assertThat(resultado.intervalo()).isEqualTo(intervalo("10:00", "11:30"));
    }

    @Test
    void elCambioDelCatalogoNoAlteraLaDuracionConservada() {
        agendado();
        e.conPrestacion(new Prestacion("LIMPIEZA", "Limpieza", 45, true));
        Turno resultado = reprogramar.ejecutar(mover("11:00"));
        assertThat(resultado.duracionMinutos()).isEqualTo(30);
        assertThat(resultado.intervalo()).isEqualTo(intervalo("11:00", "11:30"));
    }

    @Test
    void elPuntoDeEntradaSoloAceptaElInicioComoDatoTemporal() {
        Method ejecutar = Arrays.stream(ReprogramarTurno.class.getMethods())
                .filter(m -> m.getName().equals("ejecutar")).findFirst().orElseThrow();
        assertThat(ejecutar.getParameterTypes()).containsExactly(SolicitudReprogramarTurno.class);
        long temporales = Arrays.stream(SolicitudReprogramarTurno.class.getRecordComponents())
                .filter(c -> c.getType().getName().startsWith("java.time")).count();
        assertThat(temporales).isEqualTo(1);
        assertThat(Arrays.stream(SolicitudReprogramarTurno.class.getRecordComponents()).map(c -> c.getName()))
                .noneMatch(n -> n.toLowerCase().contains("fin") || n.toLowerCase().contains("duracion"));
    }

    // La disponibilidad se revalida excluyendo el propio turno

    @Test
    void rechazaElSolapamientoConElProfesionalDeOtroTurno() {
        agendado();
        turno("T-2", "P1", "S2", "LIMPIEZA", 30, "10:00", EstadoTurno.AGENDADO);
        assertRechazo(mover("10:15"), "RN-AG-02");
    }

    @Test
    void rechazaElSolapamientoConElSillonDeOtroTurno() {
        agendado();
        turno("T-2", "P2", "S1", "LIMPIEZA", 30, "10:00", EstadoTurno.AGENDADO);
        assertRechazo(mover("10:15"), "RN-AG-03");
    }

    @Test
    void aceptaUnBordeContiguo() {
        turno(ID, "P1", "S1", "LIMPIEZA", 30, "09:00", EstadoTurno.AGENDADO);
        turno("T-2", "P1", "S1", "LIMPIEZA", 30, "10:00", EstadoTurno.AGENDADO);
        Turno resultado = reprogramar.ejecutar(mover("10:30"));
        assertThat(resultado.intervalo()).isEqualTo(intervalo("10:30", "11:00"));
    }

    @Test
    void aceptaElMismoHorarioConOtroSillonLibre() {
        agendado();
        Turno resultado = reprogramar.ejecutar(pedido(ID, "10:00", null, "S2", null, USUARIO));
        assertThat(resultado.id()).isEqualTo(ID);
        assertThat(resultado.sillonId()).isEqualTo("S2");
    }

    @Test
    void laFranjaAnteriorQuedaLibre() {
        agendado();
        reprogramar.ejecutar(mover("11:00"));
        ServicioDisponibilidad disponibilidad = new ServicioDisponibilidad(new OcupacionPorTurnos(e.turnos));
        var resultado = disponibilidad.evaluar(new SolicitudDisponibilidad(
                "P1", "S1", intervalo("10:00", "10:30"), null));
        assertThat(resultado.disponible()).isTrue();
    }

    // La no-op no produce cambios ni registros

    @Test
    void reprogramarConLosValoresExactosQueYaTeníaNoProduceEfectos() {
        Turno actual = agendado();
        Turno resultado = reprogramar.ejecutar(mismosValores());
        assertThat(resultado).isEqualTo(actual);
        assertThat(e.turnos.buscarPorId(ID)).contains(actual);
        assertThat(e.auditoria.todos()).isEmpty();
        assertThat(detalles.todos()).isEmpty();
        assertThat(e.notificador.avisos).isEmpty();
    }

    @Test
    void cambiarSoloElSillonConElMismoHorarioRegistraElCambio() {
        agendado();
        Turno resultado = reprogramar.ejecutar(pedido(ID, "10:00", null, "S2", null, USUARIO));
        assertCambioRegistrado(resultado);
        assertThat(detalles.listarPorTurno(ID).get(0).anterior().sillonId()).isEqualTo("S1");
        assertThat(detalles.listarPorTurno(ID).get(0).nuevo().sillonId()).isEqualTo("S2");
    }

    @Test
    void cambiarSoloElProfesionalConElMismoHorarioRegistraElCambio() {
        agendado();
        Turno resultado = reprogramar.ejecutar(pedido(ID, "10:00", "P2", null, null, USUARIO));
        assertCambioRegistrado(resultado);
        assertThat(detalles.listarPorTurno(ID).get(0).anterior().profesionalId()).isEqualTo("P1");
        assertThat(detalles.listarPorTurno(ID).get(0).nuevo().profesionalId()).isEqualTo("P2");
    }

    @Test
    void cambiarSoloLaPrestacionConIgualDuracionRegistraElCambio() {
        agendado();
        e.conPrestacion(new Prestacion("OTRA", "Otra", 30, true));
        Turno resultado = reprogramar.ejecutar(pedido(ID, "10:00", null, null, "OTRA", USUARIO));
        assertCambioRegistrado(resultado);
        assertThat(detalles.listarPorTurno(ID).get(0).anterior().prestacionId()).isEqualTo("LIMPIEZA");
        assertThat(detalles.listarPorTurno(ID).get(0).nuevo().prestacionId()).isEqualTo("OTRA");
        assertThat(resultado.intervalo()).isEqualTo(intervalo("10:00", "10:30"));
    }

    // Auditoria y notificacion

    @Test
    void rechazaLaReprogramacionSinUsuarioResponsable() {
        agendado();
        assertRechazo(pedido(ID, "11:00", null, null, null, null), "RN-AU-01");
        assertRechazo(pedido(ID, "11:00", null, null, null, "  "), "RN-AU-01");
    }

    @Test
    void elEventoDeAuditoriaLlevaUsuarioYTimestamp() {
        agendado();
        reprogramar.ejecutar(mover("11:00"));
        assertThat(e.auditoria.listarPorTurno(ID)).singleElement().satisfies(ev -> {
            assertThat(ev.accion()).isEqualTo(AccionAuditoria.REPROGRAMADO);
            assertThat(ev.usuario()).isEqualTo(USUARIO);
            assertThat(ev.timestamp()).isEqualTo(hora("08:00"));
        });
    }

    @Test
    void unaNotificacionQueFallaNoInvalidaLaReprogramacion() {
        agendado();
        e.notificador.falla = true;
        Turno resultado = reprogramar.ejecutar(mover("11:00"));
        assertThat(e.turnos.buscarPorId(ID)).contains(resultado);
        assertThat(e.auditoria.listarPorTurno(ID)).extracting(EventoAuditoria::accion)
                .containsExactly(AccionAuditoria.REPROGRAMADO);
        assertThat(detalles.listarPorTurno(ID)).hasSize(1);
    }

    // El detalle queda disponible por separado

    @Test
    void elDetalleConservaElHorarioAnteriorYElNuevo() {
        agendado();
        reprogramar.ejecutar(mover("11:00"));
        var detalle = detalles.listarPorTurno(ID).get(0);
        assertThat(detalle.anterior().inicio()).isEqualTo(hora("10:00"));
        assertThat(detalle.anterior().fin()).isEqualTo(hora("10:30"));
        assertThat(detalle.nuevo().inicio()).isEqualTo(hora("11:00"));
        assertThat(detalle.nuevo().fin()).isEqualTo(hora("11:30"));
    }

    @Test
    void elDetalleConservaLosRecursosAnterioresYNuevos() {
        agendado();
        reprogramar.ejecutar(pedido(ID, "10:00", "P2", "S2", null, USUARIO));
        var detalle = detalles.listarPorTurno(ID).get(0);
        assertThat(detalle.anterior().profesionalId()).isEqualTo("P1");
        assertThat(detalle.anterior().sillonId()).isEqualTo("S1");
        assertThat(detalle.nuevo().profesionalId()).isEqualTo("P2");
        assertThat(detalle.nuevo().sillonId()).isEqualTo("S2");
    }

    @Test
    void sinCambioDeRecursosElDetalleLosRepite() {
        agendado();
        reprogramar.ejecutar(mover("11:00"));
        var detalle = detalles.listarPorTurno(ID).get(0);
        assertThat(detalle.anterior().profesionalId()).isEqualTo(detalle.nuevo().profesionalId());
        assertThat(detalle.anterior().sillonId()).isEqualTo(detalle.nuevo().sillonId());
        assertThat(detalle.anterior().prestacionId()).isEqualTo(detalle.nuevo().prestacionId());
    }

    @Test
    void eventoYDetalleSePuedenCorrelacionarPorTurnoUsuarioYTimestamp() {
        agendado();
        reprogramar.ejecutar(mover("11:00"));
        EventoAuditoria evento = e.auditoria.listarPorTurno(ID).get(0);
        var detalle = detalles.listarPorTurno(ID).get(0);
        assertThat(detalle.turnoId()).isEqualTo(evento.turnoId());
        assertThat(detalle.usuario()).isEqualTo(evento.usuario());
        assertThat(detalle.timestamp()).isEqualTo(evento.timestamp());
    }

    @Test
    void variasReprogramacionesConservanCadaDetalle() {
        agendado();
        reprogramar.ejecutar(mover("11:00"));
        ReprogramarTurno masTarde = nuevo(e, detalles, RELOJ_TARDE);

        masTarde.ejecutar(mover("12:00"));

        assertThat(detalles.listarPorTurno(ID)).hasSize(2);
        assertThat(detalles.listarPorTurno(ID).get(0).timestamp()).isEqualTo(hora("08:00"));
        assertThat(detalles.listarPorTurno(ID).get(1).timestamp()).isEqualTo(hora("08:30"));
        assertThat(e.auditoria.listarPorTurno(ID)).hasSize(2);
    }

    // Los recursos resultantes deben ser validos y el inicio futuro

    @Test
    void rechazaUnProfesionalResultanteInactivo() {
        agendado();
        e.conProfesional(new Profesional("P2", "Dr. Ficticio Dos", false));
        assertRechazo(pedido(ID, "11:00", "P2", null, null, USUARIO), "RN-AG-08");
    }

    @Test
    void rechazaUnSillonResultanteInactivo() {
        agendado();
        e.conSillon(new Sillon("S2", "Sillon 2", false));
        assertRechazo(pedido(ID, "11:00", null, "S2", null, USUARIO), "RN-AG-08");
    }

    @Test
    void rechazaUnaPrestacionResultanteInactiva() {
        agendado();
        e.conPrestacion(new Prestacion("CONDUCTO", "Conducto", 90, false));
        assertRechazo(pedido(ID, "10:00", null, null, "CONDUCTO", USUARIO), "RN-AG-08");
    }

    @Test
    void rechazaUnRecursoResultanteInexistente() {
        agendado();
        assertRechazo(pedido(ID, "11:00", "NO-EXISTE", null, null, USUARIO), "RN-AG-08");
    }

    @Test
    void rechazaUnInicioEnElPasado() {
        agendado();
        assertRechazo(mover("07:00"), "RN-AG-08");
    }

    @Test
    void rechazaUnInicioIgualAlInstanteActual() {
        agendado();
        assertRechazo(mover("08:00"), "RN-AG-08");
    }

    @Test
    void aceptaUnInicioFuturoConRecursosValidos() {
        agendado();
        Turno resultado = reprogramar.ejecutar(mover("08:01"));
        assertThat(resultado.intervalo()).isEqualTo(
                new Intervalo(hora("08:01"), hora("08:01").plusMinutes(30)));
    }
}
