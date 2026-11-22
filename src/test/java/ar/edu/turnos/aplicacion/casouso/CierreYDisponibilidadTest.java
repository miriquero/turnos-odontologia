package ar.edu.turnos.aplicacion.casouso;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dobles.Escenario;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.servicio.OcupacionPorTurnos;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import java.time.Clock;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CierreYDisponibilidadTest {

    private static final String USUARIO = "recepcion.ficticia";
    private static final Clock RELOJ_CIERRE = Clock.fixed(hora("10:00").toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    private static final Clock RELOJ_ALTA = Clock.fixed(hora("08:00").toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    private final Escenario e = Escenario.vacio();

    private void turnoOriginal() {
        e.conTurno(Turno.reconstruir("T1", "PA2", "P1", "S1", "LIMPIEZA", 30, hora("10:00"),
                EstadoTurno.AGENDADO, false));
    }

    private AgendarTurno agendar() {
        return new AgendarTurno(e.turnos, e.pacientes, e.profesionales, e.sillones, e.prestaciones,
                new ServicioDisponibilidad(new OcupacionPorTurnos(e.turnos)), e.auditoria, e.notificador, RELOJ_ALTA);
    }

    private static SolicitudAgendarTurno alta(String inicio) {
        return new SolicitudAgendarTurno("PA1", "P1", "S1", "LIMPIEZA", hora(inicio), USUARIO);
    }

    @Test
    void despuesDeRegistrarAusenciaSePuedeAgendarSobreLaMismaFranja() {
        turnoOriginal();
        new RegistrarAusencia(e.turnos, e.auditoria, RELOJ_CIERRE)
                .ejecutar(new SolicitudRegistrarAusencia("T1", "no se presento", USUARIO));

        Turno nuevo = agendar().ejecutar(alta("10:00"));

        assertThat(e.turnos.buscarPorId("T1")).get()
                .satisfies(t -> assertThat(t.estado()).isEqualTo(EstadoTurno.AUSENTE));
        assertThat(nuevo.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(nuevo.intervalo()).isEqualTo(intervalo("10:00", "10:30"));
    }

    @Test
    void despuesDeRegistrarAtencionLaFranjaSigueOcupada() {
        turnoOriginal();
        new MarcarTurnoAtendido(e.turnos, e.auditoria, RELOJ_CIERRE)
                .ejecutar(new SolicitudMarcarAtendido("T1", USUARIO));

        assertThatThrownBy(() -> agendar().ejecutar(alta("10:15")))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("RN-AG-02"));
        assertThat(e.turnos.buscarPorId("T1")).get()
                .satisfies(t -> assertThat(t.estado()).isEqualTo(EstadoTurno.ATENDIDO));
    }
}
