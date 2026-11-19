package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.Profesional;
import java.util.Optional;

public interface ProfesionalRepositorio {
    Optional<Profesional> buscarPorId(String id);
}
