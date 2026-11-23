# Design: Cancelar Turno con Anticipación Mínima (C-03)

## Contexto y Arquitectura

El sistema implementa una arquitectura hexagonal (puertos y adaptadores) en Java 17 puro en la capa de dominio y aplicación, sin dependencias de Spring.
La operación de cancelación pertenece a la capa de aplicación y orquesta las validaciones de negocio, la persistencia en memoria y los efectos secundarios (auditoría y notificación).

## Decisiones Técnicas

### 1. Caso de Uso `CancelarTurno`
Ubicado en `ar.edu.turnos.aplicacion.casouso.CancelarTurno`.
Recibe por constructor:
- `TurnoRepositorio turnos`: para recuperar y persistir el turno.
- `RegistroAuditoria auditoria`: para registrar el evento `CANCELADO`.
- `NotificadorPaciente notificador`: para avisar al paciente (opcional/seguro ante fallos).
- `Clock reloj`: para consultar `ahora` de forma determinista y desacoplada del tiempo del sistema.
- `int anticipacionMinimaHoras`: política configurable de anticipación (valor por defecto 24 horas, acorde a `SU-03`).

### 2. Flujo de Ejecución del Caso de Uso
1. **Validación de Solicitud y Usuario (`RN-AU-01`)**:
   Si `solicitud.usuario()` es nulo o vacío, lanza `ReglaDeNegocioViolada(RN_AU_01, "No se puede identificar al usuario responsable")`.
2. **Validación del Motivo (`RN-TU-05`)**:
   Si `solicitud.motivo()` es nulo o está en blanco tras `trim()`, lanza `ReglaDeNegocioViolada(RN_TU_05, "El motivo de cancelacion es obligatorio")`.
3. **Búsqueda del Turno**:
   Se busca en `turnos.buscarPorId(solicitud.turnoId())`. Si no existe, lanza `ReglaDeNegocioViolada(RN_TU_02, "No existe el turno ...")`.
4. **Validación de Estado Previo (`RN-TU-02`)**:
   Verifica `turno.estado().puedeTransicionarA(EstadoTurno.CANCELADO)`. Si el turno está en estado terminal (`CANCELADO`, `ATENDIDO`, `AUSENTE`), lanza `ReglaDeNegocioViolada(RN_TU_02, "El turno ya esta en un estado terminal")`.
5. **Cálculo de Anticipación Mínima (`RN-AG-05`, `D-02`)**:
   Se calcula `ahora = LocalDateTime.now(reloj)`.
   El límite inferior permitido para cancelar es: `turno.intervalo().inicio().minusHours(anticipacionMinimaHoras)`.
   - Si `ahora.isAfter(limite)`, significa que faltan menos de `N` horas para el inicio $\rightarrow$ lanza `ReglaDeNegocioViolada(RN_AG_05, "No se puede cancelar con menos de N horas de anticipacion")`.
   - **Caso de borde inclusivo**: Si `ahora.isEqual(limite)`, la cancelación **se acepta** (límite inclusivo, decisión `D-02`).
6. **Transición del Turno**:
   Se produce una nueva instancia de `Turno` con estado `CANCELADO` y motivo registrado.
   Se guarda en `turnos.guardar(turnoCancelado)`.
7. **Efectos Secundarios (Auditoría y Notificación)**:
   - Se registra en `auditoria` el evento: `EventoAuditoria(turno.id(), AccionAuditoria.CANCELADO, solicitud.usuario(), ahora)`.
   - Se notifica mediante `notificador.notificar(turnoCancelado)` envolviendo en `try-catch` para que cualquier excepción del canal de notificación no interrumpa la cancelación (`RN-NO-03`).
8. **Retorno**:
   Devuelve el `Turno` cancelado resultante.

## Componentes y Archivos

- **Nuevos**:
  - `ar.edu.turnos.aplicacion.casouso.CancelarTurno`
  - `ar.edu.turnos.aplicacion.casouso.SolicitudCancelarTurno`
  - `ar.edu.turnos.aplicacion.casouso.CancelarTurnoTest`
- **Archivos Congelados que NO se tocan**:
  - `Intervalo.java`
  - `ServicioDisponibilidad.java`
  - `EstadoTurno.java`
  - Puertos (`TurnoRepositorio`, `RegistroAuditoria`, `NotificadorPaciente`, etc.)
