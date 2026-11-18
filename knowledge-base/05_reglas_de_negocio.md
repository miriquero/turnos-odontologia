# Reglas de Negocio

Cada regla tiene un código único `RN-{DOMINIO}-{NN}` para trazabilidad desde las
historias de usuario (`06`), los flujos (`07`) y los tests.

Dominios: **AG** Agenda y disponibilidad · **TU** Ciclo de vida del turno ·
**CA** Catálogo y recursos · **PA** Pacientes · **AU** Auditoría ·
**NO** Notificaciones · **DI** Diferenciador (no v1)

---

## Dominio: Agenda y disponibilidad (RN-AG)

Estas reglas son **el núcleo del producto**. El relevamiento de 19 fichas en
tres mercados no encontró **una sola descripción** de qué hace un sistema cuando
se intenta agendar un turno que pisa otro: lo más cercano son dos frases
comerciales ("evita sobreagendamientos" de Dentalink, "control de sillones para
evitar horarios duplicados" de Simples Dental), y en los siete productos
argentinos la prevención de solapamientos figura como "No evidenciado". Esta
sección es la respuesta a ese vacío, y debe estar **cubierta por tests
automatizados**.

Todas las reglas `RN-AG-02` a `RN-AG-04` viven en **un único servicio de
dominio** (`ServicioDisponibilidad`) y en ningún otro lugar.

### RN-AG-01 — La duración la define la prestación; el turno la hereda

Cada tipo de prestación define su duración en minutos (por ejemplo: **limpieza
30 minutos, conducto 90 minutos**). La duración del turno **no se escribe a
mano**: se deriva de la prestación elegida.

- `turno.fin = turno.inicio + prestacion.duracionMinutos`
- `prestacion.duracionMinutos > 0` siempre.
- Cambiar la prestación de un turno recalcula el `fin` y **obliga a revalidar la
  disponibilidad** (`RN-AG-02`, `RN-AG-03`).
- Modificar la duración de una prestación en el catálogo **no** recalcula los
  turnos ya agendados: el turno conserva la duración vigente al momento de
  agendarse.
- *Nota*: el catálogo de duraciones es un **supuesto no validado** con un
  profesional (`SU-02`, `PA-02`). Por eso es dato configurable y no constante en
  el código.

### RN-AG-02 — Un profesional no puede tener dos turnos simultáneos

No pueden existir dos turnos `AGENDADO` del **mismo profesional** cuyos
intervalos se solapen. El intento de agendar uno que solape se **rechaza**.

### RN-AG-03 — Un sillón no puede estar ocupado dos veces en la misma franja

No pueden existir dos turnos `AGENDADO` del **mismo sillón (o box)** cuyos
intervalos se solapen. El intento se **rechaza**.

`RN-AG-02` y `RN-AG-03` se evalúan **de forma independiente**: un turno puede ser
válido para el profesional y aun así inválido por el sillón, y viceversa. Es la
razón por la que el modelo es multi-profesional **y** multi-sillón incluso para
un consultorio de un solo odontólogo.

### RN-AG-04 — Los intervalos son semiabiertos: `[inicio, fin)`

> **Esta es la regla de borde central del proyecto.**

Un turno ocupa el intervalo **semiabierto** `[inicio, fin)`: incluye su instante
de inicio y **excluye** su instante de fin.

**Consecuencia directa: un turno que empieza exactamente cuando termina otro ES
VÁLIDO.** Dos turnos consecutivos que comparten el instante frontera **no se
solapan** y ambos pueden coexistir, aun siendo del mismo profesional y del mismo
sillón.

Definición formal de solapamiento, que es la que debe estar implementada:

```
solapan(A, B)  ⟺  A.inicio < B.fin  ∧  B.inicio < A.fin
```

Obsérvese que la comparación es **estrictamente menor** (`<`) en ambos lados, no
`<=`. Un predicado escrito con `<=` haría que los turnos contiguos se rechacen,
que es el error que esta regla existe para prevenir.

**Casos borde que deben tener test propio** (profesional y sillón iguales,
turnos de 30 minutos):

| # | Turno existente | Turno nuevo | Resultado esperado |
|---|---|---|---|
| 1 | 10:00 – 10:30 | 10:30 – 11:00 | **VÁLIDO** — el nuevo empieza cuando termina el otro |
| 2 | 10:00 – 10:30 | 09:30 – 10:00 | **VÁLIDO** — el nuevo termina cuando empieza el otro |
| 3 | 10:00 – 10:30 | 10:29 – 10:59 | RECHAZADO — solapa 1 minuto |
| 4 | 10:00 – 10:30 | 10:00 – 10:30 | RECHAZADO — idéntico |
| 5 | 10:00 – 11:30 | 10:30 – 11:00 | RECHAZADO — contenido dentro del existente |
| 6 | 10:30 – 11:00 | 10:00 – 11:30 | RECHAZADO — contiene al existente |
| 7 | 10:00 – 10:30 | 10:15 – 10:45 | RECHAZADO — solapa parcialmente por la derecha |
| 8 | 10:00 – 10:30 | 09:45 – 10:15 | RECHAZADO — solapa parcialmente por la izquierda |
| 9 | 10:00 – 10:30 | 11:00 – 11:30 | VÁLIDO — sin contacto |

Los casos 1 y 2 son los que distinguen una implementación correcta de una
incorrecta, y son los que ningún competidor del relevamiento documenta.

### RN-AG-05 — Anticipación mínima para cancelar

**No se puede cancelar un turno con menos de `N` horas de anticipación**
respecto de su hora de inicio.

- `N` es **configurable** y su valor queda como **pregunta abierta** (`PA-04`):
  el Discovery no lo definió. Hasta que se decida, el valor por defecto se
  declara como supuesto (`SU-03`).
- Un intento de cancelación fuera de plazo se rechaza indicando la regla
  violada. Lo que corresponde hacer en ese caso (¿registrar ausencia?,
  ¿cancelación con penalidad?) también es pregunta abierta.
- Toda cancelación válida **exige motivo registrado** y **libera el horario**: la
  franja vuelve a estar disponible para `RN-AG-02` y `RN-AG-03`.

### RN-AG-06 — El sobreturno rompe la regla de solapamiento solo con autorización explícita, y queda registrado

Un turno puede violar `RN-AG-02` y/o `RN-AG-03` **únicamente** si se agenda como
**sobreturno**, y para eso hace falta:

1. **Autorización explícita** — una marca deliberada en la operación de alta; no
   es un reintento silencioso ni un comportamiento por defecto.
2. **Usuario responsable** registrado (`autorizadoPor`) — obligatorio.
3. **Motivo** registrado — obligatorio.
4. Un **evento de auditoría** `SOBRETURNO_AUTORIZADO` (`RN-AU-01`).

Un sobreturno sin autorización, sin responsable o sin motivo **se rechaza**: la
ausencia de cualquiera de los tres datos invalida la excepción. El turno queda
marcado con `esSobreturno = true` para que sea distinguible en toda consulta y
reporte posterior.

**Quién puede autorizar** es una pregunta abierta (`PA-03`). Hasta resolverla, el
supuesto es que solo el odontólogo dueño (`SU-04`).

### RN-AG-07 — Solo los turnos vigentes ocupan la agenda

Los turnos en estado `CANCELADO` y `AUSENTE` **no ocupan** su franja: no
participan de la evaluación de solapamiento. Los estados `AGENDADO` y `ATENDIDO`
sí ocupan.

- Consecuencia de `AUSENTE`: si el paciente no se presentó, el horario queda
  registrado como ausencia (información de gestión) pero no bloquea un reagendado
  en esa franja.
- Consecuencia de `ATENDIDO`: un turno ya atendido sigue ocupando su franja
  histórica, porque esa hora de sillón efectivamente se usó.

### RN-AG-08 — El turno se agenda sobre recursos válidos y activos

- `profesionalId`, `sillonId`, `pacienteId` y `prestacionId` deben existir.
- El profesional y el sillón deben estar `activo = true`.
- La prestación debe estar `activa = true`.
- `inicio` debe ser **futuro** al momento de agendar (no se agendan turnos en el
  pasado). Marcar `ATENDIDO` o `AUSENTE` un turno pasado sí es válido.

### RN-AG-09 — Consulta de disponibilidad

La búsqueda de huecos libres para una prestación dada devuelve franjas que
satisfacen simultáneamente `RN-AG-02`, `RN-AG-03`, `RN-AG-08` y —en el bloque
diferenciador— `RN-DI-01` (bloqueos). La duración de cada hueco candidato es la
de la prestación consultada (`RN-AG-01`), y un hueco que empieza exactamente
donde termina otro turno **es un hueco válido** (`RN-AG-04`).

---

## Dominio: Ciclo de vida del turno (RN-TU)

### RN-TU-01 — Estados posibles

`AGENDADO` (inicial) · `ATENDIDO` (terminal) · `CANCELADO` (terminal) ·
`AUSENTE` (terminal).

### RN-TU-02 — Transiciones permitidas

Solo desde `AGENDADO`: a `ATENDIDO`, a `CANCELADO` o a `AUSENTE`. Los tres son
terminales: **no hay transición de salida** de ninguno de ellos. Cualquier otra
transición se rechaza.

### RN-TU-03 — Reprogramación

Reprogramar cambia `inicio` (y por consiguiente `fin`, vía `RN-AG-01`) y puede
cambiar profesional y/o sillón. **Revalida la disponibilidad completa en el nuevo
horario** con las mismas reglas que un alta (`RN-AG-02` a `RN-AG-04`,
`RN-AG-08`), y la franja anterior queda liberada. Genera un evento de auditoría
`REPROGRAMADO` con el horario anterior y el nuevo.

Al revalidar, **el propio turno que se está reprogramando se excluye** de la
comparación de solapamiento: un turno no se solapa consigo mismo.

### RN-TU-04 — Un turno cancelado o ya atendido no se reprograma

Solo un turno en estado `AGENDADO` puede reprogramarse. El intento de reprogramar
un turno `CANCELADO`, `ATENDIDO` o `AUSENTE` se **rechaza**. Si el paciente
quiere otro horario después de una cancelación, se agenda un **turno nuevo**.

### RN-TU-05 — Cancelación con motivo

Toda cancelación exige un motivo no vacío y se somete a `RN-AG-05`. El motivo se
guarda de forma estructurada para que el reporte de ausentismo del bloque
diferenciador pueda agregarlo. El relevamiento no encontró **ningún** producto
que capture esta información.

### RN-TU-06 — La ausencia es distinta de la cancelación

Marcar `AUSENTE` registra que el paciente no se presentó y no avisó. No es una
cancelación: no se le aplica `RN-AG-05` (la anticipación mínima no tiene sentido
retroactivo) y es el insumo del seguimiento de ausentes.

---

## Dominio: Catálogo y recursos (RN-CA)

- **RN-CA-01**: `prestacion.duracionMinutos > 0`. Una prestación sin duración
  válida no puede usarse para agendar.
- **RN-CA-02**: `sillon.nombre` y `prestacion.nombre` son únicos.
- **RN-CA-03**: `profesional.matricula` es única.
- **RN-CA-04**: no se puede desactivar un profesional, un sillón o una prestación
  que tenga turnos `AGENDADO` futuros sin resolverlos antes (cancelarlos o
  reprogramarlos).
- **RN-CA-05**: no se elimina físicamente un recurso con historial de turnos; se
  desactiva (`activo = false`). El historial de agenda debe permanecer íntegro.

---

## Dominio: Pacientes (RN-PA)

- **RN-PA-01**: la ficha del paciente es **básica**: contacto e identificación.
  Sin odontograma, sin plan de tratamiento, sin historia clínica (v1).
- **RN-PA-02**: `documento` es único.
- **RN-PA-03**: para que el puerto de notificación pueda operar, el paciente debe
  tener al menos un canal de contacto (`telefono` o `email`).
- **RN-PA-04 (restricción dura)**: **está prohibido almacenar datos reales de
  pacientes**. Todos los ejemplos, seeds, fixtures, tests y capturas usan datos
  ficticios exclusivamente. Fundamento: son datos sensibles de salud bajo la
  **Ley 25.326**, y el relevamiento mostró que el cumplimiento está **declarado
  por 2 de 7 productos argentinos y certificado por ninguno** — este proyecto no
  va a sumarse a ese vacío creando riesgo real sin necesidad.
- **RN-PA-05**: no se elimina un paciente con turnos históricos; se desactiva.

---

## Dominio: Auditoría (RN-AU)

- **RN-AU-01**: toda operación sobre un turno (alta, modificación,
  reprogramación, cancelación, marcado de atendido o ausente, autorización de
  sobreturno) genera un `EventoAuditoria` con **usuario y timestamp
  obligatorios**. Una operación que no puede identificar a su usuario se rechaza.
- **RN-AU-02**: el registro de auditoría es **append-only**. No se modifica ni se
  borra.
- **RN-AU-03**: el detalle de una reprogramación incluye el horario anterior y el
  nuevo, y de un cambio de recurso, el recurso anterior y el nuevo.

> Justificación del Discovery: la auditoría de cambios está evidenciada de forma
> parcial y heterogénea en el mercado (Odonto.ar solo audita el odontograma,
> Dentia audita "cambios importantes" sin definir cuáles) y en la mayoría es "No
> evidenciado". Es el vacío de mercado **más barato de cubrir si se diseña desde
> el inicio** y es requisito de un sistema que maneja datos de salud.

---

## Dominio: Notificaciones (RN-NO)

- **RN-NO-01**: la notificación al paciente se expresa como **puerto del
  dominio** (`NotificadorPaciente`). El dominio nunca conoce WhatsApp, email ni
  ningún proveedor.
- **RN-NO-02**: en la v1 el único adaptador es **simulado**: registra el envío y
  no sale a la red. **Ninguna prueba depende de credenciales ni de un servicio
  externo.**
- **RN-NO-03**: un fallo de notificación **no invalida** la operación de agenda
  que lo disparó. Agendar un turno no puede fallar porque el recordatorio falló.
- **RN-NO-04**: ninguna notificación incluye datos clínicos; solo fecha, hora,
  profesional y prestación.

---

## Dominio: Diferenciador — no v1 (RN-DI)

Reglas ya decididas para cuando se implemente el bloque diferenciador, anotadas
acá para que el diseño del MVP no las contradiga.

- **RN-DI-01**: un `BloqueoAgenda` **participa de la regla de disponibilidad
  exactamente igual que un turno**: ocupa `[inicio, fin)` sobre el profesional,
  el sillón o ambos, y se le aplica el mismo predicado de solapamiento de
  `RN-AG-04`.
- **RN-DI-02**: cuando una cancelación libera un horario, el hueco queda
  registrado como **ofrecible** y la lista de espera se consulta por prestación y
  franja. Qué se hace con ese hueco es pregunta abierta (`PA-05`).
- **RN-DI-03**: la reserva online del paciente usa un **identificador de acceso
  por enlace, sin cuenta previa**, y queda sujeta a las mismas reglas de
  disponibilidad que una reserva de recepción. Un paciente **nunca** puede
  agendar un sobreturno.
- **RN-DI-04**: la exportación completa de datos del consultorio se entrega en
  formato abierto. Evidenciada en solo 2 de 19 competidores (Dentia y tab32).

---

## Dominio: Excepciones globales

| Situación | Tratamiento |
|---|---|
| Regla de negocio violada | Se rechaza la operación con el **código de regla** (`RN-AG-04`, etc.) en la respuesta, para que el error sea trazable desde el test |
| Dos altas concurrentes sobre la misma franja | La validación de disponibilidad debe ser atómica respecto del alta. Un bloqueo optimista sobre los recursos, o un constraint de exclusión en base, evita la condición de carrera. **No alcanza con validar antes de insertar** |
| Operación sin usuario identificable | Se rechaza (`RN-AU-01`) |
| Turno con `fin <= inicio` | Imposible por construcción (`RN-AG-01`); si se detecta, es un bug, no un caso de negocio |
| Dato real de paciente detectado en el repositorio | Es un **defecto bloqueante**, no una observación (`RN-PA-04`) |
