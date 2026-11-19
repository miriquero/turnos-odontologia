package ar.edu.turnos.dobles;

import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.modelo.Turno;

/** Builder de fixtures compartido por los tests del servicio y del caso de uso (tarea 6.10). */
public class Escenario {

    public final TurnoRepositorioEnMemoria turnos = new TurnoRepositorioEnMemoria();
    public final RepositoriosEnMemoria.Profesionales profesionales = new RepositoriosEnMemoria.Profesionales();
    public final RepositoriosEnMemoria.Sillones sillones = new RepositoriosEnMemoria.Sillones();
    public final RepositoriosEnMemoria.Prestaciones prestaciones = new RepositoriosEnMemoria.Prestaciones();
    public final RepositoriosEnMemoria.Pacientes pacientes = new RepositoriosEnMemoria.Pacientes();
    public final RegistroAuditoriaEnMemoria auditoria = new RegistroAuditoriaEnMemoria();
    public final NotificadorEspia notificador = new NotificadorEspia();

    /** Catalogo ficticio completo, sin turnos. */
    public static Escenario vacio() {
        Escenario e = new Escenario();
        DatosFicticios.pacientes().forEach(e.pacientes::agregar);
        DatosFicticios.profesionales().forEach(e.profesionales::agregar);
        DatosFicticios.sillones().forEach(e.sillones::agregar);
        DatosFicticios.prestaciones().forEach(e.prestaciones::agregar);
        return e;
    }

    /** Catalogo mas el par de turnos contiguos de las 10:30. */
    public static Escenario conSeed() {
        Escenario e = vacio();
        DatosFicticios.turnosContiguos().forEach(e.turnos::guardar);
        return e;
    }

    public Escenario conTurno(Turno turno) {
        turnos.guardar(turno);
        return this;
    }

    public Escenario conProfesional(Profesional p) { profesionales.agregar(p); return this; }
    public Escenario conSillon(Sillon s) { sillones.agregar(s); return this; }
    public Escenario conPrestacion(Prestacion p) { prestaciones.agregar(p); return this; }
    public Escenario conPaciente(Paciente p) { pacientes.agregar(p); return this; }
}
