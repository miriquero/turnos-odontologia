package ar.edu.turnos.dominio.servicio;

/** Veredicto como dato: disponible, o no disponible con el codigo de la regla incumplida. */
public record ResultadoDisponibilidad(boolean disponible, String codigoRegla) {

    public static ResultadoDisponibilidad libre() {
        return new ResultadoDisponibilidad(true, null);
    }

    public static ResultadoDisponibilidad ocupado(String codigoRegla) {
        return new ResultadoDisponibilidad(false, codigoRegla);
    }
}
