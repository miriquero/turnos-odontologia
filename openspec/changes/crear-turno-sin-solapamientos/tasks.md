# Tasks

> **Strict TDD Mode activo.** Toda tarea que produzca código de producción
> nombra primero el test que se escribe (RED), después la implementación mínima
> (GREEN) y, donde corresponda, el segundo caso que obliga a generalizar
> (TRIANGULATE). No se escribe producción sin un test en rojo.
>
> Referencias: `specs/agenda/disponibilidad/spec.md` y
> `specs/agenda/alta-de-turno/spec.md` para qué construir; `design.md` para cómo.

## 1. Scaffolding del proyecto (sin tests de producción: es setup)

- [ ] 1.1 Crear `settings.gradle` con `rootProject.name = 'turnos-odontologia'` y
  `build.gradle` con el plugin de Java, el toolchain de Java 17
  (`java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }`,
  ver `design.md` → riesgo del JDK), Spring Boot 3.x y las dependencias de test
  JUnit 5 + AssertJ + Mockito. Verificar con `./gradlew -q javaToolchains` que
  resuelve un JDK 17 y con `./gradlew build` que la build vacía pasa.
- [ ] 1.2 Versionar el wrapper de Gradle (`gradlew`, `gradlew.bat`,
  `gradle/wrapper/`) y verificar que `./gradlew --version` funciona sin Gradle
  instalado globalmente.
- [ ] 1.3 Crear el árbol de paquetes
  `ar.edu.turnos.{dominio.modelo, dominio.servicio, dominio.puerto,
  dominio.excepcion, aplicacion.casouso, infraestructura}` en `src/main/java` y
  el espejo de test en `src/test/java`. Verificar que `./gradlew test` corre con
  cero tests y termina en verde.
- [ ] 1.4 Dejar anotado en el `README` o en el PR con qué JDK se compiló y si
  hizo falta forzarlo, cerrando el pendiente asociado de `D-08`. Verificar que
  otro integrante puede reproducir la build siguiendo solo esa nota.

## 2. `Intervalo` — la regla `RN-AG-04`

- [ ] 2.1 RED: escribir `IntervaloTest#rechazaFinAnteriorAlInicio` y
  `#rechazaDuracionCero`, que esperan el fallo de la invariante `fin > inicio`.
  GREEN: implementar el constructor de `Intervalo` con la validación. Verificar
  que ambos tests pasan.
- [ ] 2.2 RED: escribir `IntervaloTest#casosDeLaTablaRnAg04` como
  `@ParameterizedTest` con las **nueve filas** de la tabla de `RN-AG-04`
  (`[10:00,10:30)` vs `[10:30,11:00)` → no solapan; vs `[09:30,10:00)` → no
  solapan; vs `[10:29,10:59)`, `[10:00,10:30)`, contenido, contenedor,
  `[10:15,10:45)`, `[09:45,10:15)` → solapan; vs `[11:00,11:30)` → no solapan),
  cada caso nombrado con su número de fila. GREEN: implementar `seSolapaCon`
  con `A.inicio < B.fin && B.inicio < A.fin`, `<` estricto. Verificar en el
  reporte de Gradle que figuran los nueve casos y que los nueve pasan.
- [ ] 2.3 TRIANGULATE: agregar `IntervaloTest#elSolapamientoEsSimetrico`
  invirtiendo los operandos de las nueve filas. Verificar que pasa sin tocar la
  implementación; si falla, generalizar el predicado.
- [ ] 2.4 REFACTOR: extraer la fábrica de intervalos de los tests a un helper de
  fixtures y verificar que la suite sigue en verde después de cada paso.

## 3. Modelo de dominio y estados

- [ ] 3.1 RED: escribir `EstadoTurnoTest` con los escenarios "transiciones
  válidas desde AGENDADO", "los estados terminales no tienen salida" y "cada
  estado declara si ocupa la agenda" (`AGENDADO` y `ATENDIDO` ocupan;
  `CANCELADO` y `AUSENTE` no). GREEN: implementar `EstadoTurno` con
  `puedeTransicionarA(...)` y `ocupaAgenda()` según `RN-TU-02` y `RN-AG-07`.
  Verificar que los tres tests pasan.
