# Decisiones y Supuestos

> Decisiones de diseño tomadas durante la fundación del proyecto, los supuestos
> que sostienen el dominio y los riesgos detectados. Las preguntas sin responder
> viven en `10_preguntas_abiertas.md`.

## Decisiones (formato ADR corto)

### D-01 — El dominio se modela multi-profesional y multi-sillón

**Contexto.** El usuario objetivo elegido es el odontólogo independiente. La
regla central del proyecto, en cambio, es evitar solapamientos **por profesional
y por sillón**, que con un solo profesional y un solo sillón pierde sentido.

**Decisión.** El dominio se escribe multi-profesional y multi-sillón desde el
día uno, aunque el usuario típico opere con dos o tres sillones y a veces un
colega.

**Consecuencias.** La regla de solapamiento tiene filo real y es verificable con
tests. A cambio, el modelo es más rico que el caso de uso mínimo. Depende del
supuesto `SU-01`.

### D-02 — Los intervalos son semiabiertos

**Contexto.** Un turno que empieza exactamente cuando termina otro es una
situación cotidiana en un consultorio y, a la vez, el caso de borde más fácil de
implementar mal.

**Decisión.** El intervalo incluye su instante de inicio y excluye el de fin. La
semántica se encapsula en el objeto de valor `Intervalo` y la implementa un
único método.

**Consecuencias.** Turnos contiguos siempre válidos, sin ambigüedad. La regla
queda testeable de forma aislada. Por coherencia, el límite de anticipación
mínima de `RN-AG-05` también es inclusivo: con exactamente `N` horas se acepta.

### D-03 — Arquitectura hexagonal con un único servicio de disponibilidad

**Contexto.** Cinco personas trabajan en paralelo y todos los changes tocan la
entidad `Turno`. Las integraciones externas quedan fuera de la v1 pero tienen
que estar en el diseño.

**Decisión.** Puertos y adaptadores, con la regla de solapamiento concentrada en
`ServicioDisponibilidad` y en ningún otro lugar.

**Consecuencias.** Una sola implementación de la regla, imposible de divergir
entre flujos. Los tests del núcleo corren sin Spring. Es también la mitigación
del riesgo `R-05`. El costo es más interfaces y más indirección de la que
pediría un sistema de este tamaño.

### D-04 — Las integraciones externas son puertos con adaptador simulado

**Contexto.** Los recordatorios al paciente fueron marcados como innegociables
en el Discovery, pero dependen de WhatsApp Business API, que requiere cuenta,
claves aprobadas y costo por mensaje. La consigna prohíbe claves en el
repositorio.

**Decisión.** El puerto `NotificadorPaciente` se define en el dominio y la v1
solo trae un adaptador simulado que registra el envío.

**Consecuencias.** Los tests no dependen de servicios de terceros ni de
credenciales. El recordatorio existe en el diseño sin existir en la realidad, lo
cual hay que declarar con honestidad en la defensa. Conectar WhatsApp más
adelante no obliga a rehacer el dominio.

**Evidencia que la respalda.** El relevamiento mostró que WhatsApp está tarifado
por uso y con tope en todos los productos que lo ofrecen: AgendaPro limita a 50
recordatorios mensuales con packs adicionales, y Simples Dental cobra la
confirmación automática aparte. El vacío no es técnico sino comercial.

### D-05 — El sobreturno es la única excepción a la regla de solapamiento

**Contexto.** Un consultorio real necesita poder forzar un turno sobre una
franja ocupada, pero una excepción sin control vacía la regla.

**Decisión.** El sobreturno salta solo `RN-AG-02` y `RN-AG-03`, exige
autorización explícita de un rol habilitado y un motivo, y queda auditado. El
`ServicioDisponibilidad` nunca miente: la decisión de saltar la verificación
vive en el caso de uso.

**Consecuencias.** La regla se mantiene íntegra y la excepción es rastreable.
Quién puede autorizar está sin decidir (`PA-03`); el supuesto provisorio es
`SU-04`.

### D-06 — El MVP se recorta a lógica de dominio verificable

**Contexto.** El Discovery marcó cinco funcionalidades como innegociables. Para
un equipo de cinco estudiantes con plazo de materia, es demasiado.

**Decisión.** La reserva online del paciente pasa al bloque diferenciador y los
recordatorios quedan como puerto simulado. El MVP se concentra en la gestión de
turnos con prevención de solapamientos.

