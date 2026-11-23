# Proposal

## Why

Elegí este change porque la cancelación de turnos con política de anticipación mínima y motivo estructurado es una funcionalidad crítica para la gestión operativa real de un consultorio: evita franjas ociosas, permite a la recepción reasignar los huecos liberados y genera información clave sobre las razones de deserción de pacientes. C-03 completa las operaciones de ciclo de vida del turno de la FASE 1 y depende únicamente del change base C-01.

## What Changes

- Agregar el caso de uso `aplicacion.casouso.CancelarTurno`: pasa un turno de `AGENDADO` a `CANCELADO` de forma terminal (`RN-TU-02`).
- Exigir un motivo no vacío (`RN-TU-05`), normalizando espacios en blanco y rechazando solicitudes sin motivo.
- Validar la anticipación mínima configurable de `N` horas respecto a la hora de inicio del turno (`RN-AG-05`, `SU-03`, `PA-04`). Si el intento ocurre con menos de `N` horas, se rechaza.
- Soportar el caso de borde inclusivo (`D-02`, `RN-AG-04`): si la cancelación ocurre con **exactamente `N` horas** de anticipación, la operación debe ser aceptada.
- Utilizar un reloj inyectable (`java.time.Clock`) para desacoplar el cálculo temporal del reloj del sistema y posibilitar pruebas unitarias deterministas.
- Liberar la franja horaria inmediatamente: el turno cancelado deja de ocupar la agenda (`RN-AG-07`), permitiendo que una nueva alta sobre la misma franja sea aceptada sin conflicto.
- Registrar el evento `CANCELADO` en `RegistroAuditoria` con usuario y timestamp obligatorios (`RN-AU-01`).
- Notificar al paciente mediante `NotificadorPaciente` (`RN-NO-03`), asegurando que eventuales fallas del notificador no aborten la transacción de cancelación.
- Mantener inmutables y congelados los componentes del núcleo (`Intervalo`, `ServicioDisponibilidad`, `EstadoTurno` y puertos base).

## Capabilities

### New Capabilities

- `agenda/cancelacion`: cancelación de turnos agendados con validación de política de anticipación mínima configurable, motivo obligatorio, registro de auditoría, notificación al paciente y liberación inmediata de la franja en la agenda.

### Modified Capabilities

- Ninguna. `agenda/disponibilidad` ya especifica que `CANCELADO` no bloquea la franja (`RN-AG-07`), y `agenda/alta-de-turno` ya contempla que `CANCELADO` es un estado terminal.

## Impact

- **Código**: nuevo caso de uso `CancelarTurno` y su solicitud `SolicitudCancelarTurno` en `ar.edu.turnos.aplicacion.casouso`. Adaptación inmutable en `Turno` para registrar `motivoCancelacion` (o reconstrucción equivalente) respetando la inmutabilidad del dominio.
- **Auditoría**: reutiliza `AccionAuditoria.CANCELADO` ya definido en el modelo base.
- **Gobernanza**: MEDIO. Sin cambios en la capa visual ni persistencia real requerida en esta etapa.
