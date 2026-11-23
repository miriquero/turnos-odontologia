# Checklist de entrega

Estado al **2026-11-19**. Ordenado por los criterios de evaluación de la
consigna, con el puntaje de cada uno.

---

## Informe de Discovery — 25 puntos

| Requisito | Estado |
|---|---|
| Secciones A, B, C y D completas | **Listo** |
| 15 sistemas como mínimo | **Listo** — 19 filas, 18 sistemas distintos |
| Fuentes con URL y fecha de consulta | **Listo** — 71 URLs, todas con fecha |
| Uso correcto de "No evidenciado" | **Listo** — 60 celdas |
| Distinción comprobado / afirmación comercial | **Listo** — marcado `(C)` y `(AC)` |
| Criterio crítico en C y D | **Listo** |
| Resumen ejecutivo de máximo una página | **Listo** |
| Nota metodológica en la matriz B | **Listo** |
| **Versión en PDF** | **Listo** — 26 páginas, A4 apaisado |

### Cómo se generó el PDF

Sin pandoc instalado, se usó la cadena markdown → HTML → PDF con herramientas ya
presentes en la máquina:

```bash
npx -y marked --gfm -i docs/discovery/informe-discovery.md -o informe.html
# (se le antepone una hoja de estilos de impresión: A4 apaisado, tablas ajustadas)
chrome --headless --no-pdf-header-footer --print-to-pdf=informe.pdf file:///ruta/informe.html
```

Resultado: `docs/discovery/informe-discovery.pdf`, 26 páginas. Se eligió **A4
apaisado** porque la tabla comparativa A tiene 8 columnas y en vertical se corta.

Si hay que regenerarlo tras editar el informe, se repite la misma cadena.

---

## Verificación de fuentes — 10 puntos

**Completo.** Las cinco fuentes comprobadas a mano el 2026-11-19 y firmadas por
Martín Ignacio Riquero.

| Fuente | Tipo | Resultado |
|---|---|---|
| dentia.tech | Sitio oficial | Se sostiene sin cambios |
| agendapro.com/ar/planes | Precios | **Error**: se citaba la vertical de belleza, no la de salud. Factor 2,5 |
| Centro de ayuda de Dentalink | Centro de ayuda | **Error**: la funcionalidad se apoyaba en un artículo vacío |
| Capterra (Dentalink) | Reseñas | **Precisión**: el precio es por usuario y por mes, no por plan |
| doctolib.fr | Sitio bloqueado | **Error**: el 403 era anti-bot. Puntaje de 0,35 a 1,00 y cambia el orden |

Cuatro de las cinco obligaron a corregir el informe.

---

## Fundación del proyecto — 20 puntos

| Requisito | Estado |
|---|---|
| Knowledge-base coherente con el Discovery | **Listo** — 10 archivos canónicos |
| Roadmap alineado con el MVP de la sección D | **Listo** — `CHANGES.md`, 18 changes |
| `AGENTS.md` y `CLAUDE.md` con reglas revisadas | **Listo** — 4 reglas confirmadas una por una |
| Registro de skills generado | **Listo** — `.atl/skill-registry.md`, 31 skills |
| `.active-orchestrator-state.json` | **Listo** — las 8 fases registradas |

Nada pendiente.

---

## Change OPSX — 25 puntos

| Requisito | Estado |
|---|---|
| Elección justificada por escrito | **Listo** — en el `proposal.md` |
| Spec con 3 escenarios verificables como mínimo | **Listo** — 52 escenarios |
| Implementación fiel al spec | **Listo** |
| Un test por escenario, todos en verde | **Listo** — suite completa: 207 tests, 0 fallos |
| Change archivado | **Listo** — `2026-11-19-crear-turno-sin-solapamientos` |
| Specs sincronizados | **Listo** — 4 capacidades vigentes |

Nada pendiente para `C-01`. Los changes de los compañeros son trabajo adicional
del grupo, no un requisito de este criterio. Al 2026-11-23 están mergeados
`C-02` (reprogramar, Julián), `C-04` (sobreturno, Maximiliano) y `C-05`
(ausencia/atención, Emiliano); `C-03` (cancelar, Augusto) sigue pendiente.


---

## Video de defensa — 10 puntos

