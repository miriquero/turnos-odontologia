# Design

## Context

`C-01` dejó implementado y congelado (`R-05`) el núcleo del sistema en `master`:
`Intervalo`, `ServicioDisponibilidad`, `EstadoTurno`, `AccionAuditoria`,
`Turno`, `AutorizacionSobreturno` (solo estructura), `EventoAuditoria`, los ocho
puertos de `dominio.puerto`, `AgendarTurno` y los dobles compartidos en
`src/test/java/ar/edu/turnos/dobles/`.

Ver `proposal.md` para la motivación y el alcance. Restricciones técnicas que
condicionan este diseño:

- **Núcleo congelado (`R-05`) y política de cero conflictos**: `C-04` agrega
  únicamente archivos nuevos; **no modifica** ningún archivo de producción ni
  helper compartido de `C-01`.
- **`D-05`**: `ServicioDisponibilidad` jamás se modifica ni responde disponible
  sobre una franja ocupada; la decisión de omitir `RN-AG-02` y `RN-AG-03` vive
  exclusivamente en `AgendarSobreturno`.
- **Regla dura 1 (`ReglaDeOroTest`)**: cero imports de Spring, JPA o HTTP en
  producción (`dominio` y `aplicacion`) y cero `@SpringBootTest` en tests.
- **Strict TDD Mode activo**: toda implementación nace de un test en rojo
  seguido de implementación mínima, triangulación y refactor.

## Goals / Non-Goals

**Goals:**

- Implementar `aplicacion.casouso.AgendarSobreturno` en archivos nuevos sin
  tocar ningún archivo existente del repositorio.
- Garantizar que toda validación de `RN-AG-06`, `SU-04`, `RN-AG-08`, `RN-AG-01`
  y `RN-AU-01` ocurra antes de producir cualquier efecto secundario.
- Persistir la relación 1:1 `Turno` (`esSobreturno = true`) ↔
  `AutorizacionSobreturno` mediante un puerto dedicado nuevo.
- Probar explícitamente en conjunto con `AgendarTurno` y `ServicioDisponibilidad`
  que el comportamiento por defecto del alta normal no se ve alterado.

**Non-Goals:**

- Implementar persistencia JPA/H2 (`C-09`), API REST (`C-10`) o RBAC/autenticación
  real (`C-15`).
- Modificar `EventoAuditoria`, `Turno`, `Profesional`, `ReglaDeNegocioViolada`,
  `TurnoRepositorio` o los dobles compartidos de `src/test/.../dobles/`.
- Resolver concurrencia entre altas simultáneas (`R-06`, reservado para `C-09`).

## Decisions

### D-C04-1 — `AgendarSobreturno` no depende de `ServicioDisponibilidad`

`AgendarSobreturno` omite la consulta a `ServicioDisponibilidad` (no lo recibe
por constructor ni lo invoca), en lugar de pasarle un flag `ignorarSolapamiento`.

**Por qué**: cumple literalmente `D-05` y `R-05`: `ServicioDisponibilidad`
permanece congelado y libre de cualquier noción de sobreturno.

**Alternativa descartada**: ampliar `AgendarTurno` o agregar un parámetro booleano
en `SolicitudDisponibilidad`. Se descarta porque rompe `R-05`, modifica archivos
congelados y falla el test de reflexión de `ServicioDisponibilidadTest`.

### D-C04-2 — Nuevo puerto `AutorizacionSobreturnoRepositorio` en `dominio.puerto`

`C-01` definió el record `AutorizacionSobreturno(turnoId, autorizadoPor, motivo,
autorizadoEn)`, pero los 8 puertos congelados no incluyen persistencia para esta
entidad. Se crea una interfaz nueva `AutorizacionSobreturnoRepositorio` con:

```java
AutorizacionSobreturno guardar(AutorizacionSobreturno autorizacion);
Optional<AutorizacionSobreturno> buscarPorTurnoId(String turnoId);
```

**Por qué**: evita tocar `TurnoRepositorio` (congelado por `R-05`) y deja un
contrato limpio que `C-09` podrá implementar con JPA (tabla con `UNIQUE` sobre
`turno_id` para garantizar 1:1). Su doble en memoria vive como clase privada
dentro de `AgendarSobreturnoTest` para no modificar los helpers compartidos en
`src/test/.../dobles/`.

### D-C04-3 — Construcción del `Turno` sobreturno sin modificar `Turno.java`

`Turno` ya expone `Turno.reconstruir(id, pacienteId, profesionalId, sillonId,
prestacionId, duracionMinutos, inicio, estado, esSobreturno)` y deriva
internamente `new Intervalo(inicio, inicio.plusMinutes(duracionMinutos))`.
`AgendarSobreturno` instancia el turno generando `UUID.randomUUID().toString()`,
tomando `prestacion.duracionMinutos()`, `EstadoTurno.AGENDADO` y `esSobreturno =
true`.

