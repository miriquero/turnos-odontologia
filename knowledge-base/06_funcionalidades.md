# Funcionalidades

Organizadas por **épica** y luego por **historia de usuario** (`US-NNN`). Las
épicas 1 a 5 son el **MVP** (bloque imprescindible de la sección D.3 del
informe). La épica 6 es el **bloque diferenciador** (solo si el MVP queda
firme). La épica 7 queda **fuera de alcance**.

| Épica | Alcance | Bloque |
|---|---|---|
| 1. Recursos y catálogo | Profesionales, sillones, prestaciones | MVP |
| 2. Pacientes (ficha básica) | Alta y búsqueda | MVP |
| 3. Agenda: alta con validación de disponibilidad | **El núcleo** | MVP |
| 4. Ciclo de vida del turno | Reprogramar, cancelar, atender, ausencia | MVP |
| 5. Consulta, notificación y auditoría | Agenda, huecos, puerto simulado, trazas | MVP |
| 6. Diferenciador | Reserva online, lista de espera, bloqueos, reportes, roles, exportación | Si hay margen |
| 7. Fuera de alcance | WhatsApp real, Mercado Pago, ARCA, obras sociales, odontograma, IA | No v1 |

---

## Épica 1: Recursos y catálogo

### US-001 — Administrar profesionales
**Como** odontólogo dueño
**Quiero** registrar los profesionales del consultorio con su matrícula y rol
**Para** que la agenda pueda validar la ocupación de cada uno por separado

**Criterios de aceptación**:
- [ ] Puedo crear un profesional con nombre, apellido, matrícula y rol (`DUENIO`/`COLEGA`)
- [ ] La matrícula es única; un duplicado se rechaza
- [ ] Puedo desactivar un profesional, y un profesional inactivo no acepta turnos nuevos
- [ ] No puedo desactivar un profesional con turnos agendados futuros sin resolverlos

**Reglas relacionadas**: RN-CA-03, RN-CA-04, RN-CA-05, RN-AG-08

### US-002 — Administrar sillones
**Como** odontólogo dueño
**Quiero** registrar los sillones o boxes del consultorio
**Para** que el sistema refleje la restricción física real y no solo quién atiende

**Criterios de aceptación**:
- [ ] Puedo crear un sillón con nombre único
- [ ] Puedo desactivarlo, y un sillón inactivo no acepta turnos nuevos
- [ ] El sillón es un recurso de primera clase: la agenda se puede consultar por sillón

**Reglas relacionadas**: RN-CA-02, RN-CA-04, RN-CA-05, RN-AG-03, RN-AG-08

> **Nota de Discovery**: el sillón como recurso agendable aparece en solo 4 de 19
> fichas relevadas y en **ninguna de origen argentino**. Esta historia es
> literalmente el vacío de mercado que el proyecto ataca.

### US-003 — Administrar el catálogo de prestaciones con su duración
**Como** odontólogo dueño
**Quiero** definir cada prestación con su duración en minutos
**Para** que el turno herede la duración y nadie la escriba a mano

**Criterios de aceptación**:
- [ ] Puedo crear una prestación con nombre único y duración en minutos
- [ ] Una duración `<= 0` se rechaza
- [ ] La duración es configurable sin tocar el código (es un supuesto no validado)
- [ ] Cambiar la duración de una prestación **no** recalcula los turnos ya agendados

**Reglas relacionadas**: RN-AG-01, RN-CA-01, RN-CA-02

---

## Épica 2: Pacientes (ficha básica)

### US-004 — Registrar un paciente con ficha básica
**Como** recepción
**Quiero** dar de alta un paciente con sus datos de contacto e identificación
**Para** poder asignarle turnos y poder notificarlo

**Criterios de aceptación**:
- [ ] Puedo crear un paciente con nombre, apellido, documento y al menos un canal (teléfono o email)
- [ ] El documento es único
- [ ] La ficha **no** incluye odontograma, plan de tratamiento ni historia clínica
- [ ] **Todos los datos usados en ejemplos, seeds y tests son ficticios**

**Reglas relacionadas**: RN-PA-01, RN-PA-02, RN-PA-03, **RN-PA-04**