- [ ] 3.2 Implementar `Profesional`, `Sillon`, `Prestacion`
  (`duracionMinutos > 0`, `RN-CA-01`) y `Paciente` (ficha básica). RED primero:
  un test por invariante que la clase deba garantizar —en particular
  `PrestacionTest#rechazaDuracionNoPositiva`. Verificar que los tests de
  invariante pasan.
- [ ] 3.3 RED: escribir `TurnoTest#derivaElFinDeLaPrestacion` (limpieza de 30 min
  a las 10:00 → `[10:00, 10:30)`) y, como segundo caso,
  `#derivaElFinDeUnaPrestacionLarga` (conducto de 90 min → `[10:00, 11:30)`).
  GREEN: implementar `Turno` con la factoría que recibe `inicio` y `Prestacion` y
  deriva el `Intervalo`, sin constructor ni setter que acepten `fin`
  (`RN-AG-01`, `design.md` → `D-C01-6`). Verificar que ambos pasan y que el tipo
  no expone ninguna vía para fijar `fin`.
- [ ] 3.4 Implementar `AutorizacionSobreturno` (solo la estructura:
  `turnoId`, `autorizadoPor`, `motivo`, `autorizadoEn`, los tres primeros
  obligatorios) con un test de sus invariantes. **Sin caso de uso.** Verificar
  que el test de obligatoriedad pasa.
- [ ] 3.5 Implementar `EventoAuditoria` con el enum completo de acciones
  (`CREADO`, `MODIFICADO`, `REPROGRAMADO`, `CANCELADO`, `MARCADO_ATENDIDO`,
  `MARCADO_AUSENTE`, `SOBRETURNO_AUTORIZADO`) y usuario y timestamp obligatorios
  (`RN-AU-01`). RED: un test que verifique que las siete acciones existen y que
  un evento sin usuario se rechaza. Verificar que pasa.
- [ ] 3.6 Implementar `ReglaDeNegocioViolada` en `dominio.excepcion`, que expone
  el **código de regla** además del mensaje. RED: un test que construya la
  excepción con `RN-AG-02` y verifique que el código es recuperable de forma
  programática, no sólo legible en el texto.

## 4. Puertos congelados (`R-05`) y dobles en memoria

- [ ] 4.1 Declarar en `dominio.puerto` las ocho interfaces: `TurnoRepositorio`
  (`buscarPorId`, `guardar`, `buscarVigentesSolapados(recurso, Intervalo,
  excluirTurnoId)`), `ProfesionalRepositorio`, `SillonRepositorio`,
  `PrestacionRepositorio`, `PacienteRepositorio`, `RegistroAuditoria`,
  `NotificadorPaciente` y `FuenteDeOcupacion` (`design.md` → `D-C01-2`).
  Verificar que `./gradlew compileJava` pasa y que ninguna importa Spring, JPA
  ni HTTP.
- [ ] 4.2 Implementar los dobles en memoria en `src/test`, con el filtro de
  vigencia dentro de `buscarVigentesSolapados` (`design.md` → `D-C01-3`). RED
  primero: `TurnoRepositorioEnMemoriaTest` con el caso "un turno `CANCELADO` no
  aparece en la búsqueda de vigentes solapados" y "un turno `ATENDIDO` sí
  aparece". Verificar que ambos pasan.
- [ ] 4.3 Cargar en los dobles un juego de datos **ficticios** espejo del seed de
  `04_modelo_de_datos.md` (`RN-PA-04`), incluido el **par de turnos contiguos**
  de las 10:30. Verificar con un test que el fixture contiene ese par y que
  ningún dato de persona es real.

## 5. `ServicioDisponibilidad` — el núcleo

