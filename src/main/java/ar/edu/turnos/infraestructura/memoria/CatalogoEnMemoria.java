package ar.edu.turnos.infraestructura.memoria;

import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Prestacion;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Sillon;
import ar.edu.turnos.dominio.puerto.PacienteRepositorio;
import ar.edu.turnos.dominio.puerto.PrestacionRepositorio;
import ar.edu.turnos.dominio.puerto.ProfesionalRepositorio;
import ar.edu.turnos.dominio.puerto.SillonRepositorio;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catalogo en memoria: pacientes, profesionales, sillones y prestaciones. Cada repositorio
 * implementa su propio puerto; esta clase solo los agrupa para armar el contexto de la demo.
 *
 * <p>Todos los datos de persona son ficticios, sin excepcion (RN-PA-04).
 */
public final class CatalogoEnMemoria {

    public final Pacientes pacientes = new Pacientes();
    public final Profesionales profesionales = new Profesionales();
    public final Sillones sillones = new Sillones();
    public final Prestaciones prestaciones = new Prestaciones();

    /** Repositorio en memoria indexado por id, con el orden de carga preservado. */
    private abstract static class PorId<T> {
        final Map<String, T> datos = new LinkedHashMap<>();

        abstract String idDe(T valor);

        synchronized void agregar(T valor) {
            datos.put(idDe(valor), valor);
        }

        synchronized Optional<T> porId(String id) {
            return Optional.ofNullable(datos.get(id));
        }

        public synchronized List<T> todos() {
            return List.copyOf(datos.values());
        }
    }

    public static final class Pacientes extends PorId<Paciente> implements PacienteRepositorio {
        @Override
        String idDe(Paciente p) {
            return p.id();
        }

        @Override
        public Optional<Paciente> buscarPorId(String id) {
            return porId(id);
        }

        public void cargar(List<Paciente> valores) {
            valores.forEach(this::agregar);
        }
    }

    public static final class Profesionales extends PorId<Profesional> implements ProfesionalRepositorio {
        @Override
        String idDe(Profesional p) {
            return p.id();
        }

        @Override
        public Optional<Profesional> buscarPorId(String id) {
            return porId(id);
        }

        public void cargar(List<Profesional> valores) {
            valores.forEach(this::agregar);
        }
    }

    public static final class Sillones extends PorId<Sillon> implements SillonRepositorio {
        @Override
        String idDe(Sillon s) {
            return s.id();
        }

        @Override
        public Optional<Sillon> buscarPorId(String id) {
            return porId(id);
        }

        public void cargar(List<Sillon> valores) {
            valores.forEach(this::agregar);
        }
    }

    public static final class Prestaciones extends PorId<Prestacion> implements PrestacionRepositorio {
        @Override
        String idDe(Prestacion p) {
            return p.id();
        }

        @Override
        public Optional<Prestacion> buscarPorId(String id) {
            return porId(id);
        }

        public void cargar(List<Prestacion> valores) {
            valores.forEach(this::agregar);
        }
    }
}
