package ar.edu.turnos.dominio.excepcion;

/** Violacion de una regla de negocio; expone el codigo de regla (p. ej. RN-AG-02) de forma programatica. */
public class ReglaDeNegocioViolada extends RuntimeException {

    public static final String RN_AG_02 = "RN-AG-02";
    public static final String RN_AG_03 = "RN-AG-03";
    public static final String RN_AG_08 = "RN-AG-08";
    public static final String RN_TU_02 = "RN-TU-02";
    public static final String RN_AU_01 = "RN-AU-01";

    private final String codigo;

    public ReglaDeNegocioViolada(String codigo, String mensaje) {
        super(codigo + ": " + mensaje);
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo de regla es obligatorio");
        }
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
