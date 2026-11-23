# Tasks

## 1. Tipos del detalle y doble en memoria

- [x] 1.1 Crear `SnapshotTurno` con profesional, sillón, prestación, duración, inicio y fin, e igualdad por valor; verificar con un test de igualdad y desigualdad que `anterior.equals(nuevo)` detecta cualquier campo distinto
- [x] 1.2 Crear `DetalleReprogramacion` con `turnoId`, `anterior`, `nuevo`, `usuario` y `timestamp`; verificar con un test que rechaza usuario vacío y conserva ambos snapshots
- [x] 1.3 Crear el puerto `RegistroDetalleReprogramacion` y su doble `RegistroDetalleReprogramacionEnMemoria` append-only; verificar con un test que `registrar` + `listarPorTurno` devuelve los detalles en orden y conserva el primero tras un segundo registro

## 2. Entrada, estado y validaciones

- [x] 2.1 Crear `SolicitudReprogramarTurno` con identificador de turno, nuevo inicio, profesional/sillón/prestación opcionales y usuario; verificar por inspección de la API que no acepta `fin` ni duración
- [x] 2.2 Escribir el test y luego implementar el rechazo con `ReprogramarTurno.RN_TU_04` para los estados `CANCELADO`, `ATENDIDO` y `AUSENTE`; verificar los tres escenarios del spec en verde
- [x] 2.3 Escribir el test y luego implementar el rechazo `RN-AU-01` cuando el usuario está ausente o en blanco; verificar que el turno no se modifica ni se registra nada
- [x] 2.4 Escribir los tests y luego implementar la validación `RN-AG-08` de recursos resultantes existentes y activos y de inicio estrictamente futuro con `Clock` inyectado; verificar los cinco escenarios del spec

## 3. Revalidación y reemplazo del turno

- [x] 3.1 Escribir los tests y luego implementar la verificación de disponibilidad con `excluirTurnoId = turno.id`; verificar `RN-AG-02`, `RN-AG-03`, contigüidad aceptada y mismo horario con otro sillón libre
- [x] 3.2 Escribir los tests y luego implementar el reemplazo con `Turno.reconstruir` y `guardar`, conservando `id`, paciente y `esSobreturno`; verificar duración derivada al cambiar de prestación, duración conservada al no cambiarla aunque el catálogo cambie, y franja anterior disponible

## 4. No-op, auditoría, detalle y notificación

- [x] 4.1 Escribir el test y luego implementar la no-op cuando los cuatro campos del snapshot coinciden; verificar que no hay `guardar`, evento, detalle ni notificación
- [x] 4.2 Escribir los tests y luego implementar el flujo completo cuando cambia solo el sillón, solo el profesional o solo la prestación con igual duración; verificar que en los tres casos se registran evento y detalle y se notifica
- [x] 4.3 Escribir los tests y luego implementar el evento `REPROGRAMADO` con usuario y timestamp y el `DetalleReprogramacion` con snapshots anterior/nuevo; verificar la consulta conjunta por identificador de turno y el carácter append-only del detalle
- [x] 4.4 Escribir el test y luego implementar la notificación con `AccionAuditoria.REPROGRAMADO` dentro de `try/catch`; verificar con `NotificadorEspia` que un fallo del aviso no invalida la reprogramación

## 5. Verificación transversal

- [x] 5.1 Verificar que todas las fixtures y datos de prueba del change son ficticios (`RN-PA-04`) y que `DatosFicticiosTest` sigue en verde
- [x] 5.2 Correr `./gradlew test` y verificar la suite completa en verde, sin Spring, sin base de datos y sin red; reportar el total de tests
- [x] 5.3 Verificar la regla de oro sobre los archivos nuevos: `dominio` no importa Spring, JPA ni HTTP, y `ReglaDeOroTest` pasa
- [x] 5.4 Verificar que la ficha C-08 de `CHANGES.md` incluye la lectura de `RegistroDetalleReprogramacion` y el puntero al spec de este change

## Workflow follow-up

- Revisar el spec completo con el líder antes de habilitar Apply, prestando atención a la inclusión de `RN-AG-08` que la ficha original de C-02 no listaba.
- Avisar al dueño de C-08 que su ficha ya contiene el contrato de consumo del detalle.
- Ejecutar `/openspec-apply-change reprogramar-turno` para implementar (no incluido en este planeamiento).
- Verificar y archivar el change con `openspec archive`, sincronizando `agenda/reprogramacion` a los specs vigentes.
