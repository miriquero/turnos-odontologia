package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.Paciente;
import java.util.Optional;

public interface PacienteRepositorio {
    Optional<Paciente> buscarPorId(String id);
}
