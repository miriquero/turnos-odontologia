# Modelo de Datos

## Dominios

| Dominio | Descripción |
|---|---|
| **Agenda** | Núcleo del sistema. `Turno`, su ciclo de vida y la validación de disponibilidad contra profesional y sillón. |
| **Recursos** | `Profesional` y `Sillon`: los dos recursos que la regla de solapamiento protege. |
| **Catálogo** | `Prestacion` con su duración. Es el dominio que determina cuánto dura un turno. |
| **Pacientes** | Ficha **básica** del paciente: datos de contacto e identificación. Sin odontograma ni historia clínica en la v1. |
| **Auditoría** | `EventoAuditoria`: quién hizo qué sobre qué turno y cuándo. |
| **Notificaciones** | Puerto de dominio, sin entidad persistida propia en el MVP más allá del registro simulado. |

### Hallazgo del Discovery que justifica este modelo

El **sillón como recurso agendable de primera clase** aparece en solo **4 de 19
fichas** relevadas (Dentalink con su vista de ocupación de sillones, AgendaPro
con agenda por odontólogo/práctica/sillón, Simples Dental con control de sillones
y tab32 con columnas por operatorio) y en **ninguno de los seis productos de
origen argentino**: el único con oferta argentina que lo menciona es AgendaPro,
un generalista multirrubro con oferta localizada. En los productos argentinos la
agenda se modela **solo por profesional**, lo cual es insuficiente en cuanto hay
dos sillones y dos profesionales que se alternan.

Por eso `Sillon` es una entidad de primera clase y no un atributo de `Turno`.

## ERD

```
   Profesional 1 ──────────< Turno >────────── 1 Sillon
                               │  │
                               │  └──────────── 1 Prestacion ──► duracionMinutos
                               │
                               └──────────────< 1 Paciente

   Turno 1 ──────< EventoAuditoria
   Turno 0..1 ───── 1 AutorizacionSobreturno   (solo si esSobreturno)
   Turno 0..1 ───── 1 MotivoCancelacion        (solo si estado = CANCELADO)

   [DIFERENCIADOR]
   Profesional 0..1 ──< BloqueoAgenda >── 0..1 Sillon
   Paciente     1 ────< EntradaListaEspera >── 1 Prestacion
```

Un `Turno` tiene **dos claves de ocupación independientes**: `(profesionalId,
intervalo)` y `(sillonId, intervalo)`. La regla de disponibilidad se evalúa
contra ambas por separado (ver `05_reglas_de_negocio.md`).

## Entidades

### Profesional

| Atributo | Tipo | Nota |
|---|---|---|
| `id` | `Long` / `UUID` | PK |
| `nombre`, `apellido` | `String` | Obligatorios |
| `matricula` | `String` | Único. Dato ficticio en seeds y tests |
| `rol` | `enum RolProfesional` | `DUENIO`, `COLEGA` |
| `activo` | `boolean` | Un profesional inactivo no acepta turnos nuevos |

- Relaciones: `1..N Turno`.
- Índices: `matricula` único.

### Sillon

| Atributo | Tipo | Nota |
|---|---|---|
| `id` | `Long` / `UUID` | PK |
| `nombre` | `String` | Ej. "Sillón 1", "Box A". Único |
| `activo` | `boolean` | Un sillón en mantenimiento no acepta turnos (en la v1 se modela con `activo`; el bloque diferenciador lo refina con `BloqueoAgenda`) |

- Relaciones: `1..N Turno`.
- Constraint: `nombre` único.

### Prestacion

| Atributo | Tipo | Nota |
|---|---|---|
| `id` | `Long` / `UUID` | PK |
| `nombre` | `String` | Ej. "Limpieza", "Conducto". Único |
| `duracionMinutos` | `int` | **> 0**. Es la fuente de la duración del turno (`RN-AG-01`) |
| `activa` | `boolean` | — |

- Constraints: `duracionMinutos > 0`; `nombre` único.
- **Las duraciones del catálogo son un supuesto no validado** con un profesional
  (ver `SU-02` y `PA-02`). Son configurables precisamente por eso.

### Paciente

Ficha **básica** únicamente. Datos de contacto e identificación.

| Atributo | Tipo | Nota |
|---|---|---|
| `id` | `Long` / `UUID` | PK |
| `nombre`, `apellido` | `String` | Obligatorios |
| `documento` | `String` | Único. **Siempre ficticio** en el repositorio |
| `telefono` | `String` | Canal de notificación |
| `email` | `String` | Canal alternativo |
| `fechaNacimiento` | `LocalDate` | Opcional |
| `observaciones` | `String` | Texto libre breve. **No es historia clínica** |

- Constraint: `documento` único.
- **Restricción dura**: nunca almacenar datos reales de pacientes. Ver
  `08_arquitectura_propuesta.md`.

### Turno — entidad central

