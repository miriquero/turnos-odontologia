package ar.edu.turnos.infraestructura.demo;

import ar.edu.turnos.aplicacion.casouso.SolicitudAgendarTurno;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Turno;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API de la demo. Traduce pedidos HTTP a llamadas a los casos de uso y errores de negocio a
 * codigos de regla. No contiene ni una regla: cuando rechaza algo, el que rechazo fue el nucleo.
 */
public final class ApiDemo {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final ContextoDemo contexto;

    public ApiDemo(ContextoDemo contexto) {
        this.contexto = contexto;
    }

    /** Respuesta de la API: codigo HTTP y cuerpo JSON. */
    public record Respuesta(int codigoHttp, String cuerpo) {}

    public Respuesta estado() {
        return new Respuesta(200, estadoJson(null, null, null));
    }

    public Respuesta reiniciar() {
        contexto.reiniciarAgenda();
        return new Respuesta(200, estadoJson("Agenda reiniciada", null, null));
    }

    public Respuesta agendar(String cuerpo) {
        Map<String, String> f = formulario(cuerpo);
        try {
            LocalTime hora = LocalTime.parse(f.getOrDefault("hora", ""));
            LocalDateTime inicio = LocalDateTime.of(SeedDemo.fechaDeAgenda(), hora);
            Turno t = contexto.agendarTurno.ejecutar(new SolicitudAgendarTurno(
                    f.get("pacienteId"), f.get("profesionalId"), f.get("sillonId"),
                    f.get("prestacionId"), inicio, "recepcion.ficticia"));
            String aviso = "Turno agendado " + t.intervalo().inicio().format(HORA)
                    + " a " + t.intervalo().fin().format(HORA);
            return new Respuesta(200, estadoJson(aviso, null, null));
        } catch (ReglaDeNegocioViolada e) {
            return new Respuesta(200, estadoJson(null, e.codigo(), e.getMessage()));
        } catch (RuntimeException e) {
            return new Respuesta(200, estadoJson(null, "ENTRADA", "Revisa los datos: " + e.getMessage()));
        }
    }

    private String estadoJson(String aviso, String codigoRechazo, String mensajeRechazo) {
        return Json.objeto(
                "fecha", Json.texto(SeedDemo.fechaDeAgenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                "ahora", Json.texto(LocalDateTime.now(contexto.reloj).format(FECHA_HORA)),
                "aviso", Json.texto(aviso),
                "codigoRechazo", Json.texto(codigoRechazo),
                "mensajeRechazo", Json.texto(mensajeRechazo),
                "profesionales", Json.lista(contexto.catalogo.profesionales.todos().stream()
                        .map(p -> Json.objeto("id", Json.texto(p.id()), "nombre", Json.texto(p.nombre()))).toList()),
                "sillones", Json.lista(contexto.catalogo.sillones.todos().stream()
                        .map(s -> Json.objeto("id", Json.texto(s.id()), "nombre", Json.texto(s.nombre()))).toList()),
                "pacientes", Json.lista(contexto.catalogo.pacientes.todos().stream()
                        .map(p -> Json.objeto("id", Json.texto(p.id()), "nombre", Json.texto(p.nombre()))).toList()),
                "prestaciones", Json.lista(contexto.catalogo.prestaciones.todos().stream()
                        .map(p -> Json.objeto("id", Json.texto(p.id()), "nombre", Json.texto(p.nombre()),
                                "duracion", String.valueOf(p.duracionMinutos()))).toList()),
                "turnos", Json.lista(contexto.turnos.todos().stream().map(this::turnoJson).toList()),
                "auditoria", Json.lista(contexto.auditoria.todos().stream().map(this::eventoJson).toList()),
                "avisos", Json.listaDeTextos(contexto.notificador.ultimos()));
    }

    private String turnoJson(Turno t) {
        return Json.objeto(
                "id", Json.texto(t.id()),
                "inicio", Json.texto(t.intervalo().inicio().format(HORA)),
                "fin", Json.texto(t.intervalo().fin().format(HORA)),
                "paciente", Json.texto(nombre(contexto.catalogo.pacientes.buscarPorId(t.pacienteId())
                        .map(p -> p.nombre()), t.pacienteId())),
                "profesionalId", Json.texto(t.profesionalId()),
                "profesional", Json.texto(nombre(contexto.catalogo.profesionales.buscarPorId(t.profesionalId())
                        .map(p -> p.nombre()), t.profesionalId())),
                "sillonId", Json.texto(t.sillonId()),
                "sillon", Json.texto(nombre(contexto.catalogo.sillones.buscarPorId(t.sillonId())
                        .map(s -> s.nombre()), t.sillonId())),
                "prestacion", Json.texto(nombre(contexto.catalogo.prestaciones.buscarPorId(t.prestacionId())
                        .map(p -> p.nombre()), t.prestacionId())),
                "estado", Json.texto(t.estado().name()),
                "ocupa", String.valueOf(t.estado().ocupaAgenda()),
                "esSobreturno", String.valueOf(t.esSobreturno()));
    }

    private String eventoJson(EventoAuditoria e) {
        return Json.objeto(
                "turnoId", Json.texto(e.turnoId()),
                "accion", Json.texto(e.accion().name()),
                "usuario", Json.texto(e.usuario()),
                "momento", Json.texto(e.timestamp().format(FECHA_HORA)));
    }

    private static String nombre(java.util.Optional<String> encontrado, String idPorDefecto) {
        return encontrado.orElse(idPorDefecto);
    }

    /** Decodifica un cuerpo application/x-www-form-urlencoded. */
    static Map<String, String> formulario(String cuerpo) {
        Map<String, String> campos = new LinkedHashMap<>();
        if (cuerpo == null || cuerpo.isBlank()) {
            return campos;
        }
        for (String par : cuerpo.split("&")) {
            int i = par.indexOf('=');
            if (i > 0) {
                campos.put(
                        URLDecoder.decode(par.substring(0, i), StandardCharsets.UTF_8),
                        URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        return campos;
    }
}
