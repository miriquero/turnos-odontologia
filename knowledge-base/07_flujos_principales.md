# Flujos Principales

> Fuente: `docs/discovery/discovery.md` (casos de uso) y
> `docs/discovery/informe-discovery.md` (sección D.3, MVP imprescindible).
> Cada paso referencia las reglas de `05_reglas_de_negocio.md` y las historias
> de `06_funcionalidades.md`.

Notación: **(OK)** camino feliz · **(E)** camino de error de negocio ·
**(B)** caso de borde.

---

## F-01 — Agendar un turno

**Actor**: recepción u odontólogo dueño · **Historias**: `US-006` a `US-009`

1. El actor elige **paciente**, **profesional**, **sillón**, **prestación** y
   **fecha y hora de inicio**.
2. El sistema valida que los cuatro recursos existan y estén **activos**
   (`RN-AG-08`). **(E)** Si alguno está inactivo o no existe, rechaza.
3. El sistema calcula `fin = inicio + duracion(prestacion)` (`RN-AG-01`). La
   duración **no la elige el actor**: la deriva de la prestación.
4. El sistema invoca `ServicioDisponibilidad` con el intervalo `[inicio, fin)`:
   - **(E)** Si el **profesional** ya tiene un turno vigente que se solapa →
     rechaza con `RN-AG-02`.
   - **(E)** Si el **sillón** ya está ocupado por un turno vigente que se solapa
     → rechaza con `RN-AG-03`.
   - **(B)** Si el turno existente **termina exactamente** cuando el nuevo
     empieza (o empieza exactamente cuando el nuevo termina) → **no hay
     solapamiento**: los intervalos son semiabiertos (`RN-AG-04`). **Se acepta.**
   - Solo cuentan los turnos **vigentes**: los cancelados y las ausencias no
     ocupan agenda (`RN-AG-07`).
5. **(OK)** El turno se crea en estado `PENDIENTE` (`RN-TU-01`).
6. Se registra el evento en auditoría (`RN-AU`).
7. Se encola la notificación al paciente a través del **puerto** de
   notificación, que en la v1 resuelve contra el adaptador simulado
   (`RN-NO`). El fallo de la notificación **no** invalida el turno.

> Este flujo es el **change de referencia de la Etapa 6** del trabajo: el paso 4
> con sus tres salidas (feliz, error de negocio, borde) son los tres escenarios
> mínimos que exige la especificación.

---

## F-02 — Reprogramar un turno

**Actor**: recepción u odontólogo dueño · **Historias**: `US-011`, `US-012`

1. El actor elige un turno existente y propone **nueva fecha y hora**, y
   opcionalmente **otro profesional o sillón**.
2. **(E)** Si el turno está `CANCELADO` o `ATENDIDO`, se rechaza (`RN-TU-04`).
3. El sistema recalcula `fin` con la duración de la prestación (`RN-AG-01`). Si
   la prestación cambió, la duración cambia con ella.
4. Se **revalida la disponibilidad completa** del nuevo intervalo con el mismo
   `ServicioDisponibilidad` del flujo F-01, **excluyendo el propio turno** de la
   comparación: un turno nunca se solapa consigo mismo.
   - **(E)** Solapamiento de profesional → `RN-AG-02`.
   - **(E)** Solapamiento de sillón → `RN-AG-03`.
   - **(B)** Contigüidad exacta → se acepta (`RN-AG-04`).
   - **(B)** Reprogramar al **mismo horario** que ya tenía: se acepta y es
     idempotente, porque al excluirse a sí mismo no hay conflicto.
5. **(OK)** El turno queda en el nuevo horario. El horario anterior queda libre
   en el mismo acto (`RN-TU-03`).
6. Auditoría y notificación, como en F-01.

---

## F-03 — Cancelar un turno y liberar el horario

**Actor**: recepción, odontólogo dueño o paciente (ver `PA-03`) ·
**Historias**: `US-013`, `US-014`

1. El actor elige el turno e indica un **motivo** (`RN-TU-05`).
2. **(E)** Si el turno ya está `CANCELADO` o `ATENDIDO`, se rechaza
   (`RN-TU-02`).
