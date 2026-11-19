package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.Sillon;
import java.util.Optional;

public interface SillonRepositorio {
    Optional<Sillon> buscarPorId(String id);
}
