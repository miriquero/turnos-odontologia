# CHANGES — Secuencia de Implementación

> Índice canónico de todos los changes del proyecto **turnos-odontologia** (motor de disponibilidad de agenda para consultorios odontológicos).
> Cada change es atómico: un integrante puede implementarlo en una sesión (~4-6 horas).
> **Leer este archivo antes de empezar un item.**

Stack: Java + Spring Boot, JUnit 5, arquitectura hexagonal (`ar.edu.turnos.{dominio,aplicacion,infraestructura}`). Sin interfaz gráfica obligatoria. Cada integrante trabaja en su rama y abre un pull request a `master`, que revisa el líder.

---

## Cómo usar este documento

1. Identificar el item a implementar y verificar que sus dependencias figuran completas (`[x]`) en este roadmap.
2. Leer los docs de la knowledge-base indicados en "Leer antes".
3. Acordar el alcance y los criterios de aceptación del item (`openspec new change "<nombre>"`, ciclo OPSX: propose → apply → archive).
4. Implementar con TDD, verificar los escenarios mínimos y registrar la evidencia en el PR.
5. Marcar el checkbox `[x]` en este archivo al completar la verificación técnica.

Conservá los IDs de las fichas al editar o reordenar.

### Prerrequisito bloqueante (no es un change)

> **PA-01 — ¿Maven o Gradle?** — **RESUELTA el 2026-11-19: Gradle**, con wrapper (`gradlew`) versionado. Ver `D-08` en `knowledge-base/09_decisiones_y_supuestos.md`. **Ya no bloquea C-01.**

### Alcance respecto del Discovery (informe sección D.3)

- **MVP imprescindible** = FASES 0, 1 y 2 (C-01 a C-11). Cubre los 11 puntos de D.3 (tabla de trazabilidad al final).
- **Diferenciador** = FASE 3 (C-12 a C-18). **Fuera del MVP**: solo si el MVP queda firme y hay margen.
- **Etapas posteriores, sin change en este roadmap**: WhatsApp Business API real, Mercado Pago, ARCA, Google Calendar, obras sociales/prepagas, caja y cuenta corriente, multi-sede, reportes avanzados, IA, portal con historia clínica. Existen solo como puertos o no existen (`knowledge-base/01_vision_y_objetivos.md` § Fuera de alcance).

### Equipo

| Integrante | GitHub | Rol |
|---|---|---|
| Martín Riquero | `miriquero` | Líder, dueño del change base, revisa todos los PR |
| Julián Fernández Drago | `julianfdrago` | Integrante |
| Augusto Quiroga | `augusto163` | Integrante |
| Maximiliano Di Natale | `maximiliano-di-natale` | Integrante |
| [COMPLETAR: quinto integrante] | [COMPLETAR: usuario GitHub] | Integrante |

---

## Árbol de dependencias

```
[PA-01 RESUELTA: Gradle]      ← prerrequisito ya cumplido
  └── C-01 crear-turno-sin-solapamientos        ← CHANGE BASE (líder). ÚNICO del camino crítico
        │      (modelo + Intervalo + ServicioDisponibilidad + alta con validación
        │       por profesional y por sillón; puertos y EstadoTurno COMPLETOS y CONGELADOS)
        │
        │   ── FASE 1: los 4 cambios paralelos. Dependen SOLO de C-01, NO entre sí ──
        ├── C-02 reprogramar-turno                          [julianfdrago]
        ├── C-03 cancelar-turno-con-anticipacion-minima     [augusto163]
        ├── C-04 agendar-sobreturno-autorizado              [maximiliano-di-natale]
        ├── C-05 registrar-ausencia-y-atencion-de-turno     [quinto integrante]
        │
        │   ── FASE 2: cierre del MVP. También dependen SOLO de C-01 ──
        ├── C-06 administrar-recursos-catalogo-y-pacientes
        ├── C-07 consultar-agenda-y-huecos-libres
        ├── C-08 auditoria-y-notificacion-simulada
        ├── C-09 persistencia-jpa-h2-y-concurrencia
        ├── C-10 api-rest-turnos                  (opcional en v1; depende de C-01 + C-09)
        │
        └── C-11 verificacion-transversal-mvp     ← depende de C-02…C-09 (cierre de MVP)

  ── FASE 3: DIFERENCIADOR — FUERA DEL MVP ──────────────────────────────
  C-11 ✓ (MVP firme)
    ├── C-12 bloqueos-de-agenda                 (+ C-07)
    ├── C-13 reporte-ausentismo-motivos-estructurados (+ C-03, C-05)
    ├── C-14 lista-de-espera-sobre-huecos       (+ C-03, C-07)   ← PA-05
    ├── C-15 roles-y-permisos-efectivos         (+ C-04)         ← PA-03
    ├── C-16 exportacion-completa-de-datos      (+ C-09)
    ├── C-17 reserva-online-paciente            (+ C-07, C-14, C-15)  ← PA-06
    └── C-18 odontograma-y-ficha-clinica        (solo si todo lo anterior está cerrado)
```

### Regla de contención del riesgo R-05 (obligatoria para los cinco)

Los cinco primeros changes tocan la entidad `Turno` y la validación de horarios. Mitigación arquitectónica (`knowledge-base/08_arquitectura_propuesta.md` § Organización para cinco personas; `09_decisiones_y_supuestos.md` `D-03`, `R-05`):

1. **`Intervalo` y `ServicioDisponibilidad` se escriben UNA sola vez, en C-01, y después NO se modifican.** Ningún change de FASE 1 ni FASE 2 abre esos archivos. Un PR que los toque se rechaza salvo acuerdo explícito del líder.
2. **Cada change agrega su propio caso de uso en archivos NUEVOS** (`aplicacion/casouso/ReprogramarTurno`, `CancelarTurno`, `AutorizarSobreturno`, `RegistrarAusencia`, ...). Nadie amplía un caso de uso existente.
3. **`EstadoTurno` (estados + tabla de transiciones de `RN-TU-02`) y el enum de acciones de `EventoAuditoria` se escriben completos en C-01.** Ningún change los modifica.
4. **Los puertos (`TurnoRepositorio` con `buscarPorId`, `guardar`, `buscarVigentesSolapados(...)` que acepta `excluirTurnoId`; `RegistroAuditoria`; `NotificadorPaciente`) se definen completos en C-01.** Los changes posteriores solo los consumen. Si falta un método, se pide un hotfix al líder sobre C-01, no se agrega en el change propio.
5. El líder mergea de a un PR por vez; el resto hace rebase sobre `master` antes de pedir revisión.
6. **Única excepción prevista**: C-12 (bloqueos, FASE 3, fuera del MVP) puede extender la disponibilidad, y solo después de cerrar el MVP. Para que no edite `ServicioDisponibilidad`, C-01 lo hace depender de un puerto de lectura de ocupación (ver C-01 Scope); C-12 agrega una segunda fuente de ocupación sin tocar el servicio.

