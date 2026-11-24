package ar.edu.turnos.infraestructura.demo;

import ar.edu.turnos.dominio.modelo.AutorizacionSobreturno;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Agenda de arranque de la demo: un dia de consultorio con tres profesionales.
 *
 * <p><b>Todos los datos de persona son FICTICIOS</b>, sin excepcion (RN-PA-04). Los nombres
 * son verosimiles para que la pantalla se parezca a un consultorio real, pero ninguno
 * corresponde a una persona existente, y los documentos son numeros inventados.
 *
 * <p>La agenda se ancla al reloj: los turnos se siembran como desplazamientos respecto de la
 * hora en punto actual, de modo que al abrir la demo siempre haya turnos ya pasados —sobre los
 * que se puede registrar la asistencia o la ausencia— y turnos futuros —sobre los que se puede
 * agendar, reprogramar y cancelar—. Sin eso, la demo solo funcionaria a ciertas horas del dia.
 */
public final class SeedDemo {

    /** Minutos de cada fila de la grilla. */
    public static final int MINUTOS_POR_FILA = 15;

    /** Cuantas horas antes de la hora actual arranca la grilla. */
    private static final int HORAS_ANTES = 2;

    /** Cuantas horas despues de la hora actual termina la grilla. */
    private static final int HORAS_DESPUES = 6;

    private SeedDemo() {}

    /** Hora en punto actual: el ancla de toda la agenda sembrada. */
    public static LocalDateTime ancla(Clock reloj) {
        return LocalDateTime.now(reloj).truncatedTo(ChronoUnit.HOURS);
    }

    /** Primera y ultima hora de la jornada cuando la grilla no es la de hoy. */
    private static final int APERTURA = 8;
    private static final int CIERRE = 20;

    /**
     * Ventana que muestra la grilla para una fecha.
     *
     * <p>Para <b>hoy</b> la ventana se ancla al reloj —dos horas atras, seis adelante— para que
     * siempre haya turnos pasados, sobre los que se puede registrar la asistencia o la ausencia,
     * y futuros, sobre los que se puede agendar. Para cualquier <b>otro dia</b> se muestra la
     * jornada completa, de {@value #APERTURA} a {@value #CIERRE}, que es lo que esperaria ver
     * quien abre la agenda de la semana que viene.
     */
    public static LocalDateTime inicioDeGrilla(Clock reloj, LocalDate fecha) {
        return esHoy(reloj, fecha)
                ? ancla(reloj).minusHours(HORAS_ANTES)
                : fecha.atTime(APERTURA, 0);
    }

    public static LocalDateTime finDeGrilla(Clock reloj, LocalDate fecha) {
        return esHoy(reloj, fecha)
                ? ancla(reloj).plusHours(HORAS_DESPUES)
                : fecha.atTime(CIERRE, 0);
    }

    public static boolean esHoy(Clock reloj, LocalDate fecha) {
        return fecha.equals(LocalDate.now(reloj));
    }

    public static LocalDate hoy(Clock reloj) {
        return LocalDate.now(reloj);
    }

    public static List<Paciente> pacientes() {
        return List.of(
                new Paciente("PA1", "Sosa, Mariela", "28400111", true),
                new Paciente("PA2", "Benitez, Rodrigo", "31255222", true),
                new Paciente("PA3", "Ferreyra, Lucia", "35900333", true),
                new Paciente("PA4", "Juarez, Ernesto", "20144444", true),
                new Paciente("PA5", "Molina, Agustina", "40788555", true),
                new Paciente("PA6", "Cabrera, Tomas", "38066666", true),
                new Paciente("PA7", "Vera, Natalia", "33477777", true),
                new Paciente("PA8", "Acosta, Julian", "42311888", true));
    }

    public static List<Profesional> profesionales() {
        return List.of(
                new Profesional("P1", "Dra. Peralta", true),
                new Profesional("P2", "Dr. Mansilla", true),
                new Profesional("P3", "Dra. Ibarra", true));
    }

