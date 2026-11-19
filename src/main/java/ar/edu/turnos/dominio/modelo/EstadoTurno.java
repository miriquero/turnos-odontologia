package ar.edu.turnos.dominio.modelo;

/** Estados del turno (RN-TU-01, RN-TU-02) y si ocupan la agenda (RN-AG-07). */
public enum EstadoTurno {
    AGENDADO(true),
    ATENDIDO(true),
    CANCELADO(false),
    AUSENTE(false);

    private final boolean ocupaAgenda;

    EstadoTurno(boolean ocupaAgenda) {
        this.ocupaAgenda = ocupaAgenda;
    }

    public boolean ocupaAgenda() {
        return ocupaAgenda;
    }

    /** Solo AGENDADO puede pasar a ATENDIDO, CANCELADO o AUSENTE; los demas son terminales. */
    public boolean puedeTransicionarA(EstadoTurno destino) {
        return this == AGENDADO && destino != AGENDADO;
    }
}
