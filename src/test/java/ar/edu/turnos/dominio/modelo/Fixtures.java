package ar.edu.turnos.dominio.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Fabricas compartidas de los tests. Todos los datos de persona son ficticios (RN-PA-04). */
public final class Fixtures {

    /** Dia fijo, posterior al reloj fijo de los tests de caso de uso. */
    public static final LocalDateTime DIA_FUTURO = LocalDateTime.of(2030, 3, 4, 0, 0);

    private Fixtures() {}

    public static LocalDateTime hora(String hhmm) {
        return LocalDate.from(DIA_FUTURO).atTime(LocalTime.parse(hhmm));
    }

    public static Intervalo intervalo(String desde, String hasta) {
        return new Intervalo(hora(desde), hora(hasta));
    }
}
