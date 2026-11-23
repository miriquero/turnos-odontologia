# Proposal

## Why

Hoy el sistema solo sabe crear turnos: el modelo declara los estados terminales
`ATENDIDO` y `AUSENTE`, pero no existe ninguna operación para alcanzarlos. Sin
ese cierre del ciclo, la agenda no puede registrar que la hora de sillón se usó
efectivamente ni distinguir la ausencia del paciente de una cancelación, que es
justamente la información de gestión que diferencia al producto (US-015,
US-016, RN-TU-06).

## What Changes

- Se agrega `aplicacion.casouso.RegistrarAusencia`: pasa un turno de `AGENDADO` a
  `AUSENTE`, con `motivoAusencia` opcional de tipo `String`. Solo se permite
  cuando `ahora >= inicio`; un turno futuro se rechaza con `RN-TU-02`. No se le
  aplica la anticipación mínima de cancelación (`RN-TU-06`) y no dispara
  notificación al paciente.
- Se agrega `aplicacion.casouso.MarcarTurnoAtendido`: pasa un turno de
  `AGENDADO` a `ATENDIDO`, sin restricción horaria. Sigue ocupando su franja
  histórica (`RN-AG-07`).
- `Turno` incorpora el campo propio `motivoAusencia` y una operación de
  transición que preserva su inmutabilidad. La categorización de motivos queda
  fuera de alcance y se reserva para C-13.
- Ambos casos de uso validan usuario responsable (`RN-AU-01`), rechazan toda
  transición desde un estado terminal (`RN-TU-02`) y registran auditoría con
  `MARCADO_AUSENTE` o `MARCADO_ATENDIDO`.
- La liberación de la franja tras una ausencia y la ocupación tras un turno
  atendido se verifican contra `AgendarTurno` de C-01, sin modificarlo.

## Capabilities

### New Capabilities

- `agenda/cierre-de-turno`: cierre del ciclo de vida del turno mediante las
  transiciones terminales a `ATENDIDO` y `AUSENTE`, sus validaciones de estado y
  de reloj, el registro del motivo de ausencia y la trazabilidad en auditoría.

### Modified Capabilities

Ninguna. `agenda/disponibilidad` ya especifica que `AUSENTE` no bloquea y que
`ATENDIDO` sí ocupa la franja (RN-AG-07), y `agenda/alta-de-turno` ya especifica
qué estados son terminales. Este change consume esas reglas sin cambiarlas.

## Impact

- Código: nuevos casos de uso en `aplicacion.casouso`; `Turno`,
  `ReglaDeNegocioViolada` y las fabricas de tests se ajustan para soportar la
  transición y el motivo. `AgendarTurno` y `ServicioDisponibilidad` no se
  modifican.
- Auditoría: reutiliza `AccionAuditoria.MARCADO_AUSENTE` y
  `MARCADO_ATENDIDO`, ya existentes; `RegistroAuditoria` no cambia.
- Dependencias: solo C-01, ya mergeado. No agrega puertos ni dependencias
  externas; el reloj se inyecta como `Clock`, igual que en `AgendarTurno`.
- Alcance explícitamente excluido: notificaciones al paciente, catálogo o enum
  de motivos, anticipación mínima, API REST y persistencia.
