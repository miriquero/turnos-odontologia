# Guía para integrantes — turnos-odontologia

Esta guía asume que **no sabés nada del proyecto todavía**. Seguila en orden.
Si algo no funciona, está la sección "Problemas comunes" al final.

Tiempo estimado: unas 2 horas hasta tener tu especificación lista, sin contar
la implementación.

---

## 0. Qué estamos haciendo, en 5 líneas

Construimos un sistema de **turnos para consultorios odontológicos**. En el
fondo es un validador de agenda: cuando alguien quiere agendar un turno, el
sistema tiene que decidir si esa franja está libre para ese profesional **y**
para ese sillón.

El trabajo no se programa "a ojo". Primero se escribe una **especificación**
(qué tiene que pasar, en castellano), se aprueba, y recién después se escribe el
código que la cumple. Cada escenario de la especificación termina siendo un
test automatizado.

Vos vas a llevar **un change** (una funcionalidad) de principio a fin.

---

## 1. Aceptá la invitación al repositorio

Revisá tu mail de GitHub. Tenés una invitación a `miriquero/turnos-odontologia`.
**Sin aceptarla no vas a poder subir tu trabajo.** Si no te llegó, entrá a
https://github.com/miriquero/turnos-odontologia y pedísela a Martín.

---

## 2. Instalá las herramientas

Necesitás cuatro cosas. Verificá cada una con su comando; si responde con una
versión, está bien.

### 2.1 Git

```bash
git --version
```

Si no lo tenés: https://git-scm.com/downloads

Configurá tu identidad (esto es **importante**: de acá sale que los commits
figuren como tuyos, y eso se evalúa):

```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu-email-de-github@ejemplo.com"
```

### 2.2 Java 17

```bash
java -version
```

El proyecto usa **Java 17**. Si tenés otra versión instalada no pasa nada
todavía: el proyecto trae un "toolchain" que le dice a Gradle qué Java usar. Pero
necesitás tener un Java 17 instalado en algún lado. Si no lo tenés, bajá
**Temurin 17 LTS** de https://adoptium.net

### 2.3 Node

```bash
node --version
```

Si no lo tenés: https://nodejs.org (versión LTS).

### 2.4 Un agente de código

Claude Code u OpenCode, el que prefieras. Tiene que estar funcionando y poder
abrir una carpeta.

### 2.5 Active Stack en modo Full

Esto instala las skills de OpenSpec que vas a usar.

```bash
git clone https://github.com/Group-Active-IA/active-stack
cd active-stack
```

En Windows ejecutá `build.bat`; en Linux o Mac, `build.sh`. Después corré el
instalador y **elegí el modo Full**. Cuando pregunte el agente, elegí el que
usás.

**Sacá una captura del instalador terminado**: es evidencia del trabajo.

---

## 3. Cloná el repositorio

```bash
git clone https://github.com/miriquero/turnos-odontologia
cd turnos-odontologia
```

Abrí esa carpeta con tu agente de código. **Siempre trabajá desde adentro de
esa carpeta**, no desde la carpeta de arriba.

---

## 4. Entendé el proyecto (30 minutos de lectura)

No empieces a escribir sin leer esto. En este orden:

| Orden | Archivo | Por qué |
|---|---|---|
| 1 | `AGENTS.md` | Resumen del proyecto y las 4 reglas que no se rompen |
| 2 | `knowledge-base/01_vision_y_objetivos.md` | Qué construimos y qué queda afuera |
| 3 | `knowledge-base/05_reglas_de_negocio.md` | **El más importante.** Todas las reglas, con su código `RN-*` |
| 4 | `knowledge-base/04_modelo_de_datos.md` | Las entidades: Turno, Profesional, Sillon, Prestacion |
| 5 | `CHANGES.md` | El plan completo. Buscá **tu** change |

### La regla que tenés que entender sí o sí

Se llama `RN-AG-04` y dice que los intervalos son **semiabiertos**: un turno
ocupa desde su inicio inclusive hasta su fin exclusive, o sea `[inicio, fin)`.

En criollo:

```
   Turno A: 10:00 a 10:30
   Turno B: 10:30 a 11:00     --> SE PUEDE. No se pisan.

   Turno A: 10:00 a 10:30
   Turno B: 10:15 a 10:45     --> NO SE PUEDE. Se pisan 15 minutos.
```

El turno que empieza justo cuando termina el otro **es válido**. Parece un
detalle y es la decisión central del sistema.

La fórmula que lo decide:

```
   se solapan  si y solo si   A.inicio < B.fin  Y  B.inicio < A.fin
```

Con `<` estricto, nunca `<=`.

---

## 5. Tu change

Buscá tu nombre. Cada uno lleva **un** change, y los cuatro son independientes
entre sí: nadie depende de nadie, salvo del change base de Martín.

