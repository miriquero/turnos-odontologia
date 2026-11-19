package ar.edu.turnos.dobles;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;

import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;
import java.util.List;

/** Seed ficticio espejo del modelo de datos de la KB (RN-PA-04). Ningun dato corresponde a una persona real. */
public final class DatosFicticios {

    public static final String TURNO_ANTERIOR_ID = "T-SEED-1000";
    public static final String TURNO_CONTIGUO_ID = "T-SEED-1030";

    private DatosFicticios() {}

    public static List<Paciente> pacientes() {
        return List.of(
                new Paciente("PA1", "Paciente Ficticio Uno", "00000001", true),
                new Paciente("PA2", "Paciente Ficticio Dos", "00000002", true));
    }

    public static List<Profesional> profesionales() {
        return List.of(
                new Profesional("P1", "Dra. Ficticia Uno", true),
                new Profesional("P2", "Dr. Ficticio Dos", true));
    }

    public static List<Sillon> sillones() {
        return List.of(new Sillon("S1", "Sillon 1", true), new Sillon("S2", "Sillon 2", true));
    }

    public static List<Prestacion> prestaciones() {
        return List.of(
                new Prestacion("LIMPIEZA", "Limpieza", 30, true),
                new Prestacion("CONDUCTO", "Conducto", 90, true));
    }

    /** Par de turnos contiguos: [10:00,10:30) y [10:30,11:00) del mismo profesional y sillon. */
    public static List<Turno> turnosContiguos() {
        return List.of(
                Turno.reconstruir(TURNO_ANTERIOR_ID, "PA1", "P1", "S1", "LIMPIEZA", 30, hora("10:00"),
                        EstadoTurno.AGENDADO, false),
                Turno.reconstruir(TURNO_CONTIGUO_ID, "PA2", "P1", "S1", "LIMPIEZA", 30, hora("10:30"),
                        EstadoTurno.AGENDADO, false));
    }
}
