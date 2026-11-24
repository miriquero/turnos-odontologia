package ar.edu.turnos.infraestructura.demo;

import ar.edu.turnos.aplicacion.casouso.RolAutorizador;
import ar.edu.turnos.aplicacion.casouso.SolicitudAgendarSobreturno;
import ar.edu.turnos.aplicacion.casouso.SolicitudAgendarTurno;
import ar.edu.turnos.aplicacion.casouso.SolicitudCancelarTurno;
import ar.edu.turnos.aplicacion.casouso.SolicitudMarcarAtendido;
import ar.edu.turnos.aplicacion.casouso.SolicitudRegistrarAusencia;
import ar.edu.turnos.aplicacion.casouso.SolicitudReprogramarTurno;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.AutorizacionSobreturno;
import ar.edu.turnos.dominio.modelo.DetalleReprogramacion;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.EventoAuditoria;
import ar.edu.turnos.dominio.modelo.Intervalo;
import ar.edu.turnos.dominio.modelo.SnapshotTurno;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.servicio.ResultadoDisponibilidad;
import ar.edu.turnos.dominio.servicio.SolicitudDisponibilidad;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * API de la demo. Traduce pedidos HTTP a llamadas a los casos de uso, y los codigos de regla a
 * frases que diria un sistema real.
 *
 * <p>No contiene ni una regla de negocio: cuando algo se rechaza, el que rechazo fue el nucleo.
 * Esta clase solo decide como contarlo.
 *
 * <p>Todo lo que lee sale de los puertos que ya existen. No implementa casos de uso que no
 * esten especificados: administrar el catalogo es C-06, consultar huecos libres es C-07 y el
 * historial de turno es C-08, y los tres siguen pendientes. Las pantallas de catalogo y de
 * auditoria son <b>solo lectura</b> por esa razon.
 */
public final class ApiDemo {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Locale fijo: el nombre del dia no depende de como este configurada la maquina. */
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es-AR"));
    private static final String USUARIO = "recepcion";

    private final ContextoDemo c;

    public ApiDemo(ContextoDemo contexto) {
        this.c = contexto;
    }

    public record Respuesta(int codigoHttp, String cuerpo) {}

    // ---------------------------------------------------------------- operaciones

    public Respuesta estado(String cuerpo) {
        return ok(null, null, null, fechaDe(cuerpo));
    }

    public Respuesta reiniciar(String cuerpo) {
        c.reiniciarAgenda();
        return ok("Agenda restablecida.", null, null, fechaDe(cuerpo));
    }

    /** La fecha que pide la pantalla; si no manda ninguna, hoy. */
    private LocalDate fechaDe(String cuerpo) {
        String f = formulario(cuerpo).get("fecha");
        try {
            return f == null || f.isBlank() ? SeedDemo.hoy(c.reloj) : LocalDate.parse(f);
        } catch (RuntimeException e) {
            return SeedDemo.hoy(c.reloj);
        }
    }

    public Respuesta agendar(String cuerpo) {
        Map<String, String> f = formulario(cuerpo);
        return ejecutar(fechaDe(cuerpo), () -> {
            Turno t = c.agendarTurno.ejecutar(new SolicitudAgendarTurno(
                    f.get("pacienteId"), f.get("profesionalId"), f.get("sillonId"),
                    f.get("prestacionId"), momento(f.get("inicio")), USUARIO));
            return "Turno agendado " + franja(t) + ".";
        });
    }

    public Respuesta sobreturno(String cuerpo) {
        Map<String, String> f = formulario(cuerpo);
        return ejecutar(fechaDe(cuerpo), () -> {
            Turno t = c.agendarSobreturno.ejecutar(new SolicitudAgendarSobreturno(
                    f.get("pacienteId"), f.get("profesionalId"), f.get("sillonId"),
                    f.get("prestacionId"), momento(f.get("inicio")), true,
                    f.get("autorizadoPor"), RolAutorizador.ODONTOLOGO_DUENIO, f.get("motivo")));
            return "Sobreturno autorizado " + franja(t) + ".";
        });
    }

    public Respuesta reprogramar(String cuerpo) {
        Map<String, String> f = formulario(cuerpo);
        return ejecutar(fechaDe(cuerpo), () -> {
            Turno t = c.reprogramarTurno.ejecutar(new SolicitudReprogramarTurno(
                    f.get("turnoId"), momento(f.get("inicio")), vacioANulo(f.get("profesionalId")),
                    vacioANulo(f.get("sillonId")), vacioANulo(f.get("prestacionId")), USUARIO));
            return "Turno reprogramado: ahora es " + franja(t) + ".";
        });
    }

