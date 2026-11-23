package ar.edu.turnos.infraestructura.demo;

import ar.edu.turnos.aplicacion.casouso.AgendarTurno;
import ar.edu.turnos.dominio.servicio.OcupacionPorTurnos;
import ar.edu.turnos.dominio.servicio.ServicioDisponibilidad;
import ar.edu.turnos.infraestructura.memoria.CatalogoEnMemoria;
import ar.edu.turnos.infraestructura.memoria.NotificadorRegistrado;
import ar.edu.turnos.infraestructura.memoria.RegistroAuditoriaEnMemoria;
import ar.edu.turnos.infraestructura.memoria.TurnoRepositorioEnMemoria;
import java.time.Clock;

/**
 * Composition root de la demo: arma los adaptadores en memoria y les enchufa los casos de uso
 * del nucleo. Es el unico lugar donde se decide QUE implementacion recibe cada puerto.
 *
 * <p>El dominio no sabe que existe esta clase: por eso agregar una pantalla no obligo a tocar
 * ni una linea de dominio ni de aplicacion.
 */
public final class ContextoDemo {

    public final TurnoRepositorioEnMemoria turnos = new TurnoRepositorioEnMemoria();
    public final CatalogoEnMemoria catalogo = new CatalogoEnMemoria();
    public final RegistroAuditoriaEnMemoria auditoria = new RegistroAuditoriaEnMemoria();
    public final NotificadorRegistrado notificador = new NotificadorRegistrado();
    public final ServicioDisponibilidad disponibilidad =
            new ServicioDisponibilidad(new OcupacionPorTurnos(turnos));
    public final Clock reloj = Clock.systemDefaultZone();

    public final AgendarTurno agendarTurno = new AgendarTurno(
            turnos, catalogo.pacientes, catalogo.profesionales, catalogo.sillones,
            catalogo.prestaciones, disponibilidad, auditoria, notificador, reloj);

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
        SeedDemo.turnos().forEach(turnos::guardar);
    }
}
