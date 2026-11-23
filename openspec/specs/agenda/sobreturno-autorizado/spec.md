# Sobreturno autorizado Specification

## Purpose

Cubre el alta excepcional de un sobreturno en la agenda del consultorio: la
única excepción controlada a la regla de no solapamiento, condicionada a marca
explícita, autorización del odontólogo dueño, motivo obligatorio, registro de
autorización 1:1 y evento de auditoría de excepción.

## Requirements

### Requirement: El sobreturno autorizado permite agendar sobre una franja ocupada

El sistema SHALL permitir agendar un turno en estado `AGENDADO` sobre una franja
ocupada por el profesional, por el sillón o por ambos únicamente cuando se
solicite de forma explícita como sobreturno con autorización del odontólogo
dueño y motivo válido (`RN-AG-06`, `D-05`). El turno creado SHALL quedar marcado
con `esSobreturno` verdadero.

#### Scenario: Camino feliz — sobreturno con solapamiento de profesional autorizado por el dueño

- **GIVEN** un paciente activo, un profesional activo, un sillón activo y la
  prestación "Limpieza" activa de 30 minutos
- **AND** un turno vigente del mismo profesional sobre `[10:00, 10:30)` de una
  fecha futura
- **WHEN** un odontólogo dueño solicita explícitamente un sobreturno para ese
  profesional en el mismo horario `10:00`, indicando usuario responsable y el
  motivo "Urgencia por dolor agudo"
- **THEN** el turno se crea en estado `AGENDADO` sobre `[10:00, 10:30)` con
  `esSobreturno` en verdadero
- **AND** queda persistida la autorización de sobreturno vinculada 1:1 a ese
  turno con responsable, motivo y marca de tiempo
- **AND** queda registrado un evento de auditoría `SOBRETURNO_AUTORIZADO` con el
  usuario responsable y marca de tiempo

#### Scenario: Sobreturno con solapamiento de sillón autorizado por el dueño

- **GIVEN** un turno vigente de otro profesional en el sillón `S1` sobre
  `[10:00, 10:30)` de una fecha futura
- **WHEN** un odontólogo dueño solicita explícitamente un sobreturno para un
  profesional libre en el mismo sillón `S1` a las `10:15`, con responsable y
  motivo válidos
- **THEN** el sobreturno se crea en estado `AGENDADO` con `esSobreturno` en
  verdadero, autorización 1:1 persistida y evento `SOBRETURNO_AUTORIZADO`

#### Scenario: Sobreturno con solapamiento simultáneo de profesional y sillón

- **GIVEN** un turno vigente del profesional `P1` en el sillón `S1` sobre
  `[10:00, 10:30)` de una fecha futura
- **WHEN** un odontólogo dueño solicita explícitamente un sobreturno para `P1`
  en `S1` a las `10:15`, con responsable y motivo válidos
- **THEN** el sobreturno se crea en estado `AGENDADO` con `esSobreturno` en
  verdadero, omitiendo tanto `RN-AG-02` como `RN-AG-03`

### Requirement: El sobreturno exige marca explícita, responsable y motivo

El sistema SHALL exigir de forma obligatoria una marca explícita de sobreturno,
un usuario responsable no vacío y un motivo no vacío (`RN-AG-06`, `RN-AU-01`).
Ante la falta o invalidez de cualquiera de los tres datos, el sistema SHALL
rechazar la operación con el código `RN-AG-06`, sin crear turno, sin persistir
autorización, sin auditar, sin notificar y sin reintentar automáticamente.

#### Scenario: Error de negocio — solicitud sin marca explícita de sobreturno

- **GIVEN** recursos válidos y activos sobre una franja ocupada
- **WHEN** se invoca el alta de sobreturno con la marca explícita en falso, aun
  informando responsable dueño y motivo
- **THEN** la operación se rechaza con el código `RN-AG-06`
- **AND** no se crea ningún turno, no se persiste autorización, no se registra
  auditoría y no se dispara notificación

#### Scenario: Error de negocio — solicitud sin usuario responsable

- **GIVEN** recursos válidos y activos sobre una franja ocupada
- **WHEN** se solicita un sobreturno con marca explícita verdadera y motivo
  válido, pero sin usuario responsable o con responsable en blanco
- **THEN** la operación se rechaza con el código `RN-AG-06` y sin efectos
  secundarios

