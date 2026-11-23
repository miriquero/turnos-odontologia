# Design

> Decision: no (no UI) — este change es de dominio y no toca pantallas,
> componentes ni estilos.

## Context

C-01 dejó el núcleo hexagonal con `Turno` inmutable, `EstadoTurno` con la
información de ocupación de agenda (`ocupaAgenda`) y `AccionAuditoria` con
`MARCADO_AUSENTE` y `MARCADO_ATENDIDO` ya definidos. `AgendarTurno` inyecta
`Clock` y valida usuario antes de persistir. No hay persistencia ni capa HTTP:
los tests usan dobles en memoria. Ver `proposal.md` para la motivación y el
delta de `agenda/cierre-de-turno` para el contrato de comportamiento.

Restricciones que condicionan el diseño: el dominio no importa Spring/JPA/HTTP;
cinco personas trabajan en paralelo sobre archivos de C-01, así que conviene no
romper firmas existentes; y el alcance de `CHANGES.md` no incluye notificaciones.

## Goals / Non-Goals

**Goals:**

- Simplificar el cierre de un turno sin exponer una transición inválida.
- Conservar el motivo de ausencia como dato del turno, sin categorizarlo.
- Reutilizar la regla de disponibilidad existente sin tocarla: comprobar que un
  alta posterior respeta el estado resultante.
- Mantener la trazabilidad append-only con usuario obligatorio.

**Non-Goals:**

- Notificar al paciente (fuera del scope de C-05).
- Definir un enum o catálogo de motivos (reservado para C-13).
- Aplicar la anticipación mínima de `RN-AG-05` a la ausencia.
- Modificar `AgendarTurno`, `ServicioDisponibilidad`, los puertos ni la API REST.

## Decisions

### D-C05-1 — `Turno` permanece inmutable y la transición devuelve una nueva instancia

`Turno` expone `marcarAusente(String motivo)` y `marcarAtendido()`, que
delegan en una operación privada `conEstado(...)` y devuelven un `Turno` nuevo
con el mismo identificador, intervalo y recursos. La guarda
`EstadoTurno.puedeTransicionarA` se aplica dentro del modelo: el caso de uso
valida, pero el modelo no puede dejar un terminal inválido.

Alternativa descartada: un setter de estado sobre el objeto existente. Rompe la
inmutabilidad de C-01, facilita estados inválidos y hace imposible razonar sobre
el evento de auditoría como hecho consumado.

`Turno.agendar` no cambia. `Turno.reconstruir` conserva su firma actual y se
agrega una sobrecarga con `motivoAusencia`: hay cinco llamadas en tests de
C-01 (y probablemente más en ramas paralelas), y cambiar la firma sería un
conflicto innecesario. El repositorio en memoria ya reemplaza por `id` en
`guardar`, así que persiste el nuevo objeto sin cambios.

### D-C05-2 — `motivoAusencia` es un `String` opcional en el turno

La normalización vive en `Turno.marcarAusente`: aplica `trim()` al motivo
recibido y guarda `null` cuando el resultado está vacío. Así ningún llamador
puede dejar un turno `AUSENTE` con un motivo en blanco, y el caso de uso solo
transmite lo que recibe. No se validan categorías: la lectura de "estructurado"
de US-016 se interpreta como campo dedicado del turno, no como texto incrustado
en otro dato; la taxonomía y el reporte llegan en C-13.

Alternativa descartada: normalizar en el caso de uso. Dejaría al modelo en
condición de aceptar motivos en blanco desde otro llamador. También se descarta
un enum `MotivoAusencia` con valores inventados: la KB no define esas categorías
y este change no puede inventarlas sin volver a Propose.

### D-C05-3 — Reloj inyectado y límite inclusivo para la ausencia

Ambos casos de uso reciben `java.time.Clock`. Para ausencia se calcula un único
`ahora = LocalDateTime.now(reloj)` y se exige `!ahora.isBefore(inicio)`
(equivalente a `inicio <= ahora`); el mismo valor se usa como marca de tiempo
del evento de auditoría. Para atención no hay comparación temporal: un turno
futuro en `AGENDADO` se puede marcar `ATENDIDO`.

