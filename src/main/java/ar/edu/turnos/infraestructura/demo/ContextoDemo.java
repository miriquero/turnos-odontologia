package ar.edu.turnos.infraestructura.demo;

import ar.edu.turnos.aplicacion.casouso.AgendarSobreturno;
import ar.edu.turnos.aplicacion.casouso.AgendarTurno;
import ar.edu.turnos.aplicacion.casouso.CancelarTurno;
import ar.edu.turnos.aplicacion.casouso.MarcarTurnoAtendido;
import ar.edu.turnos.aplicacion.casouso.RegistrarAusencia;
import ar.edu.turnos.aplicacion.casouso.ReprogramarTurno;
import ar.edu.turnos.dominio.servicio.OcupacionPorTurnos;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import ar.edu.turnos.infraestructura.memoria.AutorizacionSobreturnoEnMemoria;
import ar.edu.turnos.infraestructura.memoria.CatalogoEnMemoria;
import ar.edu.turnos.infraestructura.memoria.NotificadorRegistrado;
import ar.edu.turnos.infraestructura.memoria.RegistroAuditoriaEnMemoria;
import ar.edu.turnos.infraestructura.memoria.RegistroDetalleReprogramacionEnMemoria;
import ar.edu.turnos.infraestructura.memoria.TurnoRepositorioEnMemoria;
import java.time.Clock;

/**
 * Composition root de la demo: arma los adaptadores en memoria y les enchufa los seis casos de
 * uso del nucleo. Es el unico lugar donde se decide QUE implementacion recibe cada puerto.
 *
 * <p>El dominio no sabe que esta clase existe. Agregar esta pantalla no obligo a tocar ni una
 * linea de {@code dominio} ni de {@code aplicacion}: los puertos ya estaban.
 */
public final class ContextoDemo {

    /**
     * Anticipacion minima para cancelar, en horas (RN-AG-05). El valor es configurable y su
     * valor por defecto es un supuesto sin resolver (SU-03, PA-04). La demo usa 2 horas en
     * lugar de las 24 del caso de uso para que la regla se pueda ejercitar sobre la agenda del
     * dia; la pantalla lo muestra para que quede claro que es una decision de configuracion.
     */
    public static final int HORAS_MINIMAS_PARA_CANCELAR = 2;

    public final TurnoRepositorioEnMemoria turnos = new TurnoRepositorioEnMemoria();
    public final CatalogoEnMemoria catalogo = new CatalogoEnMemoria();
    public final RegistroAuditoriaEnMemoria auditoria = new RegistroAuditoriaEnMemoria();
    public final NotificadorRegistrado notificador = new NotificadorRegistrado();
    public final AutorizacionSobreturnoEnMemoria autorizaciones = new AutorizacionSobreturnoEnMemoria();
    public final RegistroDetalleReprogramacionEnMemoria detalles = new RegistroDetalleReprogramacionEnMemoria();
    public final ServicioDisponibilidad disponibilidad =
            new ServicioDisponibilidad(new OcupacionPorTurnos(turnos));
    public final Clock reloj = Clock.systemDefaultZone();

    public final AgendarTurno agendarTurno = new AgendarTurno(
            turnos, catalogo.pacientes, catalogo.profesionales, catalogo.sillones,
            catalogo.prestaciones, disponibilidad, auditoria, notificador, reloj);

    public final ReprogramarTurno reprogramarTurno = new ReprogramarTurno(
            turnos, catalogo.profesionales, catalogo.sillones, catalogo.prestaciones,
            disponibilidad, auditoria, detalles, notificador, reloj);

    public final CancelarTurno cancelarTurno =
            new CancelarTurno(turnos, auditoria, notificador, reloj, HORAS_MINIMAS_PARA_CANCELAR);

    public final AgendarSobreturno agendarSobreturno = new AgendarSobreturno(
            turnos, catalogo.pacientes, catalogo.profesionales, catalogo.sillones,
            catalogo.prestaciones, autorizaciones, auditoria, notificador, reloj);

    public final RegistrarAusencia registrarAusencia = new RegistrarAusencia(turnos, auditoria, reloj);

    public final MarcarTurnoAtendido marcarAtendido = new MarcarTurnoAtendido(turnos, auditoria, reloj);

    public ContextoDemo() {
        catalogo.pacientes.cargar(SeedDemo.pacientes());
        catalogo.profesionales.cargar(SeedDemo.profesionales());
        catalogo.sillones.cargar(SeedDemo.sillones());
        catalogo.prestaciones.cargar(SeedDemo.prestaciones());
        reiniciarAgenda();
    }

    /** Vuelve la agenda al estado inicial, sin tocar el catalogo. */
    public void reiniciarAgenda() {
        turnos.vaciar();
        auditoria.vaciar();
        notificador.vaciar();
        autorizaciones.vaciar();
        detalles.vaciar();
        SeedDemo.turnos(reloj).forEach(turnos::guardar);
        autorizaciones.guardar(SeedDemo.autorizacionDelSobreturno(reloj));
    }
}
