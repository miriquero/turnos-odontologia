<!-- active-stack:agent-instruction -->
# turnos-odontologia — Instrucciones para Agentes

Sistema de gestión de turnos y agenda de pacientes para consultorios
odontológicos del mercado argentino. En el fondo es un **motor de
disponibilidad de agenda**: el turno se valida contra la ocupación del
profesional **y** del sillón, con reglas de solapamiento explícitas,
verificables y probadas con tests.

## Stack Tecnológico

| Capa | Tecnologías | Versión mínima / nota |
|---|---|---|
| Lenguaje | Java | 17 (LTS) o superior |
| Framework | Spring Boot | 3.x |
| Web (opcional) | Spring Web — API REST | Opcional: la v1 puede ser un módulo de dominio sin capa HTTP |
| Persistencia | Spring Data JPA | Opcional en el núcleo: el dominio no depende de JPA |
| Base de datos | H2 en memoria (desarrollo y tests); PostgreSQL (producción futura) | — |
| Tests | JUnit 5 (Jupiter) + AssertJ; Mockito donde haga falta | **Obligatorio**: la regla de disponibilidad se prueba con tests |
| Build | **Gradle** con wrapper (`./gradlew`) versionado | Decidido en `D-08` |
| Interfaz gráfica | **Ninguna en la v1** | Restricción del Discovery |

Arquitectura **hexagonal (puertos y adaptadores)**. El detalle está en
[knowledge-base/08_arquitectura_propuesta.md](knowledge-base/08_arquitectura_propuesta.md).

## Base de Conocimiento

La fuente de verdad del dominio. **Leer antes de escribir código.**

| Archivo | Qué contiene |
|---|---|
| [01_vision_y_objetivos.md](knowledge-base/01_vision_y_objetivos.md) | Propósito, alcance v1, métricas de éxito |
| [02_descripcion_general.md](knowledge-base/02_descripcion_general.md) | Stack, arquitectura general, integraciones |
| [03_actores_y_roles.md](knowledge-base/03_actores_y_roles.md) | Actores y matriz de permisos |
| [04_modelo_de_datos.md](knowledge-base/04_modelo_de_datos.md) | Entidades, ERD, estados y transiciones |
| [05_reglas_de_negocio.md](knowledge-base/05_reglas_de_negocio.md) | **Reglas `RN-*`. El corazón del proyecto.** |
| [06_funcionalidades.md](knowledge-base/06_funcionalidades.md) | Historias `US-NNN` agrupadas en épicas |
| [07_flujos_principales.md](knowledge-base/07_flujos_principales.md) | Flujos `F-01` a `F-07` con caminos de error y borde |
| [08_arquitectura_propuesta.md](knowledge-base/08_arquitectura_propuesta.md) | Paquetes, núcleo, persistencia, estrategia de tests |
| [09_decisiones_y_supuestos.md](knowledge-base/09_decisiones_y_supuestos.md) | Decisiones `D-*`, supuestos `SU-*`, riesgos `R-*` |
| [10_preguntas_abiertas.md](knowledge-base/10_preguntas_abiertas.md) | Preguntas `PA-*` sin resolver |

**La regla que define el producto** es `RN-AG-04`: los intervalos son
semiabiertos `[inicio, fin)`, de modo que **un turno que empieza exactamente
cuando termina otro es válido**.

## Skills Disponibles

| Agente | Rol | Skills que carga |
|---|---|---|
| Dominio y aplicación | Núcleo hexagonal, reglas de negocio, casos de uso | `java-springboot`, `springboot-patterns`, `tdd` |
| Tests | Suite por escenario del spec | `springboot-tdd`, `java-junit`, `tdd` |
| Persistencia | Repositorios, índices, concurrencia | `jpa-patterns`, `spring-data-jpa`, `postgres` |
| Build | Configuración y auditoría del build | `gradle-best-practices` |
| OpenSpec | Ciclo OPSX del change | `openspec-explore`, `openspec-propose`, `openspec-apply-change`, `openspec-archive-change`, `openspec-sync-specs`, `openspec-update-change` |
| Fundación | Contexto e instrucciones | `kb-creator`, `roadmap-generator`, `agent-instruction`, `skill-registry` |

> Los compact rules de cada skill los resuelve el orquestador desde
> [.atl/skill-registry.md](.atl/skill-registry.md), generado por `skill-registry`.

Las skills de Java y Spring son de **instalación a nivel usuario**: no viajan
con el repositorio. **No son necesarias para compilar ni para correr los
tests.** Quien las quiera, las instala con `npx skills add`.

## Roadmap de Changes

El plan completo está en [CHANGES.md](CHANGES.md): 18 changes, 11 del MVP y 7
del bloque diferenciador marcados fuera de alcance.

**Camino crítico**: `C-01` → `C-11`. `C-01` es el único change crítico; de
`C-02` a `C-09` son mutuamente independientes y paralelos.

| Fase | Contenido |
|---|---|
| FASE 0 | `C-01 crear-turno-sin-solapamientos` — change base, gobernanza CRÍTICA |
| FASE 1 | Cuatro changes en paralelo: reprogramar, cancelar, sobreturno, ausencia |
| FASE 2 | Cierre del MVP: recursos, consultas, auditoría, persistencia, verificación |
| FASE 3 | Bloque diferenciador — **FUERA DEL MVP** |

**Primer ítem elegible**: `C-01`, ya desbloqueado.

## Reglas Duras (específicas del proyecto)

> Reglas globales ya definidas en el archivo global de instrucciones del cliente
> activo (orquestador, governance, TDD estricto, engram): el proyecto las
> hereda. Acá viven solo las reglas **específicas de este proyecto**.

1. **NUNCA importar Spring, JPA ni HTTP desde el paquete `dominio`** → un test
   de `ServicioDisponibilidad` tiene que correr sin levantar contexto de Spring.
   Si para probar una regla de negocio hace falta `@SpringBootTest`, la regla
   está en el lugar equivocado.

2. **NUNCA usar datos reales de pacientes** → ni en el repositorio, ni en seeds,
   fixtures, tests o capturas de pantalla. Todo dato de persona es ficticio, sin
   excepción (`RN-PA-04`).

3. **NUNCA inventar alcance durante Apply** → si algo no está en la
   especificación aprobada, no se implementa: se vuelve a Propose y se actualiza
   el spec. La especificación aprobada es el contrato.

4. **NUNCA archivar un change con tests en rojo** → la suite completa tiene que
   estar en verde antes de pasar a Archive.

## Flujo de Trabajo

1. **Leer la knowledge-base** del área que se va a tocar, siguiendo los punteros
   "Leer antes" del change en `CHANGES.md`.
2. **Tomar el change** de `CHANGES.md` respetando sus dependencias.
3. **Explore → Propose**: generar los artefactos del change y **revisar el spec
   completo** antes de aprobarlo. Cada escenario se redacta como comportamiento
   verificable (dado / cuando / entonces) y cubre, como mínimo, un camino feliz,
   un error de negocio y un caso de borde.
4. **Apply**: implementar contra el spec aprobado, sin decisiones de diseño
   improvisadas.
5. **Verify**: correr la suite completa. Debe haber un test por cada escenario
   del spec.
6. **Archive**: sincronizar los specs y cerrar el change.

Cada integrante trabaja en su rama y abre un pull request a `master`, que revisa
el líder.
<!-- /active-stack:agent-instruction -->
