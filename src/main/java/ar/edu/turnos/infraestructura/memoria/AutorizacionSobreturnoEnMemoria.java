package ar.edu.turnos.infraestructura.memoria;

import ar.edu.turnos.dominio.modelo.AutorizacionSobreturno;
import ar.edu.turnos.dominio.puerto.AutorizacionSobreturnoRepositorio;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistencia 1:1 de las autorizaciones de sobreturno (RN-AG-06): a cada turno marcado como
 * sobreturno le corresponde exactamente una autorizacion, con responsable, motivo y marca de
 * tiempo.
 */
public final class AutorizacionSobreturnoEnMemoria implements AutorizacionSobreturnoRepositorio {

    private final Map<String, AutorizacionSobreturno> datos = new LinkedHashMap<>();

    @Override
    public synchronized AutorizacionSobreturno guardar(AutorizacionSobreturno autorizacion) {
        datos.put(autorizacion.turnoId(), autorizacion);
        return autorizacion;
    }

    @Override
    public synchronized Optional<AutorizacionSobreturno> buscarPorTurnoId(String turnoId) {
        return Optional.ofNullable(datos.get(turnoId));
    }

    public synchronized List<AutorizacionSobreturno> todas() {
        return List.copyOf(datos.values());
    }

    public synchronized void vaciar() {
        datos.clear();
    }
}