3. El sistema verifica la **anticipación mínima** `N` respecto del inicio
   (`RN-AG-05`):
   - **(E)** Con menos de `N` horas → se rechaza.
   - **(B)** Con **exactamente** `N` horas → se acepta. El límite es inclusivo,
     por coherencia con el criterio semiabierto de `RN-AG-04`.
   - El valor de `N` es configurable y está sin decidir (`PA-04`); el valor por
     defecto es un supuesto (`SU-03`).
4. **(OK)** El turno pasa a `CANCELADO` con su motivo y su marca de tiempo.
5. **El horario queda libre de inmediato**: al dejar de ser vigente, deja de
   ocupar agenda (`RN-AG-07`), y una nueva alta sobre esa franja se acepta.
6. Qué hacer con el hueco liberado —ofrecerlo a una lista de espera— es del
   bloque diferenciador y está sin decidir (`PA-05`).
7. Auditoría y notificación.

---

## F-04 — Agendar un sobreturno

**Actor**: odontólogo dueño (supuesto `SU-04`, pendiente `PA-03`) ·
**Historia**: `US-010`

1. El actor intenta agendar y el sistema rechaza por solapamiento (F-01, paso 4).
2. El actor **solicita explícitamente** agendar igual como sobreturno.
3. **(E)** Si el actor no tiene permiso de autorización, se rechaza.
4. El sistema exige un **motivo de autorización** y registra una
   `AutorizacionSobreturno` con quién autorizó, cuándo y por qué (`RN-AG-06`).
5. **(OK)** El turno se crea marcado como sobreturno, **saltando solo la regla
   de solapamiento**. Las demás reglas siguen aplicando: recursos activos,
   duración derivada de la prestación, estados válidos.
6. El evento queda en auditoría como excepción, no como alta normal.

> El sobreturno es la **única** forma de romper `RN-AG-02` y `RN-AG-03`. No
> existe ningún otro camino que permita solapar.

---

## F-05 — Registrar una ausencia

**Actor**: recepción u odontólogo · **Historia**: `US-015`

1. Llegado el horario, el paciente no se presenta.
2. **(E)** No se puede marcar ausente un turno **futuro**: el instante de inicio
   ya tiene que haber pasado (`RN-TU-06`).
3. **(E)** No se puede marcar ausente un turno `CANCELADO` (`RN-TU-02`).
4. **(OK)** El turno pasa a `AUSENTE`.
5. La ausencia **no es una cancelación**: queda como dato de gestión y alimenta
   el reporte de ausentismo del bloque diferenciador (`RN-TU-06`).

---

## F-06 — Consultar la agenda y los huecos libres

**Actor**: cualquier rol autenticado · **Historia**: `US-016`

1. El actor pide la agenda de un **día** o un **rango**, filtrando por
   profesional o por sillón.
2. El sistema devuelve los turnos **vigentes** del período (`RN-AG-07`).
3. Para huecos libres, el sistema recibe además una **prestación** y calcula las
   franjas donde `[inicio, inicio + duracion)` no se solapa ni con el
   profesional ni con el sillón (`RN-AG-09`).
4. El cálculo usa **el mismo** `ServicioDisponibilidad` que F-01: si la consulta
   dice que una franja está libre, el alta sobre esa franja no puede fallar por
   solapamiento. Es una propiedad verificable con tests.

---

## F-07 — Reserva online del paciente *(bloque diferenciador, no v1)*

**Actor**: paciente, sin cuenta previa

1. El paciente abre un enlace público de reserva.
2. Elige prestación y ve los huecos libres calculados por F-06.
3. Se identifica con un dato de contacto verificado.
4. El alta reutiliza íntegramente F-01: **la validación de disponibilidad es la
   misma**, sin importar por qué canal entre el turno.

> Está fuera de la v1 por decisión de la sección D del informe: depende de
> identificar a un paciente sin cuenta, un problema que el relevamiento mostró
> resuelto de formas muy distintas y que este proyecto no cerró.

---

## Invariante transversal

Todos los flujos que crean o mueven un turno —F-01, F-02, F-04 y F-07— pasan por
**un único** `ServicioDisponibilidad`. No hay una segunda implementación de la
regla de solapamiento en ninguna parte del sistema. Es, a la vez, la garantía de
corrección del dominio y la mitigación del riesgo `R-05` de conflictos de merge
entre los cinco integrantes.