### Julián Fernández Drago — `C-02 reprogramar-turno`

- **Rama**: `feat/reprogramar-turno`
- **Qué hace**: mover un turno a otro horario, y opcionalmente a otro
  profesional, sillón o prestación. La franja anterior queda libre.
- **Lo difícil**: al revalidar hay que **excluir el propio turno** de la
  comparación (`excluirTurnoId`), porque si no, el turno se solapa consigo mismo
  y nunca se podría mover.
- **Leé antes**: `05_reglas_de_negocio.md` → `RN-TU-03`, `RN-TU-04`,
  `RN-AG-01`, `RN-AU-03`. Y `07_flujos_principales.md` → `F-02`.
- **Tu caso de borde**: reprogramar un turno **al mismo horario que ya tenía**
  tiene que funcionar, sin error.

### Augusto Quiroga — `C-03 cancelar-turno-con-anticipacion-minima`

- **Rama**: `feat/cancelar-turno-con-anticipacion-minima`
- **Qué hace**: cancelar un turno con un motivo, liberando la franja.
- **Lo difícil**: hay una **anticipación mínima** de `N` horas para poder
  cancelar. `N` es configurable, no un número escrito en el código. Y vas a
  necesitar un reloj inyectable (`Clock`) para poder testear sin depender de la
  hora real.
- **Leé antes**: `05_reglas_de_negocio.md` → `RN-AG-05`, `RN-AG-07`,
  `RN-TU-02`, `RN-TU-05`. Y `07_flujos_principales.md` → `F-03`.
- **Tu caso de borde**: cancelar con **exactamente** `N` horas de anticipación
  tiene que aceptarse. El límite es inclusivo.

### Maximiliano Di Natale — `C-04 agendar-sobreturno-autorizado`

- **Rama**: `feat/agendar-sobreturno-autorizado`
- **Qué hace**: permitir agendar **a propósito** sobre una franja ocupada, con
  autorización explícita y dejando registro.
- **Lo difícil**: es la **única excepción** a la regla central del sistema. La
  decisión de saltear la validación vive en tu caso de uso, **nunca** adentro de
  `ServicioDisponibilidad`: ese servicio jamás puede decir que una franja
  ocupada está libre.
- **Leé antes**: `05_reglas_de_negocio.md` → `RN-AG-06`, `RN-AU-01`. Y
  `07_flujos_principales.md` → `F-04`. Y `09_decisiones_y_supuestos.md` →
  `D-05`.
- **Tu caso de borde**: después de crear un sobreturno, el alta normal sobre una
  franja ocupada **tiene que seguir rechazándose**. El sobreturno no cambia el
  comportamiento por defecto.
- **Ojo**: tu change es de gobernanza **ALTA**. Antes de escribir código,
  contale a Martín cómo lo vas a hacer.

### Emiliano Jesus Echavarria — `C-05 registrar-ausencia-y-atencion-de-turno`

- **Rama**: `feat/registrar-ausencia-y-atencion-de-turno`
- **Qué hace**: marcar que el paciente no vino (`AUSENTE`) o que fue atendido
  (`ATENDIDO`).
- **Lo difícil**: son dos estados parecidos con consecuencias opuestas. Un turno
  `AUSENTE` **libera** la franja; un turno `ATENDIDO` **la sigue ocupando**,
  porque es historia clínica. Además, no se puede marcar ausente un turno que
  todavía no empezó.
- **Leé antes**: `05_reglas_de_negocio.md` → `RN-TU-02`, `RN-TU-06`,
  `RN-AG-07`. Y `07_flujos_principales.md` → `F-05`.
- **Tu caso de borde**: tras marcar `AUSENTE`, un alta sobre esa misma franja
  tiene que aceptarse. Tras `ATENDIDO`, no.

---

## 6. Creá tu rama

Desde la carpeta del proyecto:

```bash
git checkout master
git pull
git checkout -b TU-RAMA
```

Reemplazá `TU-RAMA` por la que figura en tu sección. Por ejemplo:

```bash
git checkout -b feat/reprogramar-turno
```

**Nunca trabajes directo sobre `master`.** Esa rama la maneja Martín.

---

## 7. ARRANCÁ YA: Explore y Propose

Estas dos fases **no necesitan nada de código**. Podés hacerlas hoy, mientras
Martín todavía está implementando el change base.

### 7.1 Explore — pensar antes de escribir

En tu agente, dentro de la carpeta del proyecto, escribí:

```
/openspec-explore mi-change
```

Reemplazá `mi-change` por el nombre del tuyo, por ejemplo `reprogramar-turno`.

Esto abre un modo de conversación. **No escribe código ni archivos.** Sirve para
que entiendas el problema antes de comprometerte. Preguntale cosas como:

- "¿Qué reglas de negocio aplican a mi change?"
- "¿Qué pasa si el turno ya está cancelado?"
- "Mostrame un diagrama del flujo"

