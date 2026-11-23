# Design

## Context

C-01 está implementado y archived. Su núcleo quedó congelado por `R-05`: `Turno` es inmutable, `ServicioDisponibilidad` ya acepta `excluirTurnoId`, `AccionAuditoria` ya incluye `REPROGRAMADO`, `TurnoRepositorio.guardar` reemplaza por identificador y `NotificadorPaciente` es genérico por acción. El conflicto está en `EventoAuditoria`: es un `record` de cuatro componentes sin campo de detalle, mientras `RN-AU-03` exige horario anterior/nuevo y recurso anterior/nuevo. Ver `proposal.md` para la motivación y el spec delta para el comportamiento observable.

Restricciones que condicionan el diseño:

- No se edita ningún archivo de C-01, ni `ReglaDeNegocioViolada`.
- No hay persistencia real ni capa HTTP; los puertos se implementan solo en `src/test`.
- La regla de oro del proyecto: `dominio` no importa Spring, JPA ni HTTP.
- TDD estricto: cada escenario del spec nace de un test que falla.

## Goals / Non-Goals

**Goals:**

- Reprogramar conservando el `id` del turno y liberando la franja anterior en el mismo acto.
- Dejar el detalle de `RN-AU-03` consultable sin tocar el evento de auditoría congelado.
- Dejar declarado el contrato que C-08 deberá consumir en su `ConsultarHistorialDeTurno`.
- Reutilizar la regla de disponibilidad existente, sin reimplementar ningún predicado de solapamiento.

**Non-Goals:**

- Resolver concurrencia entre dos reprogramaciones o entre alta y reprogramación: es `R-06`/`C-09`.
- Persistir el detalle en JPA ni exponerlo por REST: son `C-09` y `C-10`.
- Modificar `EventoAuditoria`, `RegistroAuditoria`, `Intervalo`, `ServicioDisponibilidad`, `EstadoTurno`, `AccionAuditoria` ni los puertos de C-01.
- Cambiar el paciente del turno ni crear un turno nuevo.

## Decisions

### D-C02-1 — El turno se reemplaza, no se muta

`Turno` es inmutable y no expone setter. La reprogramación construye un turno nuevo con `Turno.reconstruir(...)`, reutilizando el `id`, el `pacienteId` y la marca `esSobreturno`, forzando `EstadoTurno.AGENDADO` y aplicando los nuevos valores de inicio, profesional, sillón y prestación. `TurnoRepositorio.guardar` hace `put` por identificador, así que la franja anterior desaparece y la nueva queda vigente en un solo paso.

**Alternativa descartada**: agregar un método `reprogramar` a `Turno`. Requiere editar un archivo de C-01, prohibido por `R-05`.

### D-C02-2 — Detalle en un registro propio (opción B)

Se crean `SnapshotTurno`, `DetalleReprogramacion` y el puerto `RegistroDetalleReprogramacion` en archivos nuevos. El evento `REPROGRAMADO` sigue viviendo en `RegistroAuditoria`; el detalle vive en el puerto nuevo. Ambos son append-only.

**Alternativa descartada (A)**: agregar un componente opcional `detalle` a `EventoAuditoria` con un constructor de cuatro argumentos para no romper `AgendarTurno`. Es más limpio para el historial, pero obliga a un hotfix de C-01, a actualizar sus specs y a pedir autorización explícita al líder como excepción de `R-05`. La opción B evita tocar otras ramas, que es la prioridad acordada.

**Alternativa descartada (C)**: reducir la auditoría al evento sin detalle. Contradice `RN-AU-03` y el criterio de US-011.

### D-C02-3 — Un solo snapshot para detalle, duración y no-op

`SnapshotTurno` contiene profesional, sillón, prestación, duración en minutos, inicio y fin. El mismo objeto cumple tres funciones:

1. `anterior` y `nuevo` son los campos del detalle de `RN-AU-03`.
2. De `nuevo` se toma el intervalo que se pasa a `ServicioDisponibilidad`.
3. `anterior.equals(nuevo)` decide si la solicitud es no-op.

Así no hay tres implementaciones distintas de "qué cambió".

### D-C02-4 — Duración: se conserva salvo cambio de prestación

Si el identificador de prestación solicitado es distinto del actual, se busca esa prestación y se usa su `duracionMinutos`. Si es el mismo, se conserva `turno.duracionMinutos()`, aunque el catálogo haya cambiado: `RN-AG-01` Protege la duración vigente al momento de agendar. El nuevo fin siempre se deriva; la solicitud de reprogramación no acepta `fin` ni duración.

### D-C02-5 — No-op sin efectos

Si `anterior.equals(nuevo)`, el caso de uso devuelve el turno actual y no guarda, no registra evento, no registra detalle y no notifica. Si difiere cualquiera de los campos del snapshot —incluidos solo sillón, solo profesional o solo prestación con igual duración— se ejecuta el flujo completo. El horario no es el único disparador de auditoría.

