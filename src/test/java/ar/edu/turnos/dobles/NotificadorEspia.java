package ar.edu.turnos.dobles;

import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.NotificadorPaciente;
import java.util.ArrayList;
import java.util.List;

public class NotificadorEspia implements NotificadorPaciente {

    /** Turnos notificados, en orden. */
    public final List<Turno> avisos = new ArrayList<>();

    /** Acciones notificadas, en el mismo orden que {@link #avisos}. */
    public final List<AccionAuditoria> acciones = new ArrayList<>();

    public boolean falla = false;

    @Override
    public void notificar(Turno turno, AccionAuditoria accion) {
        if (falla) {
            throw new IllegalStateException("proveedor de mensajeria caido");
        }
        avisos.add(turno);
        acciones.add(accion);
    }
}
