package ar.edu.turnos.aplicacion.casouso;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dobles.Escenario;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.AutorizacionSobreturno;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.AutorizacionSobreturnoRepositorio;
import ar.edu.turnos.dominio.servicio.OcupacionPorTurnos;
import ar.edu.turnos.dominio.servicio.ResultadoDisponibilidad;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import ar.edu.turnos.dominio.servicio.SolicitudDisponibilidad;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AgendarSobreturnoTest {

    /** Reloj fijo: 2030-03-04 08:00, anterior a todas las franjas de los tests. */
    private static final Clock RELOJ = Clock.fixed(hora("08:00").toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final String DUENIO = "dra.ficticia.duenia";
    private static final String MOTIVO = "Urgencia por dolor agudo";

    private final Escenario e = Escenario.vacio();
    private final AutorizacionSobreturnoRepositorioEnMemoria autorizaciones =
            new AutorizacionSobreturnoRepositorioEnMemoria();
    private final AgendarSobreturno agendarSobreturno = nuevo(e, autorizaciones);

    private static AgendarSobreturno nuevo(Escenario e, AutorizacionSobreturnoRepositorio autorizaciones) {
        return new AgendarSobreturno(e.turnos, e.pacientes, e.profesionales, e.sillones, e.prestaciones,
                autorizaciones, e.auditoria, e.notificador, RELOJ);
    }

    private static SolicitudAgendarSobreturno pedido(String paciente, String prof, String sillon, String prestacion,
            String inicio, boolean esSobreturno, String autorizadoPor, RolAutorizador rol, String motivo) {
        return new SolicitudAgendarSobreturno(paciente, prof, sillon, prestacion, hora(inicio),
                esSobreturno, autorizadoPor, rol, motivo);
    }

    private static SolicitudAgendarSobreturno sobreturnoValido(String prof, String sillon, String inicio) {
        return pedido("PA1", prof, sillon, "LIMPIEZA", inicio, true, DUENIO,
                RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO);
    }

    private void existente(String id, String prof, String sillon, String desde, EstadoTurno estado) {
        e.conTurno(Turno.reconstruir(id, "PA2", prof, sillon, "LIMPIEZA", 30, hora(desde), estado, false));
    }

    private void assertRechazo(SolicitudAgendarSobreturno s, String codigo) {
        int turnosAntes = e.turnos.todos().size();
        int autorizacionesAntes = autorizaciones.todas().size();
        int auditoriaAntes = e.auditoria.todos().size();
        int avisosAntes = e.notificador.avisos.size();
        assertThatThrownBy(() -> agendarSobreturno.ejecutar(s))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class, ex -> assertThat(ex.codigo()).isEqualTo(codigo));
        assertThat(e.turnos.todos()).hasSize(turnosAntes);
        assertThat(autorizaciones.todas()).hasSize(autorizacionesAntes);
        assertThat(e.auditoria.todos()).hasSize(auditoriaAntes);
        assertThat(e.notificador.avisos).hasSize(avisosAntes);
    }

    // 1.3
    @Test
    void elPuntoDeEntradaSoloAceptaElInicioComoDatoTemporalYNoDependeDeDisponibilidad() {
        Method ejecutar = Arrays.stream(AgendarSobreturno.class.getMethods())
                .filter(m -> m.getName().equals("ejecutar")).findFirst().orElseThrow();
        assertThat(ejecutar.getParameterTypes()).containsExactly(SolicitudAgendarSobreturno.class);
        long temporales = Arrays.stream(SolicitudAgendarSobreturno.class.getRecordComponents())
                .filter(c -> c.getType().getName().startsWith("java.time")).count();
        assertThat(temporales).isEqualTo(1);
        assertThat(Arrays.stream(SolicitudAgendarSobreturno.class.getRecordComponents()).map(c -> c.getName()))
                .noneMatch(n -> n.toLowerCase().contains("fin") || n.toLowerCase().contains("duracion"));
        for (Constructor<?> ctor : AgendarSobreturno.class.getConstructors()) {
            assertThat(ctor.getParameterTypes()).doesNotContain(ServicioDisponibilidad.class);
        }
    }

    // 2.1 — Camino feliz
    @Test
    void caminoFelizSobreturnoConSolapamientoDeProfesional() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);

        Turno t = agendarSobreturno.ejecutar(sobreturnoValido("P1", "S2", "10:00"));

        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.intervalo()).isEqualTo(intervalo("10:00", "10:30"));
        assertThat(t.esSobreturno()).isTrue();
        assertThat(e.turnos.buscarPorId(t.id())).contains(t);
        assertThat(autorizaciones.buscarPorTurnoId(t.id())).isPresent().get().satisfies(aut -> {
            assertThat(aut.turnoId()).isEqualTo(t.id());
            assertThat(aut.autorizadoPor()).isEqualTo(DUENIO);
            assertThat(aut.motivo()).isEqualTo(MOTIVO);
            assertThat(aut.autorizadoEn()).isEqualTo(LocalDateTime.of(2030, 3, 4, 8, 0));
        });
        assertThat(e.auditoria.listarPorTurno(t.id())).singleElement().satisfies(ev -> {
            assertThat(ev.accion()).isEqualTo(AccionAuditoria.SOBRETURNO_AUTORIZADO);
            assertThat(ev.usuario()).isEqualTo(DUENIO);
            assertThat(ev.timestamp()).isEqualTo(LocalDateTime.of(2030, 3, 4, 8, 0));
        });
    }

    @Test
    void caminoFelizSobreturnoConSolapamientoDeSillon() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);

        Turno t = agendarSobreturno.ejecutar(sobreturnoValido("P2", "S1", "10:15"));

        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.intervalo()).isEqualTo(intervalo("10:15", "10:45"));
        assertThat(t.esSobreturno()).isTrue();
        assertThat(autorizaciones.buscarPorTurnoId(t.id())).isPresent();
        assertThat(e.auditoria.listarPorTurno(t.id()))
                .extracting(ev -> ev.accion())
                .containsExactly(AccionAuditoria.SOBRETURNO_AUTORIZADO);
    }

    @Test
    void caminoFelizSobreturnoConSolapamientoSimultaneoDeProfesionalYSillon() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);

        Turno t = agendarSobreturno.ejecutar(sobreturnoValido("P1", "S1", "10:15"));

        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.intervalo()).isEqualTo(intervalo("10:15", "10:45"));
        assertThat(t.esSobreturno()).isTrue();
        assertThat(autorizaciones.buscarPorTurnoId(t.id())).isPresent();
        assertThat(e.auditoria.listarPorTurno(t.id()))
                .extracting(ev -> ev.accion())
                .containsExactly(AccionAuditoria.SOBRETURNO_AUTORIZADO);
    }

    // 2.2 — Errores de negocio RN-AG-06 (marca, responsable, motivo, sin reintento)
    @Test
    void rechazaSinMarcaExplicitaDeSobreturno() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazo(
                pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15", false, DUENIO,
                        RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO),
                "RN-AG-06");
    }

    @Test
    void rechazaSinUsuarioResponsable() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazo(
                pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15", true, null,
                        RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO),
                "RN-AG-06");
        assertRechazo(
                pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15", true, "   ",
                        RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO),
                "RN-AG-06");
    }

    @Test
    void rechazaSinMotivoDeAutorizacion() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazo(
                pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15", true, DUENIO,
                        RolAutorizador.ODONTOLOGO_DUENIO, null),
                "RN-AG-06");
        assertRechazo(
                pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15", true, DUENIO,
                        RolAutorizador.ODONTOLOGO_DUENIO, "  "),
                "RN-AG-06");
    }

    @Test
    void nuncaHayReintentoAutomaticoTrasUnRechazo() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        SolicitudAgendarSobreturno invalida = pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15",
                true, DUENIO, RolAutorizador.ODONTOLOGO_DUENIO, "");
        assertRechazo(invalida, "RN-AG-06");
        assertThat(e.turnos.todos()).extracting(Turno::esSobreturno).containsOnly(false);
    }

    // 2.3 — Errores de negocio por rol (SU-04, PA-03)
    @Test
    void rechazaAutorizacionDeOdontologoColega() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazo(
                pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15", true, "dr.ficticio.colega",
                        RolAutorizador.ODONTOLOGO_COLEGA, MOTIVO),
                "RN-AG-06");
    }

    @Test
    void rechazaAutorizacionDeRecepcion() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazo(
                pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15", true, "recepcion.ficticia",
                        RolAutorizador.RECEPCION, MOTIVO),
                "RN-AG-06");
    }

    @Test
    void rechazaSolicitudSinRolDeAutorizacion() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazo(
                pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15", true, DUENIO, null, MOTIVO),
                "RN-AG-06");
    }

    // 3.1 — Reglas heredadas (RN-AG-08 y RN-AG-01) siguen aplicando aun con autorización
    @Test
    void rechazaProfesionalInactivoAunConAutorizacionDelDuenio() {
        e.conProfesional(new Profesional("P1", "Dra. Ficticia Uno", false));
        assertRechazo(sobreturnoValido("P1", "S1", "10:00"), "RN-AG-08");
    }

    @Test
    void rechazaSillonInactivoAunConAutorizacionDelDuenio() {
        e.conSillon(new Sillon("S1", "Sillon 1", false));
        assertRechazo(sobreturnoValido("P1", "S1", "10:00"), "RN-AG-08");
    }

    @Test
    void rechazaPrestacionInactivaAunConAutorizacionDelDuenio() {
        e.conPrestacion(new Prestacion("LIMPIEZA", "Limpieza", 30, false));
        assertRechazo(sobreturnoValido("P1", "S1", "10:00"), "RN-AG-08");
    }

    @Test
    void rechazaRecursoInexistenteAunConAutorizacionDelDuenio() {
        assertRechazo(pedido("NO-EXISTE", "P1", "S1", "LIMPIEZA", "10:00",
                true, DUENIO, RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO), "RN-AG-08");
        assertRechazo(pedido("PA1", "NO-EXISTE", "S1", "LIMPIEZA", "10:00",
                true, DUENIO, RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO), "RN-AG-08");
        assertRechazo(pedido("PA1", "P1", "NO-EXISTE", "LIMPIEZA", "10:00",
                true, DUENIO, RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO), "RN-AG-08");
        assertRechazo(pedido("PA1", "P1", "S1", "NO-EXISTE", "10:00",
                true, DUENIO, RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO), "RN-AG-08");
    }

    @Test
    void rechazaInicioEnElPasadoOExactamenteAhora() {
        assertRechazo(sobreturnoValido("P1", "S1", "07:00"), "RN-AG-08");
        assertRechazo(sobreturnoValido("P1", "S1", "08:00"), "RN-AG-08");
        assertRechazo(new SolicitudAgendarSobreturno("PA1", "P1", "S1", "LIMPIEZA", null,
                true, DUENIO, RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO), "RN-AG-08");
    }

    @Test
    void laDuracionSeDerivaDeLaPrestacionConductoDe90Minutos() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        Turno t = agendarSobreturno.ejecutar(pedido("PA1", "P1", "S1", "CONDUCTO", "10:00",
                true, DUENIO, RolAutorizador.ODONTOLOGO_DUENIO, MOTIVO));
        assertThat(t.intervalo()).isEqualTo(intervalo("10:00", "11:30"));
        assertThat(t.esSobreturno()).isTrue();
    }

    // 3.2 — Auditoría append-only, trazabilidad 1:1 y notificación resiliente
    @Test
    void elRegistroDeAuditoriaEsAppendOnlyYConservaTrazabilidadUnoAUno() {
        AgendarTurno agendarNormal = new AgendarTurno(e.turnos, e.pacientes, e.profesionales, e.sillones,
                e.prestaciones, new ServicioDisponibilidad(new OcupacionPorTurnos(e.turnos)),
                e.auditoria, e.notificador, RELOJ);
        Turno normal = agendarNormal.ejecutar(new SolicitudAgendarTurno(
                "PA2", "P1", "S1", "LIMPIEZA", hora("10:00"), "recepcion.ficticia"));
        EventoAuditoria eventoPrevio = e.auditoria.listarPorTurno(normal.id()).get(0);

        Turno sobreturno = agendarSobreturno.ejecutar(pedido("PA1", "P1", "S1", "LIMPIEZA", "10:15",
                true, "Dra. Ana Gomez", RolAutorizador.ODONTOLOGO_DUENIO, "Urgencia endodontica"));

        List<EventoAuditoria> todos = e.auditoria.todos();
        assertThat(todos).hasSize(2);
        assertThat(todos.get(0)).isEqualTo(eventoPrevio);
        assertThat(todos.get(1).turnoId()).isEqualTo(sobreturno.id());
        assertThat(todos.get(1).accion()).isEqualTo(AccionAuditoria.SOBRETURNO_AUTORIZADO);
        assertThat(todos.get(1).usuario()).isEqualTo("Dra. Ana Gomez");
        assertThat(autorizaciones.todas()).hasSize(1);
        assertThat(autorizaciones.buscarPorTurnoId(sobreturno.id())).isPresent().get().satisfies(aut -> {
            assertThat(aut.autorizadoPor()).isEqualTo("Dra. Ana Gomez");
            assertThat(aut.motivo()).isEqualTo("Urgencia endodontica");
        });
    }

    @Test
    void disparaLaNotificacionConAccionSobreturnoAutorizado() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        Turno t = agendarSobreturno.ejecutar(sobreturnoValido("P1", "S1", "10:15"));
        assertThat(e.notificador.avisos).containsExactly(t);
        assertThat(e.notificador.acciones).containsExactly(AccionAuditoria.SOBRETURNO_AUTORIZADO);
    }

    @Test
    void unaNotificacionQueFallaNoInvalidaElSobreturno() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        e.notificador.falla = true;

        Turno t = agendarSobreturno.ejecutar(sobreturnoValido("P1", "S1", "10:15"));

        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.esSobreturno()).isTrue();
        assertThat(e.turnos.buscarPorId(t.id())).isPresent();
        assertThat(autorizaciones.buscarPorTurnoId(t.id())).isPresent();
        assertThat(e.auditoria.listarPorTurno(t.id()))
                .extracting(EventoAuditoria::accion)
                .containsExactly(AccionAuditoria.SOBRETURNO_AUTORIZADO);
    }

    // 3.3 — Casos de borde
    @Test
    void casoDeBordeSobreturnoSobreFranjaLibre() {
        Turno t = agendarSobreturno.ejecutar(sobreturnoValido("P1", "S1", "11:00"));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.intervalo()).isEqualTo(intervalo("11:00", "11:30"));
        assertThat(t.esSobreturno()).isTrue();
        assertThat(autorizaciones.buscarPorTurnoId(t.id())).isPresent();
        assertThat(e.auditoria.listarPorTurno(t.id()))
                .extracting(EventoAuditoria::accion)
                .containsExactly(AccionAuditoria.SOBRETURNO_AUTORIZADO);
    }

    @Test
    void casoDeBordeSobreturnoSobreFranjaSoloContigua() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        Turno t = agendarSobreturno.ejecutar(sobreturnoValido("P1", "S1", "10:30"));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.intervalo()).isEqualTo(intervalo("10:30", "11:00"));
        assertThat(t.esSobreturno()).isTrue();
        assertThat(autorizaciones.buscarPorTurnoId(t.id())).isPresent();
        assertThat(e.auditoria.listarPorTurno(t.id()))
                .extracting(EventoAuditoria::accion)
                .containsExactly(AccionAuditoria.SOBRETURNO_AUTORIZADO);
    }

    @Test
    void casoDeBordeTrasUnSobreturnoElAltaNormalSobreFranjaOcupadaSigueRechazada() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        agendarSobreturno.ejecutar(sobreturnoValido("P1", "S1", "10:00"));

        ServicioDisponibilidad disponibilidad = new ServicioDisponibilidad(new OcupacionPorTurnos(e.turnos));
        AgendarTurno agendarNormal = new AgendarTurno(e.turnos, e.pacientes, e.profesionales, e.sillones,
                e.prestaciones, disponibilidad, e.auditoria, e.notificador, RELOJ);

        assertThatThrownBy(() -> agendarNormal.ejecutar(new SolicitudAgendarTurno(
                "PA2", "P1", "S2", "LIMPIEZA", hora("10:15"), "recepcion.ficticia")))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("RN-AG-02"));

        assertThatThrownBy(() -> agendarNormal.ejecutar(new SolicitudAgendarTurno(
                "PA2", "P2", "S1", "LIMPIEZA", hora("10:15"), "recepcion.ficticia")))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("RN-AG-03"));
    }

    @Test
    void laEvaluacionDeDisponibilidadSigueInformandoLaFranjaOcupadaPorSobreturno() {
        agendarSobreturno.ejecutar(sobreturnoValido("P1", "S1", "10:00"));
        ServicioDisponibilidad disponibilidad = new ServicioDisponibilidad(new OcupacionPorTurnos(e.turnos));

        ResultadoDisponibilidad porProfesional = disponibilidad.evaluar(
                new SolicitudDisponibilidad("P1", "S2", intervalo("10:15", "10:45"), null));
        ResultadoDisponibilidad porSillon = disponibilidad.evaluar(
                new SolicitudDisponibilidad("P2", "S1", intervalo("10:15", "10:45"), null));

        assertThat(porProfesional.disponible()).isFalse();
        assertThat(porProfesional.codigoRegla()).isEqualTo("RN-AG-02");
        assertThat(porSillon.disponible()).isFalse();
        assertThat(porSillon.codigoRegla()).isEqualTo("RN-AG-03");
    }

    /** Doble en memoria local al test para no modificar los helpers compartidos en dobles/. */
    private static final class AutorizacionSobreturnoRepositorioEnMemoria
            implements AutorizacionSobreturnoRepositorio {

        private final Map<String, AutorizacionSobreturno> datos = new LinkedHashMap<>();

        @Override
        public AutorizacionSobreturno guardar(AutorizacionSobreturno autorizacion) {
            datos.put(autorizacion.turnoId(), autorizacion);
            return autorizacion;
        }

        @Override
        public Optional<AutorizacionSobreturno> buscarPorTurnoId(String turnoId) {
            return Optional.ofNullable(datos.get(turnoId));
        }

        List<AutorizacionSobreturno> todas() {
            return List.copyOf(datos.values());
        }
    }
}
