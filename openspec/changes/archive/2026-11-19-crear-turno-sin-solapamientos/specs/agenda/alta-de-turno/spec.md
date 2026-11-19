# Spec Delta

## Purpose

Cubre el alta de un turno en la agenda del consultorio: validación de los
recursos involucrados, derivación de la duración a partir de la prestación,
creación del turno en estado inicial, registro de auditoría y aviso al paciente.

## ADDED Requirements

### Requirement: El alta crea un turno agendado sobre una franja libre

El sistema SHALL crear el turno en estado `AGENDADO` cuando los recursos son
válidos y la franja está disponible, ocupando el intervalo `[inicio, fin)`
derivado de la prestación, y SHALL devolver el turno creado con su
identificador.

#### Scenario: Camino feliz — limpieza de 30 minutos a las 10:00

- **GIVEN** un paciente activo, un profesional activo y un sillón activo, sin
  turnos vigentes en la franja, y la prestación "Limpieza" activa con duración
  de 30 minutos
- **WHEN** se agenda un turno para ese paciente con ese profesional, ese sillón y
  esa prestación, con inicio a las `10:00` de una fecha futura
- **THEN** se crea un turno en estado `AGENDADO` que ocupa `[10:00, 10:30)`, con
  `esSobreturno` en falso
- **AND** queda registrado un evento de auditoría con la acción `CREADO`
  referido a ese turno, con usuario y marca de tiempo

### Requirement: La duración del turno se deriva de la prestación

El sistema SHALL calcular `fin = inicio + duración de la prestación`
(`RN-AG-01`). La operación de alta NO SHALL aceptar una duración ni un `fin`
provistos por quien agenda, y el turno SHALL conservar la duración vigente al
momento de agendarse.

#### Scenario: Prestación de 90 minutos

- **GIVEN** la prestación "Conducto" activa con duración de 90 minutos
- **WHEN** se agenda un turno de esa prestación con inicio a las `10:00`
- **THEN** el turno creado ocupa `[10:00, 11:30)`

#### Scenario: El fin no se puede fijar a mano

- **GIVEN** la prestación "Limpieza" activa con duración de 30 minutos
- **WHEN** se inspecciona la operación de alta de turno
- **THEN** no existe ninguna forma de indicar el `fin` ni la duración del turno:
  el único dato temporal que acepta es el `inicio`

#### Scenario: Cambiar la duración del catálogo no altera un turno ya agendado

- **GIVEN** un turno agendado sobre `[10:00, 10:30)` con la prestación "Limpieza"
  de 30 minutos
- **WHEN** la duración de "Limpieza" en el catálogo pasa a 45 minutos
- **THEN** el turno ya agendado sigue ocupando `[10:00, 10:30)`

### Requirement: El alta exige recursos existentes y activos

El sistema SHALL rechazar el alta cuando el paciente, el profesional, el sillón
o la prestación no existan, y cuando el profesional, el sillón o la prestación
estén inactivos, informando el código de regla `RN-AG-08`. El rechazo SHALL
ocurrir antes de evaluar la disponibilidad.

#### Scenario: Profesional inactivo

- **GIVEN** un profesional con `activo` en falso y el resto de los recursos
  válidos
- **WHEN** se intenta agendar un turno con ese profesional
- **THEN** el alta se rechaza con el código `RN-AG-08` y no se crea ningún turno

#### Scenario: Sillón inactivo

- **GIVEN** un sillón con `activo` en falso y el resto de los recursos válidos
- **WHEN** se intenta agendar un turno en ese sillón
- **THEN** el alta se rechaza con el código `RN-AG-08` y no se crea ningún turno

#### Scenario: Prestación inactiva

- **GIVEN** una prestación con `activa` en falso y el resto de los recursos
  válidos
- **WHEN** se intenta agendar un turno con esa prestación
- **THEN** el alta se rechaza con el código `RN-AG-08` y no se crea ningún turno

#### Scenario: Recurso inexistente

- **GIVEN** un identificador de paciente que no corresponde a ningún paciente
- **WHEN** se intenta agendar un turno con ese identificador
- **THEN** el alta se rechaza con el código `RN-AG-08` y no se crea ningún turno

### Requirement: El inicio del turno debe ser futuro

El sistema SHALL rechazar el alta de un turno cuyo `inicio` no sea posterior al
instante actual, informando el código de regla `RN-AG-08`.

#### Scenario: Inicio en el pasado

- **GIVEN** recursos válidos y activos
- **WHEN** se intenta agendar un turno con inicio una hora antes del instante
  actual
- **THEN** el alta se rechaza con el código `RN-AG-08` y no se crea ningún turno

#### Scenario: Inicio exactamente en el instante actual

- **GIVEN** recursos válidos y activos
- **WHEN** se intenta agendar un turno con inicio igual al instante actual
- **THEN** el alta se rechaza con el código `RN-AG-08`, porque el inicio debe ser
  estrictamente futuro

### Requirement: El alta rechaza el solapamiento de agenda

El sistema SHALL verificar la disponibilidad de la franja antes de crear el
turno y SHALL rechazar el alta cuando la franja no esté disponible, propagando
el código de regla informado por la disponibilidad (`RN-AG-02` o `RN-AG-03`).
Un alta rechazada NO SHALL crear el turno ni registrar auditoría ni notificar.

#### Scenario: Error de negocio — el profesional ya tiene un turno solapado

- **GIVEN** un turno vigente del profesional `P1` en el sillón `S1` sobre
  `[10:00, 10:30)`
- **WHEN** se intenta agendar un turno del profesional `P1` en `[10:15, 10:45)`
- **THEN** el alta se rechaza con una violación de regla de negocio cuyo código
  es `RN-AG-02`