### US-005 — Buscar un paciente
**Como** recepción
**Quiero** buscar un paciente por documento, apellido o teléfono
**Para** agendarle un turno sin duplicar su ficha

**Criterios de aceptación**:
- [ ] Busco por documento exacto y por apellido parcial
- [ ] El resultado indica si el paciente está activo

**Reglas relacionadas**: RN-PA-02, RN-PA-05

---

## Épica 3: Agenda — alta de turno con validación de disponibilidad

> **Es el núcleo del producto.** Todo lo demás existe para sostenerlo. Las
> historias de esta épica son las que **deben** tener cobertura de tests sobre
> todos sus casos borde.

### US-006 — Agendar un turno con duración derivada de la prestación
**Como** recepción
**Quiero** agendar un turno indicando paciente, profesional, sillón, prestación y hora de inicio
**Para** ocupar la agenda sin tener que calcular la hora de fin

**Criterios de aceptación**:
- [ ] Indico solo el inicio; el fin se calcula como `inicio + prestacion.duracionMinutos`
- [ ] Una limpieza de 30 min agendada a las 10:00 ocupa `[10:00, 10:30)`
- [ ] Un conducto de 90 min agendado a las 10:00 ocupa `[10:00, 11:30)`
- [ ] No puedo fijar el fin manualmente
- [ ] Un turno con inicio en el pasado se rechaza
- [ ] Los cuatro recursos referenciados deben existir y estar activos
- [ ] El turno queda en estado `AGENDADO` y genera un evento de auditoría `CREADO`

**Reglas relacionadas**: RN-AG-01, RN-AG-08, RN-TU-01, RN-AU-01

### US-007 — Impedir que un profesional tenga dos turnos simultáneos
**Como** recepción
**Quiero** que el sistema rechace un turno que pise otro del mismo profesional
**Para** no duplicar la ocupación de quien atiende

**Criterios de aceptación**:
- [ ] Un turno que solapa con otro `AGENDADO` del mismo profesional se rechaza con el código `RN-AG-02`
- [ ] El rechazo informa con qué turno existente colisiona
- [ ] Un turno del **mismo horario** pero de **otro profesional** se acepta (si el sillón está libre)
- [ ] Un turno que solapa con uno `CANCELADO` o `AUSENTE` del mismo profesional **se acepta**

**Reglas relacionadas**: **RN-AG-02**, RN-AG-04, RN-AG-07

### US-008 — Impedir que un sillón esté ocupado dos veces en la misma franja
**Como** odontólogo dueño
**Quiero** que el sistema rechace un turno que ocupe un sillón ya ocupado
**Para** que la agenda respete la restricción física del consultorio

**Criterios de aceptación**:
- [ ] Un turno que solapa con otro `AGENDADO` del mismo sillón se rechaza con el código `RN-AG-03`
- [ ] **Se rechaza aunque los profesionales sean distintos**: el sillón es un recurso independiente
- [ ] Un turno del mismo horario en **otro sillón** se acepta (si el profesional está libre)
- [ ] La validación de profesional y la de sillón son independientes: cada una puede fallar sola

**Reglas relacionadas**: **RN-AG-03**, RN-AG-02, RN-AG-04, RN-AG-07

### US-009 — Aceptar turnos contiguos: los intervalos son semiabiertos
**Como** recepción
**Quiero** poder agendar un turno que empiece exactamente cuando termina el anterior
**Para** aprovechar la agenda sin huecos artificiales

**Criterios de aceptación** (mismo profesional y mismo sillón en todos):
- [ ] Existente `[10:00, 10:30)` + nuevo `[10:30, 11:00)` → **ACEPTADO**
- [ ] Existente `[10:00, 10:30)` + nuevo `[09:30, 10:00)` → **ACEPTADO**
- [ ] Existente `[10:00, 10:30)` + nuevo `[10:29, 10:59)` → rechazado
- [ ] Existente `[10:00, 10:30)` + nuevo idéntico → rechazado
- [ ] Existente `[10:00, 11:30)` + nuevo `[10:30, 11:00)` contenido → rechazado
- [ ] Existente `[10:30, 11:00)` + nuevo `[10:00, 11:30)` contenedor → rechazado
- [ ] Existente `[10:00, 10:30)` + nuevo `[10:15, 10:45)` → rechazado
- [ ] Existente `[10:00, 10:30)` + nuevo `[09:45, 10:15)` → rechazado
- [ ] Existente `[10:00, 10:30)` + nuevo `[11:00, 11:30)` → aceptado
- [ ] El predicado implementado es `A.inicio < B.fin && B.inicio < A.fin`, con `<` estricto