| Atributo | Tipo | Nota |
|---|---|---|
| `id` | `Long` / `UUID` | PK |
| `profesionalId` | FK → `Profesional` | Obligatorio |
| `sillonId` | FK → `Sillon` | Obligatorio |
| `pacienteId` | FK → `Paciente` | Obligatorio |
| `prestacionId` | FK → `Prestacion` | Obligatorio |
| `inicio` | `LocalDateTime` | Obligatorio |
| `fin` | `LocalDateTime` | **Derivado**: `inicio + prestacion.duracionMinutos`. No se escribe a mano (`RN-AG-01`) |
| `estado` | `enum EstadoTurno` | `AGENDADO`, `ATENDIDO`, `CANCELADO`, `AUSENTE` |
| `esSobreturno` | `boolean` | `true` solo con autorización explícita (`RN-AG-06`) |
| `motivoCancelacion` | `String` | Obligatorio si `estado = CANCELADO` |
| `motivoAusencia` | `String` | Opcional si `estado = AUSENTE` |
| `creadoPor`, `creadoEn` | `String`, `Instant` | Auditoría |
| `modificadoPor`, `modificadoEn` | `String`, `Instant` | Auditoría |

- **Invariante**: `fin > inicio` siempre.
- **El intervalo `[inicio, fin)` es semiabierto**: ver `RN-AG-04`, el caso de
  borde central del proyecto.
- Índices recomendados: `(profesionalId, inicio, fin)` y `(sillonId, inicio,
  fin)` — son exactamente las dos consultas que la regla de disponibilidad hace.
- Un turno `CANCELADO` o `ATENDIDO` **no se reprograma** (`RN-TU-04`).

#### Estados y transiciones

```
        ┌──────────► CANCELADO   (terminal)
        │
   AGENDADO ──────► ATENDIDO     (terminal)
        │
        └──────────► AUSENTE     (terminal)

   AGENDADO ──reprogramar──► AGENDADO  (nuevo inicio/fin, revalida disponibilidad)
```

`AUSENTE` es un estado distinto de `CANCELADO`: la ausencia es información de
gestión sobre el paciente, la cancelación es una liberación de horario. El
relevamiento mostró que **el seguimiento sistemático de ausentes está ausente en
toda la región** (los dos únicos casos son estadounidenses), así que separar los
dos estados es parte de la propuesta de valor.

### AutorizacionSobreturno

| Atributo | Tipo | Nota |
|---|---|---|
| `id` | `Long` / `UUID` | PK |
| `turnoId` | FK → `Turno` | 1:1 |
| `autorizadoPor` | `String` | Usuario responsable — **obligatorio** |
| `motivo` | `String` | **Obligatorio** |
| `autorizadoEn` | `Instant` | — |

Ningún producto del relevamiento documenta quién puede autorizar un sobreturno
ni si queda registrado, aunque varios los ofrecen (Dentia, Dentalink, Simples
Dental). Modelarlo como ruptura deliberada con responsable y motivo convierte
una fuente de caos en información de gestión.

### EventoAuditoria

| Atributo | Tipo | Nota |
|---|---|---|
| `id` | `Long` / `UUID` | PK |
| `turnoId` | FK → `Turno` | — |
| `accion` | `enum` | `CREADO`, `MODIFICADO`, `REPROGRAMADO`, `CANCELADO`, `MARCADO_ATENDIDO`, `MARCADO_AUSENTE`, `SOBRETURNO_AUTORIZADO` |
| `usuario` | `String` | Obligatorio |
| `ocurridoEn` | `Instant` | Obligatorio |
| `detalle` | `String` | Ej. horario anterior y nuevo en una reprogramación |

Es append-only: **nunca se modifica ni se borra** (`RN-AU-02`).

### Entidades del bloque diferenciador

- **BloqueoAgenda** — `profesionalId` (nullable), `sillonId` (nullable),
  `inicio`, `fin`, `motivo` (`VACACIONES`, `AUSENCIA_PROFESIONAL`,
  `MANTENIMIENTO_SILLON`). **Participa de la regla de disponibilidad igual que un
  turno.** Simples Dental es la única ficha del relevamiento que evidencia bloqueo
  de horarios.
- **EntradaListaEspera** — `pacienteId`, `prestacionId`, franja deseada,
  prioridad, estado. Se consulta cuando una cancelación libera un hueco. Los dos
  únicos precedentes del relevamiento son los *encaixes* de Simples Dental y el
  Web Sched ASAP de Open Dental, ambos no argentinos.

## Seed data inicial

**Todos los datos son ficticios. Está prohibido usar datos reales de pacientes.**

```
Profesionales
  1  Ana Gomez     (mat. ficticia MN-0001)  DUENIO
  2  Luis Pereyra  (mat. ficticia MN-0002)  COLEGA

Sillones
  1  Sillon 1
  2  Sillon 2
  3  Sillon 3

Prestaciones  (duraciones = SUPUESTO NO VALIDADO, ver PA-02)
  1  Consulta / diagnostico   20 min
  2  Limpieza                 30 min
  3  Restauracion simple      45 min
  4  Extraccion simple        45 min
  5  Conducto                 90 min

Pacientes (ficticios)
  1  Juan Perez     DNI 11.111.111   +54 9 351 000-0001
  2  Maria Lopez    DNI 22.222.222   +54 9 351 000-0002
  3  Carla Fernandez DNI 33.333.333  carla.ficticia@example.com
```

El seed debe incluir **al menos un par de turnos contiguos** (uno que termina
10:30 y otro que empieza 10:30, mismo profesional y mismo sillón) porque es el
caso de borde que define el proyecto y conviene que esté visible en los datos de
arranque, no solo en los tests.
