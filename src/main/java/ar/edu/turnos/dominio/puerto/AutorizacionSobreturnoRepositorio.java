package ar.edu.turnos.dominio.puerto;

import ar.edu.turnos.dominio.modelo.AutorizacionSobreturno;
import java.util.Optional;

/** Puerto de persistencia 1:1 para las autorizaciones de sobreturno (C-04). */
public interface AutorizacionSobreturnoRepositorio {

    AutorizacionSobreturno guardar(AutorizacionSobreturno autorizacion);

    Optional<AutorizacionSobreturno> buscarPorTurnoId(String turnoId);
}
