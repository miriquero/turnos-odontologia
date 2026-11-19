# Proposal

## Why

El relevamiento de 19 productos en tres mercados no encontró **una sola
descripción** de qué hace un sistema odontológico cuando se intenta agendar un
turno que pisa otro, y el sillón como recurso agendable aparece en apenas 4 de
19 fichas, en ninguna de origen argentino. Ese vacío es la propuesta de valor
del proyecto: un motor de disponibilidad que valida el turno contra la ocupación
del **profesional** y del **sillón** por separado, con la semántica semiabierta
`[inicio, fin)` de `RN-AG-04` escrita una sola vez y cubierta por tests.

Además, `C-01` es el único change del camino crítico: los otros cuatro
integrantes del equipo están bloqueados hasta que esté mergeado. Cuanto antes se
cierre, antes arrancan ellos.

## What Changes

- **Scaffolding del proyecto** con Gradle y wrapper versionado (`D-08`), Java 17,
  Spring Boot 3 como dependencia de arranque y JUnit 5 + AssertJ + Mockito para
  tests. Paquetes `ar.edu.turnos.{dominio,aplicacion,infraestructura}`. El
  paquete `dominio` **no importa Spring, JPA ni HTTP**.
- **Modelo de dominio**: `Profesional`, `Sillon`, `Prestacion`, `Paciente`
  (ficha básica), `Turno`, `EstadoTurno` (`AGENDADO`, `ATENDIDO`, `CANCELADO`,
  `AUSENTE`) con la tabla de transiciones de `RN-TU-02` y la marca "ocupa
  agenda" de `RN-AG-07`, `AutorizacionSobreturno` (solo la estructura, sin caso
  de uso) y `EventoAuditoria` con **todas** sus acciones.
- **`Intervalo`** como objeto de valor: invariante `fin > inicio` y un único
  método `seSolapaCon(Intervalo)` que implementa `A.inicio < B.fin && B.inicio <
  A.fin` con `<` estricto. La regla `RN-AG-04` vive ahí y en ningún otro lugar.
- **`ServicioDisponibilidad`**: `verificarDisponibilidad(...)` que lanza
  `ReglaDeNegocioViolada` con el código de regla (`RN-AG-02` o `RN-AG-03`) y
  `evaluar(...)` no excepcional. Evalúa profesional y sillón por separado, acepta
  `excluirTurnoId` y lee la ocupación por el puerto `FuenteDeOcupacion`.
- **Caso de uso `AgendarTurno`**: valida recursos existentes y activos e inicio
  futuro (`RN-AG-08`), deriva `fin = inicio + duración de la prestación`
  (`RN-AG-01`), invoca el servicio, crea el turno `AGENDADO`, registra `CREADO`
  en auditoría con usuario obligatorio y dispara el puerto de notificación.
- **Puertos congelados** tras este change (`R-05`): `TurnoRepositorio`,
  `ProfesionalRepositorio`, `SillonRepositorio`, `PrestacionRepositorio`,
  `PacienteRepositorio`, `RegistroAuditoria`, `NotificadorPaciente` y
  `FuenteDeOcupacion`. Se implementan con **dobles en memoria solo en
  `src/test`**.

### Fuera de alcance (recorte deliberado)

La consigna del trabajo exige que el change sea **una sola funcionalidad, no un
módulo**, y `C-01` bloquea a los otros cuatro integrantes. Por eso queda fuera:

- **Persistencia real**: nada de JPA, H2, Spring Data ni base de datos. Los
  puertos de repositorio se definen como interfaces del dominio y se implementan
  con dobles en memoria únicamente para los tests. La persistencia es `C-09`.
- **Capa REST**: ni controladores, ni DTOs, ni manejo de errores HTTP. Es `C-10`.
- **Cálculo de huecos libres** (`C-07`), **sobreturno** (`C-04`),
  **reprogramación** (`C-02`), **cancelación** (`C-03`), **ausencia/atención**
  (`C-05`) y **bloqueos de agenda** (`C-12`).

## Capabilities

### New Capabilities

- `agenda/disponibilidad`: la semántica del intervalo semiabierto y la regla de
  solapamiento por profesional y por sillón. Es el núcleo que todos los demás
  changes consumen sin modificar (`R-05`).
- `agenda/alta-de-turno`: el alta de un turno sobre recursos válidos y activos,
  con duración derivada de la prestación, creación en estado `AGENDADO`,
  registro de auditoría y notificación por puerto.

### Modified Capabilities

<!-- Ninguna: es el primer change del proyecto y no hay specs vigentes. -->

## Impact

- **Código nuevo**: todo el repositorio. No hay código previo que modificar.
  `build.gradle`, `settings.gradle`, wrapper de Gradle, y los paquetes
  `ar.edu.turnos.dominio.{modelo,servicio,puerto,excepcion}` y
  `ar.edu.turnos.aplicacion.casouso`.
- **Contrato congelado**: `Intervalo`, `ServicioDisponibilidad`, `EstadoTurno`,
  el enum de acciones de `EventoAuditoria` y los ocho puertos quedan cerrados al
  terminar este change. Un PR posterior que los toque se rechaza salvo acuerdo
  explícito del líder (`R-05`, `D-03`).
- **Desbloquea**: `C-02` a `C-10` del roadmap (`CHANGES.md`, GATE 1).
- **Dependencias externas**: ninguna en tiempo de test. Ningún test levanta
  contexto de Spring ni necesita credenciales (`RN-NO-02`).
- **Riesgo abierto**: la concurrencia de dos altas simultáneas sobre la misma
  franja (`R-06`) **no se resuelve acá**; se traslada a `C-09`.