### Paralelismo por fase

> Cada "gate" es un punto de sincronización. Los changes dentro de un grupo pueden ejecutarse en paralelo.

```
GATE 0: PA-01 resuelta (Gradle)
  → C-01 crear-turno-sin-solapamientos            [Líder — miriquero] (solo; nada más puede arrancar)

GATE 1: C-01 ✓ mergeado en master                 ← FORK (hasta 5 paralelos; 5 personas)
  → C-02 reprogramar-turno                         [julianfdrago]
  → C-03 cancelar-turno-con-anticipacion-minima    [augusto163]
  → C-04 agendar-sobreturno-autorizado             [maximiliano-di-natale]
  → C-05 registrar-ausencia-y-atencion-de-turno    [quinto integrante]
  → C-07 consultar-agenda-y-huecos-libres          [Líder, mientras revisa PRs]

GATE 2: cada integrante libera su change de FASE 1 (toma el próximo sin esperar a los demás)
  → C-06 administrar-recursos-catalogo-y-pacientes [julianfdrago, al terminar C-02]
  → C-09 persistencia-jpa-h2-y-concurrencia        [augusto163, al terminar C-03]
  → C-08 auditoria-y-notificacion-simulada         [maximiliano-di-natale, al terminar C-04]
  → C-10 api-rest-turnos (opcional)                [quinto, al terminar C-05 y si C-09 ✓]

GATE 3: C-02…C-09 ✓ (C-10 no bloquea)
  → C-11 verificacion-transversal-mvp              [Líder] ← CIERRE DEL MVP

GATE 4: C-11 ✓  — SOLO DIFERENCIADOR, FUERA DEL MVP
  → C-12 bloqueos-de-agenda
  → C-13 reporte-ausentismo-motivos-estructurados
  → C-14 lista-de-espera-sobre-huecos
  → C-15 roles-y-permisos-efectivos
  → C-16 exportacion-completa-de-datos

GATE 5: C-14 + C-15 + C-07 ✓
  → C-17 reserva-online-paciente

GATE 6: todo lo anterior ✓ (opcional)
  → C-18 odontograma-y-ficha-clinica
```

**Qué NO se puede hacer en paralelo**: nada arranca antes de que C-01 esté mergeado; C-11 espera a C-02…C-09; ningún change de FASE 3 arranca antes de C-11; C-17 espera a C-14 y C-15. **Qué sí**: C-02…C-09 son mutuamente independientes (cada uno solo importa clases de C-01 y escribe archivos nuevos). Mientras el líder trabaja C-01, el resto puede preparar (sin mergear) sus propuestas OPSX (`proposal.md`, `tasks.md`) leyendo los "Leer antes" de su change.

### Camino crítico (3 changes — mínimo irreducible)

```
[PA-01 ok] → C-01 crear-turno-sin-solapamientos → C-11 verificacion-transversal-mvp
```

C-01 es el único change del camino crítico propiamente dicho: todo el resto del MVP cuelga de él y es paralelo. C-11 cierra el MVP pero es verificación, sin código de producción nuevo. El MVP funcional mínimo de agenda es C-01 + C-02 + C-03 + C-04 + C-05.

### Plan óptimo con 5 integrantes (multi-agente)

```
Paso │ Líder (miriquero)         │ julianfdrago        │ augusto163           │ maximiliano-di-natale │ [quinto integrante]
─────┼───────────────────────────┼─────────────────────┼──────────────────────┼───────────────────────┼──────────────────────
  0  │ Decide PA-01              │        —            │         —            │          —            │         —
  1  │ C-01 (base, OPSX completo)│ prepara propuesta   │ prepara propuesta    │ prepara propuesta     │ prepara propuesta
  2  │ revisa PRs + C-07         │ C-02 reprogramar    │ C-03 cancelar        │ C-04 sobreturno       │ C-05 ausencia/atendido
  3  │ revisa PRs                │ C-06 recursos       │ C-09 persistencia    │ C-08 auditoría/notif. │ C-10 API REST (opc.)
  4  │ C-11 verificación MVP     │        —            │         —            │          —            │         —
  5  │ (diferenciador, fuera del MVP, según margen: C-12 a C-18)
```

### Ramas sugeridas y PR

| Change | Responsable | Rama | Revisa |
|---|---|---|---|
| C-01 | miriquero | `feat/crear-turno-sin-solapamientos` | el equipo valida contra los 3 escenarios |
| C-02 | julianfdrago | `feat/reprogramar-turno` | miriquero |
| C-03 | augusto163 | `feat/cancelar-turno-con-anticipacion-minima` | miriquero |
| C-04 | maximiliano-di-natale | `feat/agendar-sobreturno-autorizado` | miriquero |
| C-05 | [COMPLETAR: quinto integrante] | `feat/registrar-ausencia-y-atencion-de-turno` | miriquero |
| C-06 | julianfdrago | `feat/administrar-recursos-catalogo-y-pacientes` | miriquero |
| C-07 | miriquero | `feat/consultar-agenda-y-huecos-libres` | otro integrante |
| C-08 | maximiliano-di-natale | `feat/auditoria-y-notificacion-simulada` | miriquero |
| C-09 | augusto163 | `feat/persistencia-jpa-h2-y-concurrencia` | miriquero |
| C-10 | [COMPLETAR: quinto integrante] | `feat/api-rest-turnos` | miriquero |
| C-11 | miriquero | `feat/verificacion-transversal-mvp` | otro integrante |
| C-12 a C-18 | a definir | `feat/<nombre-del-change>` | miriquero |

---

## FASE 0 — Cimientos y núcleo (change base)

