package ar.edu.turnos.aplicacion.casouso;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dobles.Escenario;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.servicio.OcupacionPorTurnos;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgendarTurnoTest {

    /** Reloj fijo: 2030-03-04 08:00, anterior a todas las franjas de los tests. */
    private static final Clock RELOJ = Clock.fixed(hora("08:00").toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final String USUARIO = "recepcion.ficticia";

    private final Escenario e = Escenario.vacio();
    private final AgendarTurno agendar = nuevo(e);

    private static AgendarTurno nuevo(Escenario e) {
        return new AgendarTurno(e.turnos, e.pacientes, e.profesionales, e.sillones, e.prestaciones,
                new ServicioDisponibilidad(new OcupacionPorTurnos(e.turnos)), e.auditoria, e.notificador, RELOJ);
    }

    private static SolicitudAgendarTurno pedido(String paciente, String prof, String sillon, String prestacion,
            String inicio, String usuario) {
        return new SolicitudAgendarTurno(paciente, prof, sillon, prestacion, hora(inicio), usuario);
    }

    private static SolicitudAgendarTurno limpieza(String prof, String sillon, String inicio) {
        return pedido("PA1", prof, sillon, "LIMPIEZA", inicio, USUARIO);
    }

    private void existente(String id, String prof, String sillon, String desde, EstadoTurno estado) {
        e.conTurno(Turno.reconstruir(id, "PA2", prof, sillon, "LIMPIEZA", 30, hora(desde), estado, false));
    }

    private void assertRechazo(SolicitudAgendarTurno s, String codigo) {
        int turnosAntes = e.turnos.todos().size();
        assertThatThrownBy(() -> agendar.ejecutar(s))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class, ex -> assertThat(ex.codigo()).isEqualTo(codigo));
        assertThat(e.turnos.todos()).hasSize(turnosAntes);
        assertThat(e.auditoria.todos()).isEmpty();
        assertThat(e.notificador.avisos).isEmpty();
    }

    // 6.2
    @Test
    void caminoFelizLimpiezaDe30MinutosALas10() {
        Turno t = agendar.ejecutar(limpieza("P1", "S1", "10:00"));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.intervalo()).isEqualTo(intervalo("10:00", "10:30"));
        assertThat(t.esSobreturno()).isFalse();
        assertThat(e.turnos.buscarPorId(t.id())).contains(t);
        assertThat(e.auditoria.listarPorTurno(t.id())).singleElement().satisfies(ev -> {
            assertThat(ev.accion()).isEqualTo(AccionAuditoria.CREADO);
            assertThat(ev.usuario()).isEqualTo(USUARIO);
            assertThat(ev.timestamp()).isEqualTo(LocalDateTime.of(2030, 3, 4, 8, 0));
        });
    }

    @Test
    void laPrestacionDe90MinutosOcupaHastaLas1130() {
        Turno t = agendar.ejecutar(pedido("PA1", "P1", "S1", "CONDUCTO", "10:00", USUARIO));
        assertThat(t.intervalo()).isEqualTo(intervalo("10:00", "11:30"));
    }

    @Test
    void elPuntoDeEntradaSoloAceptaElInicioComoDatoTemporal() {
        Method ejecutar = Arrays.stream(AgendarTurno.class.getMethods())
                .filter(m -> m.getName().equals("ejecutar")).findFirst().orElseThrow();
        assertThat(ejecutar.getParameterTypes()).containsExactly(SolicitudAgendarTurno.class);
        long temporales = Arrays.stream(SolicitudAgendarTurno.class.getRecordComponents())
                .filter(c -> c.getType().getName().startsWith("java.time")).count();
        assertThat(temporales).isEqualTo(1);
        assertThat(Arrays.stream(SolicitudAgendarTurno.class.getRecordComponents()).map(c -> c.getName()))
                .noneMatch(n -> n.toLowerCase().contains("fin") || n.toLowerCase().contains("duracion"));
    }

    // 6.3
    @Test
    void rechazaProfesionalInactivo() {
        e.conProfesional(new Profesional("P1", "Dra. Ficticia Uno", false));
        assertRechazo(limpieza("P1", "S1", "10:00"), "RN-AG-08");
    }

    @Test
    void rechazaSillonInactivo() {
        e.conSillon(new Sillon("S1", "Sillon 1", false));
        assertRechazo(limpieza("P1", "S1", "10:00"), "RN-AG-08");
    }

    @Test
    void rechazaPrestacionInactiva() {
        e.conPrestacion(new Prestacion("LIMPIEZA", "Limpieza", 30, false));
        assertRechazo(limpieza("P1", "S1", "10:00"), "RN-AG-08");
    }

    @Test
    void rechazaRecursoInexistente() {
        assertRechazo(pedido("NO-EXISTE", "P1", "S1", "LIMPIEZA", "10:00", USUARIO), "RN-AG-08");
        assertRechazo(pedido("PA1", "NO-EXISTE", "S1", "LIMPIEZA", "10:00", USUARIO), "RN-AG-08");
        assertRechazo(pedido("PA1", "P1", "NO-EXISTE", "LIMPIEZA", "10:00", USUARIO), "RN-AG-08");
        assertRechazo(pedido("PA1", "P1", "S1", "NO-EXISTE", "10:00", USUARIO), "RN-AG-08");
    }

    @Test
    void laValidacionDeRecursosOcurreAntesDeConsultarDisponibilidad() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        e.conProfesional(new Profesional("P1", "Dra. Ficticia Uno", false));
        // el profesional esta inactivo Y ocupado: debe ganar RN-AG-08, no RN-AG-02
        assertThatThrownBy(() -> agendar.ejecutar(limpieza("P1", "S1", "10:15")))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class, ex -> assertThat(ex.codigo()).isEqualTo("RN-AG-08"));
    }

    // 6.4
    @Test
    void rechazaInicioEnElPasado() {
        assertRechazo(limpieza("P1", "S1", "07:00"), "RN-AG-08");
    }

    @Test
    void rechazaInicioExactamenteAhora() {
        assertRechazo(limpieza("P1", "S1", "08:00"), "RN-AG-08");
    }

    @Test
    void aceptaUnInicioUnMinutoDespuesDeAhora() {
        Turno t = agendar.ejecutar(new SolicitudAgendarTurno("PA1", "P1", "S1", "LIMPIEZA",
                hora("08:00").plusMinutes(1), USUARIO));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
    }

    // 6.5
    @Test
    void rechazaSolapamientoDeProfesional() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazo(limpieza("P1", "S2", "10:15"), "RN-AG-02");
    }

    @Test
    void rechazaSolapamientoDeSillon() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazo(limpieza("P2", "S1", "10:15"), "RN-AG-03");
    }

    // 6.6
    @Test
    void aceptaTurnoContiguoDelMismoProfesionalYSillon() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        Turno t = agendar.ejecutar(limpieza("P1", "S1", "10:30"));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.intervalo()).isEqualTo(intervalo("10:30", "11:00"));
    }

    @Test
    void aceptaTurnoInmediatamenteAnterior() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        Turno t = agendar.ejecutar(limpieza("P1", "S1", "09:30"));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.intervalo()).isEqualTo(intervalo("09:30", "10:00"));
    }

    @Test
    void aceptaElTurnoContiguoSobreElSeedDeLas1030() {
        Escenario conSeed = Escenario.conSeed();
        // el seed ya ocupa [10:00,10:30) y [10:30,11:00): [11:00,11:30) es contiguo del segundo
        Turno t = nuevo(conSeed).ejecutar(limpieza("P1", "S1", "11:00"));
        assertThat(t.intervalo()).isEqualTo(intervalo("11:00", "11:30"));
    }

    // 6.7
    @Test
    void aceptaAgendarSobreUnaFranjaLiberadaPorCancelacion() {
        existente("T1", "P1", "S1", "10:00", EstadoTurno.CANCELADO);
        Turno t = agendar.ejecutar(limpieza("P1", "S1", "10:00"));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
    }

    // 6.8
    @Test
    void rechazaAltaSinUsuarioResponsable() {
        assertRechazo(pedido("PA1", "P1", "S1", "LIMPIEZA", "10:00", null), "RN-AU-01");
        assertRechazo(pedido("PA1", "P1", "S1", "LIMPIEZA", "10:00", "  "), "RN-AU-01");
    }

    @Test
    void elRegistroDeAuditoriaEsAppendOnly() {
        Turno primero = agendar.ejecutar(limpieza("P1", "S1", "10:00"));
        EventoAuditoria eventoPrimero = e.auditoria.listarPorTurno(primero.id()).get(0);
        Turno segundo = agendar.ejecutar(limpieza("P2", "S2", "11:00"));
        List<EventoAuditoria> todos = e.auditoria.todos();
        assertThat(todos).hasSize(2);
        assertThat(todos.get(0)).isEqualTo(eventoPrimero);
        assertThat(todos.get(1).turnoId()).isEqualTo(segundo.id());
    }

    // 6.9
    @Test
    void disparaLaNotificacionTrasCrearElTurno() {
        Turno t = agendar.ejecutar(limpieza("P1", "S1", "10:00"));
        assertThat(e.notificador.avisos).containsExactly(t);
    }

    @Test
    void unaNotificacionQueFallaNoInvalidaElAlta() {
        e.notificador.falla = true;
        Turno t = agendar.ejecutar(limpieza("P1", "S1", "10:00"));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(e.turnos.buscarPorId(t.id())).isPresent();
        assertThat(e.auditoria.listarPorTurno(t.id())).extracting(EventoAuditoria::accion)
                .containsExactly(AccionAuditoria.CREADO);
    }
}
