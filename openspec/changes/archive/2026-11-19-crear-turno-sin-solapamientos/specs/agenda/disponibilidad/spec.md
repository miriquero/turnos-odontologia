# Spec Delta

## Purpose

Define qué significa que una franja horaria esté disponible en la agenda del
consultorio: la semántica del intervalo semiabierto `[inicio, fin)` y la regla
de solapamiento, evaluada de forma independiente contra la ocupación del
profesional y la del sillón.

## ADDED Requirements

### Requirement: El intervalo de agenda es semiabierto

Un intervalo de agenda SHALL representar la franja semiabierta `[inicio, fin)`:
incluye su instante de inicio y excluye su instante de fin. El sistema SHALL
decidir si dos intervalos se solapan con el predicado
`A.inicio < B.fin && B.inicio < A.fin`, con comparación estrictamente menor en
ambos lados. Esta definición SHALL ser la única del sistema (`RN-AG-04`).

#### Scenario: Caso 1 de RN-AG-04 — el nuevo empieza cuando termina el existente

- **GIVEN** un intervalo existente `[10:00, 10:30)`
- **WHEN** se evalúa el intervalo nuevo `[10:30, 11:00)`
- **THEN** el sistema responde que **no se solapan**

#### Scenario: Caso 2 de RN-AG-04 — el nuevo termina cuando empieza el existente

- **GIVEN** un intervalo existente `[10:00, 10:30)`
- **WHEN** se evalúa el intervalo nuevo `[09:30, 10:00)`
- **THEN** el sistema responde que **no se solapan**

#### Scenario: Caso 3 de RN-AG-04 — solapamiento de un minuto

- **GIVEN** un intervalo existente `[10:00, 10:30)`
- **WHEN** se evalúa el intervalo nuevo `[10:29, 10:59)`
- **THEN** el sistema responde que **se solapan**

#### Scenario: Caso 4 de RN-AG-04 — intervalos idénticos

- **GIVEN** un intervalo existente `[10:00, 10:30)`
- **WHEN** se evalúa el intervalo nuevo `[10:00, 10:30)`
- **THEN** el sistema responde que **se solapan**

#### Scenario: Caso 5 de RN-AG-04 — el nuevo está contenido en el existente

- **GIVEN** un intervalo existente `[10:00, 11:30)`
- **WHEN** se evalúa el intervalo nuevo `[10:30, 11:00)`
- **THEN** el sistema responde que **se solapan**

#### Scenario: Caso 6 de RN-AG-04 — el nuevo contiene al existente

- **GIVEN** un intervalo existente `[10:30, 11:00)`
- **WHEN** se evalúa el intervalo nuevo `[10:00, 11:30)`
- **THEN** el sistema responde que **se solapan**

#### Scenario: Caso 7 de RN-AG-04 — solapamiento parcial por la derecha

- **GIVEN** un intervalo existente `[10:00, 10:30)`
- **WHEN** se evalúa el intervalo nuevo `[10:15, 10:45)`
- **THEN** el sistema responde que **se solapan**

#### Scenario: Caso 8 de RN-AG-04 — solapamiento parcial por la izquierda

- **GIVEN** un intervalo existente `[10:00, 10:30)`
- **WHEN** se evalúa el intervalo nuevo `[09:45, 10:15)`
- **THEN** el sistema responde que **se solapan**

#### Scenario: Caso 9 de RN-AG-04 — intervalos sin contacto

- **GIVEN** un intervalo existente `[10:00, 10:30)`
- **WHEN** se evalúa el intervalo nuevo `[11:00, 11:30)`
- **THEN** el sistema responde que **no se solapan**

#### Scenario: La relación de solapamiento es simétrica

- **GIVEN** dos intervalos cualesquiera A y B
- **WHEN** se evalúa el solapamiento de A contra B y el de B contra A
- **THEN** ambas evaluaciones dan el mismo resultado

### Requirement: Un intervalo inválido no se puede construir

El sistema SHALL rechazar la construcción de un intervalo cuyo `fin` no sea
estrictamente posterior a su `inicio`. Un intervalo con `fin <= inicio` es un
defecto de programación, no un caso de negocio.

#### Scenario: Fin anterior al inicio

- **GIVEN** un intento de construir el intervalo con inicio `10:30` y fin `10:00`
- **WHEN** se ejecuta la construcción
- **THEN** la construcción falla con un error de invariante y no se obtiene
  ningún intervalo

#### Scenario: Intervalo de duración cero