> Único change del camino crítico. **Es el que se lleva por el ciclo OPSX completo (explore → propose → apply → archive) en la defensa del trabajo.**

### [C-01] `crear-turno-sin-solapamientos`
- **Estado**: `[ ]` pendiente
- **Responsable**: Martín Riquero (`miriquero`) — rama `feat/crear-turno-sin-solapamientos`
- **Scope**: Modelo de dominio + `Intervalo` + `ServicioDisponibilidad` + alta de turno con validación por profesional y por sillón. Se escribe UNA vez y queda congelado (ver "Regla de contención R-05").
  - Scaffolding con **Gradle** (`D-08`): `build.gradle` + wrapper `gradlew` versionado, Spring Boot, JUnit 5, Mockito, paquetes `ar.edu.turnos.{dominio,aplicacion,infraestructura}`; el dominio **no importa Spring, JPA ni HTTP**
  - `dominio.modelo`: `Profesional`, `Sillon`, `Prestacion` (`duracionMinutos > 0`), `Paciente` (ficha básica), `Turno`, `EstadoTurno` (`AGENDADO`, `ATENDIDO`, `CANCELADO`, `AUSENTE` **con la tabla de transiciones de `RN-TU-02` y la marca "ocupa agenda" de `RN-AG-07` ya incluidas**), `AutorizacionSobreturno` (solo la estructura, sin caso de uso), `EventoAuditoria` con **todas** sus acciones (`CREADO`, `MODIFICADO`, `REPROGRAMADO`, `CANCELADO`, `MARCADO_ATENDIDO`, `MARCADO_AUSENTE`, `SOBRETURNO_AUTORIZADO`)
  - `Intervalo` (objeto de valor): invariante `fin > inicio`; único método de solapamiento `seSolapaCon(Intervalo)` con el predicado `A.inicio < B.fin && B.inicio < A.fin` (`<` estricto)
  - `ServicioDisponibilidad`: `verificarDisponibilidad(SolicitudDeTurno)` (lanza `ReglaDeNegocioViolada` con código `RN-AG-02` o `RN-AG-03`) y `evaluar(SolicitudDeTurno)` (no excepcional, para consultas y huecos). La solicitud acepta `excluirTurnoId` (lo consume C-02). Evalúa profesional y sillón **por separado**; solo cuentan turnos vigentes (`RN-AG-07`); no conoce el sobreturno. Lee la ocupación a través de un puerto de lectura (`FuenteDeOcupacion`, implementado sobre `TurnoRepositorio`) para que C-12 agregue bloqueos sin editar el servicio. El cálculo de huecos libres NO va en el servicio: lo hace C-07 usando `evaluar`
  - `dominio.excepcion`: `ReglaDeNegocioViolada` + subtipos con el **código de regla** en el mensaje
  - Puertos completos y congelados: `TurnoRepositorio` (`buscarPorId`, `guardar`, `buscarVigentesSolapados(profesionalId|sillonId, Intervalo, excluirTurnoId)`), `ProfesionalRepositorio`, `SillonRepositorio`, `PrestacionRepositorio`, `PacienteRepositorio`, `RegistroAuditoria`, `NotificadorPaciente`; dobles en memoria en `src/test`
  - `aplicacion.casouso.AgendarTurno`: valida recursos existentes y activos (`RN-AG-08`, inicio futuro), deriva `fin = inicio + duración de la prestación` (no se puede fijar a mano, `RN-AG-01`), invoca `ServicioDisponibilidad`, crea el turno `AGENDADO`, registra `CREADO` en auditoría con usuario obligatorio, dispara el puerto de notificación (un fallo de notificación no invalida el alta, `RN-NO-03`)
  - Tests (JUnit 5, sin Spring): ver escenarios mínimos
