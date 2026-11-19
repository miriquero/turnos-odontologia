package ar.edu.turnos.dominio.modelo;

import java.util.Objects;

/** Recurso cuya ocupacion se evalua: un profesional o un sillon. */
public record RecursoAgenda(Tipo tipo, String id) {

    public enum Tipo { PROFESIONAL, SILLON }

    public RecursoAgenda {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(id, "id");
    }

    public static RecursoAgenda profesional(String id) { return new RecursoAgenda(Tipo.PROFESIONAL, id); }

    public static RecursoAgenda sillon(String id) { return new RecursoAgenda(Tipo.SILLON, id); }
}
