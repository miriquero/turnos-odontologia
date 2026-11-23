package ar.edu.turnos.infraestructura.memoria;

import ar.edu.turnos.dominio.modelo.AccionAuditoria;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.NotificadorPaciente;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Notificador simulado: no sale a la red y deja el aviso en una bitacora que la demo muestra
 * en pantalla. El mensaje solo lleva fecha, hora y prestacion, nunca datos clinicos (RN-NO-04).
 */
public final class NotificadorRegistrado implements NotificadorPaciente {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final int MAXIMO = 20;

    private final Deque<String> avisos = new ArrayDeque<>();

    @Override
    public synchronized void notificar(Turno turno, AccionAuditoria accion) {
        String texto = switch (accion) {
            case CREADO -> "Turno confirmado para el " + turno.intervalo().inicio().format(FORMATO);
            case REPROGRAMADO -> "Tu turno paso al " + turno.intervalo().inicio().format(FORMATO);
            case CANCELADO -> "Tu turno del " + turno.intervalo().inicio().format(FORMATO) + " fue cancelado";
            case SOBRETURNO_AUTORIZADO -> "Sobreturno confirmado para el "
                    + turno.intervalo().inicio().format(FORMATO);
            default -> accion + " sobre el turno del " + turno.intervalo().inicio().format(FORMATO);
        };
        avisos.addFirst(texto);
        while (avisos.size() > MAXIMO) {
            avisos.removeLast();
        }
    }

    public synchronized List<String> ultimos() {
        return List.copyOf(avisos);
    }

    public synchronized void vaciar() {
        avisos.clear();
    }
}