#### Scenario: Error de negocio — solicitud sin motivo de autorización

- **GIVEN** recursos válidos y activos sobre una franja ocupada
- **WHEN** se solicita un sobreturno con marca explícita verdadera y responsable
  dueño, pero sin motivo o con motivo en blanco
- **THEN** la operación se rechaza con el código `RN-AG-06` y sin efectos
  secundarios

#### Scenario: Nunca existe reintento automático tras un rechazo

- **GIVEN** una franja ocupada y una solicitud de sobreturno inválida por falta
  de motivo
- **WHEN** la operación falla con `RN-AG-06`
- **THEN** el sistema termina con rechazo inmediato y no persiste ningún turno
  ni reintenta automáticamente la operación

### Requirement: Solo el odontólogo dueño puede autorizar un sobreturno

Bajo el supuesto vigente `SU-04` (`PA-03`), el sistema SHALL aceptar la
autorización de sobreturno únicamente cuando provenga de un usuario con rol de
odontólogo dueño. Todo intento de autorización por otro rol o sin rol SHALL ser
rechazado con el código `RN-AG-06` sin efectos secundarios.

#### Scenario: Error de negocio — odontólogo colega intenta autorizar un sobreturno

- **GIVEN** recursos válidos y activos sobre una franja ocupada
- **WHEN** un usuario con rol de odontólogo colega solicita un sobreturno con
  marca explícita, responsable y motivo
- **THEN** la operación se rechaza con el código `RN-AG-06`
- **AND** no se crea turno, no se guarda autorización ni se registra auditoría

#### Scenario: Error de negocio — recepción intenta autorizar un sobreturno

- **GIVEN** recursos válidos y activos sobre una franja ocupada
- **WHEN** un usuario con rol de recepción solicita un sobreturno con marca
  explícita, responsable y motivo
- **THEN** la operación se rechaza con el código `RN-AG-06` y sin efectos
  secundarios

#### Scenario: Error de negocio — solicitud sin rol de autorización

- **GIVEN** recursos válidos y activos sobre una franja ocupada
- **WHEN** se solicita un sobreturno sin indicar el rol de quien autoriza
- **THEN** la operación se rechaza con el código `RN-AG-06` y sin efectos
  secundarios

### Requirement: Las reglas de recursos activos, inicio futuro y duración siguen aplicando

El sobreturno SHALL omitir únicamente las reglas de solapamiento (`RN-AG-02` y
`RN-AG-03`). El sistema SHALL seguir rechazando con `RN-AG-08` cualquier recurso
inexistente o inactivo y cualquier inicio que no sea estrictamente futuro, y
SHALL derivar `fin = inicio + duración de la prestación` (`RN-AG-01`).

#### Scenario: Error de negocio — profesional inactivo aun con autorización del dueño

- **GIVEN** un profesional con `activo` en falso y el resto de los recursos
  válidos
- **WHEN** un odontólogo dueño solicita un sobreturno con marca explícita,
  responsable y motivo válidos
- **THEN** la operación se rechaza con el código `RN-AG-08` y sin efectos
  secundarios

#### Scenario: Error de negocio — sillón inactivo aun con autorización del dueño

- **GIVEN** un sillón con `activo` en falso y el resto de los recursos válidos
- **WHEN** un odontólogo dueño solicita un sobreturno con autorización completa
- **THEN** la operación se rechaza con el código `RN-AG-08` y sin efectos
  secundarios

#### Scenario: Error de negocio — prestación inactiva aun con autorización del dueño

- **GIVEN** una prestación con `activa` en falso y el resto de los recursos
  válidos
- **WHEN** un odontólogo dueño solicita un sobreturno con autorización completa
- **THEN** la operación se rechaza con el código `RN-AG-08` y sin efectos
  secundarios

#### Scenario: Error de negocio — recurso inexistente aun con autorización del dueño

- **GIVEN** un identificador de paciente que no corresponde a ningún paciente
- **WHEN** un odontólogo dueño solicita un sobreturno con autorización completa
- **THEN** la operación se rechaza con el código `RN-AG-08` y sin efectos
  secundarios

#### Scenario: Error de negocio — inicio pasado o igual al instante actual

- **GIVEN** recursos válidos y activos
- **WHEN** un odontólogo dueño solicita un sobreturno con inicio en el pasado o
  igual al instante actual
