package ar.edu.turnos.dominio.modelo;

import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Turno de la agenda. El fin no es construible desde fuera: se deriva de inicio + duracion
 * de la prestacion (RN-AG-01, D-C01-6).
 */
public final class Turno {

    private final String id;
    private final String pacienteId;
    private final String profesionalId;
    private final String sillonId;
    private final String prestacionId;
    private final int duracionMinutos;
    private final Intervalo intervalo;
    private final EstadoTurno estado;
    private final boolean esSobreturno;
    private final String motivoAusencia;
    private final String motivoCancelacion;

    private Turno(String id, String pacienteId, String profesionalId, String sillonId, String prestacionId,
            int duracionMinutos, LocalDateTime inicio, EstadoTurno estado, boolean esSobreturno,
            String motivoAusencia, String motivoCancelacion) {
        this.id = Objects.requireNonNull(id, "id");
        this.pacienteId = Objects.requireNonNull(pacienteId, "pacienteId");
        this.profesionalId = Objects.requireNonNull(profesionalId, "profesionalId");
        this.sillonId = Objects.requireNonNull(sillonId, "sillonId");
        this.prestacionId = Objects.requireNonNull(prestacionId, "prestacionId");
        this.duracionMinutos = duracionMinutos;
        this.intervalo = new Intervalo(inicio, inicio.plusMinutes(duracionMinutos));
        this.estado = Objects.requireNonNull(estado, "estado");
        this.esSobreturno = esSobreturno;
        this.motivoAusencia = motivoAusencia;
        this.motivoCancelacion = motivoCancelacion;
    }

    /** Alta: nace AGENDADO, sin sobreturno, con el fin derivado de la prestacion. */
    public static Turno agendar(Paciente paciente, Profesional profesional, Sillon sillon, Prestacion prestacion,
            LocalDateTime inicio) {
        return new Turno(UUID.randomUUID().toString(), paciente.id(), profesional.id(), sillon.id(),
                prestacion.id(), prestacion.duracionMinutos(), inicio, EstadoTurno.AGENDADO, false, null, null);
    }

    /** Rehidratacion de un turno ya existente (usada por adaptadores y fixtures); tampoco acepta fin. */
    public static Turno reconstruir(String id, String pacienteId, String profesionalId, String sillonId,
            String prestacionId, int duracionMinutos, LocalDateTime inicio, EstadoTurno estado,
            boolean esSobreturno) {
        return reconstruir(id, pacienteId, profesionalId, sillonId, prestacionId, duracionMinutos, inicio, estado,
                esSobreturno, null, null);
    }

    public static Turno reconstruir(String id, String pacienteId, String profesionalId, String sillonId,
            String prestacionId, int duracionMinutos, LocalDateTime inicio, EstadoTurno estado,
            boolean esSobreturno, String motivoAusencia) {
        return new Turno(id, pacienteId, profesionalId, sillonId, prestacionId, duracionMinutos, inicio, estado,
                esSobreturno, motivoAusencia, null);
    }

    public static Turno reconstruir(String id, String pacienteId, String profesionalId, String sillonId,
            String prestacionId, int duracionMinutos, LocalDateTime inicio, EstadoTurno estado,
            boolean esSobreturno, String motivoAusencia, String motivoCancelacion) {
        return new Turno(id, pacienteId, profesionalId, sillonId, prestacionId, duracionMinutos, inicio, estado,
                esSobreturno, motivoAusencia, motivoCancelacion);
    }

    public String id() { return id; }
    public String pacienteId() { return pacienteId; }
    public String profesionalId() { return profesionalId; }
    public String sillonId() { return sillonId; }
    public String prestacionId() { return prestacionId; }
    public int duracionMinutos() { return duracionMinutos; }
    public Intervalo intervalo() { return intervalo; }
    public EstadoTurno estado() { return estado; }
    public boolean esSobreturno() { return esSobreturno; }
    public String motivoAusencia() { return motivoAusencia; }
    public String motivoCancelacion() { return motivoCancelacion; }

    public Turno marcarAusente(String motivo) {
        String normalizado = motivo == null ? null : motivo.trim();
        return conEstado(EstadoTurno.AUSENTE, normalizado == null || normalizado.isEmpty() ? null : normalizado, null);
    }

    public Turno marcarAtendido() {
        return conEstado(EstadoTurno.ATENDIDO, null, null);
    }

    public Turno cancelar(String motivo) {
        String normalizado = motivo == null ? null : motivo.trim();
        return conEstado(EstadoTurno.CANCELADO, null, normalizado == null || normalizado.isEmpty() ? null : normalizado);
    }

    private Turno conEstado(EstadoTurno destino, String motivoAusencia, String motivoCancelacion) {
        if (!estado.puedeTransicionarA(destino)) {
            throw new ReglaDeNegocioViolada(ReglaDeNegocioViolada.RN_TU_02,
                    "No hay transicion de " + estado + " a " + destino);
        }
        return new Turno(id, pacienteId, profesionalId, sillonId, prestacionId, duracionMinutos,
                intervalo.inicio(), destino, esSobreturno, motivoAusencia, motivoCancelacion);
    }
}
