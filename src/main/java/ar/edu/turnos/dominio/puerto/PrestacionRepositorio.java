package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.Prestacion;
import java.util.Optional;

public interface PrestacionRepositorio {
    Optional<Prestacion> buscarPorId(String id);
}
