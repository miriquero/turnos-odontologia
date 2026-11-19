package ar.edu.turnos.dobles;

import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.RecursoAgenda;
import ar.edu.turnos.dominio.modelo.Turno;
import org.junit.jupiter.api.Test;

class TurnoRepositorioEnMemoriaTest {

    private final TurnoRepositorioEnMemoria repo = new TurnoRepositorioEnMemoria();

    private Turno turno(String id, String prof, String sillon, String hora, EstadoTurno estado) {
        return Turno.reconstruir(id, "PA1", prof, sillon, "PR1", 30, ar.edu.turnos.dominio.modelo.Fixtures.hora(hora),
                estado, false);
    }

    @Test
    void unTurnoCanceladoNoApareceEnLaBusquedaDeVigentesSolapados() {
        repo.guardar(turno("T1", "P1", "S1", "10:00", EstadoTurno.CANCELADO));
        assertThat(repo.buscarVigentesSolapados(RecursoAgenda.profesional("P1"), intervalo("10:00", "10:30"), null))
                .isEmpty();
    }

    @Test
    void unTurnoAtendidoSiApareceEnLaBusquedaDeVigentesSolapados() {
        repo.guardar(turno("T1", "P1", "S1", "10:00", EstadoTurno.ATENDIDO));
        assertThat(repo.buscarVigentesSolapados(RecursoAgenda.profesional("P1"), intervalo("10:00", "10:30"), null))
                .extracting(Turno::id).containsExactly("T1");
    }

    @Test
    void unTurnoAusenteNoApareceYUnoAgendadoSi() {
        repo.guardar(turno("T1", "P1", "S1", "10:00", EstadoTurno.AUSENTE));
        repo.guardar(turno("T2", "P1", "S1", "10:00", EstadoTurno.AGENDADO));
        assertThat(repo.buscarVigentesSolapados(RecursoAgenda.sillon("S1"), intervalo("10:00", "10:30"), null))
                .extracting(Turno::id).containsExactly("T2");
    }

    @Test
    void filtraPorRecursoPorSolapamientoYPorExclusion() {
        repo.guardar(turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO));
        assertThat(repo.buscarVigentesSolapados(RecursoAgenda.profesional("P2"), intervalo("10:00", "10:30"), null))
                .isEmpty();
        assertThat(repo.buscarVigentesSolapados(RecursoAgenda.sillon("S2"), intervalo("10:00", "10:30"), null))
                .isEmpty();
        assertThat(repo.buscarVigentesSolapados(RecursoAgenda.profesional("P1"), intervalo("10:30", "11:00"), null))
                .isEmpty();
        assertThat(repo.buscarVigentesSolapados(RecursoAgenda.profesional("P1"), intervalo("10:00", "10:30"), "T1"))
                .isEmpty();
    }

    @Test
    void guardarYBuscarPorId() {
        Turno t = turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        repo.guardar(t);
        assertThat(repo.buscarPorId("T1")).contains(t);
        assertThat(repo.buscarPorId("nada")).isEmpty();
    }
}