- **GIVEN** un intento de construir el intervalo con inicio `10:00` y fin `10:00`
- **WHEN** se ejecuta la construcción
- **THEN** la construcción falla con un error de invariante y no se obtiene
  ningún intervalo

### Requirement: Un profesional no puede tener dos turnos solapados

El sistema SHALL rechazar una solicitud de turno cuyo intervalo se solape con el
de otro turno vigente del **mismo profesional**, informando el código de regla
`RN-AG-02` (`RN-AG-02`).

#### Scenario: Solapamiento parcial del mismo profesional

- **GIVEN** un turno vigente del profesional `P1` que ocupa `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P1` en `[10:15, 10:45)`, en un
  sillón libre
- **THEN** la solicitud se rechaza con una violación de regla de negocio cuyo
  código es `RN-AG-02`

#### Scenario: Turnos contiguos del mismo profesional

- **GIVEN** un turno vigente del profesional `P1` que ocupa `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P1` en `[10:30, 11:00)`
- **THEN** la solicitud se acepta, porque los intervalos son semiabiertos

### Requirement: Un sillón no puede estar ocupado dos veces en la misma franja

El sistema SHALL rechazar una solicitud de turno cuyo intervalo se solape con el
de otro turno vigente del **mismo sillón**, informando el código de regla
`RN-AG-03`, aunque el profesional sea distinto (`RN-AG-03`).

#### Scenario: Solapamiento del mismo sillón con otro profesional

- **GIVEN** un turno vigente del profesional `P1` en el sillón `S1` que ocupa
  `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P2`, que está libre, en el
  sillón `S1` en `[10:15, 10:45)`
- **THEN** la solicitud se rechaza con una violación de regla de negocio cuyo
  código es `RN-AG-03`

#### Scenario: Turnos contiguos en el mismo sillón

- **GIVEN** un turno vigente en el sillón `S1` que ocupa `[10:00, 10:30)`
- **WHEN** se solicita un turno en el sillón `S1` en `[10:30, 11:00)`
- **THEN** la solicitud se acepta

### Requirement: Profesional y sillón se evalúan de forma independiente

El sistema SHALL evaluar la ocupación del profesional y la del sillón como dos
comprobaciones separadas, de modo que cada una pueda fallar por sí sola. Cuando
ambas fallan, el rechazo SHALL informar el código de regla del profesional
(`RN-AG-02`), por ser la comprobación de mayor precedencia.

#### Scenario: Profesional ocupado y sillón libre

- **GIVEN** un turno vigente del profesional `P1` en el sillón `S1` que ocupa
  `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P1` en el sillón `S2`, libre, en
  `[10:15, 10:45)`
- **THEN** la solicitud se rechaza con el código `RN-AG-02`

#### Scenario: Profesional libre y sillón ocupado

- **GIVEN** un turno vigente del profesional `P1` en el sillón `S1` que ocupa
  `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P2`, libre, en el sillón `S1` en
  `[10:15, 10:45)`
- **THEN** la solicitud se rechaza con el código `RN-AG-03`

#### Scenario: Profesional y sillón ocupados a la vez

- **GIVEN** un turno vigente del profesional `P1` en el sillón `S1` que ocupa
  `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P1` en el sillón `S1` en
  `[10:15, 10:45)`
- **THEN** la solicitud se rechaza con el código `RN-AG-02`

### Requirement: Solo los turnos vigentes ocupan la agenda

El sistema SHALL considerar ocupantes de la agenda únicamente a los turnos en
estado `AGENDADO` y `ATENDIDO`. Los turnos `CANCELADO` y `AUSENTE` NO SHALL
participar de la evaluación de solapamiento (`RN-AG-07`).

#### Scenario: Un turno cancelado no bloquea la franja

- **GIVEN** un turno en estado `CANCELADO` del profesional `P1` en el sillón `S1`
  sobre `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P1` en el sillón `S1` en
  `[10:00, 10:30)`
- **THEN** la solicitud se acepta

#### Scenario: Un turno con ausencia registrada no bloquea la franja

- **GIVEN** un turno en estado `AUSENTE` del profesional `P1` en el sillón `S1`
  sobre `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P1` en el sillón `S1` en
  `[10:00, 10:30)`
- **THEN** la solicitud se acepta

#### Scenario: Un turno atendido sigue ocupando su franja histórica

- **GIVEN** un turno en estado `ATENDIDO` del profesional `P1` en el sillón `S1`
  sobre `[10:00, 10:30)`
- **WHEN** se solicita un turno del profesional `P1` en `[10:15, 10:45)`
- **THEN** la solicitud se rechaza con el código `RN-AG-02`