    public Respuesta cancelar(String cuerpo) {
        Map<String, String> f = formulario(cuerpo);
        return ejecutar(fechaDe(cuerpo), () -> {
            Turno t = c.cancelarTurno.ejecutar(
                    new SolicitudCancelarTurno(f.get("turnoId"), f.get("motivo"), USUARIO));
            return "Turno cancelado. La franja " + franja(t) + " queda libre.";
        });
    }

    public Respuesta atendido(String cuerpo) {
        Map<String, String> f = formulario(cuerpo);
        return ejecutar(fechaDe(cuerpo), () -> {
            Turno t = c.marcarAtendido.ejecutar(new SolicitudMarcarAtendido(f.get("turnoId"), USUARIO));
            return "Atencion registrada: " + nombrePaciente(t) + ".";
        });
    }

    public Respuesta ausente(String cuerpo) {
        Map<String, String> f = formulario(cuerpo);
        return ejecutar(fechaDe(cuerpo), () -> {
            Turno t = c.registrarAusencia.ejecutar(
                    new SolicitudRegistrarAusencia(f.get("turnoId"), f.get("motivo"), USUARIO));
            return "Ausencia registrada. La franja " + franja(t) + " queda libre.";
        });
    }

    /**
     * Consulta de disponibilidad: pregunta al motor del dominio si una franja esta libre para un
     * profesional y un sillon, y devuelve el veredicto con la regla que lo explica. No modifica
     * nada: es {@code ServicioDisponibilidad.evaluar} en crudo.
     */
    public Respuesta disponibilidad(String cuerpo) {
        Map<String, String> f = formulario(cuerpo);
        try {
            LocalDateTime inicio = momento(f.get("inicio"));
            int duracion = c.catalogo.prestaciones.buscarPorId(f.get("prestacionId"))
                    .map(p -> p.duracionMinutos()).orElse(30);
            Intervalo franja = new Intervalo(inicio, inicio.plusMinutes(duracion));
            ResultadoDisponibilidad r = c.disponibilidad.evaluar(new SolicitudDisponibilidad(
                    f.get("profesionalId"), f.get("sillonId"), franja, vacioANulo(f.get("excluirTurnoId"))));
            String consulta = Json.objeto(
                    "inicio", Json.texto(franja.inicio().format(HORA)),
                    "fin", Json.texto(franja.fin().format(HORA)),
                    "duracion", String.valueOf(duracion),
                    "profesional", Json.texto(nombreProfesional(f.get("profesionalId"))),
                    "sillon", Json.texto(nombreSillon(f.get("sillonId"))),
                    "disponible", String.valueOf(r.disponible()),
                    "codigoRegla", Json.texto(r.codigoRegla()),
                    "explicacion", Json.texto(r.disponible()
                            ? "La franja esta libre para ese profesional y ese sillon."
                            : explicarCodigo(r.codigoRegla(), "")));
            return new Respuesta(200, estadoJson(null, null, null, consulta, fechaDe(cuerpo)));
        } catch (RuntimeException e) {
            return ok(null, null, "No se pudo evaluar la franja. Revisa los datos.", fechaDe(cuerpo));
        }
    }

    // ---------------------------------------------------------------- plomeria

    private interface Operacion {
        String ejecutar();
    }

    private Respuesta ejecutar(LocalDate fecha, Operacion op) {
        try {
            return ok(op.ejecutar(), null, null, fecha);
        } catch (ReglaDeNegocioViolada e) {
            return ok(null, e.codigo(), explicarCodigo(e.codigo(), e.getMessage()), fecha);
        } catch (RuntimeException e) {
            return ok(null, null, "No se pudo completar la operacion. Revisa los datos cargados.", fecha);
        }
    }

    /**
     * Convierte el codigo de regla en la frase que diria un sistema real. El codigo igual viaja
     * aparte: la pantalla lo muestra como referencia, porque es lo que permite rastrear la
     * decision hasta la regla de negocio y hasta el test que la cubre.
     */
    private String explicarCodigo(String codigo, String mensaje) {
        return switch (codigo == null ? "" : codigo) {
            case "RN-AG-02" -> "El profesional ya tiene otro turno en esa franja. "
                    + "Para pisarlo hace falta autorizar un sobreturno.";
            case "RN-AG-03" -> "El sillon ya esta ocupado en esa franja. "
                    + "Proba con otro sillon o autoriza un sobreturno.";
            case "RN-AG-05" -> "Faltan menos de " + ContextoDemo.HORAS_MINIMAS_PARA_CANCELAR
                    + " horas para el turno: ya no se puede cancelar.";
            case "RN-AG-06" -> "El sobreturno necesita autorizacion del odontologo duenio y un motivo.";
            case "RN-AG-08" -> mensaje.contains("futuro")
                    ? "Ese horario ya paso. Un turno solo se agenda hacia adelante."
                    : "Falta un dato del turno o el recurso elegido no esta activo.";
            case "RN-TU-02" -> "Ese turno ya esta cerrado: no admite mas cambios.";
            case "RN-TU-04" -> "Solo se puede reprogramar un turno agendado.";
            case "RN-TU-05" -> "Hace falta indicar un motivo de cancelacion.";
            case "RN-AU-01" -> "No se pudo identificar al usuario responsable.";
            default -> mensaje;
        };
    }

