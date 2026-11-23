# Spec Delta: Cancelar Turno con Anticipación Mínima

## Purpose

Permitir cancelar un turno agendado indicando un motivo, respetando la política de anticipación mínima configurable de N horas, liberando la franja horaria para nuevas altas y registrando auditoría y notificación.

## ADDED Requirements

### Requirement: Solo se cancela un turno agendado y con motivo obligatorio

El sistema SHALL permitir la cancelación únicamente cuando el turno se encuentra en estado `AGENDADO` y SHALL rechazar la operación con el código `RN-TU-02` si el turno ya está en estado `CANCELADO`, `ATENDIDO` o `AUSENTE`. Toda cancelación SHALL exigir un motivo no vacío (`RN-TU-05`).

#### Scenario: Cancelar un turno agendado con motivo válido
- **GIVEN** un turno en estado `AGENDADO` cuyo inicio dista más de `N` horas del instante actual
- **WHEN** se solicita su cancelación indicando un motivo válido y usuario responsable
- **THEN** la operación se acepta, el turno pasa a estado `CANCELADO` conservando su motivo y la franja queda liberada

#### Scenario: Cancelar sin motivo o con motivo en blanco
- **GIVEN** un turno en estado `AGENDADO` con anticipación suficiente
- **WHEN** se intenta cancelarlo con motivo nulo o compuesto únicamente por espacios en blanco
- **THEN** la operación se rechaza con el código `RN-TU-05` y el turno permanece `AGENDADO`

#### Scenario: Cancelar un turno ya cancelado
- **GIVEN** un turno que ya se encuentra en estado `CANCELADO`
- **WHEN** se intenta cancelarlo nuevamente
- **THEN** la operación se rechaza con el código `RN-TU-02`

#### Scenario: Cancelar un turno atendido
- **GIVEN** un turno en estado `ATENDIDO`
- **WHEN** se intenta cancelar
- **THEN** la operación se rechaza con el código `RN-TU-02`

#### Scenario: Cancelar un turno ausente
- **GIVEN** un turno en estado `AUSENTE`
- **WHEN** se intenta cancelar
- **THEN** la operación se rechaza con el código `RN-TU-02`

### Requirement: Respetar la política de anticipación mínima configurable

El sistema SHALL validar que la cancelación se realice con al menos `N` horas de anticipación respecto del inicio del turno (`RN-AG-05`), donde `N` es configurable. Si faltan menos de `N` horas, la operación SHALL ser rechazada con el código `RN-AG-05`.

#### Scenario: Cancelar fuera de plazo con menos de N horas
- **GIVEN** un turno agendado cuyo inicio dista menos de `N` horas respecto del reloj del sistema
- **WHEN** se solicita su cancelación con motivo válido
- **THEN** la operación se rechaza con el código `RN-AG-05` y el turno no se cancela

#### Scenario: Cancelar con exactamente N horas de anticipación (borde inclusivo)
- **GIVEN** un turno agendado y un reloj posicionado exactamente a `N` horas del inicio del turno
- **WHEN** se solicita su cancelación con motivo válido
- **THEN** la operación se acepta, porque el límite de anticipación es inclusivo (`D-02`)

### Requirement: Liberación inmediata de la franja horaria

El sistema SHALL asegurar que un turno en estado `CANCELADO` deje de ocupar la agenda de inmediato (`RN-AG-07`).

#### Scenario: Franja liberada disponible para una nueva alta
- **GIVEN** un turno cancelado exitosamente en la franja `[10:00, 10:30)`
- **WHEN** se solicita dar de alta un nuevo turno para el mismo profesional y sillón en la franja `[10:00, 10:30)`
- **THEN** la nueva alta es aceptada sin conflicto de disponibilidad

#### Scenario: Alta contigua tras cancelación
- **GIVEN** un turno cancelado en `[10:00, 10:30)`
- **WHEN** se solicita un nuevo turno en `[10:30, 11:00)` para los mismos recursos
- **THEN** el turno se agenda normalmente

### Requirement: Auditoría y notificación al paciente

El sistema SHALL registrar un evento de auditoría `CANCELADO` con el usuario responsable y la marca de tiempo (`RN-AU-01`). Asimismo, SHALL emitir un aviso al paciente vía el notificador, y las fallas del notificador NO SHALL impedir la cancelación (`RN-NO-03`).

#### Scenario: Registro de auditoría obligatorio
- **GIVEN** un turno agendado y un usuario identificado
- **WHEN** se cancela el turno válidamente
- **THEN** se registra un evento de auditoría con acción `CANCELADO`, el usuario y el timestamp de la operación

#### Scenario: Cancelación rechazada sin usuario responsable
- **GIVEN** una solicitud de cancelación con usuario nulo o vacío
- **WHEN** se ejecuta la operación
- **THEN** se rechaza con el código `RN-AU-01`

#### Scenario: Resiliencia ante falla del notificador
- **GIVEN** un notificador de pacientes que lanza un error de comunicación
- **WHEN** se cancela un turno con anticipación y motivo válidos
- **THEN** la cancelación se completa con éxito y el turno queda `CANCELADO`