**Reglas relacionadas**: **RN-AG-04** (regla de borde central del proyecto)

> Los dos primeros criterios son los que distinguen una implementación correcta
> de una incorrecta, y son exactamente lo que **ningún** sistema de los tres
> mercados relevados documenta.

### US-010 — Agendar un sobreturno con autorización explícita y auditada
**Como** odontólogo dueño
**Quiero** poder romper la regla de solapamiento de forma deliberada, dejando constancia
**Para** atender una urgencia sin que el sistema se convierta en un calendario sin reglas

**Criterios de aceptación**:
- [ ] El alta de sobreturno exige una marca explícita, un usuario responsable y un motivo
- [ ] Sin cualquiera de esos tres datos, el sobreturno se rechaza
- [ ] El turno queda con `esSobreturno = true` y es distinguible en toda consulta
- [ ] Se genera un evento de auditoría `SOBRETURNO_AUTORIZADO` con usuario y motivo
- [ ] Un sobreturno **no** es el comportamiento por defecto ante un rechazo: nunca hay reintento automático

**Reglas relacionadas**: **RN-AG-06**, RN-AU-01

---

## Épica 4: Ciclo de vida del turno

### US-011 — Reprogramar un turno revalidando la disponibilidad
**Como** recepción
**Quiero** mover un turno a un horario libre
**Para** reacomodar la agenda sin perder al paciente

**Criterios de aceptación**:
- [ ] Puedo cambiar inicio, profesional y/o sillón
- [ ] El nuevo horario se valida con las mismas reglas que un alta (`RN-AG-02` a `RN-AG-04`)
- [ ] El propio turno se excluye de la comparación: no se solapa consigo mismo
- [ ] La franja anterior queda liberada inmediatamente
- [ ] Se genera auditoría `REPROGRAMADO` con horario anterior y nuevo

**Reglas relacionadas**: RN-TU-03, RN-AG-01…04, RN-AU-01, RN-AU-03

### US-012 — No reprogramar un turno cancelado ni ya atendido
**Como** sistema
**Quiero** rechazar la reprogramación de un turno que ya terminó su ciclo
**Para** que el historial de agenda sea íntegro

**Criterios de aceptación**:
- [ ] Reprogramar un turno `CANCELADO` se rechaza con el código `RN-TU-04`
- [ ] Reprogramar un turno `ATENDIDO` se rechaza
- [ ] Reprogramar un turno `AUSENTE` se rechaza
- [ ] Solo un turno `AGENDADO` se reprograma
- [ ] La alternativa ofrecida es agendar un turno nuevo

**Reglas relacionadas**: **RN-TU-04**, RN-TU-02

### US-013 — Cancelar un turno liberando el horario
**Como** recepción
**Quiero** cancelar un turno con su motivo
**Para** que esa franja vuelva a estar disponible

**Criterios de aceptación**:
- [ ] La cancelación exige un motivo no vacío
- [ ] Tras cancelar, la franja acepta un turno nuevo del mismo profesional y sillón
- [ ] El turno queda en `CANCELADO`, estado terminal
- [ ] Se genera auditoría `CANCELADO` con usuario y motivo

**Reglas relacionadas**: RN-TU-05, RN-AG-07, RN-AU-01

### US-014 — Respetar la anticipación mínima para cancelar
**Como** odontólogo dueño
**Quiero** que no se pueda cancelar con menos de `N` horas de anticipación
**Para** tener margen de reacomodar la agenda

**Criterios de aceptación**:
- [ ] `N` es configurable, no una constante en el código
- [ ] Una cancelación con menos de `N` horas respecto del inicio se rechaza con el código `RN-AG-05`
- [ ] Una cancelación con exactamente `N` horas se acepta
- [ ] El valor de `N` está documentado como **pregunta abierta** (`PA-04`) y el valor por defecto como supuesto (`SU-03`)

