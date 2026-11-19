package ar.edu.turnos.dominio.modelo;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class TurnoTest {

    private static final Paciente PACIENTE = new Paciente("PA1", "Paciente Ficticio Uno", "00000001", true);
    private static final Profesional P1 = new Profesional("P1", "Dra. Ficticia Uno", true);
    private static final Sillon S1 = new Sillon("S1", "Sillon 1", true);

    @Test
    void derivaElFinDeLaPrestacion() {
        Turno t = Turno.agendar(PACIENTE, P1, S1, new Prestacion("PR1", "Limpieza", 30, true), hora("10:00"));
        assertThat(t.intervalo()).isEqualTo(intervalo("10:00", "10:30"));
    }

    @Test
    void derivaElFinDeUnaPrestacionLarga() {
        Turno t = Turno.agendar(PACIENTE, P1, S1, new Prestacion("PR2", "Conducto", 90, true), hora("10:00"));
        assertThat(t.intervalo()).isEqualTo(intervalo("10:00", "11:30"));
    }

    @Test
    void naceAgendadoYNoEsSobreturno() {
        Turno t = Turno.agendar(PACIENTE, P1, S1, new Prestacion("PR1", "Limpieza", 30, true), hora("10:00"));
        assertThat(t.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(t.esSobreturno()).isFalse();
        assertThat(t.id()).isNotBlank();
        assertThat(t.profesionalId()).isEqualTo("P1");
        assertThat(t.sillonId()).isEqualTo("S1");
        assertThat(t.pacienteId()).isEqualTo("PA1");
    }

    @Test
    void conservaLaDuracionVigenteAlAgendarse() {
        Turno t = Turno.agendar(PACIENTE, P1, S1, new Prestacion("PR1", "Limpieza", 30, true), hora("10:00"));
        Prestacion limpiezaNueva = new Prestacion("PR1", "Limpieza", 45, true); // cambia el catalogo
        assertThat(limpiezaNueva.duracionMinutos()).isEqualTo(45);
        assertThat(t.intervalo()).isEqualTo(intervalo("10:00", "10:30"));
        assertThat(t.duracionMinutos()).isEqualTo(30);
    }

    @Test
    void ningunaViaPublicaPermiteFijarElFin() {
        assertThat(Turno.class.getConstructors()).isEmpty();
        // Toda fabrica estatica publica acepta a lo sumo un dato temporal (el inicio) y ningun Intervalo.
        Method[] fabricas = Arrays.stream(Turno.class.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers())).toArray(Method[]::new);
        assertThat(fabricas).isNotEmpty();
        for (Method m : fabricas) {
            assertThat(Arrays.stream(m.getParameterTypes()).filter(LocalDateTime.class::equals).count())
                    .as(m.getName()).isLessThanOrEqualTo(1);
            assertThat(m.getParameterTypes()).as(m.getName()).doesNotContain(Intervalo.class);
        }
        assertThat(Arrays.stream(Turno.class.getMethods()).map(Method::getName))
                .noneMatch(n -> n.toLowerCase().startsWith("set") || n.toLowerCase().contains("fijarfin"));
    }
}
