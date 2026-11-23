# Spec Delta

## Purpose

Cubre el cierre del ciclo de vida del turno: las transiciones terminales a
`ATENDIDO` y `AUSENTE`, sus validaciones de estado y de instante, el motivo de
ausencia y la trazabilidad en auditoría.

## ADDED Requirements

### Requirement: Solo un turno agendado puede cerrarse

El sistema SHALL permitir registrar `ATENDIDO` o `AUSENTE` únicamente desde
`AGENDADO`, y SHALL rechazar con `RN-TU-02` cualquier intento de salir de un
estado terminal (`RN-TU-01`, `RN-TU-02`).

#### Scenario: Transiciones válidas desde AGENDADO

- **GIVEN** un turno en estado `AGENDADO`
- **WHEN** se registra su atención y, por separado, su ausencia
- **THEN** ambos cierres se aceptan y el turno queda `ATENDIDO` y `AUSENTE`,
  respectivamente

#### Scenario: Un turno atendido no puede cerrarse de nuevo

- **GIVEN** un turno en estado `ATENDIDO`
- **WHEN** se intenta registrar su ausencia o su atención
- **THEN** la operación se rechaza con `RN-TU-02` y el turno sigue `ATENDIDO`

#### Scenario: Un turno ausente no puede cerrarse de nuevo

- **GIVEN** un turno en estado `AUSENTE`
- **WHEN** se intenta registrar su atención o su ausencia
- **THEN** la operación se rechaza con `RN-TU-02` y el turno sigue `AUSENTE`

#### Scenario: Un turno cancelado no puede cerrarse

- **GIVEN** un turno en estado `CANCELADO`
- **WHEN** se intenta registrar su atención o su ausencia
- **THEN** la operación se rechaza con `RN-TU-02` y el turno sigue `CANCELADO`

### Requirement: La ausencia exige que el inicio haya llegado

El sistema SHALL aceptar el registro de `AUSENTE` solo cuando el instante actual
sea igual o posterior al `inicio` del turno. Si el inicio es futuro, SHALL
rechazar la operación con `RN-TU-02`.

#### Scenario: Instante exacto de inicio

- **GIVEN** un turno `AGENDADO` cuyo `inicio` coincide con el instante actual
- **WHEN** se registra su ausencia
- **THEN** la operación se acepta y el turno queda `AUSENTE`

#### Scenario: Inicio ya pasado

- **GIVEN** un turno `AGENDADO` cuyo `inicio` es anterior al instante actual
- **WHEN** se registra su ausencia
- **THEN** la operación se acepta y el turno queda `AUSENTE`

#### Scenario: Turno futuro

- **GIVEN** un turno `AGENDADO` cuyo `inicio` es posterior al instante actual
- **WHEN** se intenta registrar su ausencia
- **THEN** la operación se rechaza con `RN-TU-02` y el turno sigue `AGENDADO`

### Requirement: La atención no exige esperar el inicio

El sistema SHALL aceptar el registro de `ATENDIDO` para un turno `AGENDADO` sin
comparar su `inicio` con el instante actual.

#### Scenario: Marcar atendido un turno futuro

- **GIVEN** un turno `AGENDADO` cuyo `inicio` es posterior al instante actual
- **WHEN** se registra su atención
- **THEN** la operación se acepta y el turno queda `ATENDIDO`

### Requirement: La ausencia no es una cancelación

El sistema SHALL registrar la ausencia como el estado terminal `AUSENTE`,
distinto de `CANCELADO`, y NO SHALL aplicar la anticipación mínima de
`RN-AG-05` ni exigir un motivo obligatorio (`RN-TU-06`).

#### Scenario: Ausencia con inicio inminente

- **GIVEN** un turno `AGENDADO` que empieza en menos del plazo mínimo de
  cancelación
- **WHEN** se registra su ausencia llegado el inicio
- **THEN** la operación se acepta sin evaluar la anticipación mínima

#### Scenario: El estado registrado no es cancelación

- **GIVEN** un turno `AGENDADO`
- **WHEN** se registra su ausencia
- **THEN** el turno queda en estado `AUSENTE` y no en `CANCELADO`

### Requirement: El motivo de ausencia es opcional y se conserva

El sistema SHALL aceptar un motivo de ausencia opcional como texto y SHALL
normalizarlo antes de conservarlo como dato del turno: una cadena vacía o de
solo espacios en blanco equivale a no indicar motivo y se guarda como ausente;
un motivo con texto se guarda sin los espacios sobrantes en sus extremos. No se
validan categorías predefinidas. La categorización de motivos queda fuera de
este capability.

#### Scenario: Ausencia con motivo

- **GIVEN** un turno `AGENDADO` cuyo inicio ya llegó
- **WHEN** se registra su ausencia indicando un motivo
- **THEN** el turno queda `AUSENTE` y conserva el motivo indicado

