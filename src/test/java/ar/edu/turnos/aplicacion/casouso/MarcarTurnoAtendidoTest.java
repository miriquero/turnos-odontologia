package ar.edu.turnos.aplicacion.casouso;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dobles.Escenario;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.Turno;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MarcarTurnoAtendidoTest {

    private static final Clock RELOJ = Clock.fixed(hora("10:00").toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final String USUARIO = "recepcion.ficticia";

    private final Escenario e = Escenario.vacio();
    private final MarcarTurnoAtendido marcar = new MarcarTurnoAtendido(e.turnos, e.auditoria, RELOJ);

    private Turno conTurno(String id, String inicio, EstadoTurno estado) {
        Turno t = Turno.reconstruir(id, "PA1", "P1", "S1", "LIMPIEZA", 30, hora(inicio), estado, false);
        return e.conTurno(t).turnos.buscarPorId(id).orElseThrow();
    }

    private static SolicitudMarcarAtendido pedido(String turnoId, String usuario) {
        return new SolicitudMarcarAtendido(turnoId, usuario);
    }

    private void assertRechazo(String turnoId, String usuario, String codigo, EstadoTurno estadoPrevio) {
        assertThatThrownBy(() -> marcar.ejecutar(pedido(turnoId, usuario)))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo(codigo));
        assertThat(e.turnos.buscarPorId(turnoId)).get()
                .satisfies(t -> assertThat(t.estado()).isEqualTo(estadoPrevio));
        assertThat(e.auditoria.todos()).isEmpty();
        assertThat(e.notificador.avisos).isEmpty();
    }

    @Test
    void cierraUnTurnoAgendado() {
        Turno original = conTurno("T1", "09:30", EstadoTurno.AGENDADO);
        Turno cerrado = marcar.ejecutar(pedido("T1", USUARIO));
        assertThat(cerrado.estado()).isEqualTo(EstadoTurno.ATENDIDO);
        assertThat(cerrado.motivoAusencia()).isNull();
        assertThat(e.turnos.buscarPorId("T1")).contains(cerrado);
        assertThat(original.estado()).isEqualTo(EstadoTurno.AGENDADO);
    }

    @Test
    void aceptaTurnoFuturoSinRestriccionHoraria() {
        conTurno("T1", "11:00", EstadoTurno.AGENDADO);
        Turno cerrado = marcar.ejecutar(pedido("T1", USUARIO));
        assertThat(cerrado.estado()).isEqualTo(EstadoTurno.ATENDIDO);
    }

    @Test
    void rechazaTurnoInexistenteSinAuditar() {
        assertThatThrownBy(() -> marcar.ejecutar(pedido("NO-EXISTE", USUARIO)))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("RN-TU-02"));
        assertThat(e.turnos.todos()).isEmpty();
        assertThat(e.auditoria.todos()).isEmpty();
        assertThat(e.notificador.avisos).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoTurno.class, names = {"ATENDIDO", "CANCELADO", "AUSENTE"})
    void rechazaEstadoTerminalSinAuditar(EstadoTurno estado) {
        conTurno("T1", "09:30", estado);
        assertRechazo("T1", USUARIO, "RN-TU-02", estado);
    }

    @Test
    void rechazaSinUsuarioResponsable() {
        conTurno("T1", "09:30", EstadoTurno.AGENDADO);
        assertRechazo("T1", null, "RN-AU-01", EstadoTurno.AGENDADO);
        assertRechazo("T1", "   ", "RN-AU-01", EstadoTurno.AGENDADO);
    }

    @Test
    void registraAuditoriaConUsuarioYLaMismaMarcaDeTiempo() {
        conTurno("T1", "09:30", EstadoTurno.AGENDADO);
        marcar.ejecutar(pedido("T1", USUARIO));
        assertThat(e.auditoria.listarPorTurno("T1")).singleElement().satisfies(ev -> {
            assertThat(ev.accion()).isEqualTo(AccionAuditoria.MARCADO_ATENDIDO);
            assertThat(ev.usuario()).isEqualTo(USUARIO);
            assertThat(ev.timestamp()).isEqualTo(LocalDateTime.of(2030, 3, 4, 10, 0));
        });
    }

    @Test
    void noNotificaAlPaciente() {
        conTurno("T1", "09:30", EstadoTurno.AGENDADO);
        marcar.ejecutar(pedido("T1", USUARIO));
        assertThat(e.notificador.avisos).isEmpty();
        assertThat(e.notificador.acciones).isEmpty();
    }
}