- **THEN** la operación se rechaza con el código `RN-AG-08` y sin efectos
  secundarios

#### Scenario: La duración se deriva de la prestación y no se fija a mano

- **GIVEN** la prestación "Conducto" activa de 90 minutos
- **WHEN** un odontólogo dueño agenda un sobreturno de esa prestación a las
  `10:00`
- **THEN** el sobreturno ocupa `[10:00, 11:30)` y la solicitud solo acepta el
  `inicio` como dato temporal

### Requirement: El sobreturno queda trazado y avisa al paciente de forma resiliente

En todo sobreturno aceptado, el sistema SHALL persistir la autorización 1:1 con
el turno, registrar un evento de auditoría `SOBRETURNO_AUTORIZADO` en modalidad
append-only (`RN-AU-01`, `RN-AU-02`) y disparar la notificación al paciente sin
que un fallo del canal invalide el sobreturno (`RN-NO-03`).

#### Scenario: Persistencia 1:1 de autorización y auditoría append-only

- **GIVEN** un sobreturno autorizado con responsable "Dra. Ana Gomez" y motivo
  "Urgencia endodóntica"
- **WHEN** se consulta la autorización asociada al identificador del turno y su
  historial de auditoría
- **THEN** existe exactamente una autorización 1:1 con ese turno que conserva
  responsable, motivo y marca temporal
- **AND** el historial de auditoría contiene el evento `SOBRETURNO_AUTORIZADO`
  sin alterar ni borrar eventos previos

#### Scenario: Un fallo en la notificación no invalida el sobreturno

- **GIVEN** un canal de notificación que falla al ser invocado
- **WHEN** un odontólogo dueño agenda un sobreturno válido sobre una franja
  ocupada
- **THEN** el turno queda `AGENDADO` con `esSobreturno` verdadero, la
  autorización y el evento `SOBRETURNO_AUTORIZADO` quedan persistidos, y la
  operación no falla

### Requirement: El sobreturno sobre franja libre o contigua es válido y no altera el alta normal

El sistema SHALL permitir agendar un sobreturno sobre una franja libre o solo
contigua sin romper la regla de solapamiento. Asimismo, la existencia de
sobreturnos NO SHALL alterar el comportamiento por defecto del alta normal ni la
veracidad de la evaluación de disponibilidad (`D-05`).

#### Scenario: Caso de borde — sobreturno sobre franja libre

- **GIVEN** recursos válidos y activos sin ningún turno vigente en la franja
  `[11:00, 11:30)`
- **WHEN** un odontólogo dueño solicita un sobreturno válido a las `11:00`
- **THEN** el turno se crea en estado `AGENDADO` con `esSobreturno` en
  verdadero, autorización 1:1 y evento `SOBRETURNO_AUTORIZADO`

#### Scenario: Caso de borde — sobreturno sobre franja solo contigua

- **GIVEN** un turno vigente sobre `[10:00, 10:30)` del profesional `P1` en el
  sillón `S1`
- **WHEN** un odontólogo dueño solicita un sobreturno válido de 30 minutos para
  `P1` y `S1` a las `10:30`
- **THEN** el turno se crea en estado `AGENDADO` sobre `[10:30, 11:00)` con
  `esSobreturno` en verdadero, autorización 1:1 y evento `SOBRETURNO_AUTORIZADO`

#### Scenario: Caso de borde — el alta normal sobre franja ocupada sigue rechazada

- **GIVEN** un turno vigente sobre `[10:00, 10:30)` y un sobreturno ya creado
  sobre esa misma franja para el profesional `P1` en el sillón `S1`
- **WHEN** se intenta realizar un alta normal sobre `[10:15, 10:45)` para `P1` o
  en `S1`
- **THEN** el alta normal se rechaza con el código `RN-AG-02` (o `RN-AG-03` si el
  conflicto es solo de sillón), porque el sobreturno no cambia el comportamiento
  por defecto

#### Scenario: La evaluación de disponibilidad sigue informando la franja como ocupada

- **GIVEN** un sobreturno vigente en estado `AGENDADO` sobre `[10:00, 10:30)`
- **WHEN** se evalúa la disponibilidad sobre `[10:15, 10:45)` para el mismo
  profesional o el mismo sillón
- **THEN** la evaluación informa que la franja no está disponible (`RN-AG-02` o
  `RN-AG-03`), sin ocultar la ocupación por tratarse de un sobreturno
