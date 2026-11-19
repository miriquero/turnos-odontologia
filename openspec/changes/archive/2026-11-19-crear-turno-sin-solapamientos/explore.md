# Explore — crear-turno-sin-solapamientos

Relevamiento previo al Propose. **Esta fase no implementa**: investiga el
problema y el estado del código para que el Propose no arranque a ciegas.

- **Fecha**: 2026-11-19
- **Conducido por**: Martín Ignacio Riquero (líder)
- **Change**: `C-01`, fase 0 del roadmap, único del camino crítico

---

## 1. Estado del código

```
openspec list --specs   ->  No specs found
ls src                  ->  no existe
ls build.gradle         ->  no existe
openspec list --json    ->  changes: []
```

**No hay código.** Ni fuentes, ni build, ni specs vigentes, ni changes activos.

No es un dato menor: significa que `C-01` no sólo implementa una funcionalidad,
también **funda el proyecto técnico**. El scaffolding de Gradle, la estructura
de paquetes y los puertos nacen acá. Y explica por qué es el único change del
camino crítico: los otros cuatro no tienen dónde apoyarse hasta que esto exista.

## 2. El problema, reducido a su núcleo

Todo el change se juega en una sola pregunta: **¿esta franja está libre?**

Dos turnos se solapan si y sólo si cada uno empieza antes de que el otro
termine:

```
   se solapan   <=>   A.inicio < B.fin   Y   B.inicio < A.fin
```

El `<` es **estricto**, nunca `<=`. Ahí está toda la regla:

```
   A: [10:00 ======= 10:30)
   B:          [10:30 ======= 11:00)     ACEPTADO
                    ^
                    A.fin no pertenece a A

   A: [10:00 ======= 10:30)
   B:      [10:15 ======= 10:45)         RECHAZADO  (RN-AG-02)
              ^^^^^^ se pisan 15 minutos
```

Con `<=` los turnos contiguos darían conflicto y la agenda sería inusable: no se
podría atender a dos pacientes seguidos.

## 3. La validación corre dos veces, no una

```
   AgendarTurno
        |
        v
   ServicioDisponibilidad
        |
        +--> ocupacion del PROFESIONAL --> solapa? --> RN-AG-02
        |
        +--> ocupacion del SILLON ------> solapa? --> RN-AG-03
        |
        v
   Turno AGENDADO  +  evento CREADO
```

Profesional y sillón se evalúan **por separado**, y cada uno puede fallar solo:
un profesional libre con el sillón ocupado es un rechazo tan válido como el
inverso. Son dos escenarios distintos, no uno.

## 4. Verificación contra la knowledge-base

El roadmap pedía rechazar turnos con inicio en el pasado. **Sospeché que fuera
alcance inventado** y lo fui a buscar: está en `RN-AG-08`, junto con la
aclaración de que marcar `ATENDIDO` o `AUSENTE` un turno pasado sí es válido.
No era invención.

También se confirmó que la tabla de `RN-AG-04` tiene efectivamente sus nueve
casos, que son las nueve posiciones relativas posibles de dos intervalos.

## 5. Decisiones de alcance tomadas en esta fase

El Explore cerró tres preguntas antes de permitir el Propose. Las tres se
decidieron **recortando**, con el mismo argumento de fondo: `C-01` bloquea a
cuatro personas y la consigna exige "una sola funcionalidad, no un módulo".

| Decisión | Qué se eligió | Qué se descartó y por qué |
|---|---|---|
| Persistencia | Puertos como interfaces, dobles en memoria sólo en tests | JPA, H2 y los índices: son el change `C-09`, de otro integrante |
| Capa HTTP | Ninguna | La API REST es `C-10`. La consigna admite un módulo de dominio sin interfaz |
| Escenarios | Los 9 casos de `RN-AG-04` como escenarios formales | Resumirlos en uno: cada posición relativa merece su test |

La primera versión de estas respuestas fue la contraria —incluir JPA y REST— y
**se revirtió al calcular su costo**: habría dejado sin alcance propio a dos
compañeros y habría alargado el bloqueo de los otros cuatro.

## 6. Qué entra y qué no

**Entra**: scaffolding con Gradle y wrapper; el modelo de dominio con sus
estados y transiciones; `Intervalo` con la regla de solapamiento; el
`ServicioDisponibilidad`; el caso de uso `AgendarTurno`; los ocho puertos, que
quedan congelados; y la suite de tests.

**No entra**: persistencia real, API REST, reprogramar, cancelar, sobreturno,
ausencia, búsqueda de huecos libres, reserva online, notificación real.

## 7. Riesgo detectado en esta fase

`JAVA_HOME` de la máquina apunta a un **JDK 26** que Spring Boot 3 puede no
soportar, y el proyecto apunta a Java 17. Se arrastra de otros trabajos de la
misma materia. Mitigación propuesta: declarar el toolchain de Java 17 en
`build.gradle`, **sin versionar rutas absolutas**, porque el repositorio lo
comparten cinco máquinas distintas.

## 8. Cierre

El problema quedó entendido, el alcance cerrado y los riesgos nombrados.
Habilita el Propose.