Cuando sientas que entendiste, pasá al siguiente paso.

### 7.2 Propose — escribir la especificación

```
/openspec-propose mi-change
```

Esto crea cuatro cosas en `openspec/changes/mi-change/`:

| Archivo | Qué es |
|---|---|
| `proposal.md` | Qué vas a hacer y por qué |
| `design.md` | Cómo lo vas a hacer |
| `specs/` | **La especificación**: los escenarios |
| `tasks.md` | La lista de tareas para implementarlo |

**En `proposal.md` tenés que justificar en dos o tres líneas por qué elegiste
este change.** Lo pide la consigna del trabajo.

---

## 8. REVISÁ tu especificación (esto se evalúa)

**No apruebes lo primero que te genere el agente.** La consigna del trabajo dice
textualmente:

> No aprueben a la primera: lean el spec completo y usen Ajustar si el alcance
> quedó de más, de menos o inventado.

O sea que **se evalúa que encuentres algo para corregir**. Abrí el archivo de
`specs/` y revisá:

- [ ] ¿Están los **tres escenarios mínimos**? Uno feliz, uno de error de
      negocio y uno de borde.
- [ ] ¿El escenario de borde dice lo correcto? Mirá el tuyo en la sección 5. Es
      el más fácil de escribir al revés.
- [ ] ¿Hay algo que **no pediste**? Por ejemplo, base de datos, API REST,
      pantallas. Si aparece, es alcance inventado: sacalo.
- [ ] ¿Falta alguna regla de las que te marqué en "Leé antes"?
- [ ] ¿Los escenarios están en formato **dado / cuando / entonces**?
- [ ] ¿Se entienden sin saber Java? Un escenario describe comportamiento, no
      código.

**Anotá qué corregiste.** Lo vas a necesitar para la reflexión escrita y para el
video.

Para corregir, pedíselo al agente en palabras: "sacá el escenario X", "el
escenario de borde está al revés, tiene que aceptarse", "falta la regla
`RN-TU-04`".

Cuando esté bien, verificá que el change es válido:

```bash
openspec validate --changes mi-change --strict
```

Tiene que pasar sin errores.

### Commiteá tu especificación

```bash
git add openspec/
git commit -m "Propose: especificacion de mi-change"
git push -u origin TU-RAMA
```

Avisale a Martín que tu spec está listo para que lo revise.

---

## 9. ESPERÁ el merge del change base

Acá sí tenés que frenar. Para implementar necesitás el código de Martín:
`Intervalo`, `ServicioDisponibilidad`, el modelo de dominio y los puertos.

Cuando Martín te avise que mergeó `C-01`:

```bash
git checkout master
git pull
git checkout TU-RAMA
git merge master
```

Ahora ya tenés el código base en tu rama.

---

## 10. Apply — implementar

```
/openspec-apply-change mi-change
```

Esto implementa las tareas de `tasks.md`, una por una.

### La regla de oro del Apply

> Si algo **no está en la especificación**, no se inventa acá: se vuelve a
> Propose y se actualiza el spec.

Si mientras implementás descubrís que falta un caso, **no lo agregues de una**.
Actualizá primero la especificación y después implementalo. La especificación
aprobada es el contrato.

### Se trabaja con TDD

Primero el test que falla, después el código mínimo que lo hace pasar. En ese
orden. El proyecto tiene el modo TDD estricto activado y el agente te va a
guiar.

### Qué NO podés tocar

Estos archivos los escribió Martín en el change base y **quedan congelados**:

```
Intervalo
ServicioDisponibilidad
EstadoTurno
Los puertos (TurnoRepositorio, RegistroAuditoria, NotificadorPaciente, etc.)
```

**Tu change agrega archivos nuevos**, no modifica los existentes. Si sentís que
necesitás cambiar uno de esos, pará y hablalo con Martín: probablemente haya
otra forma, y si no, es una decisión que afecta a los cinco.

Esto no es burocracia: si cuatro personas editan los mismos archivos, los
merges se vuelven un infierno.

### Los helpers de test son compartidos: no les agregues métodos

Hay un punto donde los cuatro pueden chocar, y no es el código de producción
sino los tests. En `src/test/java/ar/edu/turnos/dobles/` viven estos helpers
compartidos:

```
Escenario              armador de escenarios (vacio(), conSeed(), conTurno(...))
DatosFicticios         catalogo de pacientes, profesionales, sillones, prestaciones
RepositoriosEnMemoria  los dobles de los repositorios
TurnoRepositorioEnMemoria
RegistroAuditoriaEnMemoria
NotificadorEspia
```

Los cuatro los van a usar, y es muy probable que a los cuatro se les ocurra
agregarles un método: uno que cree un turno a punto de vencer, otro con un turno
ya pasado, otro con dos turnos para mover uno sobre el otro.

