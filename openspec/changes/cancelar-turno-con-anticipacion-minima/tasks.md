# Tasks: Cancelar Turno con Anticipación Mínima (C-03)

- [x] **1. Especificación y Diseño**
  - [x] Redactar `proposal.md`, `design.md` y `specs/agenda/cancelacion/spec.md`.
  - [x] Revisión crítica del spec contra la checklist de la guía (ajuste de alcance inventado / validación de reglas).
  - [ ] Validar el change con OpenSpec y commitear la especificación en la rama `feat/cancelar-turno-con-anticipacion-minima`.

- [ ] **2. Pruebas Unitarias (TDD - Red Phase)**
  - [ ] Crear `CancelarTurnoTest.java` en `ar.edu.turnos.aplicacion.casouso`.
  - [ ] Test 1: Cancelación exitosa con motivo y más de N horas (verifica estado `CANCELADO`, motivo, auditoría y notificación).
  - [ ] Test 2: Rechazar cancelación sin motivo o con motivo en blanco (`RN-TU-05`).
  - [ ] Test 3: Rechazar cancelación de turno inexistente o en estado terminal (`RN-TU-02`).
  - [ ] Test 4: Rechazar cancelación fuera de plazo con menos de N horas (`RN-AG-05`).
  - [ ] Test 5: Aceptar cancelación en el caso de borde con exactamente N horas de anticipación (`D-02`).
  - [ ] Test 6: Rechazar cancelación sin usuario responsable (`RN-AU-01`).
  - [ ] Test 7: Tolerancia a fallos si el notificador lanza excepción (`RN-NO-03`).

- [ ] **3. Implementación (TDD - Green Phase)**
  - [ ] Crear el DTO `SolicitudCancelarTurno.java` (turnoId, motivo, usuario).
  - [ ] Agregar método de cancelación inmutable en `Turno.java` (o reconstrucción con estado `CANCELADO` y motivo).
  - [ ] Implementar la clase de caso de uso `CancelarTurno.java` cumpliendo las validaciones del spec.
  - [ ] Correr tests unitarios de `CancelarTurnoTest` hasta que pasen todos en verde.

- [ ] **4. Verificación de Integración de Agenda (RN-AG-07)**
  - [ ] Agregar test de integración verificando que tras cancelar un turno, un nuevo alta sobre esa misma franja horaria mediante `AgendarTurno` se acepta.

- [ ] **5. Verificación Transversal y Cierre**
  - [ ] Ejecutar `./gradlew.bat test` asegurando 100% de la suite en verde.
  - [ ] Archivar el change y sincronizar specs con el proyecto.
  - [ ] Realizar commit final y push a la rama `feat/cancelar-turno-con-anticipacion-minima`.
  - [ ] Abrir Pull Request hacia `master`.
