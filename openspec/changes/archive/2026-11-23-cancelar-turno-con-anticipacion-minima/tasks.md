# Tasks: Cancelar Turno con Anticipación Mínima (C-03)

- [x] **1. Especificación y Diseño**
  - [x] Redactar `proposal.md`, `design.md` y `specs/agenda/cancelacion/spec.md`.
  - [x] Revisión crítica del spec contra la checklist de la guía (ajuste de alcance inventado / validación de reglas).
  - [x] Validar el change con OpenSpec y commitear la especificación en la rama `feat/cancelar-turno-con-anticipacion-minima`.

- [x] **2. Pruebas Unitarias (TDD - Red Phase)**
  - [x] Crear `CancelarTurnoTest.java` en `ar.edu.turnos.aplicacion.casouso`.
  - [x] Test 1: Cancelación exitosa con motivo y más de N horas (verifica estado `CANCELADO`, motivo, auditoría y notificación).
  - [x] Test 2: Rechazar cancelación sin motivo o con motivo en blanco (`RN-TU-05`).
  - [x] Test 3: Rechazar cancelación de turno inexistente o en estado terminal (`RN-TU-02`).
  - [x] Test 4: Rechazar cancelación fuera de plazo con menos de N horas (`RN-AG-05`).
  - [x] Test 5: Aceptar cancelación en el caso de borde con exactamente N horas de anticipación (`D-02`).
  - [x] Test 6: Rechazar cancelación sin usuario responsable (`RN-AU-01`).
  - [x] Test 7: Tolerancia a fallos si el notificador lanza excepción (`RN-NO-03`).

- [x] **3. Implementación (TDD - Green Phase)**
  - [x] Crear el DTO `SolicitudCancelarTurno.java` (turnoId, motivo, usuario).
  - [x] Agregar método de cancelación inmutable en `Turno.java` (o reconstrucción con estado `CANCELADO` y motivo).
  - [x] Implementar la clase de caso de uso `CancelarTurno.java` cumpliendo las validaciones del spec.
  - [x] Correr tests unitarios de `CancelarTurnoTest` hasta que pasen todos en verde.

- [x] **4. Verificación de Integración de Agenda (RN-AG-07)**
  - [x] Agregar test de integración verificando que tras cancelar un turno, un nuevo alta sobre esa misma franja horaria mediante `AgendarTurno` se acepta.

- [x] **5. Verificación Transversal y Cierre**
  - [x] Ejecutar `./gradlew.bat test` asegurando 100% de la suite en verde.
  - [x] Archivar el change y sincronizar specs con el proyecto.
  - [x] Realizar commit final y push a la rama `feat/cancelar-turno-con-anticipacion-minima`.
  - [x] Abrir Pull Request hacia `master`.
