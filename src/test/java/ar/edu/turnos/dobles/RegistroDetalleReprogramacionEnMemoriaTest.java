package ar.edu.turnos.dobles;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.turnos.dominio.modelo.DetalleReprogramacion;
import ar.edu.turnos.dominio.modelo.SnapshotTurno;
import org.junit.jupiter.api.Test;

class RegistroDetalleReprogramacionEnMemoriaTest {

    private static final SnapshotTurno A =
            new SnapshotTurno("P1", "S1", "LIMPIEZA", 30, hora("10:00"), hora("10:30"));
    private static final SnapshotTurno B =
            new SnapshotTurno("P1", "S1", "LIMPIEZA", 30, hora("11:00"), hora("11:30"));

    private static DetalleReprogramacion detalle(String turnoId, String usuario, String hhmm) {
        return new DetalleReprogramacion(turnoId, A, B, usuario, hora(hhmm));
    }

    @Test
    void listarPorTurnoDevuelveLosDetallesEnOrden() {
        RegistroDetalleReprogramacionEnMemoria registro = new RegistroDetalleReprogramacionEnMemoria();
        DetalleReprogramacion primero = detalle("T-1", "u1", "09:00");
        DetalleReprogramacion segundo = detalle("T-1", "u2", "09:30");

        registro.registrar(primero);
        registro.registrar(segundo);

        assertThat(registro.listarPorTurno("T-1")).containsExactly(primero, segundo);
    }

    @Test
    void esAppendOnlyYConservaElPrimeroTrasUnSegundoRegistro() {
        RegistroDetalleReprogramacionEnMemoria registro = new RegistroDetalleReprogramacionEnMemoria();
        DetalleReprogramacion primero = detalle("T-1", "u1", "09:00");

        registro.registrar(primero);
        registro.registrar(detalle("T-1", "u2", "09:30"));

        assertThat(registro.listarPorTurno("T-1").get(0)).isEqualTo(primero);
        assertThat(registro.todos()).hasSize(2);
    }

    @Test
    void filtraPorTurno() {
        RegistroDetalleReprogramacionEnMemoria registro = new RegistroDetalleReprogramacionEnMemoria();
        DetalleReprogramacion ajeno = detalle("T-2", "u1", "09:00");

        registro.registrar(detalle("T-1", "u1", "09:00"));
        registro.registrar(ajeno);

        assertThat(registro.listarPorTurno("T-2")).containsExactly(ajeno);
    }
}