- **AND** no se crea ningún turno, no se registra ningún evento de auditoría y no
  se dispara ninguna notificación

#### Scenario: Error de negocio — el sillón ya está ocupado por otro profesional

- **GIVEN** un turno vigente del profesional `P1` en el sillón `S1` sobre
  `[10:00, 10:30)`
- **WHEN** se intenta agendar un turno del profesional `P2`, libre, en el sillón
  `S1` en `[10:15, 10:45)`
- **THEN** el alta se rechaza con una violación de regla de negocio cuyo código
  es `RN-AG-03`
- **AND** no se crea ningún turno

#### Scenario: Caso de borde — turno contiguo al existente

- **GIVEN** un turno vigente sobre `[10:00, 10:30)` del profesional `P1` en el
  sillón `S1`
- **WHEN** se agenda un turno de 30 minutos con inicio a las `10:30`, para el
  **mismo profesional `P1` y el mismo sillón `S1`**
- **THEN** el alta se **acepta** y el turno nuevo queda `AGENDADO` ocupando
  `[10:30, 11:00)`, porque los intervalos son semiabiertos y el solapamiento se
  decide con `A.inicio < B.fin && B.inicio < A.fin`, con `<` estricto

#### Scenario: Caso de borde — turno inmediatamente anterior al existente

- **GIVEN** un turno vigente sobre `[10:00, 10:30)` del profesional `P1` en el
  sillón `S1`
- **WHEN** se agenda un turno de 30 minutos con inicio a las `09:30`, para el
  mismo profesional y el mismo sillón
- **THEN** el alta se acepta y el turno nuevo queda `AGENDADO` ocupando
  `[09:30, 10:00)`

#### Scenario: Una franja liberada por cancelación vuelve a estar disponible

- **GIVEN** un turno en estado `CANCELADO` del profesional `P1` en el sillón `S1`
  sobre `[10:00, 10:30)`
- **WHEN** se agenda un turno del profesional `P1` en el sillón `S1` sobre
  `[10:00, 10:30)`
- **THEN** el alta se acepta y el turno nuevo queda `AGENDADO`

### Requirement: Toda alta queda auditada con usuario obligatorio

El sistema SHALL registrar un evento de auditoría con la acción `CREADO`, el
identificador del turno, el usuario responsable y la marca de tiempo, en toda
alta exitosa. El sistema SHALL rechazar el alta cuando no se pueda identificar
al usuario responsable (`RN-AU-01`).

#### Scenario: Alta sin usuario responsable

- **GIVEN** recursos válidos, activos y una franja libre
- **WHEN** se intenta agendar un turno sin indicar el usuario responsable
- **THEN** el alta se rechaza con el código `RN-AU-01` y no se crea ningún turno

#### Scenario: El registro de auditoría es append-only

- **GIVEN** un turno recién agendado con su evento `CREADO` registrado
- **WHEN** se agenda un segundo turno
- **THEN** el registro conserva el evento anterior y suma el nuevo, sin modificar
  ni borrar ninguno

### Requirement: El alta avisa al paciente sin depender del aviso

El sistema SHALL disparar el aviso al paciente a través de un puerto de
notificación del dominio tras crear el turno. Un fallo de la notificación NO
SHALL invalidar el alta (`RN-NO-03`), y el dominio NO SHALL conocer ningún
proveedor concreto de mensajería (`RN-NO-01`).

#### Scenario: El alta exitosa dispara la notificación

- **GIVEN** recursos válidos y una franja libre
- **WHEN** se agenda el turno
- **THEN** el puerto de notificación recibe un aviso referido a ese turno

#### Scenario: Una notificación que falla no invalida el turno

- **GIVEN** un puerto de notificación que falla al ser invocado
- **WHEN** se agenda un turno sobre una franja libre con recursos válidos
- **THEN** el turno queda creado en estado `AGENDADO` y su evento `CREADO` queda
  registrado, y la operación de alta no falla

### Requirement: El turno nace agendado y sus transiciones son terminales

El estado inicial de todo turno creado SHALL ser `AGENDADO`. El sistema SHALL
permitir pasar únicamente de `AGENDADO` a `ATENDIDO`, `CANCELADO` o `AUSENTE`,
y SHALL rechazar toda otra transición, incluida cualquier salida de esos tres
estados terminales (`RN-TU-01`, `RN-TU-02`).

#### Scenario: Transiciones válidas desde AGENDADO

- **GIVEN** un turno en estado `AGENDADO`
- **WHEN** se consulta si la transición a `ATENDIDO`, a `CANCELADO` y a `AUSENTE`
  está permitida
- **THEN** las tres resultan permitidas

#### Scenario: Los estados terminales no tienen salida

- **GIVEN** un turno en estado `CANCELADO`, otro en `ATENDIDO` y otro en
  `AUSENTE`
- **WHEN** se consulta si alguno puede pasar a cualquier otro estado
- **THEN** ninguna transición resulta permitida

#### Scenario: Cada estado declara si ocupa la agenda

- **GIVEN** los cuatro estados del turno
- **WHEN** se consulta cuáles ocupan la agenda
- **THEN** `AGENDADO` y `ATENDIDO` ocupan, y `CANCELADO` y `AUSENTE` no ocupan

### Requirement: Los datos de paciente usados por el sistema son ficticios

Ningún dato de persona incorporado al repositorio —semillas, fixtures, ejemplos
o pruebas— SHALL corresponder a una persona real. Un dato real detectado es un
defecto bloqueante (`RN-PA-04`).

#### Scenario: Los datos de prueba son ficticios

- **GIVEN** el conjunto de datos de pacientes usado por las pruebas del change
- **WHEN** se revisan nombres, documentos, teléfonos y correos
- **THEN** todos son ficticios y ninguno identifica a una persona real
