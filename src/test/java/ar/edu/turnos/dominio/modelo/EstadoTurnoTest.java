package ar.edu.turnos.dominio.modelo;

import static ar.edu.turnos.dominio.modelo.EstadoTurno.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EstadoTurnoTest {

    @Test
    void transicionesValidasDesdeAgendado() {
        assertThat(AGENDADO.puedeTransicionarA(ATENDIDO)).isTrue();
        assertThat(AGENDADO.puedeTransicionarA(CANCELADO)).isTrue();
        assertThat(AGENDADO.puedeTransicionarA(AUSENTE)).isTrue();
    }

    @Test
    void agendadoNoTransicionaASiMismo() {
        assertThat(AGENDADO.puedeTransicionarA(AGENDADO)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoTurno.class, names = {"ATENDIDO", "CANCELADO", "AUSENTE"})
    void losEstadosTerminalesNoTienenSalida(EstadoTurno terminal) {
        for (EstadoTurno destino : EstadoTurno.values()) {
            assertThat(terminal.puedeTransicionarA(destino))
                    .as("%s -> %s", terminal, destino).isFalse();
        }
    }

    @Test
    void cadaEstadoDeclaraSiOcupaLaAgenda() {
        assertThat(EnumSet.allOf(EstadoTurno.class).stream().filter(EstadoTurno::ocupaAgenda))
                .containsExactlyInAnyOrder(AGENDADO, ATENDIDO);
        assertThat(CANCELADO.ocupaAgenda()).isFalse();
        assertThat(AUSENTE.ocupaAgenda()).isFalse();
    }
}
