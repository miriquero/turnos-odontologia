package ar.edu.turnos.dominio.modelo;

import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class IntervaloTest {

    private static final LocalDateTime DIA = Fixtures.DIA_FUTURO;

    @Test
    void rechazaFinAnteriorAlInicio() {
        assertThatThrownBy(() -> new Intervalo(DIA.withHour(10).withMinute(30), DIA.withHour(10).withMinute(0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaDuracionCero() {
        assertThatThrownBy(() -> new Intervalo(DIA.withHour(10).withMinute(0), DIA.withHour(10).withMinute(0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaExtremosNulos() {
        assertThatThrownBy(() -> new Intervalo(null, DIA)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Intervalo(DIA, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void aceptaUnIntervaloValido() {
        Intervalo i = intervalo("10:00", "10:30");
        assertThat(i.inicio()).isEqualTo(DIA.withHour(10).withMinute(0));
        assertThat(i.fin()).isEqualTo(DIA.withHour(10).withMinute(30));
    }

    // Tabla de RN-AG-04: existente, nuevo, se solapan.
    static Stream<Arguments> tablaRnAg04() {
        return Stream.of(
                Arguments.of("Caso 1 nuevo empieza cuando termina el existente", "10:00", "10:30", "10:30", "11:00", false),
                Arguments.of("Caso 2 nuevo termina cuando empieza el existente", "10:00", "10:30", "09:30", "10:00", false),
                Arguments.of("Caso 3 solapamiento de un minuto", "10:00", "10:30", "10:29", "10:59", true),
                Arguments.of("Caso 4 intervalos identicos", "10:00", "10:30", "10:00", "10:30", true),
                Arguments.of("Caso 5 nuevo contenido en el existente", "10:00", "11:30", "10:30", "11:00", true),
                Arguments.of("Caso 6 nuevo contiene al existente", "10:30", "11:00", "10:00", "11:30", true),
                Arguments.of("Caso 7 parcial por la derecha", "10:00", "10:30", "10:15", "10:45", true),
                Arguments.of("Caso 8 parcial por la izquierda", "10:00", "10:30", "09:45", "10:15", true),
                Arguments.of("Caso 9 sin contacto", "10:00", "10:30", "11:00", "11:30", false));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tablaRnAg04")
    void casosDeLaTablaRnAg04(String caso, String eIni, String eFin, String nIni, String nFin, boolean seSolapan) {
        Intervalo existente = intervalo(eIni, eFin);
        Intervalo nuevo = intervalo(nIni, nFin);
        assertThat(nuevo.seSolapaCon(existente)).as(caso).isEqualTo(seSolapan);
    }

    @ParameterizedTest(name = "{0} (invertido)")
    @MethodSource("tablaRnAg04")
    void elSolapamientoEsSimetrico(String caso, String eIni, String eFin, String nIni, String nFin, boolean seSolapan) {
        Intervalo a = intervalo(eIni, eFin);
        Intervalo b = intervalo(nIni, nFin);
        assertThat(a.seSolapaCon(b)).as(caso).isEqualTo(b.seSolapaCon(a)).isEqualTo(seSolapan);
    }
}
