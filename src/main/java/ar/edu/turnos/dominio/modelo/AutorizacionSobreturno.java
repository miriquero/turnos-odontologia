package ar.edu.turnos.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/** Solo la estructura: sin caso de uso en este change. */
public record AutorizacionSobreturno(String turnoId, String autorizadoPor, String motivo, LocalDateTime autorizadoEn) {
    public AutorizacionSobreturno {
        exigir(turnoId, "turnoId");
        exigir(autorizadoPor, "autorizadoPor");
        exigir(motivo, "motivo");
        Objects.requireNonNull(autorizadoEn, "autorizadoEn");
    }

    private static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " obligatorio");
        }
    }
}