- **Dependencias**: ninguna (prerrequisito PA-01 **ya resuelto**: Gradle, `D-08`)
- **Governance**: CRITICO (modelo core que todo lo demás referencia; regla que define el producto)
- **Historias**: US-006, US-007, US-008, US-009 (núcleo) y la parte de dominio de US-001, US-002, US-003, US-004, US-010 (estructura), US-021 (casos borde de `Intervalo`)
- **Reglas**: RN-AG-01, RN-AG-02, RN-AG-03, RN-AG-04, RN-AG-07, RN-AG-08, RN-TU-01, RN-TU-02, RN-CA-01, RN-AU-01, RN-NO-01, RN-NO-03, RN-PA-04
- **Escenarios mínimos de la spec (los tres obligatorios)**:
  1. **Camino feliz**: limpieza de 30 min a las 10:00 con profesional y sillón libres → turno `AGENDADO`, ocupa `[10:00, 10:30)`, evento `CREADO` registrado.
  2. **Error de negocio por solapamiento**: nuevo `[10:15, 10:45)` sobre existente `[10:00, 10:30)` del mismo profesional → `ReglaDeNegocioViolada` con `RN-AG-02`; mismo sillón con otro profesional → `RN-AG-03`; cada validación puede fallar sola.
  3. **Borde de turnos contiguos (intervalos semiabiertos)**: existente `[10:00, 10:30)` + nuevo `[10:30, 11:00)` mismo profesional y sillón → **aceptado**; y `[09:30, 10:00)` → **aceptado**.
  - Complementarios: los 9 casos de la tabla de `RN-AG-04` con un test cada uno sobre `Intervalo`; turno `CANCELADO`/`AUSENTE` no bloquea; recurso inactivo rechazado; inicio en el pasado rechazado; `fin` no escribible a mano.
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-AG-01`, `RN-AG-02`, `RN-AG-03`, `RN-AG-04` (tabla de 9 casos), `RN-AG-07`, `RN-AG-08`
  - `knowledge-base/04_modelo_de_datos.md` → entidades `Turno`, `Profesional`, `Sillon`, `Prestacion`, `Paciente`, § Estados y transiciones
  - `knowledge-base/08_arquitectura_propuesta.md` → § Estructura de paquetes, § El núcleo: `ServicioDisponibilidad`, § Organización para cinco personas en paralelo
  - `knowledge-base/07_flujos_principales.md` → F-01 y § Invariante transversal
  - `knowledge-base/09_decisiones_y_supuestos.md` → `D-01`, `D-02`, `D-03`, `R-05`; `10_preguntas_abiertas.md` → `PA-01`

---

## FASE 1 — Ciclo de vida del turno (4 changes en paralelo)

> Los cuatro dependen **solo de C-01** y no entre sí. Cada uno agrega su caso de uso en **archivos nuevos** y no modifica `Intervalo`, `ServicioDisponibilidad`, `EstadoTurno` ni los puertos.

### [C-02] `reprogramar-turno`
- **Estado**: `[ ]` pendiente
- **Responsable**: Julián Fernández Drago (`julianfdrago`) — rama `feat/reprogramar-turno`
- **Scope**:
  - `aplicacion.casouso.ReprogramarTurno` (archivo nuevo): cambia `inicio` y opcionalmente profesional, sillón y prestación; recalcula `fin` (`RN-AG-01`)
  - Revalida con `ServicioDisponibilidad` pasando `excluirTurnoId = turno.id` (un turno no se solapa consigo mismo); la franja anterior queda libre en el mismo acto
  - Solo desde `AGENDADO`: `CANCELADO`, `ATENDIDO` y `AUSENTE` se rechazan con `RN-TU-04`
  - Auditoría `REPROGRAMADO` con horario anterior y nuevo (y recurso anterior/nuevo si cambió, `RN-AU-03`); notificación vía puerto
  - Tests: ver escenarios mínimos
- **Dependencias**: `C-01`
- **Governance**: MEDIO
- **Historias**: US-011, US-012
- **Reglas**: RN-TU-03, RN-TU-04, RN-AG-01, RN-AG-02, RN-AG-03, RN-AG-04, RN-AU-03
- **Escenarios mínimos**:
  1. Camino feliz: mover `[10:00, 10:30)` a `[11:00, 11:30)` libre → nuevo horario, franja anterior libre, auditoría `REPROGRAMADO`.
  2. Error por solapamiento: mover sobre un turno ajeno del mismo profesional → `RN-AG-02`; del mismo sillón → `RN-AG-03`.
  3. **Borde**: reprogramar a un horario contiguo `[10:30, 11:00)` de otro turno → aceptado (`RN-AG-04`); reprogramar al **mismo horario** que ya tenía → aceptado e idempotente (se excluye a sí mismo).
  4. Turno `CANCELADO` / `ATENDIDO` / `AUSENTE` → `RN-TU-04`.
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-TU-03`, `RN-TU-04`, `RN-AG-01`, `RN-AU-03`
  - `knowledge-base/07_flujos_principales.md` → F-02
  - `knowledge-base/06_funcionalidades.md` → US-011, US-012
  - `knowledge-base/08_arquitectura_propuesta.md` → § Organización para cinco personas en paralelo

### [C-03] `cancelar-turno-con-anticipacion-minima`
- **Estado**: `[ ]` pendiente
- **Responsable**: Augusto Quiroga (`augusto163`) — rama `feat/cancelar-turno-con-anticipacion-minima`
- **Scope**:
  - `aplicacion.casouso.CancelarTurno` (archivo nuevo): exige motivo no vacío; transición `AGENDADO → CANCELADO` (terminal); guarda motivo y marca de tiempo
  - Anticipación mínima `N` horas **configurable** (propiedad de configuración, no constante); con menos de `N` horas → `RN-AG-05`; con **exactamente** `N` horas → aceptado (límite inclusivo, `D-02`); valor por defecto declarado como supuesto `SU-03`
  - Reloj inyectable (`Clock`) para testear sin depender de la hora real
  - Liberación inmediata: tras cancelar, una nueva alta sobre la franja se acepta (`RN-AG-07`)
  - Auditoría `CANCELADO` con usuario y motivo; notificación vía puerto
  - Tests: ver escenarios mínimos
- **Dependencias**: `C-01`
- **Governance**: MEDIO
- **Historias**: US-013, US-014
- **Reglas**: RN-AG-05, RN-AG-07, RN-TU-02, RN-TU-05, RN-AU-01
- **Escenarios mínimos**:
  1. Camino feliz: cancelar con motivo y con más de `N` horas → `CANCELADO`, auditoría con motivo.
  2. Error de negocio: con menos de `N` horas → `RN-AG-05`; sin motivo → rechazado; turno ya `CANCELADO` o `ATENDIDO` → `RN-TU-02`.
  3. **Borde**: cancelar con exactamente `N` horas → aceptado; y alta posterior sobre la franja liberada, incluso contigua, aceptada (usa `AgendarTurno` de C-01 sin modificarlo).
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-AG-05`, `RN-AG-07`, `RN-TU-02`, `RN-TU-05`
  - `knowledge-base/07_flujos_principales.md` → F-03
  - `knowledge-base/06_funcionalidades.md` → US-013, US-014
  - `knowledge-base/09_decisiones_y_supuestos.md` → `SU-03`, `D-02`; `10_preguntas_abiertas.md` → `PA-04`

### [C-04] `agendar-sobreturno-autorizado`
- **Estado**: `[ ]` pendiente
- **Responsable**: Maximiliano Di Natale (`maximiliano-di-natale`) — rama `feat/agendar-sobreturno-autorizado`
- **Scope**:
  - `aplicacion.casouso.AgendarSobreturno` (archivo nuevo): la decisión de saltar la verificación vive **en el caso de uso**; `ServicioDisponibilidad` nunca se modifica ni devuelve "disponible" para un intervalo ocupado
  - Exige marca explícita, `autorizadoPor` y motivo; sin cualquiera de los tres → rechazado (`RN-AG-06`); nunca hay reintento automático tras un rechazo
  - Salta **solo** `RN-AG-02`/`RN-AG-03`; siguen aplicando recursos activos, duración derivada de la prestación y estados válidos
  - Persiste `AutorizacionSobreturno` (1:1 con el turno) y marca `esSobreturno = true`
  - Autorización solo del odontólogo dueño (supuesto `SU-04`, `PA-03`): chequeo mínimo por rol, sin framework de seguridad (los roles efectivos son C-15)
  - Auditoría `SOBRETURNO_AUTORIZADO` con usuario y motivo
  - Tests: ver escenarios mínimos
- **Dependencias**: `C-01`
- **Governance**: ALTO (única excepción a la regla central; requiere autorización y auditoría)
- **Historias**: US-010
- **Reglas**: RN-AG-06, RN-AG-02, RN-AG-03, RN-AG-08, RN-AU-01
- **Escenarios mínimos**:
  1. Camino feliz: solapamiento + autorización del dueño + motivo → turno con `esSobreturno = true`, `AutorizacionSobreturno` y evento `SOBRETURNO_AUTORIZADO`.
  2. Error de negocio: sin marca, sin responsable o sin motivo → rechazado; usuario sin permiso (`COLEGA`/recepción) → rechazado; recurso inactivo sigue rechazado aun con autorización.
  3. **Borde**: sobreturno sobre franja libre o solo contigua → se crea sin romper la regla; el alta normal sobre franja ocupada sigue rechazada (el sobreturno no cambia el comportamiento por defecto).
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-AG-06`, `RN-AU-01`
  - `knowledge-base/07_flujos_principales.md` → F-04
  - `knowledge-base/04_modelo_de_datos.md` → `AutorizacionSobreturno`, `EventoAuditoria`
  - `knowledge-base/09_decisiones_y_supuestos.md` → `D-05`, `SU-04`; `10_preguntas_abiertas.md` → `PA-03`

