package ar.edu.turnos.infraestructura.demo;

import java.util.List;
import java.util.StringJoiner;

/**
 * Escritor minimo de JSON. La demo no agrega ninguna dependencia al build, asi que serializa
 * a mano en lugar de usar Jackson. Solo escribe: nunca parsea (la pagina manda formularios).
 */
public final class Json {

    private Json() {}

    /** Devuelve el valor como texto JSON entre comillas, o null si el valor es null. */
    public static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    /**
     * Objeto JSON a partir de pares clave/valor. Las claves son texto plano; los valores ya
     * vienen serializados (usar {@link #texto} para los strings).
     */
    public static String objeto(String... clavesYValores) {
        if (clavesYValores.length % 2 != 0) {
            throw new IllegalArgumentException("se esperan pares clave/valor");
        }
        StringJoiner sj = new StringJoiner(",", "{", "}");
        for (int i = 0; i < clavesYValores.length; i += 2) {
            sj.add(texto(clavesYValores[i]) + ":" + clavesYValores[i + 1]);
        }
        return sj.toString();
    }

    /** Lista JSON a partir de valores ya serializados. */
    public static String lista(List<String> valores) {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        valores.forEach(sj::add);
        return sj.toString();
    }

    public static String listaDeTextos(List<String> valores) {
        return lista(valores.stream().map(Json::texto).toList());
    }
}
