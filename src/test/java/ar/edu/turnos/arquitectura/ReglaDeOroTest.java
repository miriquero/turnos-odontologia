package ar.edu.turnos.arquitectura;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Regla dura 1: el paquete dominio no importa Spring, JPA ni HTTP; ningun test del dominio levanta Spring. */
class ReglaDeOroTest {

    private static final Path DOMINIO = Path.of("src/main/java/ar/edu/turnos/dominio");
    private static final List<String> PROHIBIDOS = List.of(
            "org.springframework", "jakarta.", "javax.persistence", "javax.servlet", "java.net.http");

    private static List<Path> fuentes(Path raiz) throws IOException {
        try (Stream<Path> s = Files.walk(raiz)) {
            return s.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void elDominioNoImportaSpringJpaNiHttp() throws IOException {
        List<Path> fuentes = fuentes(DOMINIO);
        assertThat(fuentes).isNotEmpty();
        for (Path f : fuentes) {
            List<String> imports = Files.readAllLines(f).stream()
                    .filter(l -> l.startsWith("import ")).toList();
            for (String prohibido : PROHIBIDOS) {
                assertThat(imports).as(f.toString()).noneMatch(l -> l.contains(prohibido));
            }
        }
    }

    @Test
    void ningunTestUsaSpringBootTest() throws IOException {
        List<Path> tests = fuentes(Path.of("src/test/java"));
        assertThat(tests).isNotEmpty();
        for (Path f : tests) {
            if (f.getFileName().toString().equals("ReglaDeOroTest.java")) {
                continue;
            }
            assertThat(Files.readString(f)).as(f.toString()).doesNotContain("@SpringBootTest");
        }
    }

    @Test
    void ningunaFuenteDeProduccionTocaPersistenciaNiRest() throws IOException {
        for (Path f : fuentes(Path.of("src/main/java"))) {
            String txt = Files.readString(f);
            assertThat(txt).as(f.toString()).doesNotContain("org.springframework").doesNotContain("jakarta.persistence");
        }
    }
}
