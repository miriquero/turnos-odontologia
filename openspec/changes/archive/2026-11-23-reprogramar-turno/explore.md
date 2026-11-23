# Explore — reprogramar-turno

Relevamiento previo al Propose. Esta fase no implementa: investiga el problema y el estado del código para que el Propose no arranque a ciegas.

- **Fecha**: 2026-11-23
- **Change**: `C-02`, FASE 1 del roadmap
- **Rama**: `feat/reprogramar-turno`

---

## 1. Estado relevado

```
openspec list --json        ->  changes: []
openspec list --specs       ->  agenda/alta-de-turno (9), agenda/disponibilidad (11)
openspec status C-01        ->  archived en openspec/changes/archive/2026-11-19-crear-turno-sin-solapamientos
git status                  ->  arbol limpio; C-01 ya mergeado en master
```

C-01 está implementada: `Turno` inmutable, `Intervalo`, `ServicioDisponibilidad`, `EstadoTurno`, `AccionAuditoria.REPROGRAMADO`, `NotificadorPaciente` genérico por acción y `TurnoRepositorio.buscarVigentesSolapados(..., excluirTurnoId)`. La dependencia de C-02 está cubierta.

No hay change activo. El scaffold se creó con `openspec new change reprogramar-turno`.

## 2. El problema reducido

Reprogramar es un alta con dos diferencias: el turno ya existe y debe conservar su identidad, y el propio turno no cuenta como ocupante de la franja.

```
C-01 (congelado)
  Turno inmutable --reconstruir(mismo id)--> guardar (upsert)
  ServicioDisponibilidad.verificar(Solicitud(..., excluirTurnoId))
  NotificadorPaciente.notificar(turno, REPROGRAMADO)
  EventoAuditoria(turnoId, accion, usuario, timestamp)   SIN detalle

C-02 (archivos nuevos)
  ReprogramarTurno
    |-- valida usuario, estado, recursos, inicio futuro
    |-- SnapshotTurno anterior vs nuevo -> decide no-op
    |-- verifica disponibilidad excluyendose
    |-- guarda el turno con el MISMO id -> franja vieja libre
    |-- RegistroAuditoria -> REPROGRAMADO
    |-- RegistroDetalleReprogramacion -> DetalleReprogramacion   (NUEVO)
    `-- notifica con try/catch
```

## 3. El conflicto que condicionó el diseño

`RN-AU-03` exige horario anterior/nuevo y recurso anterior/nuevo en la auditoría. Pero `EventoAuditoria` tiene cuatro componentes y no admite detalle; `RegistroAuditoria.registrar` solo acepta ese tipo y C-08 construirá el historial leyendo ese registro.

Se evaluaron tres salidas:

| Opción | Costo |
|---|---|
| A: hotfix de C-01 agregando un componente opcional `detalle` | Rompe el congelamiento y necesita aprobación del líder como excepción `R-05` |
| **B: registro propio de detalle + puerto nuevo, declarado como contrato para C-08** | Dos fuentes que C-08 debe combinar; no toca C-01 |
| C: evento sin detalle | Contradice `RN-AU-03` y US-011 |

Se eligió **B**: evita modificar otras ramas, que fue la prioridad acordada.

## 4. Decisiones cerradas en esta fase

- **Detalle**: `DetalleReprogramacion` + `RegistroDetalleReprogramacion` en archivos nuevos, con doble en memoria. El evento `REPROGRAMADO` sigue en `RegistroAuditoria`.
- **C-08**: su ficha en `CHANGES.md` pasa a indicar que `ConsultarHistorialDeTurno` consulta ambas fuentes, con puntero al spec y al design de C-02.
- **No-op**: si inicio, profesional, sillón y prestación coinciden con los actuales, no hay guardado, evento, detalle ni notificación. Si cambia cualquiera de esos campos —aunque sea solo el sillón— se ejecuta el flujo completo.
- **`RN-TU-04`**: constante local en `ReprogramarTurno`, sin tocar `ReglaDeNegocioViolada` de C-01.
- **`RN-AG-08`**: se incluye aunque la ficha original de C-02 no lo listara, porque `RN-TU-03` exige "mismas reglas que un alta". Queda señalado para revisión del líder.
- **Duración**: se deriva de la prestación resultante; si la prestación no cambia, se conserva la duración del turno aunque el catálogo haya cambiado.

## 5. Riesgo principal detectado

Con dos registros append-only y sin transacciones en v1, si falla el segundo write el turno puede quedar movido con auditoría incompleta. No hay compensación posible sobre un log append-only. Mitigación: orden explícito, propagación del fallo, sin notificación posterior, y exigencia de escritura atómica cuando C-08/C-09 traigan JPA.

## 6. Cierre

El alcance quedó cerrado contra el roadmap, la KB y las specs vigentes. Habilita el Propose; los artefactos de planeamiento se escriben en este mismo change.
