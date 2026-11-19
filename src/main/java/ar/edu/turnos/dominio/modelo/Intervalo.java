package ar.edu.turnos.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/** Franja semiabierta [inicio, fin) de la agenda (RN-AG-04). Unica definicion de solapamiento del sistema. */
public record Intervalo(LocalDateTime inicio, LocalDateTime fin) {

    public Intervalo {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fin, "fin");
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("fin debe ser estrictamente posterior a inicio: [" + inicio + ", " + fin + ")");
        }
    }

    /** A.inicio < B.fin && B.inicio < A.fin, con comparacion estricta en ambos lados. */
    public boolean seSolapaCon(Intervalo otro) {
        return this.inicio.isBefore(otro.fin) && otro.inicio.isBefore(this.fin);
    }
}
