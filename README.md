# turnos-odontologia

Sistema de gestion de turnos y agenda de pacientes para consultorios odontologicos
(Argentina). En el fondo es un **motor de disponibilidad de agenda**: cada turno se valida
contra la ocupacion del profesional **y** del sillon, con reglas de solapamiento explicitas y
probadas con tests.

Trabajo Global de Desarrollo de Software, comision 3K10. Cinco changes especificados,
implementados, probados y archivados con el ciclo OPSX de OpenSpec.

## Requisitos

Un **JDK 17** instalado. Nada mas: el wrapper de Gradle 8.10.2 esta versionado, asi que no
hace falta instalar Gradle.

Si tenes un JDK mas nuevo como predeterminado, no hay que tocar nada:
`gradle/gradle-daemon-jvm.properties` fija el toolchain en 17 y Gradle lo resuelve solo.
Para verificar que lo encuentra: `./gradlew -q javaToolchains`.

## Correr los tests

    ./gradlew test          # en Windows: gradlew.bat test

Son **223 tests**, uno por cada escenario de las especificaciones. Tienen que pasar todos.

## Ver el sistema funcionando

    ./gradlew demo          # en Windows: gradlew.bat demo

Levanta `http://localhost:8080` con cinco pantallas (Agenda, Turno, Disponibilidad,
Auditoria y Recursos) sobre el mismo nucleo que prueban los tests. Para usar otro puerto:
`./gradlew demo --args="9090"`.

> Esta pantalla **no es parte del MVP**. El Discovery decidio que la v1 no llevaba interfaz
> grafica, asi que entra como adaptador de demostracion. Los datos viven en memoria y se
> pierden al cortar el proceso: la persistencia esta especificada como `C-09` y quedo
> declarada fuera de alcance. Todos los datos de pacientes son ficticios (`RN-PA-04`).

## Como esta organizado

    knowledge-base/     Base de conocimiento, 10 archivos. 05_reglas_de_negocio.md es el corazon
    CHANGES.md          Roadmap: 18 changes con dependencias y gates de paralelismo
    AGENTS.md           Reglas del proyecto para los agentes (igual que CLAUDE.md)
    openspec/specs/     Especificaciones vigentes: 6 capacidades
    openspec/changes/   Los 5 changes, archivados bajo archive/
    src/main/java/      Codigo. El paquete dominio/ no importa Spring, JPA ni HTTP
    src/test/java/      Un test por escenario
    docs/discovery/     Informe de Discovery (19 sistemas relevados), en .md y .pdf
    docs/REFLEXION.*    Reflexion escrita del equipo
    docs/evidencia/     Capturas de la instalacion y evidencia de memoria persistente

## La regla que define el producto

`RN-AG-04`: los intervalos son **semiabiertos** `[inicio, fin)`. Un turno que empieza
exactamente cuando termina otro **es valido**. Es el caso de borde mas facil de implementar
mal, y tiene sus propios tests.

## Arquitectura

Hexagonal (puertos y adaptadores). El paquete `dominio` no conoce ningun framework, asi que
sus reglas se prueban sin levantar contexto de Spring. Esa restriccion no es una
recomendacion: la verifica `src/test/java/ar/edu/turnos/arquitectura/ReglaDeOroTest.java`,
que falla si una fuente de produccion nombra el framework.

El detalle esta en [knowledge-base/08_arquitectura_propuesta.md](knowledge-base/08_arquitectura_propuesta.md).

## Los cinco changes

| Change | Capacidad | Integrante |
|---|---|---|
| `C-01` crear-turno-sin-solapamientos | Alta de turno y disponibilidad | Martin Riquero |
| `C-02` reprogramar-turno | Reprogramacion | Julian Fernandez Drago |
| `C-03` cancelar-turno-con-anticipacion-minima | Cancelacion | Augusto Quiroga |
| `C-04` agendar-sobreturno-autorizado | Sobreturno autorizado | Maximiliano Di Natale |
| `C-05` registrar-ausencia-y-atencion-de-turno | Cierre de turno | Emiliano Echavarria |

Cada uno trabajo en su rama y entro por pull request a `master`.
