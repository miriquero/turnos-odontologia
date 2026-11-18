# Descripción General

## Stack tecnológico

| Capa | Tecnologías | Versión mínima / nota |
|---|---|---|
| Lenguaje | Java | 17 (LTS) o superior |
| Framework | Spring Boot | 3.x |
| Web (opcional) | Spring Web — API REST | Opcional: la v1 puede ser un módulo de dominio sin capa HTTP |
| Persistencia | Spring Data JPA | Opcional en el núcleo: el dominio no depende de JPA (ver `08`) |
| Base de datos | H2 en memoria (desarrollo y tests); PostgreSQL (producción futura) | — |
| Tests | JUnit 5 (Jupiter) + AssertJ; Mockito donde haga falta | **Obligatorio**: la regla de disponibilidad se prueba con tests |
| Build | Maven o Gradle (una sola elección, ver `PA-01` en `10_preguntas_abiertas.md`) | — |
| Interfaz gráfica | **Ninguna en la v1** | Restricción del Discovery punto 9 |

## Arquitectura general

Arquitectura **hexagonal (puertos y adaptadores)** con el núcleo de dominio
aislado. La justificación no es ortodoxia: es que los dos requisitos duros del
proyecto la imponen.

1. Las integraciones externas (WhatsApp, Mercado Pago, ARCA, Google Calendar)
   quedan **fuera de la v1 pero dentro del diseño**, como puertos con adaptador
   simulado. Así las pruebas no dependen de credenciales ni de servicios de
   terceros — que es la única forma de tener recordatorios en el diseño sin que
   el equipo necesite una cuenta de WhatsApp Business.
2. Los cinco integrantes trabajan en paralelo y **todos los changes tocan la
   entidad `Turno` y la validación de horarios**. Aislar esa validación en un
   único servicio de dominio reutilizable es la mitigación del riesgo de
   conflictos de merge (ver `R-05` en `09_decisiones_y_supuestos.md`).

```
                  ┌───────────────────────────────────────────┐
  Adaptadores     │                DOMINIO                    │   Puertos de salida
  de entrada      │                                           │   (interfaces del dominio)
                  │   Turno  Profesional  Sillon  Paciente    │
  REST Controller │   Prestacion  Bloqueo(dif.)               │   NotificadorPaciente ──► AdaptadorSimulado (v1)
  (opcional v1) ──►│                                           │                            └► WhatsApp/Email (futuro)
                  │   ServicioDisponibilidad  ◄── NUCLEO      │   RegistroAuditoria  ────► En memoria / tabla
  Tests JUnit  ──►│   (la regla de solapamiento vive aqui     │
                  │    y SOLO aqui)                           │   CobrosPort (futuro)  ──► MercadoPago
                  │                                           │   FacturacionPort (fut)──► ARCA
                  │   Puertos de repositorio (interfaces)     │
                  └──────────────┬────────────────────────────┘
                                 │
                   Repositorios JPA / en memoria (tests)
```

**Regla de oro de la arquitectura**: el paquete de dominio no importa nada de
Spring, de JPA ni de HTTP. Un test de `ServicioDisponibilidad` debe poder correr
sin levantar un contexto de Spring.

## Integraciones externas

Todas se modelan como **puerto del dominio con adaptador**. En la v1 solo el
puerto de notificación tiene adaptador, y es simulado.

| Servicio | Propósito | Tipo | Estado v1 |
|---|---|---|---|
| Notificación al paciente (WhatsApp Business API o email) | Recordatorio de turno, confirmación | Puerto de dominio → REST/SDK en el futuro | **Puerto definido + adaptador simulado que registra el envío** |
| Mercado Pago | Señas y cobros online | Puerto → REST | Fuera de alcance (etapa posterior) |
| ARCA | Facturación (A, B, C, notas de crédito y débito) | Puerto → REST | Fuera de alcance (etapa posterior) |
| Google Calendar | Sincronización de calendario externo | Puerto → REST/OAuth | Fuera de alcance (etapa posterior) |

**Por qué está así**: el relevamiento mostró que WhatsApp, que en Argentina es
*el* canal de comunicación con el paciente, está tarifado por uso y con tope en
todos los productos que lo tienen (AgendaPro limita a 50 recordatorios mensuales
con packs desde ARS 7.900; Simples Dental cobra la confirmación automática
aparte). El vacío no es técnico sino comercial, y la respuesta de diseño es
aislar el canal detrás de una interfaz para poder empezar simulado, seguir con
email y conectar WhatsApp después sin rehacer nada.

## API REST (si se implementa la capa HTTP)

Agrupada por recurso. Es un esquema indicativo: la v1 puede prescindir de ella.

| Recurso | Operaciones principales |
|---|---|
| `/profesionales` | `GET` listar, `GET /{id}`, `POST` crear |
| `/sillones` | `GET` listar, `POST` crear |
| `/prestaciones` | `GET` listar (con duración), `POST` crear, `PUT /{id}` |
| `/pacientes` | `GET` buscar, `GET /{id}`, `POST` crear, `PUT /{id}` (ficha básica) |
| `/turnos` | `POST` agendar (valida disponibilidad), `GET /{id}`, `GET?profesionalId=&sillonId=&desde=&hasta=` |
| `/turnos/{id}/reprogramar` | `POST` — revalida disponibilidad en el nuevo horario |
| `/turnos/{id}/cancelar` | `POST` — exige motivo; valida anticipación mínima |
| `/turnos/{id}/ausencia` | `POST` — registra ausencia del paciente |
| `/turnos/{id}/atender` | `POST` — marca atendido |
| `/disponibilidad` | `GET?prestacionId=&desde=&hasta=[&profesionalId=][&sillonId=]` — huecos libres |

Errores de negocio devueltos como **ProblemDetail (RFC 7807)** con el código de
regla violada (`RN-AG-02`, etc.) en el cuerpo, para que la causa sea trazable
desde el test de integración.
