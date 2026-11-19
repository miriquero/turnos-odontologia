package ar.edu.turnos.dobles;

import static ar.edu.turnos.dominio.modelo.Fixtures.hora;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.turnos.dominio.modelo.Paciente;
import ar.edu.turnos.dominio.modelo.Profesional;
import ar.edu.turnos.dominio.modelo.Turno;
import org.junit.jupiter.api.Test;

class DatosFicticiosTest {

    @Test
    void elSeedContieneElParDeTurnosContiguosDeLas1030() {
        Escenario e = Escenario.conSeed();
        Turno anterior = e.turnos.buscarPorId(DatosFicticios.TURNO_ANTERIOR_ID).orElseThrow();
        Turno contiguo = e.turnos.buscarPorId(DatosFicticios.TURNO_CONTIGUO_ID).orElseThrow();
        assertThat(anterior.intervalo().fin()).isEqualTo(hora("10:30"));
        assertThat(contiguo.intervalo().inicio()).isEqualTo(hora("10:30"));
        assertThat(contiguo.profesionalId()).isEqualTo(anterior.profesionalId());
        assertThat(contiguo.sillonId()).isEqualTo(anterior.sillonId());
        assertThat(anterior.intervalo().seSolapaCon(contiguo.intervalo())).isFalse();
    }

    @Test
    void ningunDatoDePersonaEsReal() {
        // RN-PA-04: todo dato de persona esta marcado como ficticio y usa documentos/correos reservados.
        for (Paciente p : DatosFicticios.pacientes()) {
            assertThat(p.nombre()).contains("Ficticio");
            assertThat(p.documento()).startsWith("0000");
        }
        for (Profesional p : DatosFicticios.profesionales()) {
            assertThat(p.nombre()).contains("Fictici");
        }
        assertThat(DatosFicticios.pacientes()).isNotEmpty();
        assertThat(DatosFicticios.profesionales()).hasSizeGreaterThanOrEqualTo(2);
    }
}