**Reglas relacionadas**: **RN-AG-05**

### US-015 — Marcar un turno como atendido
**Como** odontólogo
**Quiero** marcar el turno como atendido
**Para** cerrar su ciclo y que la hora de sillón quede registrada como usada

**Criterios de aceptación**:
- [ ] Solo desde `AGENDADO`
- [ ] `ATENDIDO` es terminal y sigue ocupando su franja histórica
- [ ] Se genera auditoría `MARCADO_ATENDIDO`

**Reglas relacionadas**: RN-TU-02, RN-AG-07, RN-AU-01

### US-016 — Registrar la ausencia del paciente
**Como** recepción
**Quiero** registrar que el paciente no se presentó, con su motivo si lo hay
**Para** tener información de ausentismo y no confundirlo con una cancelación

**Criterios de aceptación**:
- [ ] `AUSENTE` es un estado distinto de `CANCELADO`
- [ ] No se le aplica la anticipación mínima de cancelación
- [ ] El motivo de ausencia es opcional pero se guarda estructurado
- [ ] La franja de un turno `AUSENTE` no bloquea un turno nuevo
- [ ] Se genera auditoría `MARCADO_AUSENTE`

**Reglas relacionadas**: RN-TU-06, RN-AG-07, RN-AU-01

> **Nota de Discovery**: el seguimiento sistemático de ausentes **no tiene
> evidencia en ninguno de los seis productos latinoamericanos ni de los siete
> argentinos**; los dos únicos casos son estadounidenses. Separar ausencia de
> cancelación es el primer paso para capturar información que hoy nadie captura.

---

## Épica 5: Consulta, notificación y auditoría

### US-017 — Consultar la agenda por profesional y por sillón
**Como** odontólogo o recepción
**Quiero** ver los turnos de un profesional o de un sillón en un rango de fechas
**Para** saber cómo está la ocupación

**Criterios de aceptación**:
- [ ] Filtro por profesional, por sillón, o por ambos, en un rango de fechas
- [ ] Los sobreturnos se distinguen visiblemente en el resultado
- [ ] Puedo filtrar por estado

**Reglas relacionadas**: RN-AG-06, RN-AG-07

### US-018 — Consultar huecos libres para una prestación
**Como** recepción
**Quiero** saber qué huecos libres hay para una prestación en un rango
**Para** ofrecerle opciones al paciente sin probar y fallar

**Criterios de aceptación**:
- [ ] El tamaño del hueco buscado es la duración de la prestación
- [ ] El resultado respeta `RN-AG-02`, `RN-AG-03` y `RN-AG-08`
- [ ] Un hueco que empieza exactamente cuando termina otro turno **es un hueco válido**
- [ ] Puedo restringir por profesional y/o sillón

**Reglas relacionadas**: RN-AG-09, RN-AG-04

### US-019 — Notificar al paciente mediante puerto con adaptador simulado
**Como** consultorio
**Quiero** que el sistema recuerde el turno al paciente
**Para** reducir el ausentismo — sin que la v1 dependa de un servicio externo

**Criterios de aceptación**:
- [ ] El dominio expone un puerto `NotificadorPaciente` y no conoce WhatsApp ni email
- [ ] El único adaptador de la v1 es simulado: registra el envío y no sale a la red
- [ ] **Ningún test requiere credenciales ni acceso a internet**
- [ ] Un fallo de notificación no invalida la operación de agenda
- [ ] La notificación no contiene datos clínicos

**Reglas relacionadas**: RN-NO-01…04

### US-020 — Auditar quién hizo qué sobre cada turno
**Como** odontólogo dueño
**Quiero** saber quién creó, modificó, reprogramó o canceló cada turno, y cuándo
**Para** tener trazabilidad sobre datos de salud

**Criterios de aceptación**:
- [ ] Cada operación sobre un turno genera un evento con usuario y timestamp
- [ ] Una operación sin usuario identificable se rechaza
- [ ] El registro es append-only
- [ ] Puedo consultar el historial completo de un turno

