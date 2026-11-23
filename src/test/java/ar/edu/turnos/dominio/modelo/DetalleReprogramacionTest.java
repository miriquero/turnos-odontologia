package ar.edu.turnos.dominio.modelo;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DetalleReprogramacionTest {

    private static final SnapshotTurno ANTERIOR =
            new SnapshotTurno("P1", "S1", "LIMPIEZA", 30, hora("10:00"), hora("10:30"));
    private static final SnapshotTurno NUEVO =
            new SnapshotTurno("P2", "S2", "CONDUCTO", 90, hora("11:00"), hora("12:30"));

    @Test
    void conservaAmbosSnapshotsYSusDatosDeCorrelacion() {
        DetalleReprogramacion detalle = new DetalleReprogramacion(
                "T-1", ANTERIOR, NUEVO, "recepcion.ficticia", hora("09:00"));

        assertThat(detalle.turnoId()).isEqualTo("T-1");
        assertThat(detalle.anterior()).isEqualTo(ANTERIOR);
        assertThat(detalle.nuevo()).isEqualTo(NUEVO);
        assertThat(detalle.usuario()).isEqualTo("recepcion.ficticia");
        assertThat(detalle.timestamp()).isEqualTo(hora("09:00"));
    }

    @Test
    void rechazaUsuarioVacio() {
        assertThatThrownBy(() -> new DetalleReprogramacion("T-1", ANTERIOR, NUEVO, null, hora("09:00")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DetalleReprogramacion("T-1", ANTERIOR, NUEVO, "  ", hora("09:00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaDatosObligatoriosAusentes() {
        assertThatThrownBy(() -> new DetalleReprogramacion(" ", ANTERIOR, NUEVO, "u", hora("09:00")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DetalleReprogramacion("T-1", null, NUEVO, "u", hora("09:00")))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DetalleReprogramacion("T-1", ANTERIOR, NUEVO, "u", null))
                .isInstanceOf(NullPointerException.class);
    }
}