- [ ] 5.1 RED: `ServicioDisponibilidadTest#rechazaSolapamientoDelProfesional`
  (existente `[10:00,10:30)` de `P1`, nuevo `[10:15,10:45)` de `P1` en sillón
  libre → `ReglaDeNegocioViolada` con `RN-AG-02`). GREEN: implementar
  `verificarDisponibilidad` con la comprobación del profesional leyendo por
  `FuenteDeOcupacion`. Verificar que pasa.
- [ ] 5.2 RED: `#rechazaSolapamientoDelSillon` (mismo sillón, **otro**
  profesional → `RN-AG-03`). GREEN: agregar la comprobación del sillón como paso
  independiente. Verificar que ambos tests de 5.1 y 5.2 pasan.
- [ ] 5.3 TRIANGULATE: `#profesionalOcupadoYSillonLibre`,
  `#profesionalLibreYSillonOcupado` y `#ambosOcupadosReportaRnAg02`
  (precedencia fijada en `design.md` → `D-C01-4`). Verificar los tres.
- [ ] 5.4 RED: `#turnosContiguosDelMismoProfesionalYSillonSonValidos`
  (`[10:00,10:30)` + `[10:30,11:00)`) y la variante anterior
  (`[09:30,10:00)`). Verificar que pasan apoyándose en `Intervalo` sin duplicar
  el predicado dentro del servicio.
- [ ] 5.5 RED: `#elTurnoCanceladoNoBloquea`, `#elTurnoConAusenciaNoBloquea` y
  `#elTurnoAtendidoSigueOcupando` (`RN-AG-07`). Verificar los tres.
- [ ] 5.6 RED: `#excluirTurnoIdEvitaElConflictoConsigoMismo` y su par sin
  exclusión, que sí debe fallar con `RN-AG-02`. GREEN: propagar
  `excluirTurnoId` desde la solicitud hasta la consulta de ocupación (lo consume
  `C-02`). Verificar ambos.
- [ ] 5.7 RED: `#evaluarDevuelveDisponibleSinLanzar` y
  `#evaluarExponeElCodigoDeLaReglaIncumplida`. GREEN: implementar `evaluar(...)`
  no excepcional y hacer que `verificarDisponibilidad` se apoye en ella, de modo
  que las dos formas no puedan divergir. Verificar que ambas dan el mismo
  veredicto sobre las mismas solicitudes de 5.1 a 5.6.
- [ ] 5.8 RED: `#consultaLaOcupacionDelProfesionalYLaDelSillon`, con un doble de
  `FuenteDeOcupacion` que registra las consultas recibidas. Verificar que llegan
  dos consultas acotadas al intervalo pedido y que el servicio no accede a
  ninguna otra fuente.
- [ ] 5.9 REFACTOR: revisar que la interfaz pública de `ServicioDisponibilidad`
  sea exactamente `verificarDisponibilidad(...)` y `evaluar(...)`, sin ninguna
  operación de búsqueda de huecos ni noción de sobreturno. Verificar que la
  suite sigue en verde y dejar constancia en el PR de que el archivo queda
  congelado (`R-05`).

## 6. Caso de uso `AgendarTurno`

- [ ] 6.1 SAFETY NET: correr `./gradlew test` y anotar el baseline de tests en
  verde antes de abrir este grupo. Un fallo previo se reporta, no se arregla acá.
- [ ] 6.2 RED: `AgendarTurnoTest#caminoFelizLimpiezaDe30MinutosALas10`
  (profesional, sillón y paciente activos, franja libre, `Clock` fijo) que espera
  turno `AGENDADO` sobre `[10:00, 10:30)`, `esSobreturno` en falso y un evento
  `CREADO` en el registro de auditoría. GREEN: implementar `AgendarTurno` con el
  `Clock` inyectado (`design.md` → `D-C01-5`). Verificar que pasa.
- [ ] 6.3 RED: `#rechazaProfesionalInactivo`, `#rechazaSillonInactivo`,
  `#rechazaPrestacionInactiva` y `#rechazaRecursoInexistente`, todos con
  `RN-AG-08` y sin crear turno. GREEN: la validación de recursos, **antes** de
  consultar disponibilidad. Verificar los cuatro.
