# Design

## Context

Repositorio vacío de código: este change crea el scaffolding además del núcleo.
La motivación está en `proposal.md`; las reglas, en los dos spec delta de
`specs/agenda/`. Las restricciones que condicionan el diseño:

- **`R-05`** — cinco integrantes van a tocar `Turno` en paralelo apenas se
  mergee este change. Todo lo que ellos consumen tiene que quedar escrito y
  congelado acá: `Intervalo`, `ServicioDisponibilidad`, `EstadoTurno`, el enum de
  acciones de `EventoAuditoria` y los ocho puertos.
- **Regla dura 1 del proyecto** — el paquete `dominio` no importa Spring, JPA ni
  HTTP. Un test del núcleo corre sin levantar contexto de Spring.
- **`D-08`** — Gradle con wrapper versionado.
- **Alcance cerrado** — sin persistencia real y sin capa REST (`C-09` y `C-10`).
- **Strict TDD Mode activo** — cada tarea escribe primero el test.

## Goals / Non-Goals

**Goals:**

- Dejar el contrato del núcleo congelado y consumible por `C-02` a `C-10` sin
  que ninguno tenga que abrir `Intervalo` ni `ServicioDisponibilidad`.
- Que la regla `RN-AG-04` esté implementada en un único método y testeada de
  forma aislada, sin pasar por el caso de uso.
- Que la suite completa corra en segundos, sin Spring, sin base de datos y sin
  red.

**Non-Goals:**

- Rendimiento de la consulta de ocupación: con dobles en memoria no hay índices
  que optimizar. Los índices `(profesional_id, inicio, fin)` y
  `(sillon_id, inicio, fin)` son de `C-09`.
- Resolver la condición de carrera de dos altas simultáneas (`R-06`).
- Mapeo de errores a códigos HTTP y forma del cuerpo de error (`C-10`).
- Caso de uso de sobreturno: acá va solo la estructura de
  `AutorizacionSobreturno`, sin operación que la cree.

## Decisions

### D-C01-1 — Los repositorios se declaran en el dominio y se implementan solo en `src/test`

Las ocho interfaces de puerto viven en `ar.edu.turnos.dominio.puerto`. Sus
únicas implementaciones en este change son dobles en memoria bajo `src/test`.

**Alternativa descartada**: adaptadores JPA sobre H2 ya en este change. Se
descarta porque infla `C-01` —que bloquea a cuatro personas— con un módulo
entero que ya tiene dueño (`C-09`), y porque la consigna pide que el change sea
una sola funcionalidad. El costo es que al terminar `C-01` el sistema no
persiste nada; es aceptable, porque lo verificable es la regla, no el guardado.

### D-C01-2 — `FuenteDeOcupacion` es un puerto propio, distinto de `TurnoRepositorio`

`ServicioDisponibilidad` no depende de `TurnoRepositorio` sino de
`FuenteDeOcupacion`, que devuelve los intervalos ocupados de un recurso en una
franja. En este change la única implementación consulta
`TurnoRepositorio.buscarVigentesSolapados(...)`.

**Por qué**: `RN-DI-01` dice que un `BloqueoAgenda` participa de la
disponibilidad exactamente igual que un turno. Con el puerto intermedio, `C-12`
agrega una segunda fuente **sin editar el servicio**, que está congelado por
`R-05`. Sin él, `C-12` tendría que abrir el archivo prohibido.

**Nota de contradicción**: el punto 4 de la "Regla de contención R-05" en
`CHANGES.md` enumera solo `TurnoRepositorio`, `RegistroAuditoria` y
`NotificadorPaciente` como puertos congelados, mientras que el Scope de `C-01`
en el mismo archivo enumera los ocho. Se toma la lista de ocho del Scope, que es
la que coincide con el alcance cerrado por el usuario.

### D-C01-3 — El filtro de vigencia (`RN-AG-07`) vive en la consulta, no en el servicio

`buscarVigentesSolapados(...)` devuelve únicamente turnos en estados que ocupan
agenda. El servicio no filtra por estado. Lo decide `EstadoTurno.ocupaAgenda()`,
que es el único lugar donde está escrito qué estado ocupa.

**Alternativa descartada**: que el servicio traiga todos los turnos y filtre. Se
descarta porque duplicaría el criterio de vigencia en el servicio y en el futuro
adaptador JPA, que sí necesita el filtro en el `WHERE` por rendimiento.

### D-C01-4 — Precedencia del código de regla cuando fallan profesional y sillón a la vez

La verificación evalúa primero el profesional y después el sillón, y corta en el
primer fallo. Con ambos ocupados, el código reportado es `RN-AG-02`.

**Por qué se decide acá**: la KB dice que las dos reglas se evalúan "de forma
independiente" pero no dice cuál se reporta cuando ambas fallan. Dejarlo
indefinido haría que el test de ese caso dependiera del orden de los `if`. El
spec lo fija como comportamiento observable.

### D-C01-5 — El instante actual entra por un `Clock` inyectado

`RN-AG-08` exige que el inicio sea futuro, lo que hace al caso de uso dependiente
del reloj. `AgendarTurno` recibe un `java.time.Clock` por constructor; los tests
usan `Clock.fixed(...)`.

**Alternativa descartada**: `LocalDateTime.now()` dentro del caso de uso, que
vuelve el test no determinístico y obliga a fechas relativas frágiles.
`java.time.Clock` es JDK puro: no viola la regla de no importar Spring.

### D-C01-6 — El fin del turno no es construible desde fuera