### [C-05] `registrar-ausencia-y-atencion-de-turno`
- **Estado**: `[ ]` pendiente
- **Responsable**: [COMPLETAR: quinto integrante] — rama `feat/registrar-ausencia-y-atencion-de-turno`
- **Scope**:
  - `aplicacion.casouso.RegistrarAusencia` (archivo nuevo): `AGENDADO → AUSENTE` (terminal), motivo opcional estructurado; no se aplica la anticipación mínima (`RN-TU-06`); solo si el instante de inicio ya pasó (turno futuro rechazado); turno `CANCELADO` rechazado (`RN-TU-02`)
  - `aplicacion.casouso.MarcarTurnoAtendido` (archivo nuevo): `AGENDADO → ATENDIDO` (terminal), sigue ocupando su franja histórica (`RN-AG-07`)
  - La franja de un turno `AUSENTE` no bloquea un turno nuevo (verificado con `AgendarTurno` de C-01, sin modificarlo)
  - Auditoría `MARCADO_AUSENTE` y `MARCADO_ATENDIDO`; `AUSENTE` es un estado distinto de `CANCELADO`
  - Reloj inyectable (`Clock`)
  - Tests: ver escenarios mínimos
- **Dependencias**: `C-01`
- **Governance**: MEDIO
- **Historias**: US-016 (ausencia) y US-015 (atendido)
- **Reglas**: RN-TU-02, RN-TU-06, RN-AG-07, RN-AU-01
- **Escenarios mínimos**:
  1. Camino feliz: turno `AGENDADO` ya vencido → `AUSENTE` con motivo opcional; auditoría `MARCADO_AUSENTE`; y turno vencido → `ATENDIDO` con auditoría `MARCADO_ATENDIDO`.
  2. Error de negocio: turno futuro → rechazado; turno `CANCELADO` → `RN-TU-02`; desde `ATENDIDO`/`AUSENTE` ninguna transición de salida.
  3. **Borde**: tras marcar `AUSENTE`, un alta sobre la misma franja se acepta; tras `ATENDIDO` la misma franja sigue ocupada (`RN-AG-07`); el instante exacto de inicio vs. "ahora" se define y se testea.
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-TU-02`, `RN-TU-06`, `RN-AG-07`
  - `knowledge-base/07_flujos_principales.md` → F-05
  - `knowledge-base/06_funcionalidades.md` → US-015, US-016
  - `knowledge-base/04_modelo_de_datos.md` → § Estados y transiciones

---

## FASE 2 — Cierre del MVP (dependen solo de C-01, salvo C-10 y C-11)

> Mismo criterio que la FASE 1: archivos nuevos, sin tocar el núcleo. Se asignan a quien termine antes su change de FASE 1.

### [C-06] `administrar-recursos-catalogo-y-pacientes`
- **Estado**: `[ ]` pendiente
- **Responsable**: Julián Fernández Drago (`julianfdrago`) — rama `feat/administrar-recursos-catalogo-y-pacientes`
- **Scope**:
  - Casos de uso nuevos: `AdministrarProfesionales`, `AdministrarSillones`, `AdministrarPrestaciones`, `RegistrarPaciente`, `BuscarPaciente`
  - Unicidad de matrícula, nombre de sillón, nombre de prestación y documento (`RN-CA-02`, `RN-CA-03`, `RN-PA-02`); duración `<= 0` rechazada; duraciones configurables (supuesto `SU-02`)
  - Desactivar en lugar de borrar (`RN-CA-05`, `RN-PA-05`); no desactivar un recurso con turnos `AGENDADO` futuros (`RN-CA-04`), consultando `TurnoRepositorio` de C-01
  - Cambiar la duración de una prestación **no** recalcula turnos ya agendados
  - Paciente con al menos un canal de contacto (`RN-PA-03`); búsqueda por documento exacto y apellido parcial
  - Tests con datos **ficticios** únicamente (`RN-PA-04`)
- **Dependencias**: `C-01`
- **Governance**: BAJO
- **Historias**: US-001, US-002, US-003, US-004, US-005
- **Reglas**: RN-CA-01…05, RN-PA-01…05, RN-AG-08
- **Escenarios mínimos**: alta válida de cada recurso; duplicado de matrícula/documento rechazado; duración `0` rechazada; desactivar profesional con turno futuro rechazado y sin turnos aceptado; **borde**: el profesional inactivo ya no acepta turnos (el siguiente `AgendarTurno` falla con `RN-AG-08`).
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` → US-001 a US-005
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-CA-01`…`RN-CA-05`, `RN-PA-01`…`RN-PA-05`
  - `knowledge-base/04_modelo_de_datos.md` → `Profesional`, `Sillon`, `Prestacion`, `Paciente`, § Seed data inicial

### [C-07] `consultar-agenda-y-huecos-libres`
- **Estado**: `[ ]` pendiente
- **Responsable**: Martín Riquero (`miriquero`) — rama `feat/consultar-agenda-y-huecos-libres`
- **Scope**:
  - `aplicacion.casouso.ConsultarAgenda` y `ConsultarDisponibilidad` (archivos nuevos)
  - Agenda por profesional, por sillón o ambos, en un rango, con filtro por estado; los sobreturnos se distinguen en el resultado
  - Huecos libres para una prestación: tamaño = duración de la prestación; se calcula **reutilizando `ServicioDisponibilidad.evaluar(...)`** sobre franjas candidatas (no se reimplementa el solapamiento ni se modifica el servicio); un hueco que empieza donde termina otro turno es válido
  - Propiedad verificable: si la consulta dice "libre", el alta sobre esa franja no falla por solapamiento
  - Tests: ver escenarios mínimos
- **Dependencias**: `C-01`
- **Governance**: MEDIO
- **Historias**: US-017, US-018
- **Reglas**: RN-AG-09, RN-AG-04, RN-AG-06, RN-AG-07
- **Escenarios mínimos**: agenda del día filtrada por profesional y por sillón; huecos libres para un conducto de 90 min con un turno intermedio; **borde**: hueco que empieza exactamente cuando termina el turno anterior es válido y turnos `CANCELADO`/`AUSENTE` no reducen huecos; propiedad consulta-libre ⇒ alta exitosa.
- **Leer antes**:
  - `knowledge-base/07_flujos_principales.md` → F-06
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-AG-09`, `RN-AG-04`, `RN-AG-07`
  - `knowledge-base/06_funcionalidades.md` → US-017, US-018
  - `knowledge-base/08_arquitectura_propuesta.md` → § El núcleo: `ServicioDisponibilidad`

