package ar.edu.turnos.aplicacion.casouso;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dobles.Escenario;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.servicio.OcupacionPorTurnos;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CancelarTurnoTest {

    // Reloj fijado en 2030-03-04 10:00 UTC
    private static final LocalDateTime AHORA = LocalDateTime.of(2030, 3, 4, 10, 0);
    private static final Clock RELOJ = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final String USUARIO = "recepcion.ficticia";
    private static final String MOTIVO_VALIDO = "Viaje imprevisto del paciente";

    private final Escenario e = Escenario.vacio();
    // Caso de uso con anticipacion minima por defecto de 24 horas
    private final CancelarTurno casoUso = new CancelarTurno(e.turnos, e.auditoria, e.notificador, RELOJ);

    private Turno conTurno(String id, LocalDateTime inicio, EstadoTurno estado) {
        Turno t = Turno.reconstruir(id, "PA1", "P1", "S1", "LIMPIEZA", 30, inicio, estado, false);
        return e.conTurno(t).turnos.buscarPorId(id).orElseThrow();
    }

    private static SolicitudCancelarTurno pedido(String turnoId, String motivo, String usuario) {
        return new SolicitudCancelarTurno(turnoId, motivo, usuario);
    }

    private void assertRechazo(String turnoId, String motivo, String usuario, String codigo, EstadoTurno estadoPrevio) {
        assertThatThrownBy(() -> casoUso.ejecutar(pedido(turnoId, motivo, usuario)))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo(codigo));
        assertThat(e.turnos.buscarPorId(turnoId)).get()
                .satisfies(t -> assertThat(t.estado()).isEqualTo(estadoPrevio));
        assertThat(e.auditoria.todos()).isEmpty();
        assertThat(e.notificador.avisos).isEmpty();
    }

    @Test
    void cancelaTurnoAgendadoConAnticipacionSuficienteYMotivoValido() {
        // Inicio a 48 horas de AHORA (bien por encima de las 24 horas minimas)
        LocalDateTime inicio = AHORA.plusHours(48);
        Turno original = conTurno("T1", inicio, EstadoTurno.AGENDADO);

        Turno cancelado = casoUso.ejecutar(pedido("T1", "  " + MOTIVO_VALIDO + "  ", USUARIO));

        assertThat(cancelado.estado()).isEqualTo(EstadoTurno.CANCELADO);
        assertThat(cancelado.motivoCancelacion()).isEqualTo(MOTIVO_VALIDO);
        assertThat(original.estado()).isEqualTo(EstadoTurno.AGENDADO); // inmutabilidad
        assertThat(e.turnos.buscarPorId("T1")).contains(cancelado);

        // Auditoria registrada
        assertThat(e.auditoria.listarPorTurno("T1")).singleElement().satisfies(ev -> {
            assertThat(ev.accion()).isEqualTo(AccionAuditoria.CANCELADO);
            assertThat(ev.usuario()).isEqualTo(USUARIO);
            assertThat(ev.timestamp()).isEqualTo(AHORA);
        });

        // Notificacion al paciente emitida
        assertThat(e.notificador.avisos).containsExactly(cancelado);
        assertThat(e.notificador.acciones).containsExactly(AccionAuditoria.CANCELADO);
    }

    @Test
    void aceptaCancelacionEnElBordeConExactamenteNHorasDeAnticipacion() {
        // Exactamente 24 horas despues de AHORA: debe aceptarse por ser limite inclusivo (D-02)
        LocalDateTime inicio = AHORA.plusHours(24);
        conTurno("T1", inicio, EstadoTurno.AGENDADO);

        Turno cancelado = casoUso.ejecutar(pedido("T1", MOTIVO_VALIDO, USUARIO));

        assertThat(cancelado.estado()).isEqualTo(EstadoTurno.CANCELADO);
        assertThat(cancelado.motivoCancelacion()).isEqualTo(MOTIVO_VALIDO);
    }

    @Test
    void rechazaCancelacionConMenosDeNHorasDeAnticipacion() {
        // A 23 horas y 59 minutos de AHORA (menos de 24 horas)
        LocalDateTime inicio = AHORA.plusHours(24).minusMinutes(1);
        conTurno("T1", inicio, EstadoTurno.AGENDADO);

        assertRechazo("T1", MOTIVO_VALIDO, USUARIO, CancelarTurno.RN_AG_05, EstadoTurno.AGENDADO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", " \t "})
    void rechazaCancelacionSinMotivoOConMotivoEnBlanco(String motivoInvalido) {
        LocalDateTime inicio = AHORA.plusHours(48);
        conTurno("T1", inicio, EstadoTurno.AGENDADO);

        assertRechazo("T1", motivoInvalido, USUARIO, CancelarTurno.RN_TU_05, EstadoTurno.AGENDADO);
    }

    @Test
    void rechazaTurnoInexistenteSinAuditar() {
        assertThatThrownBy(() -> casoUso.ejecutar(pedido("INEXISTENTE", MOTIVO_VALIDO, USUARIO)))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("RN-TU-02"));
        assertThat(e.turnos.todos()).isEmpty();
        assertThat(e.auditoria.todos()).isEmpty();
        assertThat(e.notificador.avisos).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoTurno.class, names = {"ATENDIDO", "CANCELADO", "AUSENTE"})
    void rechazaEstadoTerminalSinAuditar(EstadoTurno estadoTerminal) {
        LocalDateTime inicio = AHORA.plusHours(48);
        conTurno("T1", inicio, estadoTerminal);

        assertRechazo("T1", MOTIVO_VALIDO, USUARIO, "RN-TU-02", estadoTerminal);
    }

    @Test
    void rechazaSinUsuarioResponsable() {
        LocalDateTime inicio = AHORA.plusHours(48);
        conTurno("T1", inicio, EstadoTurno.AGENDADO);

        assertRechazo("T1", MOTIVO_VALIDO, null, "RN-AU-01", EstadoTurno.AGENDADO);
        assertRechazo("T1", MOTIVO_VALIDO, "   ", "RN-AU-01", EstadoTurno.AGENDADO);
    }

    @Test
    void toleraFallosDelNotificadorDePacientes() {
        LocalDateTime inicio = AHORA.plusHours(48);
        conTurno("T1", inicio, EstadoTurno.AGENDADO);
        e.notificador.falla = true; // el proveedor de mensajeria falla

        Turno cancelado = casoUso.ejecutar(pedido("T1", MOTIVO_VALIDO, USUARIO));

        assertThat(cancelado.estado()).isEqualTo(EstadoTurno.CANCELADO);
        assertThat(e.turnos.buscarPorId("T1")).contains(cancelado);
        assertThat(e.auditoria.listarPorTurno("T1")).singleElement().satisfies(ev -> {
            assertThat(ev.accion()).isEqualTo(AccionAuditoria.CANCELADO);
        });
    }

    @Test
    void permiteConfigurarUnaAnticipacionDistintaDeLas24Horas() {
        // Configuramos 12 horas en lugar de 24
        CancelarTurno casoUso12hs = new CancelarTurno(e.turnos, e.auditoria, e.notificador, RELOJ, 12);

        // Turno a 15 horas de anticipacion: para 24hs fallaria, pero con 12hs se acepta
        LocalDateTime inicio = AHORA.plusHours(15);
        conTurno("T1", inicio, EstadoTurno.AGENDADO);

        Turno cancelado = casoUso12hs.ejecutar(pedido("T1", MOTIVO_VALIDO, USUARIO));
        assertThat(cancelado.estado()).isEqualTo(EstadoTurno.CANCELADO);
    }

    @Test
    void despuesDeCancelarTurnoSePuedeAgendarSobreLaMismaFranja() {
        LocalDateTime inicio = AHORA.plusHours(48);
        conTurno("T1", inicio, EstadoTurno.AGENDADO);

        casoUso.ejecutar(pedido("T1", MOTIVO_VALIDO, USUARIO));

        AgendarTurno agendar = new AgendarTurno(e.turnos, e.pacientes, e.profesionales, e.sillones, e.prestaciones,
                new ServicioDisponibilidad(new OcupacionPorTurnos(e.turnos)), e.auditoria, e.notificador, RELOJ);

        Turno nuevo = agendar.ejecutar(new SolicitudAgendarTurno("PA1", "P1", "S1", "LIMPIEZA", inicio, USUARIO));

        assertThat(e.turnos.buscarPorId("T1")).get().satisfies(t -> assertThat(t.estado()).isEqualTo(EstadoTurno.CANCELADO));
        assertThat(nuevo.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(nuevo.intervalo().inicio()).isEqualTo(inicio);
    }
}