#### Scenario: Ausencia sin motivo

- **GIVEN** un turno `AGENDADO` cuyo inicio ya llegó
- **WHEN** se registra su ausencia sin indicar un motivo
- **THEN** la operación se acepta y el turno queda `AUSENTE` sin motivo

#### Scenario: Motivo vacío o solo con espacios

- **GIVEN** un turno `AGENDADO` cuyo inicio ya llegó
- **WHEN** se registra su ausencia con motivo `""` y, por separado, con motivo
  `"   "`
- **THEN** en ambos casos el turno queda `AUSENTE` sin motivo

#### Scenario: Motivo con espacios en los extremos

- **GIVEN** un turno `AGENDADO` cuyo inicio ya llegó
- **WHEN** se registra su ausencia con motivo `"  no se presento  "`
- **THEN** el turno queda `AUSENTE` y conserva el motivo `"no se presento"`

### Requirement: El cierre exige un turno existente

El sistema SHALL rechazar con `RN-TU-02` toda operación de cierre referida a un
identificador de turno inexistente, sin registrar ningún evento de auditoría.

#### Scenario: Ausencia sobre un turno inexistente

- **GIVEN** un identificador de turno que no corresponde a ningún turno y un
  usuario responsable válido
- **WHEN** se intenta registrar su ausencia
- **THEN** la operación se rechaza con `RN-TU-02`, no se persiste ningún turno y
  no se registra ningún evento de auditoría

#### Scenario: Atención sobre un turno inexistente

- **GIVEN** un identificador de turno que no corresponde a ningún turno y un
  usuario responsable válido
- **WHEN** se intenta registrar su atención
- **THEN** la operación se rechaza con `RN-TU-02`, no se persiste ningún turno y
  no se registra ningún evento de auditoría

### Requirement: El cierre queda auditado con usuario obligatorio

El sistema SHALL registrar un evento `MARCADO_AUSENTE` o `MARCADO_ATENDIDO` con
usuario e instante en todo cierre exitoso, y SHALL rechazar la operación con
`RN-AU-01` cuando no se pueda identificar al usuario. Un rechazo NO SHALL
generar ningún evento (`RN-AU-01`, `RN-AU-02`).

#### Scenario: Auditoría de la ausencia

- **GIVEN** un turno `AGENDADO` cuyo inicio ya llegó
- **WHEN** se registra su ausencia indicando un usuario responsable
- **THEN** se registra un evento `MARCADO_AUSENTE` referido al turno, con ese
  usuario y la marca de tiempo del instante actual

#### Scenario: Auditoría de la atención

- **GIVEN** un turno `AGENDADO`
- **WHEN** se registra su atención indicando un usuario responsable
- **THEN** se registra un evento `MARCADO_ATENDIDO` referido al turno, con ese
  usuario y la marca de tiempo del instante actual

#### Scenario: Cierre sin usuario responsable

- **GIVEN** un turno `AGENDADO` y un usuario vacío
- **WHEN** se intenta registrar su atención o su ausencia
- **THEN** la operación se rechaza con `RN-AU-01` y no se registra ningún evento

#### Scenario: Un cierre rechazado no deja auditoría

- **GIVEN** un turno en estado terminal y un usuario responsable
- **WHEN** se intenta cerrarlo de nuevo
- **THEN** la operación se rechaza con `RN-TU-02` y no se registra ningún evento

### Requirement: La ausencia libera la franja y la atención la conserva

Después de registrar `AUSENTE`, el sistema SHALL permitir agendar otro turno
sobre la misma franja; después de registrar `ATENDIDO`, SHALL seguir
considerando ocupada esa franja (`RN-AG-07`).

#### Scenario: Alta sobre una franja ausente

- **GIVEN** un turno del profesional `P1` en el sillón `S1` sobre `[10:00, 10:30)`
  que pasó a `AUSENTE`
- **WHEN** se agenda un turno de `P1` en `S1` sobre `[10:00, 10:30)`
- **THEN** el alta se acepta y el turno nuevo queda `AGENDADO`

#### Scenario: Alta sobre una franja atendida

- **GIVEN** un turno del profesional `P1` en el sillón `S1` sobre `[10:00, 10:30)`
  que pasó a `ATENDIDO`
- **WHEN** se agenda un turno de `P1` en `S1` sobre `[10:15, 10:45)`
- **THEN** el alta se rechaza con `RN-AG-02`

### Requirement: El cierre del turno no notifica al paciente

El sistema NO SHALL emitir ningún aviso al paciente como consecuencia de
registrar `ATENDIDO` o `AUSENTE`.

#### Scenario: Cerrar un turno no genera avisos

- **GIVEN** un turno `AGENDADO`
- **WHEN** se registra su atención o su ausencia
- **THEN** no se emite ningún aviso al paciente
