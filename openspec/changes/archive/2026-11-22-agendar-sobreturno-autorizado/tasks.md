# Tasks

> **Strict TDD Mode activo.** Toda tarea que produzca código de producción
> parte de un test en rojo (RED), continúa con la implementación mínima (GREEN),
> triangula con al menos un segundo caso (TRIANGULATE) y refactoriza manteniendo
> la suite en verde (REFACTOR).
>
> Referencias: `specs/agenda/sobreturno-autorizado/spec.md` para qué construir;
> `design.md` para cómo construirlo sin tocar archivos congelados (`R-05`).

## 1. Puerto de autorización y contratos de entrada

- [x] 1.1 SAFETY NET: correr `gradlew.bat test` (o `./gradlew test`) y verificar
  que el baseline de 104 tests de `C-01` pasa en verde antes de agregar código.
- [x] 1.2 Declarar la interfaz nueva
  `ar.edu.turnos.dominio.puerto.AutorizacionSobreturnoRepositorio` (`guardar` y
  `buscarPorTurnoId`), sin modificar ningún puerto existente ni importar Spring,
  JPA o HTTP (`D-C04-2`). Verificar con compilación (`compileJava`) y
  `ReglaDeOroTest`.
- [x] 1.3 Crear el enum `ar.edu.turnos.aplicacion.casouso.RolAutorizador`
  (`ODONTOLOGO_DUENIO`, `ODONTOLOGO_COLEGA`, `RECEPCION`) y el record
  `ar.edu.turnos.aplicacion.casouso.SolicitudAgendarSobreturno` (aceptando solo
  `inicio` como dato temporal, `esSobreturno`, `autorizadoPor`,
  `rolAutorizador` y `motivo`, `D-C04-4`). Verificar con un test de reflexión en
  `AgendarSobreturnoTest` que el único dato temporal de entrada es `inicio`.

## 2. Caso de uso `AgendarSobreturno` — camino feliz y validaciones de `RN-AG-06`

- [x] 2.1 RED: escribir en `AgendarSobreturnoTest` (con un doble en memoria
  privado de `AutorizacionSobreturnoRepositorio`, sin tocar `dobles/`) los tests
  de camino feliz con solapamiento de profesional, de sillón y de ambos a la
  vez, verificando turno `AGENDADO` con `esSobreturno = true`, `AutorizacionSobreturno`
  persistida 1:1 y evento `SOBRETURNO_AUTORIZADO`. GREEN: implementar
  `AgendarSobreturno` sin depender de `ServicioDisponibilidad` (`D-C04-1`,
  `D-C04-3`, `D-C04-5`). Verificar que los tres tests pasan.
- [x] 2.2 RED: escribir tests de error de negocio para ausencia de marca
  explícita (`esSobreturno = false`), `autorizadoPor` nulo/en blanco y `motivo`
  nulo/en blanco, comprobando rechazo con código `RN-AG-06` y ausencia total de
  efectos secundarios ni reintento automático. GREEN: implementar la validación
  de `RN-AG-06` al inicio de `AgendarSobreturno.ejecutar` (`D-C04-6`). Verificar
  que todos pasan.
- [x] 2.3 RED: escribir tests de autorización por rol rechazando
  `ODONTOLOGO_COLEGA`, `RECEPCION` y rol nulo con código `RN-AG-06` y sin
  efectos secundarios (`SU-04`, `PA-03`). GREEN: agregar el chequeo mínimo de
  rol `ODONTOLOGO_DUENIO` en `AgendarSobreturno`. Verificar que pasan.

## 3. Reglas heredadas (`RN-AG-08`, `RN-AG-01`, `RN-NO-03`) y casos de borde

- [x] 3.1 RED: escribir tests que verifiquen que aun con autorización completa
  del odontólogo dueño se rechazan profesional inactivo, sillón inactivo,
  prestación inactiva, recurso inexistente e inicio pasado o igual a ahora con
  código `RN-AG-08` y sin efectos secundarios, y que la duración deriva de la
  prestación (ej. Conducto de 90 minutos → `[10:00, 11:30)`). GREEN: implementar
  las validaciones de recursos e inicio futuro en `AgendarSobreturno`. Verificar
  que todos pasan.
- [x] 3.2 RED: escribir tests para auditoría append-only (`SOBRETURNO_AUTORIZADO`
  sin alterar eventos previos), notificación resiliente ante fallos del puerto
  (`RN-NO-03`) y trazabilidad 1:1 en `AutorizacionSobreturno`. GREEN: invocar
  `RegistroAuditoria` y `NotificadorPaciente.notificar(turno, SOBRETURNO_AUTORIZADO)`
  dentro de `try/catch`. Verificar que pasan.
- [x] 3.3 RED / TRIANGULATE: escribir tests de casos de borde: sobreturno sobre
  franja libre, sobreturno sobre franja solo contigua (`[10:30, 11:00)` pegado a
  `[10:00, 10:30)`), alta normal posterior (`AgendarTurno`) sobre franja ocupada
  que sigue rechazada (`RN-AG-02` / `RN-AG-03`), y `ServicioDisponibilidad` que
  sigue reportando la franja ocupada por un sobreturno como no disponible.
  REFACTOR: limpiar duplicaciones en `AgendarSobreturno` y `AgendarSobreturnoTest`.
  Verificar que todos pasan.

## 4. Verificación transversal del change

- [x] 4.1 Verificar con `git status` y `git diff master` que no se modificó
  ningún archivo existente del núcleo congelado (`R-05`) ni de `src/test/.../dobles/`.
- [x] 4.2 Verificar cobertura 1:1 de escenarios del spec en `AgendarSobreturnoTest`,
  datos estrictamente ficticios (`RN-PA-04`) y cumplimiento de `ReglaDeOroTest`.
- [x] 4.3 Correr `gradlew.bat clean test` y `openspec validate --changes "agendar-sobreturno-autorizado" --strict` y verificar todo en verde.

## Workflow follow-up

- Commitear el spec validado y los cambios de implementación en la rama `feat/agendar-sobreturno-autorizado`.
- Archivar el change con `/openspec-archive-change agendar-sobreturno-autorizado` una vez que toda la suite esté en verde.
- Pushear la rama `feat/agendar-sobreturno-autorizado` y abrir el Pull Request hacia `master` para revisión de Martín.
