package ar.edu.turnos.dominio.servicio;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static ar.edu.turnos.dominio.modelo.Fixtures.intervalo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.edu.turnos.dobles.Escenario;
import ar.edu.turnos.dominio.excepcion.ReglaDeNegocioViolada;
import ar.edu.turnos.dominio.modelo.EstadoTurno;
import ar.edu.turnos.dominio.modelo.Intervalo;
import ar.edu.turnos.dominio.modelo.RecursoAgenda;
import ar.edu.turnos.dominio.modelo.Turno;
import ar.edu.turnos.dominio.puerto.FuenteDeOcupacion;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServicioDisponibilidadTest {

    private final Escenario escenario = Escenario.vacio();
    private final ServicioDisponibilidad servicio =
            new ServicioDisponibilidad(new OcupacionPorTurnos(escenario.turnos));

    private Turno turno(String id, String prof, String sillon, String desde, EstadoTurno estado) {
        Turno t = Turno.reconstruir(id, "PA1", prof, sillon, "LIMPIEZA", 30, hora(desde), estado, false);
        escenario.conTurno(t);
        return t;
    }

    private SolicitudDisponibilidad solicitud(String prof, String sillon, String desde, String hasta) {
        return new SolicitudDisponibilidad(prof, sillon, intervalo(desde, hasta), null);
    }

    private void assertRechazaCon(SolicitudDisponibilidad s, String codigo) {
        assertThatThrownBy(() -> servicio.verificarDisponibilidad(s))
                .isInstanceOfSatisfying(ReglaDeNegocioViolada.class, e -> assertThat(e.codigo()).isEqualTo(codigo));
    }

    // 5.1
    @Test
    void rechazaSolapamientoDelProfesional() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazaCon(solicitud("P1", "S2", "10:15", "10:45"), "RN-AG-02");
    }

    @Test
    void rechazaSolapamientoDelProfesionalConOtroIntervalo() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazaCon(solicitud("P1", "S2", "09:45", "10:15"), "RN-AG-02");
    }

    // 5.2
    @Test
    void rechazaSolapamientoDelSillon() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazaCon(solicitud("P2", "S1", "10:15", "10:45"), "RN-AG-03");
    }

    // 5.3
    @Test
    void profesionalOcupadoYSillonLibre() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazaCon(solicitud("P1", "S2", "10:15", "10:45"), "RN-AG-02");
    }

    @Test
    void profesionalLibreYSillonOcupado() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazaCon(solicitud("P2", "S1", "10:15", "10:45"), "RN-AG-03");
    }

    @Test
    void ambosOcupadosReportaRnAg02() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazaCon(solicitud("P1", "S1", "10:15", "10:45"), "RN-AG-02");
    }

    // 5.4
    @Test
    void turnosContiguosDelMismoProfesionalYSillonSonValidos() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertThatCode(() -> servicio.verificarDisponibilidad(solicitud("P1", "S1", "10:30", "11:00")))
                .doesNotThrowAnyException();
    }

    @Test
    void turnoInmediatamenteAnteriorEsValido() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertThatCode(() -> servicio.verificarDisponibilidad(solicitud("P1", "S1", "09:30", "10:00")))
                .doesNotThrowAnyException();
    }

    // 5.5
    @Test
    void elTurnoCanceladoNoBloquea() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.CANCELADO);
        assertThat(servicio.evaluar(solicitud("P1", "S1", "10:00", "10:30")).disponible()).isTrue();
    }

    @Test
    void elTurnoConAusenciaNoBloquea() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AUSENTE);
        assertThat(servicio.evaluar(solicitud("P1", "S1", "10:00", "10:30")).disponible()).isTrue();
    }

    @Test
    void elTurnoAtendidoSigueOcupando() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.ATENDIDO);
        assertRechazaCon(solicitud("P1", "S2", "10:15", "10:45"), "RN-AG-02");
    }

    // 5.6
    @Test
    void excluirTurnoIdEvitaElConflictoConsigoMismo() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        SolicitudDisponibilidad s = new SolicitudDisponibilidad("P1", "S1", intervalo("10:00", "10:30"), "T1");
        assertThat(servicio.evaluar(s).disponible()).isTrue();
        assertThatCode(() -> servicio.verificarDisponibilidad(s)).doesNotThrowAnyException();
    }

    @Test
    void sinExclusionElMismoIntervaloSiFalla() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        assertRechazaCon(solicitud("P1", "S1", "10:00", "10:30"), "RN-AG-02");
    }

    @Test
    void excluirUnTurnoNoLiberaOtroTurnoSolapado() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        turno("T2", "P1", "S2", "10:15", EstadoTurno.AGENDADO);
        SolicitudDisponibilidad s = new SolicitudDisponibilidad("P1", "S1", intervalo("10:00", "10:30"), "T1");
        assertThat(servicio.evaluar(s).codigoRegla()).isEqualTo("RN-AG-02");
    }

    // 5.7
    @Test
    void evaluarDevuelveDisponibleSinLanzar() {
        ResultadoDisponibilidad r = servicio.evaluar(solicitud("P1", "S1", "10:00", "10:30"));
        assertThat(r.disponible()).isTrue();
        assertThat(r.codigoRegla()).isNull();
    }

    @Test
    void evaluarExponeElCodigoDeLaReglaIncumplida() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        ResultadoDisponibilidad r = servicio.evaluar(solicitud("P1", "S2", "10:15", "10:45"));
        assertThat(r.disponible()).isFalse();
        assertThat(r.codigoRegla()).isEqualTo("RN-AG-02");
    }

    @Test
    void ambasFormasDanElMismoVeredictoSobreLasMismasSolicitudes() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        turno("T2", "P2", "S2", "11:00", EstadoTurno.ATENDIDO);
        List<SolicitudDisponibilidad> solicitudes = List.of(
                solicitud("P1", "S2", "10:15", "10:45"),
                solicitud("P2", "S1", "10:15", "10:45"),
                solicitud("P1", "S1", "10:15", "10:45"),
                solicitud("P1", "S1", "10:30", "11:00"),
                solicitud("P1", "S1", "09:30", "10:00"),
                solicitud("P2", "S2", "11:15", "11:45"),
                new SolicitudDisponibilidad("P1", "S1", intervalo("10:00", "10:30"), "T1"));
        for (SolicitudDisponibilidad s : solicitudes) {
            ResultadoDisponibilidad r = servicio.evaluar(s);
            if (r.disponible()) {
                assertThatCode(() -> servicio.verificarDisponibilidad(s)).as(s.toString()).doesNotThrowAnyException();
            } else {
                assertThatThrownBy(() -> servicio.verificarDisponibilidad(s)).as(s.toString())
                        .isInstanceOfSatisfying(ReglaDeNegocioViolada.class,
                                e -> assertThat(e.codigo()).isEqualTo(r.codigoRegla()));
            }
        }
    }

    // 5.8
    @Test
    void consultaLaOcupacionDelProfesionalYLaDelSillon() {
        List<Object[]> consultas = new ArrayList<>();
        FuenteDeOcupacion espia = (recurso, franja, excluir) -> {
            consultas.add(new Object[] {recurso, franja, excluir});
            return List.of();
        };
        Intervalo pedido = intervalo("10:00", "10:30");
        ResultadoDisponibilidad r =
                new ServicioDisponibilidad(espia).evaluar(new SolicitudDisponibilidad("P1", "S1", pedido, null));
        assertThat(r.disponible()).isTrue();
        assertThat(consultas).hasSize(2);
        assertThat(consultas).extracting(c -> c[0])
                .containsExactly(RecursoAgenda.profesional("P1"), RecursoAgenda.sillon("S1"));
        assertThat(consultas).allSatisfy(c -> assertThat(c[1]).isEqualTo(pedido));
    }

    // 5.9 y escenarios "sin busqueda de huecos" y "no conoce el sobreturno"
    @Test
    void laInterfazPublicaSonSoloVerificarDisponibilidadYEvaluar() {
        List<String> publicos = Arrays.stream(ServicioDisponibilidad.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers())).map(Method::getName).sorted().toList();
        assertThat(publicos).containsExactly("evaluar", "verificarDisponibilidad");
    }

    @Test
    void laSolicitudNoTransportaNingunDatoDeAutorizacionDeSobreturno() {
        assertThat(Arrays.stream(SolicitudDisponibilidad.class.getRecordComponents()).map(c -> c.getName()))
                .containsExactly("profesionalId", "sillonId", "intervalo", "excluirTurnoId");
    }

    @Test
    void unaFranjaOcupadaNuncaSeInformaComoDisponible() {
        turno("T1", "P1", "S1", "10:00", EstadoTurno.AGENDADO);
        ResultadoDisponibilidad r = servicio.evaluar(solicitud("P1", "S1", "10:15", "10:45"));
        assertThat(r.disponible()).isFalse();
        assertThat(r.codigoRegla()).isEqualTo("RN-AG-02");
    }

    @Test
    void ningunaOperacionPublicaDevuelveFranjasLibres() {
        assertThat(Arrays.stream(ServicioDisponibilidad.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getReturnType)).noneMatch(t -> List.class.isAssignableFrom(t));
    }
}
