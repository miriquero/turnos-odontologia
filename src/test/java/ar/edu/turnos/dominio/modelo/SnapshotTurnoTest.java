package ar.edu.turnos.dominio.modelo;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dobles.DatosFicticios;
import org.junit.jupiter.api.Test;

class SnapshotTurnoTest {

    private static SnapshotTurno base() {
        return new SnapshotTurno("P1", "S1", "LIMPIEZA", 30, hora("10:00"), hora("10:30"));
    }

    @Test
    void laIgualdadEsPorValorYDetectaCualquierCampoDistinto() {
        SnapshotTurno original = base();
        SnapshotTurno copia = base();

        assertThat(original).isEqualTo(copia).hasSameHashCodeAs(copia);
        assertThat(original).isNotEqualTo(new SnapshotTurno("P2", "S1", "LIMPIEZA", 30, hora("10:00"), hora("10:30")));
        assertThat(original).isNotEqualTo(new SnapshotTurno("P1", "S2", "LIMPIEZA", 30, hora("10:00"), hora("10:30")));
        assertThat(original).isNotEqualTo(new SnapshotTurno("P1", "S1", "CONDUCTO", 30, hora("10:00"), hora("10:30")));
        assertThat(original).isNotEqualTo(new SnapshotTurno("P1", "S1", "LIMPIEZA", 90, hora("10:00"), hora("11:30")));
        assertThat(original).isNotEqualTo(new SnapshotTurno("P1", "S1", "LIMPIEZA", 30, hora("11:00"), hora("11:30")));
        assertThat(original).isNotEqualTo(new SnapshotTurno("P1", "S1", "LIMPIEZA", 30, hora("10:00"), hora("11:00")));
    }

    @Test
    void desdeTurnoCopiaTodosLosCampos() {
        Turno turno = DatosFicticios.turnosContiguos().get(0);

        assertThat(SnapshotTurno.desde(turno)).isEqualTo(new SnapshotTurno(
                "P1", "S1", "LIMPIEZA", 30, hora("10:00"), hora("10:30")));
    }

    @Test
    void rechazaUnFinNoPosteriorAlInicio() {
        assertThatThrownBy(() -> new SnapshotTurno("P1", "S1", "LIMPIEZA", 30, hora("10:00"), hora("10:00")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