**Por qué**: evita editar `Turno.java` en paralelo con `C-02`, `C-03` y `C-05`
(cero riesgo de conflicto de merge) y conserva intacta la invariante `RN-AG-01`
verificada por `TurnoTest`.

**Alternativa descartada**: agregar una factoría `Turno.agendarSobreturno(...)`
en `Turno.java`. Queda disponible como mejora opcional del líder sobre `master`
si el equipo decide consolidar factorías tras FASE 1.

### D-C04-4 — Chequeo mínimo de rol mediante `RolAutorizador` en `aplicacion.casouso`

`Profesional` en `C-01` no posee atributo `rol` y el MVP no usa framework de
seguridad (`SU-04`, `PA-03`, `C-15`). Se introduce el enum liviano
`RolAutorizador` (`ODONTOLOGO_DUENIO`, `ODONTOLOGO_COLEGA`, `RECEPCION`) como
campo de `SolicitudAgendarSobreturno`. Solo `ODONTOLOGO_DUENIO` está autorizado;
cualquier otro valor (o `null`) se rechaza con `ReglaDeNegocioViolada("RN-AG-06", ...)`.

**Por qué**: desacopla la autorización provisoria del modelo `Profesional`
congelado y facilita que `C-15` reemplace este chequeo por RBAC real sin
rehacer la regla de negocio.

### D-C04-5 — Traza de usuario y motivo respetando `EventoAuditoria` congelado

`EventoAuditoria` en `C-01` tiene 4 campos `(turnoId, accion, usuario,
timestamp)` sin campo `detalle`. Para no romper la firma congelada de
`EventoAuditoria` ni a los demás integrantes:
- `RegistroAuditoria.registrar(...)` recibe `new EventoAuditoria(turno.id(),
  AccionAuditoria.SOBRETURNO_AUTORIZADO, solicitud.autorizadoPor(), ahora)`.
- El `motivo` completo, junto con `autorizadoPor` y `autorizadoEn`, se persiste
  en `AutorizacionSobreturno` (1:1 por `turnoId`) a través de
  `AutorizacionSobreturnoRepositorio`.

**Por qué**: satisface la trazabilidad de usuario y motivo sin introducir un
cambio disruptivo en un record compartido del núcleo. Si `C-08` (del mismo
responsable en GATE 2) acuerda ampliar `EventoAuditoria` con `detalle`, la
evolución será directa.

### D-C04-6 — Orden de validaciones y uso de código `"RN-AG-06"`

1. Validaciones de la excepción (`RN-AG-06`): solicitud no nula,
   `esSobreturno == true`, `autorizadoPor` no nulo ni en blanco, `motivo` no
   nulo ni en blanco, `rolAutorizador == ODONTOLOGO_DUENIO`.
2. Validaciones heredadas del alta normal (`RN-AG-08`): existencia de
   `Paciente`, `Profesional`, `Sillon` y `Prestacion`; actividad de profesional,
   sillón y prestación; `inicio` no nulo y estrictamente posterior a
   `LocalDateTime.now(reloj)`.
3. Persistencia del turno (`esSobreturno = true`), persistencia de
   `AutorizacionSobreturno` (1:1), registro de auditoría `SOBRETURNO_AUTORIZADO`
   y notificación `NotificadorPaciente.notificar(turno, SOBRETURNO_AUTORIZADO)`
   dentro de `try/catch` (`RN-NO-03`).

Se pasa el literal `"RN-AG-06"` al constructor `ReglaDeNegocioViolada(String
codigo, String mensaje)` (sin modificar `ReglaDeNegocioViolada.java`) y se
reutiliza la constante existente `ReglaDeNegocioViolada.RN_AG_08` para recursos
e inicio.

## Risks / Trade-offs

- **Desfase temporal entre `EventoAuditoria` (4 campos en `C-01`) y el atributo
  `detalle` de `04_modelo_de_datos.md`** → Mitigación: el motivo queda
  persistido de forma obligatoria y recuperable 1:1 en `AutorizacionSobreturno`,
  y el evento `SOBRETURNO_AUTORIZADO` registra `turnoId`, `usuario` y
  `timestamp`; queda documentado para `C-08` si se incorpora `detalle`.
- **Resolución de `PA-03` en el futuro** → Mitigación: la política de roles está
  encapsulada en `RolAutorizador` / un método privado de autorización en
  `AgendarSobreturno`; habilitar otro rol o migrar a `C-15` es localizado.
- **No se ejecuta validación de disponibilidad, por lo que un sobreturno sobre
  franja libre también se marca `esSobreturno = true`** → Mitigación: comportamiento
  explícitamente previsto por el escenario de borde de `C-04` en `CHANGES.md`
  ("se crea sin romper la regla").
- **Toolchain Java 17 en el entorno local** → Mitigación: `build.gradle` ya fija
  Java 17 y no se modifica; verificar ejecución completa con `gradlew.bat test`
  antes de archivar.
