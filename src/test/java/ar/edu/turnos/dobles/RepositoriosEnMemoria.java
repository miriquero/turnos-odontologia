package ar.edu.turnos.dobles;

import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.puerto.PacienteRepositorio;
import ar.edu.turnos.dominio.puerto.PrestacionRepositorio;
import ar.edu.turnos.dominio.puerto.ProfesionalRepositorio;
import ar.edu.turnos.dominio.puerto.SillonRepositorio;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Dobles en memoria de los repositorios de catalogo y recursos. */
public final class RepositoriosEnMemoria {

    private RepositoriosEnMemoria() {}

    public static class Profesionales implements ProfesionalRepositorio {
        private final Map<String, Profesional> datos = new HashMap<>();
        public void agregar(Profesional p) { datos.put(p.id(), p); }
        @Override public Optional<Profesional> buscarPorId(String id) { return Optional.ofNullable(datos.get(id)); }
    }

    public static class Sillones implements SillonRepositorio {
        private final Map<String, Sillon> datos = new HashMap<>();
        public void agregar(Sillon s) { datos.put(s.id(), s); }
        @Override public Optional<Sillon> buscarPorId(String id) { return Optional.ofNullable(datos.get(id)); }
    }

    public static class Prestaciones implements PrestacionRepositorio {
        private final Map<String, Prestacion> datos = new HashMap<>();
        public void agregar(Prestacion p) { datos.put(p.id(), p); }
        @Override public Optional<Prestacion> buscarPorId(String id) { return Optional.ofNullable(datos.get(id)); }
    }

    public static class Pacientes implements PacienteRepositorio {
        private final Map<String, Paciente> datos = new HashMap<>();
        public void agregar(Paciente p) { datos.put(p.id(), p); }
        @Override public Optional<Paciente> buscarPorId(String id) { return Optional.ofNullable(datos.get(id)); }
    }
}