**Si los cuatro agregan métodos al mismo archivo, hay conflicto de merge.**

La regla, entonces:

- **Usá los helpers como están.** Alcanzan para la mayoría de los casos.
- **Armá tus fixtures propias dentro de tu clase de test**, como métodos privados.
  Por ejemplo, un `private Turno turnoQueVence(int enHoras)` en tu propio archivo.
- Si de verdad necesitás algo en el helper compartido, **avisale a Martín** y lo
  agrega él una sola vez, para todos.

### El núcleo ya tiene lo que necesitás

Antes de pedir un cambio en el código congelado, fijate: lo más probable es que
ya esté. El change base dejó resuelto:

| Lo que necesitás | Ya existe |
|---|---|
| Estados y sus transiciones | `EstadoTurno` con `puedeTransicionarA(...)` |
| Qué estados ocupan la agenda | `EstadoTurno.ocupaAgenda()` — `ATENDIDO` sí, `CANCELADO` y `AUSENTE` no |
| Acciones de auditoría | `AccionAuditoria` con las 6 acciones, incluida la tuya |
| Notificar al paciente | `NotificadorPaciente.notificar(turno, accion)` — genérico a propósito |
| Excluir un turno al revalidar | `FuenteDeOcupacion` ya recibe `excluirTurnoId` |

Si algo de esto te falta, **hablalo antes de tocarlo**: probablemente haya otra
forma, y si no la hay, es una decisión que afecta a los cinco.

---

## 11. Verify — correr los tests

```bash
./gradlew test
```

En Windows, si eso no anda:

```bash
gradlew.bat test
```

**Tiene que estar todo en verde.** La consigna dice que no se archiva un change
con algo en rojo.

Fijate además que haya **un test por cada escenario** de tu especificación. Si
un escenario no tiene test, falta trabajo.

---

## 12. Archive — cerrar el change

```
/openspec-archive-change mi-change
```

Esto sincroniza tu especificación con las specs principales del proyecto y mueve
el change a `openspec/changes/archive/`.

---

## 13. Abrí tu Pull Request

```bash
git add -A
git commit -m "Apply: implementacion de mi-change con tests"
git push
```

Después entrá a https://github.com/miriquero/turnos-odontologia y abrí un Pull
Request de **tu rama hacia master**.

En la descripción poné:

- Qué hace tu change, en dos líneas.
- Qué escenarios cubre.
- Confirmación de que los tests pasan.
- Qué corregiste cuando revisaste tu spec.

Martín lo revisa y lo mergea. Si te pide cambios, los hacés en la misma rama y
el PR se actualiza solo.

---

## 14. Reglas que no se rompen

Están en `AGENTS.md` y se evalúan:

1. **NUNCA datos reales de pacientes.** Ni en tests, ni en ejemplos, ni en
   capturas. Todo inventado. Nada de usar el nombre o el teléfono de alguien que
   conozcas.
2. **NUNCA claves, contraseñas ni tokens** en el repositorio.
3. **NUNCA inventar alcance en el Apply.** Si no está en el spec, se vuelve a
   Propose.
4. **NUNCA archivar con tests en rojo.**
5. **El dominio no importa Spring.** Si para probar una regla de negocio
   necesitás levantar Spring, la regla está en el lugar equivocado.

---

## 15. Problemas comunes

### "Error: class file version 61/70 is not supported" o similar

Tenés un Java distinto del que espera el proyecto. El `build.gradle` declara un
toolchain de Java 17. Verificá qué JDK ve Gradle:

```bash
./gradlew -q javaToolchains
```

Si no aparece ningún Java 17, instalalo de https://adoptium.net

### "gradlew: command not found" o "no se reconoce"

Estás parado en la carpeta equivocada. Tenés que estar **adentro** de
`turnos-odontologia`. En Windows usá `gradlew.bat` en lugar de `./gradlew`.

### "Permission denied" al hacer push

No aceptaste la invitación de GitHub, o no estás logueado. Revisá el mail.

### El agente no reconoce `/openspec-propose`

No instalaste Active Stack en modo Full, o no abriste la carpeta del proyecto.
Las skills de OpenSpec están en `.claude/skills/` del repositorio: si abriste la
carpeta correcta, tendrían que aparecer.

### "Mi rama está desactualizada" al abrir el PR

```bash
git checkout master
git pull
git checkout TU-RAMA
git merge master
```

Resolvé los conflictos si los hay, commiteá y volvé a pushear.

---

## 16. Si algo no te cierra

Preguntale a Martín antes de improvisar, sobre todo si:

- Te parece que necesitás modificar un archivo congelado.
- Tu change parece depender del de otro.
- Encontraste una contradicción en la knowledge-base.
- El agente te propone algo que no entendés.

Es mejor perder diez minutos preguntando que un día deshaciendo.