- [ ] 6.4 RED: `#rechazaInicioEnElPasado` y
  `#rechazaInicioExactamenteAhora` (`RN-AG-08`, inicio estrictamente futuro) con
  `Clock.fixed`. Verificar ambos.
- [ ] 6.5 RED: `#rechazaSolapamientoDeProfesional` (`RN-AG-02`) y
  `#rechazaSolapamientoDeSillon` (`RN-AG-03`), verificando además que **no** se
  crea turno, **no** se registra auditoría y **no** se notifica. GREEN:
  delegar en `ServicioDisponibilidad` y propagar el código. Verificar ambos.
- [ ] 6.6 RED: `#aceptaTurnoContiguoDelMismoProfesionalYSillon`
  (existente `[10:00,10:30)` + nuevo `[10:30,11:00)` → `AGENDADO`) y
  `#aceptaTurnoInmediatamenteAnterior` (`[09:30,10:00)`). Verificar ambos: es el
  caso de borde que define el proyecto.
- [ ] 6.7 RED: `#aceptaAgendarSobreUnaFranjaLiberadaPorCancelacion`. Verificar
  que pasa.
- [ ] 6.8 RED: `#rechazaAltaSinUsuarioResponsable` (`RN-AU-01`) y
  `#elRegistroDeAuditoriaEsAppendOnly` (`RN-AU-02`). GREEN: exigir el usuario en
  la entrada del caso de uso. Verificar ambos.
- [ ] 6.9 RED: `#disparaLaNotificacionTrasCrearElTurno` y
  `#unaNotificacionQueFallaNoInvalidaElAlta` (el puerto lanza; el turno queda
  `AGENDADO` y el evento `CREADO` registrado). GREEN: invocar el puerto como
  último paso, dentro de `try/catch` (`RN-NO-03`, `design.md` → `D-C01-7`).
  Verificar ambos.
- [ ] 6.10 REFACTOR: extraer la construcción de escenarios de los tests a un
  builder de fixtures compartido con los tests del servicio, verificando la
  suite en verde después de cada paso.

## 7. Verificación transversal del change

- [ ] 7.1 Verificar que existe **al menos un test por escenario** de los dos spec
  delta y dejar la tabla de trazabilidad escenario → test en el PR. Verificar
  que no queda ningún escenario sin test.
- [ ] 7.2 Verificar la regla de oro: ningún archivo bajo
  `src/main/java/ar/edu/turnos/dominio` importa `org.springframework`, `jakarta`,
  `javax.persistence` ni nada de HTTP, y ningún test del dominio usa
  `@SpringBootTest`. Verificar con una búsqueda de imports sobre el paquete.
- [ ] 7.3 Verificar `RN-PA-04`: ningún dato de persona en seeds, fixtures o
  tests corresponde a una persona real. Verificar con una revisión del fixture
  compartido.
- [ ] 7.4 Correr `./gradlew clean test` y verificar que la suite completa pasa en
  verde desde cero, sin red y sin base de datos, y registrar el tiempo de
  ejecución en el PR.
- [ ] 7.5 Correr `openspec validate --changes "crear-turno-sin-solapamientos"
  --strict` y verificar que pasa.

## Workflow follow-up

- Abrir el PR desde `feat/crear-turno-sin-solapamientos` hacia `master` con la
  evidencia del ciclo TDD y la tabla de trazabilidad escenario → test.
- El equipo valida el PR contra los tres escenarios obligatorios (camino feliz,
  error de negocio y borde de contigüidad) antes del merge.
- Archivar el change una vez mergeado y con la suite completa en verde (regla
  dura 4 del proyecto), y marcar `[x]` en `C-01` dentro de `CHANGES.md`.
- Avisar al equipo que GATE 1 queda abierto: `C-02` a `C-10` pueden arrancar.
