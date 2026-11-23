package ar.edu.turnos.infraestructura.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP de la demo, con el servidor que ya trae el JDK ({@code com.sun.net.httpserver}).
 *
 * <p>No usa Spring Web a proposito: el test de arquitectura {@code ReglaDeOroTest} prohibe que
 * cualquier fuente de produccion nombre el framework, y esa prohibicion es la prueba de la
 * primera regla dura del proyecto. Asi la demo no agrega ni una dependencia al build.
 */
public final class ServidorDemo {

    private final ApiDemo api;
    private final int puerto;
    private HttpServer servidor;

    public ServidorDemo(ApiDemo api, int puerto) {
        this.api = api;
        this.puerto = puerto;
    }

    public void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/", this::pagina);
        servidor.createContext("/api/estado", intercambio -> responderApi(intercambio, api.estado()));
        servidor.createContext("/api/agendar", intercambio ->
                responderApi(intercambio, api.agendar(leerCuerpo(intercambio))));
        servidor.createContext("/api/reiniciar", intercambio -> responderApi(intercambio, api.reiniciar()));
        servidor.setExecutor(Executors.newFixedThreadPool(4));
        servidor.start();
    }

    public void detener() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    public int puerto() {
        return servidor == null ? puerto : servidor.getAddress().getPort();
    }

    private void pagina(HttpExchange intercambio) throws IOException {
        if (!"/".equals(intercambio.getRequestURI().getPath())) {
            responder(intercambio, 404, "text/plain; charset=utf-8", "No encontrado".getBytes(StandardCharsets.UTF_8));
            return;
        }
        try (InputStream in = ServidorDemo.class.getResourceAsStream("/demo/index.html")) {
            if (in == null) {
                responder(intercambio, 500, "text/plain; charset=utf-8",
                        "Falta /demo/index.html".getBytes(StandardCharsets.UTF_8));
                return;
            }
            responder(intercambio, 200, "text/html; charset=utf-8", in.readAllBytes());
        }
    }

    private static void responderApi(HttpExchange intercambio, ApiDemo.Respuesta respuesta) throws IOException {
        responder(intercambio, respuesta.codigoHttp(), "application/json; charset=utf-8",
                respuesta.cuerpo().getBytes(StandardCharsets.UTF_8));
    }

    private static String leerCuerpo(HttpExchange intercambio) throws IOException {
        try (InputStream in = intercambio.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void responder(HttpExchange intercambio, int codigo, String tipo, byte[] cuerpo)
            throws IOException {
        intercambio.getResponseHeaders().set("Content-Type", tipo);
        intercambio.getResponseHeaders().set("Cache-Control", "no-store");
        intercambio.sendResponseHeaders(codigo, cuerpo.length);
        try (OutputStream out = intercambio.getResponseBody()) {
            out.write(cuerpo);
        }
    }
}
