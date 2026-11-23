# Reprogramación de turno Specification

## Purpose

La reprogramación permite mover un turno agendado a otro horario o recurso conservando su identidad, con revalidación de disponibilidad, auditoría y un detalle consultable de los valores anteriores y los nuevos.

## Requirements

### Requirement: Solo se reprograma un turno agendado

El sistema SHALL permitir la reprogramación únicamente cuando el turno está en estado `AGENDADO` y SHALL rechazar `CANCELADO`, `ATENDIDO` y `AUSENTE` con el código `RN-TU-04` (`US-012`).

#### Scenario: Reprogramar un turno cancelado

- **GIVEN** un turno en estado `CANCELADO`
- **WHEN** se intenta reprogramarlo a una franja libre
- **THEN** la operación se rechaza con el código `RN-TU-04` y el turno permanece cancelado

#### Scenario: Reprogramar un turno atendido

- **GIVEN** un turno en estado `ATENDIDO`
- **WHEN** se intenta reprogramarlo
- **THEN** la operación se rechaza con el código `RN-TU-04`

#### Scenario: Reprogramar un turno ausente

- **GIVEN** un turno en estado `AUSENTE`
- **WHEN** se intenta reprogramarlo
- **THEN** la operación se rechaza con el código `RN-TU-04`

#### Scenario: Reprogramar un turno agendado

- **GIVEN** un turno en estado `AGENDADO` y una franja libre
- **WHEN** se reprograma a esa franja
- **THEN** la operación se acepta

### Requirement: La duración se recalcula desde la prestación resultante

El sistema SHALL derivar el nuevo fin de `inicio + duración de la prestación resultante` (`RN-AG-01`). La operación NO SHALL aceptar un fin ni una duración provistos por quien reprograma. Cuando la prestación no cambia, el turno SHALL conservar la duración que ya tenía.

#### Scenario: Cambio de inicio con la misma prestación

- **GIVEN** un turno de 30 minutos sobre `[10:00, 10:30)`
- **WHEN** se reprograma a las `11:00` sin cambiar la prestación
- **THEN** el turno ocupa `[11:00, 11:30)`, conservando sus 30 minutos

#### Scenario: Cambio a una prestación de otra duración

- **GIVEN** un turno de 30 minutos sobre `[10:00, 10:30)`
- **WHEN** se reprograma a las `10:00` con una prestación de 90 minutos
- **THEN** el turno ocupa `[10:00, 11:30)`

#### Scenario: Cambiar el catálogo no altera la duración conservada

- **GIVEN** un turno con una prestación de 30 minutos y que el catálogo pasó esa prestación a 45 minutos
- **WHEN** se reprograma el turno sin cambiar la prestación
- **THEN** el turno sigue ocupando 30 minutos, no 45

#### Scenario: El fin no se puede fijar a mano

- **GIVEN** la operación de reprogramación
- **WHEN** se inspecciona qué datos temporales acepta
- **THEN** acepta únicamente el nuevo inicio; no existe forma de indicar el fin ni la duración

### Requirement: La disponibilidad se revalida excluyendo el propio turno

El sistema SHALL verificar la franja resultante con las mismas reglas de un alta (`RN-AG-02`, `RN-AG-03`, `RN-AG-04`) antes de guardar, excluyendo de la comparación al turno que se reprograma (`RN-TU-03`). La franja anterior SHALL quedar libre en el mismo acto.

#### Scenario: Solapamiento con el profesional de otro turno

- **GIVEN** un turno ajeno del profesional `P1` sobre `[10:00, 10:30)`
- **WHEN** se reprograma un turno del profesional `P1` a `[10:15, 10:45)`
- **THEN** la operación se rechaza con el código `RN-AG-02`

#### Scenario: Solapamiento con el sillón de otro turno