### [C-08] `auditoria-y-notificacion-simulada`
- **Estado**: `[ ]` pendiente
- **Responsable**: Maximiliano Di Natale (`maximiliano-di-natale`) — rama `feat/auditoria-y-notificacion-simulada`
- **Scope**:
  - `infraestructura.notificacion.AdaptadorNotificacionSimulado`: implementa `NotificadorPaciente`, registra el envío, **no sale a la red**; el mensaje solo trae fecha, hora, profesional y prestación (sin datos clínicos, `RN-NO-04`)
  - Adaptador de `RegistroAuditoria` append-only (sin update ni delete, `RN-AU-02`); `aplicacion.casouso.ConsultarHistorialDeTurno`
  - Operación sin usuario identificable se rechaza (`RN-AU-01`)
  - Resiliencia: el fallo de notificación no invalida la operación (`RN-NO-03`)
  - Tests: ver escenarios mínimos (sin credenciales ni red)
- **Dependencias**: `C-01`
- **Governance**: CRITICO (audit trail sobre datos de salud, Ley 25.326)
- **Historias**: US-019, US-020
- **Reglas**: RN-NO-01…04, RN-AU-01, RN-AU-02, RN-AU-03
- **Escenarios mínimos**: cada operación deja evento con usuario y timestamp; historial completo de un turno consultable; **borde**: sin usuario → rechazado; intento de modificar o borrar un evento → imposible; notificador que lanza excepción → el alta igual se completa.
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-NO-01`…`RN-NO-04`, `RN-AU-01`…`RN-AU-03`
  - `knowledge-base/06_funcionalidades.md` → US-019, US-020
  - `knowledge-base/04_modelo_de_datos.md` → `EventoAuditoria`
  - `knowledge-base/09_decisiones_y_supuestos.md` → `D-04`, `R-04`

### [C-09] `persistencia-jpa-h2-y-concurrencia`
- **Estado**: `[ ]` pendiente
- **Responsable**: Augusto Quiroga (`augusto163`) — rama `feat/persistencia-jpa-h2-y-concurrencia`
- **Scope**:
  - `infraestructura.persistencia`: adaptadores Spring Data JPA de los puertos de C-01 (entidades JPA separadas o anotadas, decisión menor abierta); H2 en desarrollo y tests, PostgreSQL como destino
  - Índices `(profesional_id, inicio, fin)` y `(sillon_id, inicio, fin)` filtrando estados vigentes
  - Concurrencia (`R-06`/`PA-07`): decidir y aplicar restricción de exclusión en base o bloqueo pesimista; **no alcanza con validar antes de insertar**
  - Seed ficticio (`Ana Gomez`/`Luis Pereyra`, 3 sillones, 5 prestaciones, 3 pacientes) incluyendo **un par de turnos contiguos** (10:00-10:30 y 10:30-11:00, mismo profesional y sillón)
  - Tests: `@DataJpaTest` con H2 donde la consulta de solapamiento devuelve lo mismo que el dominio; test de dos altas simultáneas sobre la misma franja
- **Dependencias**: `C-01`
- **Governance**: ALTO
- **Historias**: soporte de US-006 a US-009 (persistencia) y US-021 (repositorios)
- **Reglas**: RN-AG-02, RN-AG-03, RN-AG-04, RN-AG-07, RN-PA-04, y § Excepciones globales (concurrencia)
- **Escenarios mínimos**: la consulta JPA acepta contiguos y rechaza solapes igual que `Intervalo`; excluye `excluirTurnoId`; ignora `CANCELADO`/`AUSENTE`; **borde**: dos altas concurrentes sobre la misma franja → una sola persiste.
- **Leer antes**:
  - `knowledge-base/08_arquitectura_propuesta.md` → § Persistencia, § Estrategia de pruebas
  - `knowledge-base/04_modelo_de_datos.md` → `Turno` (índices), § Seed data inicial
  - `knowledge-base/05_reglas_de_negocio.md` → § Excepciones globales
  - `knowledge-base/09_decisiones_y_supuestos.md` → `R-06`; `10_preguntas_abiertas.md` → `PA-07`

### [C-10] `api-rest-turnos`
- **Estado**: `[ ]` pendiente — **opcional en v1** (la consigna admite módulo de dominio sin interfaz)
- **Responsable**: [COMPLETAR: quinto integrante] — rama `feat/api-rest-turnos`
- **Scope**:
  - `infraestructura.rest`: controladores + DTOs sobre los casos de uso de C-01…C-08 (agendar, reprogramar, cancelar, sobreturno, ausencia, atendido, agenda, huecos)
  - Manejador de errores que devuelve el **código de regla** (`RN-AG-04`, etc.) en la respuesta
  - Usuario identificable en cada request (cabecera simple; sin autenticación real en v1)
  - Tests `@WebMvcTest` con MockMvc: códigos de estado y forma del error
- **Dependencias**: `C-01`, `C-09` (y los casos de uso que exponga)
- **Governance**: BAJO
- **Historias**: expone US-006…US-018
- **Reglas**: § Excepciones globales (código de regla en la respuesta), RN-AU-01
- **Escenarios mínimos**: alta feliz → 201; solapamiento → 409 con `RN-AG-02`; **borde** contiguo → 201; sin usuario → rechazado.
- **Leer antes**:
  - `knowledge-base/08_arquitectura_propuesta.md` → § Estructura de paquetes (`infraestructura/rest`), § Estrategia de pruebas
  - `knowledge-base/05_reglas_de_negocio.md` → § Excepciones globales
  - `knowledge-base/07_flujos_principales.md` → F-01 a F-06

### [C-11] `verificacion-transversal-mvp`
- **Estado**: `[ ]` pendiente
- **Responsable**: Martín Riquero (`miriquero`) — rama `feat/verificacion-transversal-mvp`
- **Scope**: sin código de producción nuevo; cierre y evidencia del MVP
  - Verifica que la suite cubre: los 9 casos de `RN-AG-04`, `RN-AG-02`/`RN-AG-03` por separado y combinados, todas las transiciones válidas e inválidas de `RN-TU-02`, no reprogramar `CANCELADO`/`ATENDIDO`/`AUSENTE`, sobreturno con y sin autorización
  - Test de arquitectura (p. ej. ArchUnit): `dominio` no importa Spring/JPA/HTTP; `ServicioDisponibilidad` sigue siendo la única implementación del solapamiento
  - Confirma que la suite corre sin red ni credenciales, que ningún dato real de paciente está en el repo (`RN-PA-04`) y que `Intervalo`/`ServicioDisponibilidad` no cambiaron desde C-01 (diff)
  - Revisa las métricas de éxito de `01_vision_y_objetivos.md` y confirma `PA-02`, `PA-03` y `PA-04` antes de dar el MVP por terminado
- **Dependencias**: `C-02, C-03, C-04, C-05, C-06, C-07, C-08, C-09`
- **Governance**: BAJO
- **Historias**: US-021
- **Reglas**: todas las de `RN-AG` y `RN-TU`, RN-PA-04
- **Escenarios mínimos**: reporte de cobertura por regla (cada `RN-AG-*`/`RN-TU-*` tiene al menos un test); test de arquitectura en verde; suite completa en verde sin red.
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` → US-021
  - `knowledge-base/01_vision_y_objetivos.md` → § Métricas de éxito
  - `knowledge-base/08_arquitectura_propuesta.md` → § Estrategia de pruebas
  - `knowledge-base/10_preguntas_abiertas.md` → § Qué hacer con esta lista

