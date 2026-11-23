# Tasks

## 1. Soporte del modelo y de las reglas

- [x] 1.1 Agregar la constante `RN_TU_02` a `ReglaDeNegocioViolada` y verificar en `ReglaDeNegocioVioladaTest` que el código se expone igual que los existentes
- [x] 1.2 Escribir en `TurnoTest` los tests de transición inmutable `AGENDADO -> AUSENTE` y `AGENDADO -> ATENDIDO`, de rechazo desde estados terminales, de conservación del `motivoAusencia`, de normalización de motivo vacío, solo espacios y con espacios en los extremos, y de la sobrecarga de `reconstruir`; verificar que fallan en rojo antes de implementar
- [x] 1.3 Implementar `motivoAusencia`, `marcarAusente` con `trim()` y `null` para motivos en blanco, `marcarAtendido` y la sobrecarga de `reconstruir` en `Turno`, sin cambiar `agendar` ni la firma actual de `reconstruir`; verificar que los tests nuevos y los de C-01 pasan con `./gradlew test`

## 2. Registrar ausencia (TDD)

- [x] 2.1 Escribir `RegistrarAusenciaTest` con un test por scenario del spec: inicio pasado, instante exacto de inicio, turno futuro rechazado con `RN-TU-02`, turno inexistente rechazado con `RN-TU-02` sin auditoría, estado terminal rechazado con `RN-TU-02`, usuario ausente rechazado con `RN-AU-01`, sin anticipación mínima, motivo presente y ausente, motivo vacío y solo espacios normalizados a ausente, motivo con espacios recortado, evento `MARCADO_AUSENTE` y ningún aviso al paciente; verificar que la clase nueva aún no existe y los tests compilan en rojo
- [x] 2.2 Implementar `aplicacion.casouso.RegistrarAusencia` con `Clock` inyectado, validación de usuario, búsqueda del turno por id con rechazo `RN-TU-02` antes de auditar, límite inclusivo `!ahora.isBefore(inicio)` y auditoría con la misma marca de tiempo; verificar que todos los tests de 2.1 pasan

## 3. Marcar turno atendido (TDD)

- [x] 3.1 Escribir `MarcarTurnoAtendidoTest` con un test por scenario del spec: camino feliz, turno futuro aceptado sin restricción horaria, turno inexistente rechazado con `RN-TU-02` sin auditoría, estado terminal rechazado con `RN-TU-02`, usuario ausente rechazado con `RN-AU-01`, evento `MARCADO_ATENDIDO` y ningún aviso al paciente; verificar que los tests fallan en rojo antes de implementar
- [x] 3.2 Implementar `aplicacion.casouso.MarcarTurnoAtendido` con `Clock` inyectado, búsqueda del turno por id con rechazo `RN-TU-02` antes de auditar, sin comparación temporal y con auditoría; verificar que todos los tests de 3.1 pasan

## 4. Integración con la disponibilidad existente

- [x] 4.1 Escribir tests de secuencia que cierren un turno con los casos de uso nuevos e invoquen `AgendarTurno` sobre la misma franja: después de `AUSENTE` el alta se acepta, después de `ATENDIDO` se rechaza con `RN-AG-02`; verificar ambos escenarios en verde
- [x] 4.2 Verificar con `git diff` que `AgendarTurno.java`, `ServicioDisponibilidad.java` y los puertos del dominio no se modificaron, y que el scenario existente de ausencia en `agenda/disponibilidad` sigue pasando

## 5. Verificación del change

- [x] 5.1 Correr la suite completa con `./gradlew test` y verificar que está en verde, con un test observable por cada scenario del spec delta
- [x] 5.2 Correr `./gradlew test --tests "ar.edu.turnos.arquitectura.ReglaDeOroTest"` y verificar que el dominio sigue sin importar Spring, JPA ni HTTP