    public static List<Sillon> sillones() {
        return List.of(
                new Sillon("S1", "Sillon 1", true),
                new Sillon("S2", "Sillon 2", true),
                new Sillon("S3", "Sillon 3", true));
    }

    public static List<Prestacion> prestaciones() {
        return List.of(
                new Prestacion("CONSULTA", "Consulta", 20, true),
                new Prestacion("LIMPIEZA", "Limpieza", 30, true),
                new Prestacion("ARREGLO", "Arreglo simple", 45, true),
                new Prestacion("EXTRACCION", "Extraccion", 40, true),
                new Prestacion("CONDUCTO", "Conducto", 90, true));
    }

    /**
     * Agenda del dia. Incluye a proposito:
     * <ul>
     *   <li>un par de turnos <b>contiguos</b> de la Dra. Peralta: el conducto termina a la hora
     *       en punto + 1:30 y la limpieza arranca exactamente ahi (RN-AG-04);</li>
     *   <li>un turno <b>ATENDIDO</b>, que sigue ocupando su franja, y uno <b>AUSENTE</b> y otro
     *       <b>CANCELADO</b>, que la liberan (RN-AG-07);</li>
     *   <li>un <b>sobreturno</b> autorizado sobre una franja ya ocupada (RN-AG-06).</li>
     * </ul>
     */
    public static List<Turno> turnos(Clock reloj) {
        LocalDateTime a = ancla(reloj);
        return List.of(
                turno("T-01", "PA1", "P1", "S1", "LIMPIEZA", 30, a.minusHours(2), EstadoTurno.ATENDIDO, false, null, null),
                turno("T-02", "PA2", "P1", "S1", "CONSULTA", 20, a.minusHours(1), EstadoTurno.AUSENTE, false, "No se presento", null),
                turno("T-03", "PA3", "P1", "S1", "CONDUCTO", 90, a, EstadoTurno.AGENDADO, false, null, null),
                turno("T-04", "PA4", "P1", "S1", "LIMPIEZA", 30, a.plusMinutes(90), EstadoTurno.AGENDADO, false, null, null),
                turno("T-05", "PA5", "P2", "S2", "ARREGLO", 45, a.minusMinutes(90), EstadoTurno.ATENDIDO, false, null, null),
                turno("T-06", "PA6", "P2", "S2", "LIMPIEZA", 30, a.plusMinutes(30), EstadoTurno.AGENDADO, false, null, null),
                turno("T-07", "PA7", "P2", "S2", "EXTRACCION", 40, a.plusHours(2), EstadoTurno.CANCELADO, false, null, "El paciente reprogramo por telefono"),
                turno("T-08", "PA8", "P3", "S3", "CONSULTA", 20, a, EstadoTurno.AGENDADO, false, null, null),
                turno("T-09", "PA1", "P3", "S3", "LIMPIEZA", 30, a.plusMinutes(60), EstadoTurno.AGENDADO, true, null, null),
                turno("T-10", "PA2", "P3", "S3", "CONDUCTO", 90, a.plusHours(3), EstadoTurno.AGENDADO, false, null, null));
    }

    /** La autorizacion 1:1 del sobreturno sembrado (T-09). */
    public static AutorizacionSobreturno autorizacionDelSobreturno(Clock reloj) {
        return new AutorizacionSobreturno("T-09", "Dra. Ibarra", "Urgencia por dolor agudo",
                ancla(reloj).minusMinutes(45));
    }

    private static Turno turno(String id, String paciente, String prof, String sillon, String prestacion,
            int duracion, LocalDateTime inicio, EstadoTurno estado, boolean sobreturno,
            String motivoAusencia, String motivoCancelacion) {
        return Turno.reconstruir(id, paciente, prof, sillon, prestacion, duracion, inicio, estado,
                sobreturno, motivoAusencia, motivoCancelacion);
    }
}