### Requirement: La solicitud puede excluir un turno de la comparación

La solicitud de verificación SHALL aceptar el identificador de un turno a
excluir, y el sistema SHALL ignorar ese turno al evaluar el solapamiento, de
modo que un turno nunca se solape consigo mismo (`RN-TU-03`).

#### Scenario: El turno excluido no genera conflicto consigo mismo

- **GIVEN** un turno vigente `T1` del profesional `P1` en el sillón `S1` sobre
  `[10:00, 10:30)`
- **WHEN** se verifica la disponibilidad de `[10:00, 10:30)` para el mismo
  profesional y sillón, excluyendo el turno `T1`
- **THEN** la verificación resulta disponible

#### Scenario: Sin exclusión el mismo intervalo sí genera conflicto

- **GIVEN** un turno vigente `T1` del profesional `P1` en el sillón `S1` sobre
  `[10:00, 10:30)`
- **WHEN** se verifica la disponibilidad de `[10:00, 10:30)` para el mismo
  profesional y sillón, sin excluir ningún turno
- **THEN** la verificación se rechaza con el código `RN-AG-02`

### Requirement: La disponibilidad se ofrece en forma excepcional y en forma consultable

El sistema SHALL exponer dos formas de evaluar la misma regla: una verificación
que falla con una violación de regla de negocio cuando la franja no está
disponible, y una evaluación no excepcional que devuelve el resultado como dato,
con el código de la regla incumplida cuando corresponde. Ambas SHALL dar
siempre el mismo veredicto sobre la misma solicitud.

#### Scenario: La evaluación no excepcional informa franja disponible

- **GIVEN** un profesional y un sillón sin turnos vigentes en `[10:00, 10:30)`
- **WHEN** se evalúa la disponibilidad de esa franja sin usar la forma
  excepcional
- **THEN** se obtiene un resultado que indica disponible y no se produce ningún
  error

#### Scenario: La evaluación no excepcional informa el código de la regla incumplida

- **GIVEN** un turno vigente del profesional `P1` sobre `[10:00, 10:30)`
- **WHEN** se evalúa la disponibilidad de `[10:15, 10:45)` para `P1` sin usar la
  forma excepcional
- **THEN** se obtiene un resultado que indica no disponible y que expone el
  código `RN-AG-02`, sin que se lance ninguna excepción

### Requirement: La ocupación se lee por un puerto dedicado

El sistema SHALL obtener los turnos que ocupan una franja a través de un puerto
de lectura de ocupación, y NO SHALL consultar persistencia ni ninguna otra
fuente de datos de forma directa. Así, agregar nuevas fuentes de ocupación —por
ejemplo bloqueos de agenda— no obliga a modificar la regla (`R-05`, `RN-DI-01`).

#### Scenario: La evaluación consulta la ocupación del profesional y la del sillón

- **GIVEN** una fuente de ocupación que no devuelve turnos para el profesional ni
  para el sillón consultados
- **WHEN** se evalúa una solicitud de turno
- **THEN** la fuente de ocupación recibe una consulta por el profesional y otra
  por el sillón, cada una acotada al intervalo solicitado, y la solicitud resulta
  disponible

### Requirement: El cálculo de huecos libres queda fuera de la regla de disponibilidad

La evaluación de disponibilidad SHALL responder únicamente si una franja dada
está libre. El sistema NO SHALL incluir en ella el cálculo de las franjas libres
de un día ni ninguna forma de búsqueda de huecos, que se construye por fuera
consumiendo la evaluación no excepcional.

#### Scenario: La disponibilidad no ofrece búsqueda de huecos

- **GIVEN** la capacidad de disponibilidad tal como queda definida en este change
- **WHEN** un consumidor necesita las franjas libres de un día
- **THEN** no encuentra ninguna operación que las devuelva y debe construirlas
  invocando repetidamente la evaluación de una franja concreta

### Requirement: La regla de solapamiento no conoce el sobreturno

La evaluación de disponibilidad NO SHALL contemplar excepciones, autorizaciones
ni marcas de sobreturno: SHALL informar siempre el estado real de la franja. La
decisión de agendar pese a un conflicto vive fuera de esta capacidad (`D-05`).

#### Scenario: Una franja ocupada nunca se informa como disponible

- **GIVEN** un turno vigente del profesional `P1` sobre `[10:00, 10:30)`
- **WHEN** se evalúa la disponibilidad de `[10:15, 10:45)` para `P1` con
  cualquier dato de autorización presente en la solicitud
- **THEN** el resultado sigue siendo no disponible con el código `RN-AG-02`
