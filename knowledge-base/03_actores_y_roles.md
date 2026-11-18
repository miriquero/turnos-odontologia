# Actores y Roles

## Actores del sistema

| Actor | Descripción | Cómo interactúa |
|---|---|---|
| **Odontólogo dueño** | Titular del consultorio. Atiende y además administra. En el segmento objetivo (dos o tres sillones) es frecuentemente también quien agenda. | Administra prestaciones y duraciones, sillones y profesionales; opera su agenda completa; **autoriza sobreturnos** |
| **Odontólogo colega** | Profesional que atiende en sillones compartidos con el dueño. | Ve y opera su propia agenda; no administra el catálogo ni autoriza sobreturnos |
| **Recepción / secretaría** | Agenda, reprograma y cancela turnos de terceros. | Opera la agenda de todos los profesionales; registra motivos de cancelación y ausencia; **no** autoriza sobreturnos por sí misma (ver `PA-03`) |
| **Paciente** | Destinatario del turno. En la v1 **no es un usuario del sistema**: no tiene cuenta ni autogestión. | v1: es un dato (ficha básica), no un actor que opera. Bloque diferenciador: reserva online por enlace sin cuenta previa |
| **Sistema (proceso automático)** | Disparador de recordatorios. | Invoca el puerto `NotificadorPaciente`; en la v1 el adaptador es simulado |

**Nota de diseño del Discovery**: el relevamiento encontró que "roles y permisos
por usuario" están mencionados en la mayoría de los productos pero **casi siempre
sin detallar qué permite cada rol** (hallazgo C.1.7 del informe). Dentia es la
única ficha argentina que publica un detalle real (17 perfiles de rol). La
decisión de este proyecto es definir los permisos **efectivos sobre las
operaciones de agenda**, en particular sobre la autorización de sobreturnos, en
lugar de declarar una lista de roles sin semántica.

## RBAC — Matriz de permisos

Los roles están **diseñados** en la v1 y el modelo los contempla, pero su
**aplicación efectiva** (autenticación y autorización reales) pertenece al bloque
diferenciador, no al MVP. En el MVP el usuario que ejecuta la operación se recibe
como dato de entrada y se usa para la auditoría (`RN-AU-01`).

Leyenda: `C` crear · `R` leer · `U` modificar · `D` eliminar/anular · `—` sin acceso

| Recurso | Odontólogo dueño | Odontólogo colega | Recepción | Paciente (v1) |
|---|---|---|---|---|
| Profesional | C R U D | R | R | — |
| Sillón / box | C R U D | R | R | — |
| Prestación (y su duración) | C R U D | R | R | — |
| Paciente (ficha básica) | C R U | R | C R U | — |
| Turno — propio | C R U D | C R U D | — | — |
| Turno — de otro profesional | C R U D | R | C R U D | — |
| Cancelación de turno | C | C (solo propios) | C | — |
| Registro de ausencia | C | C (solo propios) | C | — |
| **Autorización de sobreturno** | **C** | Pendiente (`PA-03`) | Pendiente (`PA-03`) | — |
| Bloqueo de agenda *(diferenciador)* | C R U D | C R U D (propios) | R | — |
| Registro de auditoría | R | — | — | — |
| Reporte de ausentismo *(diferenciador)* | R | R (propios) | R | — |

**Regla transversal**: ningún rol puede violar la regla de solapamiento. El
sobreturno no es un permiso para ignorar la regla, es una **excepción explícita,
autorizada y auditada** (`RN-AG-06`).

## Rutas públicas

En la v1, si se implementa la capa HTTP, **no hay rutas públicas de negocio**:
el paciente no opera el sistema. Las únicas rutas sin autenticación son las
técnicas.

| Ruta | Propósito |
|---|---|
| `GET /actuator/health` | Healthcheck |
| `GET /v3/api-docs`, `GET /swagger-ui` | Documentación de la API (solo en perfil de desarrollo) |

**Bloque diferenciador** — cuando se implemente la reserva online, la ruta
pública será un **enlace con identificador opaco y de un solo uso**, sin cuenta
previa, siguiendo el patrón del Dentally Portal (el único flujo de reserva
documentado paso a paso de toda la muestra: enlace por email o SMS, QR, acceso
con móvil verificado y código de un solo uso). Es también la única referencia del
relevamiento con **verificación de identidad del paciente en la reserva**, que
resuelve el problema de las reservas falsas que ningún otro producto aborda.