`Turno` no expone constructor público con `fin` ni setter de `fin`. Se crea con
una factoría que recibe `inicio` y la `Prestacion`, y deriva el `Intervalo`.
Así, `RN-AG-01` es imposible de violar por construcción, y el escenario "el fin
no se puede fijar a mano" se verifica sobre la API del tipo, no con un test de
comportamiento.

### D-C01-7 — El aviso al paciente se dispara dentro de un `try/catch` del caso de uso

`RN-NO-03` exige que un fallo de notificación no invalide el alta. El caso de uso
invoca el puerto como último paso, después de guardar el turno y registrar
auditoría, y captura cualquier excepción del puerto. El dominio no conoce
proveedores: el único contrato es `NotificadorPaciente`.

### D-C01-8 — Spring Boot está en el build, pero no en el dominio

`build.gradle` declara Spring Boot 3.x para que `C-09` y `C-10` no tengan que
rehacer el build, pero este change no crea ninguna clase anotada ni ningún test
con `@SpringBootTest`. Se agrega una verificación de la regla de oro (un test de
arquitectura simple o, en su defecto, una revisión de imports en el PR) para que
la violación se detecte temprano.

### D-C01-9 — Un test por escenario del spec, con los 9 casos de `RN-AG-04` parametrizados

Los nueve casos de la tabla se implementan como un `@ParameterizedTest` sobre
`Intervalo` con los nueve pares y su resultado esperado, más los casos
simétricos. Cuenta como nueve escenarios verificados, uno por fila, y evita
nueve métodos casi idénticos.

## Risks / Trade-offs

- **El JDK por defecto del sistema no sirve** → En esta máquina `JAVA_HOME`
  apunta a `C:\Java\jdk-26` y `java -version` informa **OpenJDK 26**, que Spring
  Boot 3.x y el plugin de Gradle pueden no soportar. En
  `C:\Users\zbook\.jdks` hay `ms-17.0.20.1` (Java 17 LTS, el objetivo del
  proyecto) y `ms-21.0.12`. **Mitigación**: fijar el toolchain de Java 17 en
  `build.gradle` (`java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }`)
  para que Gradle resuelva el JDK sin depender de `JAVA_HOME`, y verificarlo con
  `./gradlew -q javaToolchains` en la primera tarea del scaffolding. Si el
  toolchain no lo detecta, declarar la ruta del JDK 17 en `gradle.properties`
  con `org.gradle.java.home`, **sin** versionar una ruta absoluta de una máquina
  concreta en el repositorio compartido. Esto es el pendiente asociado de `D-08`
  y debe quedar anotado en el PR para los otros cuatro integrantes.
- **Nadie puede probar el sistema de punta a punta al terminar este change** →
  no hay persistencia ni REST. Mitigación: los dobles en memoria quedan en
  `src/test` con un juego de datos ficticios que replica el seed de
  `04_modelo_de_datos.md`, incluido el **par de turnos contiguos**, para que
  `C-09` tenga un oráculo contra el cual comparar su adaptador JPA.
- **Congelar ocho puertos antes de implementar sus consumidores** → es probable
  que a algún change de FASE 1 le falte un método. Mitigación: el procedimiento
  está en `CHANGES.md` (hotfix del líder sobre `C-01`, nunca una ampliación
  unilateral). `buscarVigentesSolapados` ya acepta `excluirTurnoId` justamente
  por `C-02`.
- **`R-06`, concurrencia** → dos altas simultáneas sobre la misma franja pasan
  ambas la verificación. No se resuelve acá y queda señalado para `C-09`
  (restricción de exclusión o bloqueo pesimista). Con dobles en memoria y tests
  de un solo hilo, el riesgo no es observable en este change, lo cual lo vuelve
  fácil de olvidar: por eso queda escrito.
- **Un `@ParameterizedTest` puede ocultar una fila que no se ejecuta**
  (`D-C01-9`) → mitigación: nombrar cada caso con el número de fila de la tabla
  de `RN-AG-04` y verificar en el reporte de Gradle que figuran los nueve.

## Open Questions

- **`PA-02`** — las duraciones del catálogo (`SU-02`) no están validadas con un
  odontólogo. No afecta a este change: son datos configurables y los tests fijan
  sus propias duraciones.
- Si el proyecto suma una verificación automática de la regla de oro (dominio sin
  Spring) con una herramienta de análisis de dependencias o le alcanza con la
  revisión del PR. Se puede decidir en `C-11` sin tocar nada de lo escrito acá.

## Ajuste posterior al Apply (2026-11-19)

**`NotificadorPaciente` pasó a ser genérico.** La implementación inicial expuso
`notificarTurnoAgendado(Turno)`, una firma específica del alta. Los cuatro
changes de FASE 1 (reprogramar, cancelar, sobreturno, ausencia) también
notifican al paciente, de modo que cada uno habría tenido que **agregar un
método a este puerto** — es decir, cuatro personas editando el mismo archivo
congelado, exactamente el conflicto que `R-05` busca evitar.

Firma final:

```java
void notificar(Turno turno, AccionAuditoria accion);
```

Reutiliza `AccionAuditoria`, que ya enumera `CREADO`, `REPROGRAMADO`,
`CANCELADO`, `MARCADO_ATENDIDO`, `MARCADO_AUSENTE` y `SOBRETURNO_AUTORIZADO`, así
que no hace falta un enum nuevo. Cada change llama al mismo método con su propia
acción y el puerto no se vuelve a tocar.

El spec no fijaba la firma, así que este ajuste no modifica ningún escenario. La
suite siguió en verde: 104 tests, 0 fallas.
