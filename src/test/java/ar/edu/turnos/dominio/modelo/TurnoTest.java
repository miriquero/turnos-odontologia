package ar.edu.turnos.dominio.modelo;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Test
    void marcaAusenteDeFormaInmutable() {
        Turno original = agendar();
        Turno cerrado = original.marcarAusente("sin aviso");
        assertThat(original.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(original.motivoAusencia()).isNull();
        assertThat(cerrado).isNotSameAs(original);
        assertThat(cerrado.id()).isEqualTo(original.id());
        assertThat(cerrado.intervalo()).isEqualTo(original.intervalo());
        assertThat(cerrado.estado()).isEqualTo(EstadoTurno.AUSENTE);
        assertThat(cerrado.motivoAusencia()).isEqualTo("sin aviso");
    }

    @Test
    void marcaAtendidoDeFormaInmutable() {
        Turno original = agendar();
        Turno cerrado = original.marcarAtendido();
        assertThat(original.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(cerrado).isNotSameAs(original);
        assertThat(cerrado.id()).isEqualTo(original.id());
        assertThat(cerrado.intervalo()).isEqualTo(original.intervalo());
        assertThat(cerrado.estado()).isEqualTo(EstadoTurno.ATENDIDO);
        assertThat(cerrado.motivoAusencia()).isNull();
    }

    @Test
    void cancelaDeFormaInmutable() {
        Turno original = agendar();
        Turno cerrado = original.cancelar("motivo valido");
        assertThat(original.estado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(original.motivoCancelacion()).isNull();
        assertThat(cerrado).isNotSameAs(original);
        assertThat(cerrado.id()).isEqualTo(original.id());
        assertThat(cerrado.intervalo()).isEqualTo(original.intervalo());
        assertThat(cerrado.estado()).isEqualTo(EstadoTurno.CANCELADO);
        assertThat(cerrado.motivoCancelacion()).isEqualTo("motivo valido");
    }

    @ParameterizedTest
    @EnumSource(value = EstadoTurno.class, names = {"ATENDIDO", "CANCELADO", "AUSENTE"})
    void rechazaCerrarUnEstadoTerminal(EstadoTurno estado) {
        Turno t = reconstruir("T1", estado, null);
        assertThatThrownBy(() -> t.marcarAusente("motivo"))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("RN-TU-02"));
        assertThatThrownBy(t::marcarAtendido)
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("RN-TU-02"));
        assertThatThrownBy(() -> t.cancelar("motivo"))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("RN-TU-02"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", " \t "})
    void normalizaMotivoEnBlancoAausente(String motivo) {
        Turno cerrado = agendar().marcarAusente(motivo);
        assertThat(cerrado.estado()).isEqualTo(EstadoTurno.AUSENTE);
        assertThat(cerrado.motivoAusencia()).isNull();
    }

    @Test
    void recortaLosEspaciosDelMotivo() {
        assertThat(agendar().marcarAusente("  no se presento  ").motivoAusencia())
                .isEqualTo("no se presento");
    }

    @Test
    void reconstruirConMotivoConservaElDatoSinNormalizar() {
        Turno t = reconstruir("T1", EstadoTurno.AUSENTE, "  dato historico  ");
        assertThat(t.motivoAusencia()).isEqualTo("  dato historico  ");
    }

    private static Turno agendar() {
        return Turno.agendar(PACIENTE, P1, S1, new Prestacion("PR1", "Limpieza", 30, true), hora("10:00"));
    }

    private static Turno reconstruir(String id, EstadoTurno estado, String motivo) {
        return Turno.reconstruir(id, "PA1", "P1", "S1", "PR1", 30, hora("10:00"), estado, false, motivo);
    }
}
