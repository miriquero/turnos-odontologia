# Preguntas Abiertas

> Lo que quedó sin resolver al cerrar la fundación. Se dejan explícitas para que
> no se resuelvan solas, por omisión, en medio de la implementación. Los
> supuestos provisorios que las tapan están en `09_decisiones_y_supuestos.md`.

## Preguntas de dominio

### PA-01 — ¿Maven o Gradle?

El stack es Java con Spring Boot (`D-07`), pero la herramienta de build no se
eligió. **Hay que decidirlo antes del primer change**: cambiarlo después obliga
a todo el equipo a rehacer su entorno.
**Impacto**: alto y temprano. **Bloquea**: el change base.

### PA-02 — ¿Cuánto dura realmente cada prestación?

El catálogo de duraciones —limpieza 30 minutos, conducto 90— es un supuesto sin
validar (`SU-02`). Ningún odontólogo lo revisó.
**Cómo se resuelve**: preguntándole a un profesional.
**Mitigación aplicada**: las duraciones son dato configurable, no constantes en
el código, así que corregirlas no toca la lógica.

### PA-03 — ¿Quién puede autorizar un sobreturno, y queda auditado?

`RN-AG-06` exige autorización explícita, pero no está definido qué roles la
tienen. El supuesto provisorio es que solo el odontólogo dueño (`SU-04`), que es
la opción más restrictiva.
**Pregunta asociada**: ¿alcanza con registrar quién, cuándo y por qué, o hace
falta además una aprobación de un segundo rol?

### PA-04 — ¿Cuántas horas de anticipación mínima para cancelar?

`RN-AG-05` define la regla pero no el valor de `N`. El Discovery no lo fijó.
**Mitigación aplicada**: `N` es configurable y su valor por defecto está
declarado como supuesto (`SU-03`). La regla es testeable con cualquier `N`.

### PA-05 — ¿Qué pasa con el horario liberado tras una cancelación?

Hoy el hueco simplemente vuelve a estar disponible. La alternativa —ofrecerlo
automáticamente a una lista de espera— es del bloque diferenciador y nadie la
decidió.
**Dato del relevamiento**: la lista de espera **no aparece en ningún sistema
argentino ni latinoamericano** relevado. Solo Open Dental la documenta, como
complemento pago.

### PA-06 — ¿El paciente puede cancelar o reprogramar por su cuenta?

En la v1 solo opera la recepción. Si el paciente pudiera hacerlo, cambian la
matriz de permisos, la identificación sin cuenta y la regla de anticipación
mínima.
**Relacionado con**: `F-07`, la reserva online, que está fuera de la v1.

### PA-07 — ¿Cómo se resuelve la concurrencia en el alta?

Dos altas simultáneas sobre la misma franja pueden pasar ambas la verificación
antes de que cualquiera escriba (`R-06`). No está resuelto: falta decidir entre
restricción de exclusión en la base o bloqueo pesimista sobre el recurso.

## Preguntas que el Discovery dejó como "No evidenciado"

Estas no son dudas del equipo: son **vacíos del relevamiento de mercado**. Se
listan porque la consigna pide que las preguntas abiertas recojan lo que quedó
sin evidencia pública.

### PA-08 — ¿Cómo resuelven los competidores los sillones, los bloqueos y los solapamientos?

Es el vacío central y la justificación del producto. Las cuatro capacidades
decisivas —sillón como recurso agendable, bloqueos, sobreturnos y prevención de
solapamientos— figuran como "No evidenciado" en casi todas las fichas de los
tres mercados. El sillón aparece en solo 4 de 19 fichas y en **ninguna de origen
argentino**. Lo más explícito que se encontró son dos frases comerciales, nunca
una regla técnica.
**Cómo se resuelve**: con las demos de los cinco competidores prioritarios de la
sección D del informe. Es la pregunta que esas demos tienen que responder.

### PA-09 — ¿Qué exige concretamente la Ley 25.326 para datos de salud?

Solo dos de los siete productos argentinos declaran conformidad, y lo hacen como
afirmación comercial, sin certificación. El equipo no investigó el texto legal.
**Impacto**: afecta el modelo de auditoría, la retención y la exportación de
datos. No bloquea la v1, que usa solo datos ficticios (`RN-PA-04`).

### PA-10 — Las dos fuentes que no se pudieron verificar

Capterra y Doctolib siguen devolviendo 403 con verificación anti-bot. De la
primera depende el único precio atribuido a Dentalink, un producto que no los
publica; la segunda es la razón de que Doctolib figure último en la matriz con
casi todas sus celdas vacías, un puntaje que refleja la imposibilidad de leer el
sitio y no el producto.
**Cómo se resuelve**: abriéndolas a mano desde un navegador común. Las filas
correspondientes de la tabla de verificación del informe están pendientes.

### PA-11 — ¿Se sostiene el contenido del centro de ayuda de Dentalink?

La verificación manual encontró que el artículo que respaldaba la receta
electrónica argentina **está vacío**. Los otros tres artículos del módulo
argentino —obras sociales, recaudación, conciliaciones— tampoco se leyeron por
dentro, así que están en la misma situación de duda.

## Qué hacer con esta lista

1. `PA-01` se decide **antes** del primer change: bloquea a todo el equipo.
2. `PA-02`, `PA-03` y `PA-04` tienen supuesto provisorio y no bloquean, pero
   cada una debería confirmarse antes de dar el MVP por terminado.
3. `PA-05`, `PA-06` y `PA-07` condicionan el bloque diferenciador.
4. `PA-08` a `PA-11` se resuelven con trabajo de verificación, no de código, y
   alimentan la reflexión escrita del trabajo.
