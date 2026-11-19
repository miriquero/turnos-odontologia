package ar.edu.turnos.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class EventoAuditoriaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2030, 1, 1, 9, 0);

    @Test
    void existenLasSieteAcciones() {
        assertThat(Arrays.stream(AccionAuditoria.values()).map(Enum::name)).containsExactlyInAnyOrder(
                "CREADO", "MODIFICADO", "REPROGRAMADO", "CANCELADO", "MARCADO_ATENDIDO", "MARCADO_AUSENTE",
                "SOBRETURNO_AUTORIZADO");
    }

    @Test
    void conservaSusDatos() {
        EventoAuditoria e = new EventoAuditoria("T1", AccionAuditoria.CREADO, "recepcion.ficticia", AHORA);
        assertThat(e.turnoId()).isEqualTo("T1");
        assertThat(e.accion()).isEqualTo(AccionAuditoria.CREADO);
        assertThat(e.usuario()).isEqualTo("recepcion.ficticia");
        assertThat(e.timestamp()).isEqualTo(AHORA);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void unEventoSinUsuarioSeRechaza(String usuario) {
        assertThatThrownBy(() -> new EventoAuditoria("T1", AccionAuditoria.CREADO, usuario, AHORA))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void unEventoSinTimestampSeRechaza() {
        assertThatThrownBy(() -> new EventoAuditoria("T1", AccionAuditoria.CREADO, "u", null))
                .isInstanceOf(NullPointerException.class);
    }
}