    private Respuesta ok(String aviso, String codigo, String mensaje, LocalDate fecha) {
        return new Respuesta(200, estadoJson(aviso, codigo, mensaje, "null", fecha));
    }

    private LocalDateTime momento(String iso) {
        return LocalDateTime.parse(iso);
    }

    private static String vacioANulo(String v) {
        return v == null || v.isBlank() ? null : v;
    }

    private String franja(Turno t) {
        return "de " + t.intervalo().inicio().format(HORA) + " a " + t.intervalo().fin().format(HORA);
    }

    private String nombrePaciente(Turno t) {
        return c.catalogo.pacientes.buscarPorId(t.pacienteId()).map(p -> p.nombre()).orElse(t.pacienteId());
    }

    private String nombreProfesional(String id) {
        return c.catalogo.profesionales.buscarPorId(id).map(p -> p.nombre()).orElse(id);
    }

    private String nombreSillon(String id) {
        return c.catalogo.sillones.buscarPorId(id).map(s -> s.nombre()).orElse(id);
    }

    private String nombrePrestacion(String id) {
        return c.catalogo.prestaciones.buscarPorId(id).map(p -> p.nombre()).orElse(id);
    }

    // ---------------------------------------------------------------- serializacion

    private String estadoJson(String aviso, String codigoRechazo, String mensajeRechazo,
            String consulta, LocalDate fecha) {
        LocalDateTime desde = SeedDemo.inicioDeGrilla(c.reloj, fecha);
        LocalDateTime hasta = SeedDemo.finDeGrilla(c.reloj, fecha);
        LocalDateTime ahora = LocalDateTime.now(c.reloj);
        return Json.objeto(
                "fecha", Json.texto(desde.format(FECHA)),
                "fechaIso", Json.texto(fecha.toString()),
                "hoyIso", Json.texto(SeedDemo.hoy(c.reloj).toString()),
                "esHoy", String.valueOf(SeedDemo.esHoy(c.reloj, fecha)),
                "diaAnterior", Json.texto(fecha.minusDays(1).toString()),
                "diaSiguiente", Json.texto(fecha.plusDays(1).toString()),
                "ahora", Json.texto(ahora.format(HORA)),
                "ahoraFila", String.valueOf(fila(desde, ahora)),
                "desde", Json.texto(desde.format(HORA)),
                "filas", String.valueOf(fila(desde, hasta)),
                "minutosPorFila", String.valueOf(SeedDemo.MINUTOS_POR_FILA),
                "horasParaCancelar", String.valueOf(ContextoDemo.HORAS_MINIMAS_PARA_CANCELAR),
                "aviso", Json.texto(aviso),
                "codigoRechazo", Json.texto(codigoRechazo),
                "mensajeRechazo", Json.texto(mensajeRechazo),
                "consulta", consulta,
                "horarios", Json.lista(horariosJson(desde, hasta)),
                "profesionales", Json.lista(c.catalogo.profesionales.todos().stream()
                        .map(p -> Json.objeto("id", Json.texto(p.id()), "nombre", Json.texto(p.nombre()),
                                "activo", String.valueOf(p.activo()))).toList()),
                "sillones", Json.lista(c.catalogo.sillones.todos().stream()
                        .map(s -> Json.objeto("id", Json.texto(s.id()), "nombre", Json.texto(s.nombre()),
                                "activo", String.valueOf(s.activo()))).toList()),
                "pacientes", Json.lista(c.catalogo.pacientes.todos().stream()
                        .map(p -> Json.objeto("id", Json.texto(p.id()), "nombre", Json.texto(p.nombre()),
                                "documento", Json.texto(p.documento()),
                                "activo", String.valueOf(p.activo()))).toList()),
                "prestaciones", Json.lista(c.catalogo.prestaciones.todos().stream()
                        .map(p -> Json.objeto("id", Json.texto(p.id()), "nombre", Json.texto(p.nombre()),
                                "duracion", String.valueOf(p.duracionMinutos()),
                                "activo", String.valueOf(p.activa()))).toList()),
                "turnos", Json.lista(c.turnos.todos().stream().map(t -> turnoJson(t, desde)).toList()),
                "auditoria", Json.lista(c.auditoria.todos().stream().map(this::eventoJson).toList()),
                "avisos", Json.listaDeTextos(c.notificador.ultimos()));
    }