**Reglas relacionadas**: RN-AU-01, RN-AU-02, RN-AU-03

### US-021 — Probar la regla de disponibilidad con tests automatizados
**Como** equipo de desarrollo
**Quiero** una suite de tests sobre la regla de disponibilidad y las transiciones de estado
**Para** que la regla que define el producto sea verificable y no una afirmación

**Criterios de aceptación**:
- [ ] Los 9 casos borde de `RN-AG-04` tienen un test cada uno
- [ ] Hay tests para `RN-AG-02` y `RN-AG-03` por separado y combinados
- [ ] Hay tests para todas las transiciones válidas e inválidas de `RN-TU-02`
- [ ] Hay un test de que un turno `CANCELADO`/`ATENDIDO`/`AUSENTE` no se reprograma
- [ ] Hay un test de sobreturno con y sin autorización
- [ ] **Todos los datos de los tests son ficticios**
- [ ] La suite corre sin red, sin credenciales y sin contexto de Spring para el dominio puro

**Reglas relacionadas**: todas las de `RN-AG` y `RN-TU`, **RN-PA-04**

---

## Épica 6: Bloque diferenciador (solo si el MVP queda firme)

### US-022 — Reserva online del paciente por enlace, sin cuenta previa
Patrón tomado del **Dentally Portal** (el único flujo de reserva documentado paso
a paso del relevamiento, con verificación de identidad por móvil y código de un
solo uso) y de la **segmentación por intención de Open Dental Web Sched**
(paciente nuevo / paciente existente). Sujeta a las mismas reglas de
disponibilidad. **Un paciente nunca agenda un sobreturno.**
**Reglas**: RN-DI-03, RN-AG-02…04

### US-023 — Lista de espera sobre huecos liberados
Cuando se cancela un turno, el hueco queda registrado como ofrecible y la lista
de espera se consulta por prestación y franja. Es el vacío ausente en toda la
región (los únicos precedentes son los *encaixes* de Simples Dental y el Web
Sched ASAP de Open Dental) y **no requiere integración externa**.
**Reglas**: RN-DI-02

### US-024 — Reporte de ausentismo por profesional, sillón y prestación
Sobre los motivos estructurados de cancelación y ausencia. Produce información de
gestión que **ningún competidor del relevamiento captura**.
**Reglas**: RN-TU-05, RN-TU-06

### US-025 — Bloqueos de agenda como entidad del dominio
Vacaciones, ausencia del profesional, mantenimiento del sillón. **Participan de
la regla de disponibilidad igual que un turno.** Simples Dental es la única ficha
del relevamiento con bloqueo de horarios evidenciado.
**Reglas**: RN-DI-01

### US-026 — Exportación completa de los datos del consultorio
En formato abierto. Evidenciada en solo 2 de 19 competidores (Dentia y tab32).
Es también la respuesta concreta a la Ley 25.326 frente a competidores que solo
la declaran.
**Reglas**: RN-DI-04

### US-027 — Roles diferenciados con permisos efectivos
Odontólogo dueño, colega y recepción, con permisos reales sobre las operaciones
de agenda y **en particular sobre la autorización de sobreturnos**. El
relevamiento mostró que los productos mencionan roles casi siempre sin detallar
qué permite cada uno.
**Reglas**: matriz RBAC de `03_actores_y_roles.md`, RN-AG-06

### US-028 — Odontograma y ficha clínica extendida
**Solo si todo lo anterior está cerrado.** Es estándar de mercado (13 de 19
fichas) pero es el componente más costoso de construir bien, y hacerlo a medias
no aporta nada.

---

## Épica 7: Fuera de alcance (etapas posteriores)

WhatsApp Business API real · Mercado Pago · Facturación ARCA · Google Calendar ·
Obras sociales y prepagas con nomenclador · Caja y cuenta corriente del paciente
· Multi-sede · Reportes avanzados · **Cualquier funcionalidad de IA** · Portal
con historia clínica, periodontograma, consentimientos, firma digital e imágenes.

Ver `01_vision_y_objetivos.md` § "Fuera de alcance" para la justificación de cada
exclusión.