Alternativa descartada: `ahora.isAfter(inicio)` estricto. La decisión acordada
es que en el instante exacto de inicio ya se puede registrar la ausencia, por
coherencia con el criterio inclusivo de `RN-AG-05` en `D-02`.

### D-C05-4 — Errores de negocio con códigos explícitos

Se agrega la constante `RN_TU_02` a `ReglaDeNegocioViolada` y se reutiliza
`RN_AU_01`. El orden de validación es: usuario responsable (`RN-AU-01`),
existencia del turno por `buscarPorId` (`RN-TU-02`), estado terminal
(`RN-TU-02`) y, solo para ausencia, límite de reloj (`RN-TU-02`). Cualquier
rechazo ocurre antes de persistir y antes de auditar, de modo que un `turnoId`
inexistente no deja eventos.

Un `turnoId` inexistente se informa con `RN-TU-02` porque no hay un turno
`AGENDADO` sobre el que aplicar la transición; así se reutiliza una regla ya
definida en lugar de inventar un código nuevo. `RN-TU-06` y `RN-AG-07` no se
lanzan: son reglas que este change respeta, no violaciones que produce.

Alternativa descartada: pasar el literal `"RN-TU-02"`. Rompería la convención
de códigos como constantes y dificultaría las aserciones de los tests.

### D-C05-5 — Un capability nuevo en lugar de modificar los existentes

`agenda/cierre-de-turno` agrupa el comportamiento nuevo y duradero de cerrar un
turno. No se modifica `agenda/disponibilidad`: su scenario de `AUSENTE` ya
cubre `RN-AG-07` con un turno construido en ese estado. Tampoco
`agenda/alta-de-turno`: su requerimiento de estados terminales sigue vigente.

### D-C05-6 — La verificación de franja se hace desde el caso de uso de alta

Los tests de borde construyen la secuencia real `RegistrarAusencia` (o
`MarcarTurnoAtendido`) y después invocan `AgendarTurno` con el mismo profesional
y sillón. Así se prueba la integración sin que C-05 modifique el caso de uso de
C-01 ni duplique la regla de solapamiento.

## Risks / Trade-offs

- **[Riesgo] Conflicto con ramas paralelas al tocar `Turno` y
  `ReglaDeNegocioViolada`** → Mitigación: cambios aditivos (métodos nuevos,
  sobrecarga y constante nueva), sin alterar firmas ni eliminar miembros.
- **[Riesgo] Interpretar "motivo estructurado" como catálogo cerrado** →
  Mitigación: está resuelto en D-C05-2 y reflejado en el spec; C-13 podrá
  agregar la categorización sin cambiar el tipo del campo.
- **[Riesgo] El límite inclusivo se confunda con el criterio estricto del alta**
  → Mitigación: dos scenarios explícitos en el spec (instante exacto futuro y
  exacto actual), con reloj fijo en los tests.
- **[Trade-off] `Turno` gana un campo opcional sin catálogo** → simple y
  compatible; el reporte de ausentismo tendrá que normalizar los valores cuando
  llegue C-13.
- **[Trade-off] No notificar** → coherente con el scope aprobado; si se quiere
  avisar al paciente, debe volverse a Propose.

## Migration Plan

No hay datos persistentes ni esquema que migrar. La secuencia de apply es:
agregar el campo y los métodos de transición en `Turno`, la constante de regla,
los dos casos de uso y sus tests; ajustar las fabricas de tests que quieran
construir un turno con motivo usando la sobrecarga nueva. Rollback: revertir
los archivos del change; las firmas existentes siguen funcionando.

## Open Questions

Ninguna. Las decisiones que podían cambiar el spec (motivo y su normalización,
reloj, notificaciones y código para turno inexistente) quedaron cerradas antes
de empezar la implementación.