### D-C02-6 — `RN-TU-04` como constante local

`ReglaDeNegocioViolada` acepta cualquier string como código, así que no hace falta tocarla. Se declara `public static final String RN_TU_04 = "RN-TU-04"` en `ReprogramarTurno` y los tests la referencian desde ahí. Cero cambios en `dominio.excepcion` ni en ramas ajenas.

### D-C02-7 — Orden de escritura y límite de atomicidad

Orden: validar todo → `guardar` el turno → registrar detalle → registrar evento `REPROGRAMADO` → notificar. Si el registro del detalle o del evento falla, se propaga el error y no se notifica. No existe compensación posible sobre un log append-only, por lo que en v1 puede quedar el turno movido con auditoría incompleta. Con los dobles en memoria el riesgo no es observable; `C-08`/`C-09` deberán escribir ambos registros en una única transacción cuando llegue JPA. Queda explícito en `Risks` y en el proposal.

### D-C02-8 — Correlación evento/detalle

No hay `eventoId` en `EventoAuditoria`. La tupla `(turnoId, usuario, timestamp)` identifica la operación; el detalle lleva esos tres datos para que `ConsultarHistorialDeTurno` pueda asociarlos. Con reloj real la colisión es prácticamente imposible; los tests con `Clock.fixed` usan timestamps distintos cuando reproducen dos reprogramaciones del mismo turno.

### D-C02-9 — Contrato para C-08

El spec de C-02 describe la exposición del detalle como comportamiento, sin nombrar clases. El nombre del puerto y la obligación de consumo viven en este `design.md`, en `proposal.md → Impact` y en la ficha C-08 de `CHANGES.md`: `ConsultarHistorialDeTurno` SHALL combinar `RegistroAuditoria.listarPorTurno` con `RegistroDetalleReprogramacion.listarPorTurno`. De este modo el dueño de C-08 lo ve en el índice que lee antes de arrancar.

### D-C02-10 — Reloj inyectado

`RN-TU-03` incorpora `RN-AG-08`, que exige inicio estrictamente futuro. `ReprogramarTurno` recibe `java.time.Clock` por constructor, igual que `AgendarTurno`, y los tests usan `Clock.fixed`. Es JDK puro: no viola la regla de oro.

### D-C02-11 — Un test por escenario con dobles en memoria

El doble `RegistroDetalleReprogramacionEnMemoria` guarda por turno en una lista append-only. Cada escenario del spec tiene su test JUnit 5 sin Spring; se reutilizan los dobles de C-01 (`TurnoRepositorioEnMemoria`, `NotificadorEspia`, `RegistroAuditoriaEnMemoria`, `DatosFicticios`). El auxiliar `Escenario` de C-01 se reutiliza para no duplicar armado de fixtures.

## Risks / Trade-offs

- **[Dos fuentes de auditoría]** → El historial de C-08 debe consultar ambas; si se olvida, `RN-AU-03` queda incumplida sin que falle ningún test de C-02. Mitigación: contrato escrito en `CHANGES.md` y escenario de consulta conjunta en el spec.
- **[Escrituras no atómicas]** → Turno movido con auditoría incompleta si falla el segundo registro. Mitigación: orden explícito, propagación del fallo, sin notificación posterior; transacción única en C-08/C-09.
- **[Colisión de timestamp con `Clock.fixed`]** → Dos reprogramaciones del mismo turno podrían compartir tupla de correlación. Mitigación: tests con timestamps distintos y registro append-only que conserva ambos detalles.
- **[Validación de recursos no listada en el roadmap]** → La ficha C-02 de `CHANGES.md` no menciona `RN-AG-08`, pero `RN-TU-03` y la KB sí exigen "mismas reglas que un alta". Mitigación: se incluye en el spec como comportamiento observable y se señala esta diferencia al revisor.
- **[Cambio de `prestacionId` con igual duración]** → Podría interpretarse erróneamente como no-op. Mitigación: el snapshot incluye `prestacionId`, así que el cambio dispara auditoría y detalle.
- **[Concurrencia]** → Dos reprogramaciones simultáneas sobre la misma franja pueden pasar la validación. Mitigación: fuera de alcance, resuelto por `C-09` junto con `R-06`.

## Migration Plan

Sin migración: no hay esquema persistente ni API publicada. El único cambio de coordinación es la ficha C-08 de `CHANGES.md`, que se actualiza en este mismo change.

## UI Mockups

- Decision: no (no UI). El change es de dominio y no introduce pantallas, componentes ni estilos.

## Open Questions

- C-08 deberá decidir si muestra el detalle como entrada propia de la timeline o lo fusiona con el evento `REPROGRAMADO`. No afecta a C-02: el puerto ya expone `listarPorTurno`.
