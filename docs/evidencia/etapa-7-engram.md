# Etapa 7 — Memoria persistente del agente (Engram)

Fecha: **2026-11-19**

Engram es la memoria del agente entre sesiones. Sin ella, cada vez que se abre
una conversación nueva el agente arranca de cero: no sabe qué se decidió, por
qué, ni qué ya se intentó.

## 1. Qué quedó guardado sobre este proyecto

Estado del sistema al cierre del trabajo:

```
Sessions: 3
Observations: 190+
Projects: desarrollosoftware, rqriot, fantasy-f1-2026
```

Consulta que lo recupera:

```
mem_search(query: "turnos odontologia")
```

Devuelve **6 observaciones**, que cubren las tres categorías que pide la
consigna:

| # | Tipo | Título | Qué preserva |
|---|---|---|---|
| 191 | `decision` | Gradle como herramienta de build (D-08, cierra PA-01) | Una **decisión** y el porqué |
| 193 | `architecture` | NotificadorPaciente genérico para no romper la contención R-05 | Una **convención** de diseño |
| 192 | `discovery` | La verificación manual de fuentes encontró 2 errores del Discovery | Un **hallazgo** |
| 194 | `decision` | OPSX completo: C-01 archivado | El **progreso** del ciclo |
| 190 | `decision` | OPSX propose completado para C-01 | Progreso intermedio |
| 189 | `config` | skill-registry | **Configuración** del proyecto |

Cada una guarda qué se hizo, por qué, dónde, y qué se aprendió.

## 2. Por qué esto evita arrancar de cero

Tres ejemplos concretos de este mismo trabajo.

### El JDK

La observación 191 guarda que esta máquina tiene `JAVA_HOME` apuntando a un
**JDK 26** que Spring Boot 3 puede no soportar, que los JDK alternativos están
en `C:\Users\zbook\.jdks`, y que la solución es el toolchain de Gradle, **sin**
versionar rutas absolutas porque el repositorio lo comparten cinco máquinas.

Sin ese registro, la próxima sesión vuelve a chocarse con el mismo error de
compilación y vuelve a diagnosticarlo desde cero. Es un problema que ya había
aparecido en otro trabajo de la misma materia.

### Los errores del Discovery

La observación 192 guarda que **dos de las tres fuentes verificadas a mano
contenían afirmaciones que no se sostenían**, cuáles eran y por qué el informe
las había dado por buenas. Sin eso, una sesión futura podría volver a citar el
precio equivocado de AgendaPro.

### La firma del puerto

La observación 193 guarda **por qué** `NotificadorPaciente` es genérico. La
firma por sí sola no lo explica: alguien podría "simplificarla" más adelante y
romper la contención que protege el trabajo paralelo de cuatro personas.

## 3. Consulta real que recupera contexto de una sesión anterior

Esto ocurrió **de forma espontánea** durante esta sesión, y es la mejor
demostración.

Al guardar la observación 193, Engram detectó por su cuenta tres memorias
anteriores que podían entrar en conflicto, y una era la **178**, guardada el
**2026-11-18 en una sesión distinta**:

```
mem_get_observation(id: 178)

#178 [decision] Requisitos verbales del docente: nueva etapa 6 (C-07)
Created: 2026-11-18 10:53:17
Topic: opsx/etapa-6-requisitos-docente/contexto
```

Esa memoria conserva lo que el docente pidió verbalmente para **otro trabajo
práctico** (facturación ARCA): CRUD de facturas, filtros, paginación, modales,
PDF con CAI. Información que no está en ninguna consigna escrita y que, sin
Engram, se habría perdido al cerrar aquella conversación.

El agente la recuperó sin que nadie se lo pidiera, la comparó con lo que estaba
guardando y preguntó si había contradicción.

## 4. Un hallazgo sobre la propia herramienta

Las memorias de **los dos trabajos prácticos están bajo el mismo proyecto**,
`desarrollosoftware`. Engram deduce el proyecto del remoto de git del directorio
de trabajo (`project_source: git_remote`), y ambos trabajos cuelgan de la misma
carpeta de usuario.

Consecuencia práctica: al guardar algo de turnos-odontologia aparecen como
candidatos a conflicto memorias de facturación ARCA. No es un error grave
—se resuelve marcando la relación como `not_conflict`— pero conviene saberlo.
Se arregla declarando el nombre del proyecto en `.engram/config.json`.

## 5. Nota sobre versionar la memoria en el repositorio

Engram puede exportar la memoria del proyecto al repositorio con `engram sync`,
para que viaje con el código.

**En este proyecto no se hizo, a propósito**: el repositorio es **público**, y
versionar `.engram/` dejaría visible para cualquiera el historial de decisiones,
que además mezcla los dos trabajos prácticos del equipo. Es una decisión
consciente, no un olvido.

## Cómo reproducir esto en vivo

```
mem_stats()
mem_search(query: "turnos odontologia")
mem_get_observation(id: 191)
mem_get_observation(id: 178)
```

La segunda muestra lo guardado; la última, la recuperación de contexto de una
sesión anterior.