---

## FASE 3 — Bloque diferenciador (**FUERA DEL MVP**)

> **Ningún change de esta fase es parte del MVP.** Arrancan solo con C-11 cerrado y si hay margen (`knowledge-base/06_funcionalidades.md`, Épica 6). Varios tienen preguntas abiertas que condicionan el diseño. Sin responsable ni rama asignados hasta que el MVP esté firme.

### [C-12] `bloqueos-de-agenda`
- **Estado**: `[ ]` pendiente — fuera del MVP
- **Scope**: entidad `BloqueoAgenda` (profesional y/o sillón, `[inicio, fin)`, motivo `VACACIONES`/`AUSENCIA_PROFESIONAL`/`MANTENIMIENTO_SILLON`); participa de la disponibilidad como una **segunda fuente de ocupación** del puerto `FuenteDeOcupacion` (sin editar `ServicioDisponibilidad`; si el puerto de C-01 resultara insuficiente, es la única modificación permitida al núcleo, acordada con el líder); impacta los huecos de C-07. Tests: bloqueo solapado rechaza el alta; **borde** turno contiguo a un bloqueo aceptado.
- **Dependencias**: `C-11`, `C-07`
- **Governance**: CRITICO (modifica la fuente de verdad de la disponibilidad)
- **Historias**: US-025 · **Reglas**: RN-DI-01, RN-AG-04, RN-AG-09
- **Leer antes**:
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-DI-01`
  - `knowledge-base/04_modelo_de_datos.md` → § Entidades del bloque diferenciador
  - `knowledge-base/08_arquitectura_propuesta.md` → § El núcleo: `ServicioDisponibilidad`

### [C-13] `reporte-ausentismo-motivos-estructurados`
- **Estado**: `[ ]` pendiente — fuera del MVP
- **Scope**: catálogo de motivos de cancelación y ausencia estructurados y reporte por profesional, sillón y prestación. Tests: agregación correcta; turnos sin motivo se cuentan aparte.
- **Dependencias**: `C-11`, `C-03`, `C-05`
- **Governance**: MEDIO
- **Historias**: US-024 · **Reglas**: RN-TU-05, RN-TU-06
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` → US-024
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-TU-05`, `RN-TU-06`
  - `knowledge-base/04_modelo_de_datos.md` → `Turno` (`motivoCancelacion`, `motivoAusencia`)

### [C-14] `lista-de-espera-sobre-huecos`
- **Estado**: `[ ]` pendiente — fuera del MVP
- **Scope**: `EntradaListaEspera` (paciente, prestación, franja, prioridad, estado); al cancelar un turno el hueco queda ofrecible y se consulta por prestación y franja; sin integración externa. Condicionada por `PA-05`. Tests: cancelación genera hueco ofrecible; **borde** hueco contiguo válido.
- **Dependencias**: `C-11`, `C-03`, `C-07`
- **Governance**: MEDIO
- **Historias**: US-023 · **Reglas**: RN-DI-02
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` → US-023
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-DI-02`
  - `knowledge-base/10_preguntas_abiertas.md` → `PA-05`

### [C-15] `roles-y-permisos-efectivos`
- **Estado**: `[ ]` pendiente — fuera del MVP
- **Scope**: roles odontólogo dueño / colega / recepción con la matriz RBAC aplicada a las operaciones de agenda, en particular a la autorización de sobreturnos (reemplaza el chequeo mínimo de C-04). Cierra `PA-03`. Tests: cada rol permitido/denegado por operación.
- **Dependencias**: `C-11`, `C-04`
- **Governance**: CRITICO (autorización)
- **Historias**: US-027 · **Reglas**: RN-AG-06 y matriz de `03_actores_y_roles.md`
- **Leer antes**:
  - `knowledge-base/03_actores_y_roles.md`
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-AG-06`
  - `knowledge-base/10_preguntas_abiertas.md` → `PA-03`