- **GIVEN** un turno ajeno en el sillón `S1` sobre `[10:00, 10:30)`
- **WHEN** se reprograma un turno de otro profesional al sillón `S1` en `[10:15, 10:45)`
- **THEN** la operación se rechaza con el código `RN-AG-03`

#### Scenario: Borde contiguo

- **GIVEN** otro turno del mismo profesional y sillón sobre `[10:00, 10:30)`
- **WHEN** se reprograma el turno a `[10:30, 11:00)`
- **THEN** la operación se acepta, porque los intervalos son semiabiertos (`RN-AG-04`)

#### Scenario: Mismo horario con otro sillón libre

- **GIVEN** el turno sobre `[10:00, 10:30)` y un sillón distinto libre en esa franja
- **WHEN** se reprograma el turno al mismo inicio con ese otro sillón
- **THEN** la operación se acepta y el turno conserva su identificador

#### Scenario: La franja anterior queda libre

- **GIVEN** un turno sobre `[10:00, 10:30)` que se reprograma a `[11:00, 11:30)`
- **WHEN** se verifica la disponibilidad de `[10:00, 10:30)` para su profesional y sillón originales
- **THEN** la franja resulta disponible

### Requirement: La no-op no produce cambios ni registros

El sistema SHALL considerar que no hay cambio cuando el inicio, profesional, sillón y prestación resultantes coinciden con los actuales. En ese caso SHALL devolver el turno sin modificar y NO SHALL guardarlo, registrar evento, registrar detalle ni notificar. Si cambia cualquiera de esos campos, el flujo completo SHALL ejecutarse aunque el inicio sea el mismo.

#### Scenario: Reprogramar a los valores exactos que ya tenía

- **GIVEN** un turno agendado de inicio futuro, con profesional, sillón y prestación activos, y una solicitud con el mismo inicio, profesional, sillón y prestación
- **WHEN** se ejecuta la reprogramación
- **THEN** la operación se acepta y devuelve el turno sin modificarlo, sin registrar auditoría ni detalle y sin notificar

#### Scenario: Cambiar solo el sillón con el mismo horario

- **GIVEN** un turno agendado y una solicitud con el mismo inicio pero otro sillón libre
- **WHEN** se ejecuta la reprogramación
- **THEN** se registra el evento `REPROGRAMADO`, se registra el detalle y se notifica al paciente

#### Scenario: Cambiar solo el profesional con el mismo horario

- **GIVEN** un turno agendado y una solicitud con el mismo inicio pero otro profesional libre
- **WHEN** se ejecuta la reprogramación
- **THEN** se registra el evento `REPROGRAMADO`, se registra el detalle y se notifica al paciente

#### Scenario: Cambiar solo la prestación con igual duración

- **GIVEN** un turno agendado y una solicitud que cambia la prestación por otra de la misma duración
- **WHEN** se ejecuta la reprogramación
- **THEN** se registra el evento `REPROGRAMADO` y el detalle refleja la prestación anterior y la nueva

### Requirement: Toda reprogramación efectiva exige usuario y queda auditada y notificada

El sistema SHALL registrar un evento `REPROGRAMADO` con usuario y timestamp obligatorios en cada reprogramación efectiva (`RN-AU-01`) y SHALL rechazar la operación sin usuario identificable. El aviso al paciente SHALL dispararse a través del puerto de notificación y su fallo NO SHALL invalidar la operación (`RN-NO-03`).

#### Scenario: Sin usuario responsable

- **GIVEN** un turno agendado y una franja libre
- **WHEN** se intenta reprogramar sin usuario identificable
- **THEN** la operación se rechaza con el código `RN-AU-01` y el turno no se modifica

#### Scenario: Reprogramación efectiva con usuario

- **GIVEN** un turno agendado, una franja libre y un usuario identificable
- **WHEN** se reprograma el turno
- **THEN** queda registrado un evento `REPROGRAMADO` con ese usuario y una marca de tiempo

#### Scenario: Fallo de la notificación

