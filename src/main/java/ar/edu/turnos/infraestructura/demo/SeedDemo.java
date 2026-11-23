package ar.edu.turnos.infraestructura.demo;

import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos de arranque de la demo. Todo dato de persona es FICTICIO, sin excepcion (RN-PA-04):
 * ningun nombre ni documento corresponde a una persona real.
 *
 * <p>La agenda se siembra sobre el dia siguiente para que todos los horarios sean futuros, que
 * es lo que exige RN-AG-08 para dar de alta un turno.
 */
public final class SeedDemo {

    private SeedDemo() {}

    public static LocalDate fechaDeAgenda() {
        return LocalDate.now().plusDays(1);
    }

    public static List<Paciente> pacientes() {
        return List.of(
                new Paciente("PA1", "Paciente Ficticio Uno", "00000001", true),
                new Paciente("PA2", "Paciente Ficticio Dos", "00000002", true),
                new Paciente("PA3", "Paciente Ficticio Tres", "00000003", true));
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

    /**
     * Par de turnos CONTIGUOS de la misma profesional en el mismo sillon: [10:00, 10:30) y
     * [10:30, 11:00). Arrancan asi a proposito, para que la pantalla muestre de entrada que un
     * turno que empieza exactamente cuando termina el anterior es valido (RN-AG-04).
     */
    public static List<Turno> turnos() {
        LocalDate dia = fechaDeAgenda();
        return List.of(
                Turno.reconstruir("T-DEMO-1000", "PA1", "P1", "S1", "LIMPIEZA", 30,
                        LocalDateTime.of(dia, LocalTime.of(10, 0)), EstadoTurno.AGENDADO, false),
                Turno.reconstruir("T-DEMO-1030", "PA2", "P1", "S1", "LIMPIEZA", 30,
                        LocalDateTime.of(dia, LocalTime.of(10, 30)), EstadoTurno.AGENDADO, false));
    }
}
