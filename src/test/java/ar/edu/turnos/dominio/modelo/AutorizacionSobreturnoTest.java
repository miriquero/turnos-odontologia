package ar.edu.turnos.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AutorizacionSobreturnoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2030, 1, 1, 9, 0);

    @Test
    void conservaSusDatos() {
        AutorizacionSobreturno a = new AutorizacionSobreturno("T1", "admin.ficticio", "urgencia", AHORA);
        assertThat(a.turnoId()).isEqualTo("T1");
        assertThat(a.autorizadoPor()).isEqualTo("admin.ficticio");
        assertThat(a.motivo()).isEqualTo("urgencia");
        assertThat(a.autorizadoEn()).isEqualTo(AHORA);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void turnoAutorizanteYMotivoSonObligatorios(String vacio) {
        assertThatThrownBy(() -> new AutorizacionSobreturno(vacio, "u", "m", AHORA)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> new AutorizacionSobreturno("T1", vacio, "m", AHORA)).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> new AutorizacionSobreturno("T1", "u", vacio, AHORA)).isInstanceOf(RuntimeException.class);
    }
}