**FALTA.** De 8 a 12 minutos. Todos aparecen y hablan, con cámara encendida al
menos en la presentación de cada uno.

Orden sugerido por la consigna:

1. Qué producto eligieron y qué concluyó el Discovery, con el MVP recomendado.
2. Recorrido por el repositorio: knowledge-base, roadmap y `AGENTS.md`.
3. El change elegido, **mostrando el momento real en que revisaron Propose y qué
   corrigieron con Ajustar**.
4. Apply y Verify: los tests pasando.
5. Archive y estado final.
6. Consulta a Engram.
7. Conclusiones.

Se muestra la pantalla trabajando de verdad, no solo diapositivas.

### Momentos fuertes para mostrar

- **El escenario de borde**: un turno que empieza justo cuando termina otro se
  acepta. Es la regla que define el producto.
- **Los dos errores del Discovery**: el precio de AgendaPro equivocado por un
  factor de 2,5 y el artículo vacío de Dentalink. Responden solos la primera
  pregunta de la reflexión.
- **El ajuste del notificador**: una firma que habría hecho que cuatro personas
  editaran el mismo archivo congelado.
- **La recuperación de Engram**: la memoria del 2026-11-18, de otra sesión, que
  el agente trajo por su cuenta. Comandos en `docs/evidencia/etapa-7-engram.md`.

---

## Reflexión escrita — 5 puntos

**FALTA.** De 1 a 2 páginas, **redactada por ustedes, no por el agente**.
Responde:

1. ¿Qué información del Discovery resultó incorrecta, no verificable o inventada
   por el agente? ¿Cómo lo detectaron?
2. ¿En qué etapa usaron Ajustar, qué estaba mal y qué habría pasado si hubieran
   apretado Continuar?
3. ¿Qué diferencia notaron entre implementar con una especificación aprobada y
   pedir código con un prompt suelto?
4. ¿Qué parte de la fundación aportó más valor al ciclo OPSX y cuál menos?
5. Qué hizo cada integrante y en qué etapa.

Material disponible para responderlas, todo registrado en el repositorio: los
dos errores de fuentes, el recorte del alcance de `C-01`, las contradicciones
encontradas en la knowledge-base y el ajuste del puerto de notificación.

---

## Trazabilidad y participación — 5 puntos

| Requisito | Estado |
|---|---|
| README reproducible | **Listo** — clonar y `./gradlew test` |
| Sin secretos en el repositorio | **Listo** — verificado |
| Historial que muestra el proceso | **Listo** — 11 commits, etapa por etapa |
| **Commits propios de cada integrante** | **EN RIESGO** — los 11 son de `miriquero` |

Este es el punto más frágil de la entrega. La consigna dice que **cada persona
tiene que tener sus propios commits**. Hoy todos son del líder.

Se resuelve cuando los cuatro suban su change. Para eso:

1. **Augusto y Emi todavía no aceptaron la invitación** al repositorio.
2. Cada uno tiene que configurar su identidad de git antes del primer commit:
   ```bash
   git config --global user.name "Su Nombre"
   git config --global user.email "su-email-de-github@ejemplo.com"
   ```
3. Las fases Explore y Propose **no necesitan esperar nada**: ya pueden hacerlas.

La guía completa está en `docs/GUIA-INTEGRANTES.md`.

---

## Sobre la estructura del repositorio

La consigna dibuja carpetas `src/` y `tests/` separadas. Este proyecto usa
`src/main/java` y `src/test/java`, que es la convención estándar de Gradle y
Maven. La consigna aclara que los nombres pueden variar según la herramienta
mientras estén todos los elementos; los 12 archivos de test viven en
`src/test/java`.

---

## La interfaz: qué hay y qué no

El **MVP no tiene interfaz gráfica**, y es una decisión documentada del
Discovery, no una omisión. La consigna del Global lo admite expresamente:

> La implementación puede ser una API o un módulo con su lógica de dominio,
> **no hace falta interfaz gráfica**.

Y sobre el video pide mostrar "Apply/Verify: **los tests pasando**", no una
pantalla.

### Pero además hay una demo web

Fuera del alcance del MVP, el repositorio incluye un **adaptador de
demostración**: una página que deja ver y mostrar las reglas de agenda
funcionando contra el mismo núcleo que prueban los tests.

