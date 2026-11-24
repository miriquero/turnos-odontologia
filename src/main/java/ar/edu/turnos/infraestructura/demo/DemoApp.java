package ar.edu.turnos.infraestructura.demo;

import java.io.IOException;

/**
 * Punto de entrada de la DEMO. No es la aplicacion del MVP: el Discovery decidio que la v1 no
 * tiene interfaz grafica, y esto es un adaptador de demostracion sobre el mismo nucleo, pensado
 * para poder ver y mostrar las reglas de agenda funcionando.
 *
 * <p>Levantarla con: {@code ./gradlew demo}
 */
public final class DemoApp {

    private static final int PUERTO_POR_DEFECTO = 8080;

    private DemoApp() {}

    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : PUERTO_POR_DEFECTO;
        ContextoDemo contexto = new ContextoDemo();
        ServidorDemo servidor = new ServidorDemo(new ApiDemo(contexto), puerto);
        servidor.iniciar();
        System.out.println();
        System.out.println("  Agenda de turnos andando en  http://localhost:" + servidor.puerto());
        System.out.println("  Agenda de hoy: de "
                + SeedDemo.inicioDeGrilla(contexto.reloj, SeedDemo.hoy(contexto.reloj)).toLocalTime()
                + " a " + SeedDemo.finDeGrilla(contexto.reloj, SeedDemo.hoy(contexto.reloj)).toLocalTime()
                + " (anclada a la hora actual). Los otros dias se muestran completos.");
        System.out.println("  Ctrl+C para cortar.");
        System.out.println();
    }
}