**Consecuencias.** El alcance es terminable y cada pieza es testeable sin
dependencias externas. Se pierde la cara pública del producto, que era parte del
atractivo. La justificación está escrita en la sección D del informe.

**Evidencia que la respalda.** La prevención de solapamientos no está
documentada por ningún sistema de los tres mercados relevados, y el sillón como
recurso agendable aparece en solo 4 de 19 fichas, en ninguna de origen
argentino. Es el único punto donde el relevamiento encontró un vacío completo.

### D-07 — Stack Java con Spring Boot

**Contexto.** El stack lo define el equipo. Había alternativas más livianas para
una API de dominio.

**Decisión.** Java con Spring Boot, tests con JUnit 5.

**Consecuencias.** El equipo arranca con menos fricción por experiencia previa y
por las convenciones ya disponibles. A cambio, más código de infraestructura que
con una alternativa liviana. La elección de Maven o Gradle sigue abierta
(`PA-01`).

## Supuestos

Son afirmaciones **no verificadas** que el diseño da por ciertas. Si alguna
resulta falsa, hay que rehacer lo que indica la columna de impacto.

| ID | Supuesto | Por qué se asume | Impacto si es falso |
|---|---|---|---|
| `SU-01` | El usuario objetivo comparte dos o tres sillones, a veces con un colega | Hace que la regla de solapamiento por sillón tenga sentido real | El modelo multi-sillón sobra y `D-01` pierde fundamento |
| `SU-02` | El catálogo de duraciones por prestación refleja la práctica real | Ningún odontólogo validó esos valores | Son datos configurables, no constantes: cambiarlos no toca código |
| `SU-03` | El valor por defecto de la anticipación mínima para cancelar es razonable | El Discovery no lo definió, ver `PA-04` | Es configurable: se ajusta sin tocar la regla |
| `SU-04` | Solo el odontólogo dueño puede autorizar un sobreturno | `PA-03` sin resolver; es el supuesto más restrictivo y por lo tanto el más seguro | Ampliar el permiso es agregar roles a la matriz, no rehacer la regla |

## Riesgos

| ID | Riesgo | Mitigación |
|---|---|---|
| `R-01` | La evidencia del relevamiento es débil: las páginas se leyeron con extracción automática y los sitios de reseñas bloquearon el acceso | Verificación manual de fuentes, documentada en el informe. Dos de las tres fuentes legibles tenían afirmaciones que no se sostenían |
| `R-02` | El alcance era ambicioso: cinco innegociables para el plazo disponible | `D-06` recorta el MVP y traslada el resto a etapas posteriores |
| `R-03` | `SU-01` sin validar: el usuario objetivo podría no compartir sillones | Declarado como supuesto explícito, no como hecho |
| `R-04` | Dependencia externa en los recordatorios | `D-04`: puerto con adaptador simulado |
| `R-05` | Conflictos de merge: los cinco integrantes tocan `Turno` y la validación de horarios | `D-03` aísla la validación. El change base escribe `ServicioDisponibilidad` e `Intervalo` y después no se tocan; cada change agrega su propio caso de uso en archivos nuevos |
| `R-06` | Concurrencia: dos altas simultáneas sobre la misma franja pueden pasar ambas la verificación antes de escribir | Señalado, **no resuelto** en la v1. Mitigación mínima: restricción de exclusión en la base o bloqueo pesimista sobre el recurso |

### D-08 — La herramienta de build es Gradle

**Contexto.** `D-07` fijó Java con Spring Boot pero dejó sin decidir la
herramienta de build (`PA-01`), que bloqueaba el arranque del change base: define
el archivo de configuración del proyecto y cómo se corren los tests, y tiene que
ser la misma para los cinco integrantes.

**Decisión.** **Gradle**, con wrapper (`gradlew`) versionado en el repositorio.

**Consecuencias.** El equipo ya viene usando Gradle en otros trabajos de la
materia, así que arranca sin fricción y tiene proyectos propios de referencia.
El wrapper garantiza que los cinco compilen con la misma versión sin instalar
nada. A cambio, la mayoría de los tutoriales de Spring Boot están escritos en
Maven y hay que traducirlos.

**Pendiente asociado.** Verificar con qué JDK compila: en trabajos anteriores de
esta materia hubo que forzar un JDK distinto del que toma el sistema por
defecto. Se confirma al armar el scaffolding en `C-01`.

**Cierra**: `PA-01`.