    private List<String> horariosJson(LocalDateTime desde, LocalDateTime hasta) {
        List<String> filas = new ArrayList<>();
        for (LocalDateTime t = desde; t.isBefore(hasta); t = t.plusMinutes(SeedDemo.MINUTOS_POR_FILA)) {
            filas.add(Json.objeto(
                    "hora", Json.texto(t.format(HORA)),
                    "iso", Json.texto(t.toString()),
                    "enPunto", String.valueOf(t.getMinute() == 0),
                    "pasado", String.valueOf(t.isBefore(LocalDateTime.now(c.reloj)))));
        }
        return filas;
    }

    private String turnoJson(Turno t, LocalDateTime desde) {
        Optional<AutorizacionSobreturno> aut = c.autorizaciones.buscarPorTurnoId(t.id());
        return Json.objeto(
                "id", Json.texto(t.id()),
                "inicio", Json.texto(t.intervalo().inicio().format(HORA)),
                "fin", Json.texto(t.intervalo().fin().format(HORA)),
                "inicioIso", Json.texto(t.intervalo().inicio().toString()),
                "fecha", Json.texto(t.intervalo().inicio().toLocalDate().toString()),
                "fila", String.valueOf(fila(desde, t.intervalo().inicio())),
                "alto", String.valueOf(Math.max(1, t.duracionMinutos() / SeedDemo.MINUTOS_POR_FILA)),
                "duracion", String.valueOf(t.duracionMinutos()),
                "pacienteId", Json.texto(t.pacienteId()),
                "paciente", Json.texto(nombrePaciente(t)),
                "profesionalId", Json.texto(t.profesionalId()),
                "profesional", Json.texto(nombreProfesional(t.profesionalId())),
                "sillonId", Json.texto(t.sillonId()),
                "sillon", Json.texto(nombreSillon(t.sillonId())),
                "prestacionId", Json.texto(t.prestacionId()),
                "prestacion", Json.texto(nombrePrestacion(t.prestacionId())),
                "estado", Json.texto(t.estado().name()),
                "ocupa", String.valueOf(t.estado().ocupaAgenda()),
                "abierto", String.valueOf(t.estado() == EstadoTurno.AGENDADO),
                "esSobreturno", String.valueOf(t.esSobreturno()),
                "motivoAusencia", Json.texto(t.motivoAusencia()),
                "motivoCancelacion", Json.texto(t.motivoCancelacion()),
                "autorizadoPor", Json.texto(aut.map(a -> a.autorizadoPor()).orElse(null)),
                "motivoSobreturno", Json.texto(aut.map(a -> a.motivo()).orElse(null)),
                "autorizadoEn", Json.texto(aut.map(a -> a.autorizadoEn().format(HORA)).orElse(null)),
                "eventos", Json.lista(c.auditoria.listarPorTurno(t.id()).stream()
                        .map(this::eventoJson).toList()),
                "detalles", Json.lista(c.detalles.listarPorTurno(t.id()).stream()
                        .map(this::detalleJson).toList()));
    }

    /** El antes y el despues de una reprogramacion (RN-AU-03), que el evento de auditoria no lleva. */
    private String detalleJson(DetalleReprogramacion d) {
        return Json.objeto(
                "momento", Json.texto(d.timestamp().format(HORA)),
                "usuario", Json.texto(d.usuario()),
                "anterior", snapshotJson(d.anterior()),
                "nuevo", snapshotJson(d.nuevo()));
    }

    private String snapshotJson(SnapshotTurno s) {
        return Json.objeto(
                "franja", Json.texto(s.inicio().format(HORA) + "–" + s.fin().format(HORA)),
                "profesional", Json.texto(nombreProfesional(s.profesionalId())),
                "sillon", Json.texto(nombreSillon(s.sillonId())),
                "prestacion", Json.texto(nombrePrestacion(s.prestacionId())));
    }

    private String eventoJson(EventoAuditoria e) {
        return Json.objeto(
                "turnoId", Json.texto(e.turnoId()),
                "accion", Json.texto(e.accion().name()),
                "usuario", Json.texto(e.usuario()),
                "momento", Json.texto(e.timestamp().format(HORA)));
    }

    private static int fila(LocalDateTime desde, LocalDateTime t) {
        return (int) (Duration.between(desde, t).toMinutes() / SeedDemo.MINUTOS_POR_FILA);
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
