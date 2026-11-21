# Proposal

## Why

En un consultorio odontológico real aparecen urgencias que requieren agendar un
turno sobre una franja ya ocupada; sin un mecanismo controlado, o el sistema
bloquea la atención real o la regla de no solapamiento se termina desactivando
por completo. Se elige este change (`C-04`) porque define la **única excepción
legítima** a la regla central del producto (`D-05`, `RN-AG-06`): exige
autorización explícita, responsable habilitado y motivo, y deja trazabilidad
completa sin alterar `ServicioDisponibilidad` ni debilitar el alta normal.

## What Changes

- **Nuevo caso de uso `aplicacion.casouso.AgendarSobreturno`** (y su solicitud
  `SolicitudAgendarSobreturno` + enum liviano `RolAutorizador`): decide
  explícitamente omitir solo `RN-AG-02` y `RN-AG-03`, sin modificar
  `ServicioDisponibilidad` ni `AgendarTurno`.
- **Validación estricta de la excepción (`RN-AG-06`)**: exige marca explícita de
  sobreturno, usuario responsable (`autorizadoPor`) y `motivo`; ante la ausencia
  o invalidez de cualquiera de los tres rechaza la operación y **nunca** realiza
  reintentos automáticos.
- **Chequeo mínimo de autorización por rol (`SU-04`, `PA-03`)**: permite
  autorizar únicamente al odontólogo dueño (`ODONTOLOGO_DUENIO`), rechazando
  roles sin permiso (`ODONTOLOGO_COLEGA`, `RECEPCION` o ausente), sin introducir
  ningún framework de seguridad (los roles efectivos corresponden a `C-15`).
- **Conservación del resto de las reglas del dominio**: siguen aplicando
  recursos existentes y activos e inicio estrictamente futuro (`RN-AG-08`),
  duración derivada exclusivamente de la prestación (`RN-AG-01`) y estado inicial
  `AGENDADO` (`RN-TU-01`).
- **Persistencia 1:1 de `AutorizacionSobreturno` vía puerto nuevo**: se declara
  la nueva interfaz `dominio.puerto.AutorizacionSobreturnoRepositorio` (sin
  modificar los 8 puertos congelados de `C-01`) para guardar y consultar la
  `AutorizacionSobreturno` vinculada al turno con `esSobreturno = true`.
- **Auditoría y notificación**: registra `EventoAuditoria` con
  `AccionAuditoria.SOBRETURNO_AUTORIZADO` y usuario responsable (`RN-AU-01`),
  complementado con el motivo persistido en `AutorizacionSobreturno` (1:1 por
  `turnoId`), y dispara `NotificadorPaciente.notificar(turno, SOBRETURNO_AUTORIZADO)`
  de forma resiliente ante fallos (`RN-NO-03`).

### Fuera de alcance (recorte deliberado)

- **Persistencia real** (JPA, H2, PostgreSQL): el nuevo puerto se prueba con un
  doble en memoria local en `src/test`; la persistencia real es `C-09`.
- **Capa REST y pantallas**: sin endpoints HTTP, DTOs web ni interfaz gráfica
  (`C-10` y restricción v1).
- **Framework de seguridad / RBAC efectivo**: la gestión completa de usuarios,
  roles y permisos corresponde a `C-15` (FASE 3, fuera del MVP).
- **Modificaciones al núcleo congelado de `C-01` (`R-05`)**: no se editan
  `Intervalo`, `ServicioDisponibilidad`, `EstadoTurno`, `AccionAuditoria`,
  `Turno`, `EventoAuditoria`, `Profesional`, `AgendarTurno`, los 8 puertos
  existentes ni los helpers de test compartidos en `src/test/.../dobles/`.

## Capabilities

### New Capabilities

- `agenda/sobreturno-autorizado`: alta excepcional de un turno marcado como
  sobreturno (`esSobreturno = true`) con autorización explícita del odontólogo
  dueño, motivo obligatorio, persistencia 1:1 de `AutorizacionSobreturno`,
  auditoría `SOBRETURNO_AUTORIZADO` y preservación intacta de `RN-AG-08`,
  `RN-AG-01` y del comportamiento por defecto del alta normal.

### Modified Capabilities

<!-- Ninguna: `agenda/disponibilidad` y `agenda/alta-de-turno` no cambian sus requisitos. -->

## Impact

- **Archivos nuevos en producción** (cero archivos existentes modificados):
  - `src/main/java/ar/edu/turnos/dominio/puerto/AutorizacionSobreturnoRepositorio.java`
  - `src/main/java/ar/edu/turnos/aplicacion/casouso/RolAutorizador.java`
  - `src/main/java/ar/edu/turnos/aplicacion/casouso/SolicitudAgendarSobreturno.java`
  - `src/main/java/ar/edu/turnos/aplicacion/casouso/AgendarSobreturno.java`
- **Archivos nuevos en tests** (sin tocar `dobles/` compartidos):
  - `src/test/java/ar/edu/turnos/aplicacion/casouso/AgendarSobreturnoTest.java`
- **Dependencias**: únicamente `C-01` (ya integrado en `master`). No agrega
  dependencias en `build.gradle` ni importa Spring/JPA/HTTP en `dominio` ni en
  `aplicacion`.
- **Gobernanza**: **ALTO** — única excepción a la regla central de no
  solapamiento (`D-05`).