### [C-16] `exportacion-completa-de-datos`
- **Estado**: `[ ]` pendiente — fuera del MVP
- **Scope**: exportación completa de datos del consultorio en formato abierto (CSV/JSON); datos ficticios en tests. Tests: el export reimporta sin pérdida.
- **Dependencias**: `C-11`, `C-09`
- **Governance**: ALTO (datos de salud, `PA-09`)
- **Historias**: US-026 · **Reglas**: RN-DI-04, RN-PA-04
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` → US-026
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-DI-04`
  - `knowledge-base/10_preguntas_abiertas.md` → `PA-09`

### [C-17] `reserva-online-paciente`
- **Estado**: `[ ]` pendiente — fuera del MVP
- **Scope**: enlace público de reserva sin cuenta previa; el paciente elige prestación, ve huecos de C-07 y reserva reutilizando **íntegramente** `AgendarTurno` (misma validación); identificación con dato verificado; un paciente nunca agenda sobreturno. Condicionada por `PA-06`. Tests: reserva con hueco libre; paciente intenta sobreturno → rechazado.
- **Dependencias**: `C-11`, `C-07`, `C-14`, `C-15`
- **Governance**: ALTO
- **Historias**: US-022 · **Reglas**: RN-DI-03, RN-AG-02…04
- **Leer antes**:
  - `knowledge-base/07_flujos_principales.md` → F-07
  - `knowledge-base/05_reglas_de_negocio.md` → `RN-DI-03`
  - `knowledge-base/10_preguntas_abiertas.md` → `PA-06`

### [C-18] `odontograma-y-ficha-clinica`
- **Estado**: `[ ]` pendiente — fuera del MVP, **solo si todo lo anterior está cerrado**
- **Scope**: odontograma y ficha clínica extendida. Sin scope detallado hasta decidirlo; es el componente más costoso de hacer bien.
- **Dependencias**: `C-11` y todo lo de FASE 3 cerrado
- **Governance**: CRITICO (datos clínicos)
- **Historias**: US-028
- **Leer antes**:
  - `knowledge-base/06_funcionalidades.md` → US-028
  - `knowledge-base/01_vision_y_objetivos.md` → § Fuera de alcance

---

## Tabla resumen

| ID | Change | Fase | Responsable | Depende de | Governance | MVP |
|---|---|---|---|---|---|---|
| C-01 | `crear-turno-sin-solapamientos` | 0 | miriquero | — | CRITICO | Sí |
| C-02 | `reprogramar-turno` | 1 | julianfdrago | C-01 | MEDIO | Sí |
| C-03 | `cancelar-turno-con-anticipacion-minima` | 1 | augusto163 | C-01 | MEDIO | Sí |
| C-04 | `agendar-sobreturno-autorizado` | 1 | maximiliano-di-natale | C-01 | ALTO | Sí |
| C-05 | `registrar-ausencia-y-atencion-de-turno` | 1 | [COMPLETAR: quinto integrante] | C-01 | MEDIO | Sí |
| C-06 | `administrar-recursos-catalogo-y-pacientes` | 2 | julianfdrago | C-01 | BAJO | Sí |
| C-07 | `consultar-agenda-y-huecos-libres` | 2 | miriquero | C-01 | MEDIO | Sí |
| C-08 | `auditoria-y-notificacion-simulada` | 2 | maximiliano-di-natale | C-01 | CRITICO | Sí |
| C-09 | `persistencia-jpa-h2-y-concurrencia` | 2 | augusto163 | C-01 | ALTO | Sí |
| C-10 | `api-rest-turnos` | 2 | [COMPLETAR: quinto integrante] | C-01, C-09 | BAJO | Sí (opcional) |
| C-11 | `verificacion-transversal-mvp` | 2 | miriquero | C-02…C-09 | BAJO | Sí |
| C-12 | `bloqueos-de-agenda` | 3 | a definir | C-11, C-07 | CRITICO | No |
| C-13 | `reporte-ausentismo-motivos-estructurados` | 3 | a definir | C-11, C-03, C-05 | MEDIO | No |
| C-14 | `lista-de-espera-sobre-huecos` | 3 | a definir | C-11, C-03, C-07 | MEDIO | No |
| C-15 | `roles-y-permisos-efectivos` | 3 | a definir | C-11, C-04 | CRITICO | No |
| C-16 | `exportacion-completa-de-datos` | 3 | a definir | C-11, C-09 | ALTO | No |
| C-17 | `reserva-online-paciente` | 3 | a definir | C-11, C-07, C-14, C-15 | ALTO | No |
| C-18 | `odontograma-y-ficha-clinica` | 3 | a definir | C-11 + FASE 3 | CRITICO | No |

### Trazabilidad contra el MVP imprescindible (informe de Discovery, sección D.3)

| # D.3 | Punto del MVP | Change(s) |
|---|---|---|
| 1 | Modelo multi-profesional y multi-sillón | C-01, C-06 |
| 2 | Alta con validación de disponibilidad, intervalos semiabiertos | C-01 |
| 3 | Duración derivada de la prestación | C-01, C-06 |
| 4 | Reprogramación | C-02 |
| 5 | Cancelación con liberación, anticipación mínima y motivo | C-03 |
| 6 | Sobreturno autorizado y auditado | C-04 |
| 7 | Registro de ausencia | C-05 |
| 8 | Consulta de agenda y huecos libres | C-07 |
| 9 | Puerto de notificación con adaptador simulado | C-01 (puerto), C-08 (adaptador) |
| 10 | Auditoría mínima | C-01 (registro), C-08 (historial y append-only) |
| 11 | Suite de tests con datos ficticios | todos, cierre en C-11 |

Cobertura de historias: US-001…US-021 (MVP) en C-01…C-11; US-022…US-028 (diferenciador) en C-12…C-18.

**Primer item elegible**: C-01 `crear-turno-sin-solapamientos`. PA-01 ya está resuelta (Gradle), así que **está desbloqueado**.
