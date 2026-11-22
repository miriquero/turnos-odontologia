package ar.edu.turnos.dominio.excepcion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReglaDeNegocioVioladaTest {

    @Test
    void elCodigoEsRecuperableDeFormaProgramatica() {
        ReglaDeNegocioViolada e = new ReglaDeNegocioViolada("RN-AG-02", "El profesional ya tiene un turno");
        assertThat(e.codigo()).isEqualTo("RN-AG-02");
        assertThat(e.getMessage()).contains("El profesional ya tiene un turno");
    }

    @Test
    void otroCodigoSeConserva() {
        assertThat(new ReglaDeNegocioViolada("RN-AG-03", "sillon").codigo()).isEqualTo("RN-AG-03");
    }

    @Test
    void exponeElCodigoDeTransicionDeTurno() {
        assertThat(ReglaDeNegocioViolada.RN_TU_02).isEqualTo("RN-TU-02");
        assertThat(new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02, "transicion").codigo())
                .isEqualTo("RN-TU-02");
    }

    @Test
    void rechazaCodigoVacio() {
        assertThatThrownBy(() -> new ReglaDeNegocioViolada(" ", "x")).isInstanceOf(IllegalArgumentException.class);
    }
}
