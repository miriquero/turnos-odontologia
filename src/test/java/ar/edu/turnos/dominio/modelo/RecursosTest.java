package ar.edu.turnos.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RecursosTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -30})
    void prestacionRechazaDuracionNoPositiva(int duracion) {
        assertThatThrownBy(() -> new Prestacion("PR1", "Limpieza", duracion, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void prestacionValidaConservaSusDatos() {
        Prestacion p = new Prestacion("PR1", "Limpieza", 30, true);
        assertThat(p.duracionMinutos()).isEqualTo(30);
        assertThat(p.activa()).isTrue();
    }

    @Test
    void profesionalSillonYPacienteRequierenIdentificador() {
        assertThatThrownBy(() -> new Profesional(null, "Dra. Ficticia", true)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Sillon(null, "Sillon 1", true)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Paciente(null, "Paciente Ficticio", "00000001", true))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void conservanElFlagActivo() {
        assertThat(new Profesional("P1", "Dra. Ficticia", false).activo()).isFalse();
        assertThat(new Sillon("S1", "Sillon 1", false).activo()).isFalse();
        assertThat(new Paciente("PA1", "Paciente Ficticio", "00000001", true).activo()).isTrue();
    }
}
