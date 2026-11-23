# Proposal

## Why

La agenda ya permite dar de alta turnos sin solapamientos (C-01), pero todavía no permite mover un turno existente. Sin reprogramación, cualquier cambio de horario, profesional o sillón obliga a cancelar y volver a agendar, con pérdida del historial y de la continuidad del paciente. C-02 es el primer change de FASE 1 y solo depende de C-01, ya archived.

## What Changes

- Agregar el caso de uso `ReprogramarTurno`, que cambia `inicio` y, de forma opcional, profesional, sillón y prestación; recalcula `fin` a partir de la prestación resultante (`RN-AG-01`).
- Revalidar la disponibilidad con el mismo `ServicioDisponibilidad` de C-01, pasando `excluirTurnoId = turno.id` para que el turno no se solape consigo mismo (`RN-TU-03`). La franja anterior queda libre en el mismo acto al guardar el turno con el mismo identificador.
- Restringir la operación a turnos `AGENDADO`; rechazar `CANCELADO`, `ATENDIDO` y `AUSENTE` con `RN-TU-04` (`US-012`).
- Definir la no-op: si el nuevo inicio, profesional, sillón y prestación coinciden con los actuales, la operación no guarda, no registra auditoría, no registra detalle y no notifica. Si cambia cualquiera de esos campos —aunque sea solo el sillón o el profesional— se ejecuta el flujo completo.
- Registrar siempre el evento `REPROGRAMADO` en `RegistroAuditoria`, con usuario y timestamp obligatorios (`RN-AU-01`), y notificar al paciente mediante `NotificadorPaciente`, con tolerancia a fallos del aviso (`RN-NO-03`).
- Exponer el detalle exigido por `RN-AU-03` —horario anterior y nuevo, y recurso anterior y nuevo— en un registro propio: el tipo `DetalleReprogramacion`, el puerto `RegistroDetalleReprogramacion` y su doble en memoria. Esto evita modificar `EventoAuditoria`, que quedó congelado en C-01.
- Declarar el puerto nuevo como contrato para C-08: `ConsultarHistorialDeTurno` deberá combinar `RegistroAuditoria.listarPorTurno` con `RegistroDetalleReprogramacion.listarPorTurno`.
- Declarar `RN-TU-04` como constante local de C-02, sin tocar la clase `ReglaDeNegocioViolada` de C-01 ni ningún archivo de otras ramas.

## Capabilities

### New Capabilities

- `agenda/reprogramacion`: cambio de horario y recursos de un turno agendado, con revalidación de disponibilidad, no-op, auditoría de reprogramación y detalle de los valores anteriores y nuevos.

### Modified Capabilities

- Ninguna. `agenda/alta-de-turno` y `agenda/disponibilidad` se consumen sin cambios: la exclusión del propio turno ya está especificada en esta última.

## Impact

- **Código nuevo, sin archivos modificados de C-01**: `ReprogramarTurno`, su solicitud, `SnapshotTurno`, `DetalleReprogramacion`, `RegistroDetalleReprogramacion` y el doble en memoria del puerto. Se reutilizan `Turno.reconstruir`, `TurnoRepositorio.guardar`, `ServicioDisponibilidad`, `AccionAuditoria.REPROGRAMADO` y `NotificadorPaciente`.
- **Sin persistencia real ni capa HTTP**: el puerto nuevo se implementa solo en `src/test`, igual que los otros puertos en C-01. La atomicidad entre ambos registros queda como límite conocido de v1 y se resuelve con JPA en C-08/C-09.
- **Coordinación**: la ficha C-08 de `CHANGES.md` incorpora la lectura del nuevo puerto; el líder debe revisar ese punto porque C-08 es de otro integrante.
- **Gobernanza**: MEDIO. No hay cambios visuales, por lo que no aplica gate de design system ni de mockups.