- **GIVEN** un puerto de notificación que falla
- **WHEN** se reprograma un turno con usuario y franja libre
- **THEN** el turno queda reprogramado, el evento `REPROGRAMADO` queda registrado y la operación no falla

### Requirement: El detalle de la reprogramación queda disponible por separado

El sistema SHALL conservar, en un registro distinto del evento de auditoría y consultable por identificador de turno, el horario anterior y el nuevo y, cuando corresponda, el recurso anterior y el nuevo (`RN-AU-03`). Ese registro SHALL ser append-only.

#### Scenario: Cambio de horario

- **GIVEN** un turno sobre `[10:00, 10:30)`
- **WHEN** se reprograma a `[11:00, 11:30)`
- **THEN** el detalle conserva el horario anterior `[10:00, 10:30)` y el nuevo `[11:00, 11:30)`

#### Scenario: Cambio de recursos

- **GIVEN** un turno del profesional `P1` en el sillón `S1`
- **WHEN** se reprograma al profesional `P2` en el sillón `S2`
- **THEN** el detalle conserva `P1`/`S1` como recursos anteriores y `P2`/`S2` como nuevos

#### Scenario: Sin cambio de recursos

- **GIVEN** un turno que se reprograma solo de horario
- **WHEN** se consulta su detalle
- **THEN** los recursos anterior y nuevo figuran iguales y solo difieren los horarios

#### Scenario: Consulta del historial

- **GIVEN** un turno reprogramado
- **WHEN** un consumidor consulta por identificador de turno el registro de eventos y el registro de detalle
- **THEN** obtiene el evento `REPROGRAMADO` y el detalle asociado, con lo necesario para reconstruir el cambio

#### Scenario: Varias reprogramaciones conservan su detalle

- **GIVEN** un turno ya reprogramado una vez
- **WHEN** se reprograma una segunda vez
- **THEN** el registro conserva ambos detalles sin modificar ni borrar el primero

### Requirement: Los recursos resultantes deben ser válidos y el inicio futuro

El sistema SHALL rechazar con el código `RN-AG-08` la reprogramación cuyo profesional, sillón o prestación resultantes no existan o estén inactivos, o cuyo nuevo inicio no sea estrictamente posterior al instante actual (`RN-AG-08`, `RN-TU-03`).

#### Scenario: Profesional resultante inactivo

- **GIVEN** un profesional inactivo como nuevo profesional del turno
- **WHEN** se intenta reprogramar
- **THEN** la operación se rechaza con el código `RN-AG-08`

#### Scenario: Sillón resultante inactivo

- **GIVEN** un sillón inactivo como nuevo sillón del turno
- **WHEN** se intenta reprogramar
- **THEN** la operación se rechaza con el código `RN-AG-08`

#### Scenario: Prestación resultante inactiva

- **GIVEN** una prestación inactiva como nueva prestación del turno
- **WHEN** se intenta reprogramar
- **THEN** la operación se rechaza con el código `RN-AG-08`

#### Scenario: Inicio en el pasado o igual al instante actual

- **GIVEN** recursos válidos y un nuevo inicio que no es estrictamente futuro
- **WHEN** se intenta reprogramar
- **THEN** la operación se rechaza con el código `RN-AG-08`

#### Scenario: Inicio futuro con recursos válidos

- **GIVEN** recursos válidos y activos y un nuevo inicio estrictamente futuro
- **WHEN** se reprograma el turno
- **THEN** la operación se acepta

### Requirement: Los datos de prueba son ficticios

Ningún dato de persona incorporado a fixtures o pruebas SHALL corresponder a una persona real (`RN-PA-04`).

#### Scenario: Datos ficticios en las pruebas

- **GIVEN** el conjunto de datos usado por las pruebas del change
- **WHEN** se revisan nombres, documentos y contactos
- **THEN** todos son ficticios y ninguno identifica a una persona real