```bash
./gradlew demo
```

Y se abre en <http://localhost:8080>.

Qué se ve:

- La agenda del día siguiente, ya sembrada con **dos turnos contiguos**
  (`[10:00, 10:30)` y `[10:30, 11:00)`) — la regla que define el producto,
  visible de entrada.
- Un botón **"Probar turno pegado"** que carga las 11:00: se acepta, porque los
  intervalos son semiabiertos (`RN-AG-04`).
- Un botón **"Probar solapamiento"** que carga las 10:15: se rechaza con
  `RN-AG-02`, y ni la agenda ni la auditoría cambian.
- La auditoría append-only y los avisos simulados al paciente, en vivo.

### Por qué esto no contradice la decisión del Discovery

Porque no es alcance del MVP: es un **adaptador**, igual que lo sería una API
REST o una base de datos. El núcleo no se enteró de que existe.

- **No se tocó ni una línea** de `dominio` ni de `aplicacion` para construirla.
  Son todos archivos nuevos bajo `infraestructura/`.
- **No agrega ni una dependencia** al build: usa el servidor HTTP que ya trae el
  JDK (`com.sun.net.httpserver`), no Spring Web. Eso es obligado: el test de
  arquitectura `ReglaDeOroTest` falla si cualquier fuente de producción nombra
  el framework, y ese test es la prueba de la primera regla dura del proyecto.
- **Todos los datos son ficticios** (`RN-PA-04`): ningún nombre ni documento
  corresponde a una persona real.
- El almacenamiento es en memoria y se pierde al cortar el proceso. La
  persistencia real es `C-09`, que sigue pendiente.

Es, de paso, la mejor evidencia de que la arquitectura hexagonal valió la pena:
agregar una pantalla fue enchufar un adaptador a los puertos que ya existían.

### El API REST

Sigue planificada como `C-10 api-rest-turnos`, marcada **opcional**: depende de
`C-09` (persistencia) y se encara sólo si sobra tiempo.

---

## Cómo correr los tests

```bash
git clone https://github.com/miriquero/turnos-odontologia
cd turnos-odontologia
./gradlew test
```

**Único requisito**: tener un **JDK 17** instalado. `build.gradle` fija
`toolchain { languageVersion = JavaLanguageVersion.of(17) }`, así que la
compilación usa Java 17 aunque el JDK por defecto del sistema sea otro.

El archivo `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=17`) hace
además que **Gradle mismo** se ejecute sobre Java 17. Sin ese archivo, en una
máquina cuyo JDK por defecto sea 25 o 26 el build falla antes de compilar nada:

```
BUG! exception in phase 'semantic analysis' in source unit '_BuildScript_'
Unsupported class file major version 70
```

El motivo es que el wrapper trae Gradle 8.10.2, que soporta ejecutarse hasta
Java 24 (recién Gradle 9.4 corre sobre Java 26, y migrar a Gradle 9 obligaría a
subir también el plugin de Spring Boot). El criterio de daemon resuelve el
problema **sin rutas absolutas**: Gradle elige un JDK 17 entre los que detecta
solo, incluidos los de `~/.jdks`.

Si Gradle no encuentra ningún JDK 17 instalado, se le indica la ruta solo para
esa corrida:

```powershell
$env:JAVA_HOME = "<ruta-al-jdk-17>"; .\gradlew.bat test
```

Verificado el 2026-11-21 en una máquina con JDK 26 por defecto:
`./gradlew clean test` → **207 tests, 0 fallos**.

## Resumen

| Criterio | Puntos | Estado |
|---|---|---|
| Informe de Discovery | 25 | **Completo** |
| Verificación de fuentes | 10 | **Completo** — 5 de 5 |
| Fundación del proyecto | 20 | **Completo** |
| Change OPSX | 25 | **Completo** |
| Video de defensa | 10 | Sin empezar |
| Reflexión escrita | 5 | Sin empezar |
| Trazabilidad y participación | 5 | En riesgo: falta que commiteen los otros cuatro |

**Lo que depende solo de vos**: nada del informe. Está cerrado.
**Lo que depende del equipo**: sus changes, la reflexión y el video.
