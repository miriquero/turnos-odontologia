# Reflexión escrita

Trabajo Global, Desarrollo de Software, Comisión 3K10. Proyecto `turnos-odontologia`.
Repositorio: https://github.com/miriquero/turnos-odontologia

Martín Ignacio Riquero (51060), Julián Fernández Drago (48493), Emiliano Echavarria (41517),
Maximiliano Di Natale (48764), Augusto Quiroga (51047).

## 1. Qué resultó incorrecto en el Discovery y cómo lo detectamos

De las cinco fuentes que abrimos a mano, cuatro estaban mal. En AgendaPro el agente había citado
el plan de la vertical de belleza y no el de salud, así que el precio estaba errado por 2,5
veces. En el centro de ayuda de Dentalink la funcionalidad que el informe daba por documentada
se apoyaba en un artículo vacío. En Capterra el precio era por usuario y no por plan. Y el 403
de Doctolib no era un sitio caído sino protección anti-bot.

Doctolib fue el que más nos hizo pensar. Lo teníamos último de la matriz con 0,35. Lo leímos a
mano, subió a 1,00 y cambió el orden del ranking. Ahí nos dimos cuenta de que el puntaje no
estaba midiendo qué tan bueno era el producto, sino cuánta información había podido leer el
agente.

Pero lo que más nos sorprendió fue otra cosa: ninguno de los cuatro errores se notaba leyendo el
informe. Los cuatro estaban bien citados, con URL y fecha, y puestos como dato publicado. Salían
únicamente al abrir la fuente. Es un buen punto para tener en cuenta la próxima vez que se use
alguna metodología parecida.

## 2. Dónde usamos Ajustar y qué habría pasado con Continuar

En el Propose de C-01, sobre el puerto `NotificadorPaciente`. El agente había propuesto un
método `notificarTurnoAgendado(Turno)`. Visto solo no tenía nada de raro. El problema aparecía
al compararlo con el roadmap: los cuatro changes que venían después se iban a hacer en paralelo,
y los cuatro iban a terminar editando ese mismo archivo. Lo cambiamos por un método genérico,
`notificar(Turno, AccionAuditoria)`, y el puerto quedó congelado.

Con Continuar nos quedaban cuatro conflictos de merge sobre el archivo más delicado del
proyecto, todos juntos y todos para la misma persona. Lo que más nos quedó de esto es que el
error no lo encontramos leyendo código, porque todavía no había código escrito. Lo encontramos
leyendo el spec contra el roadmap.

## 3. Especificación aprobada vs. prompt suelto

El spec de C-01 terminó con 52 escenarios antes de escribir una línea de código, y cada escenario
después tuvo su test. La suite quedó en 223 tests en verde.

Con un prompt suelto, que algo esté listo depende de que a vos te parezca. Con un spec aprobado
no: el escenario ya está escrito de antes y el test pasa o no pasa. Donde más se notó fue
trabajando en grupo, porque los cuatro changes en paralelo se hicieron sin que ninguno tuviera
que leer el código de los otros, y al integrarlos no hubo ni un conflicto.

El ejemplo más claro es RN-AG-04: los intervalos son semiabiertos, así que un turno que arranca
justo cuando termina otro es válido. Es un error de un minuto y es de los más fáciles de
programar mal. Pedido por prompt, lo más probable es que salga mal y que nadie se dé cuenta.

Fue difícil la verificación de estos puntos, pero está buenísimo el uso que se le puede dar y la
velocidad con la que se puede programar esa idea que cada uno tiene. Si lo complementamos con lo
que venimos aprendiendo durante el cursado, el resultado es bastante potente.

## 4. Qué parte de la fundación aportó más y qué menos

La que más sirvió fue el roadmap, no por la lista de changes sino por las dependencias. Es lo que
nos dejó ver que C-01 era el único crítico y que el resto eran independientes. Sin eso no
repartíamos cuatro changes en paralelo en una semana. Y fue también lo que nos permitió detectar
el problema del punto anterior.

La que menos, la knowledge-base. De los diez archivos usamos todo el tiempo uno solo, el de las
reglas de negocio. Los otros nueve los leímos al principio y casi no volvimos a abrirlos.

De las reglas del agente hubo una que sí valió la pena: no importar Spring ni JPA desde
`dominio`. La dejamos como test de arquitectura, así que no es una recomendación que se olvida
sino un test que se pone rojo. De hecho se puso rojo una vez y nos hizo corregir.

## 5. Qué hizo cada integrante y en qué etapa

| Integrante | Etapa y change | Qué hizo |
|---|---|---|
| Martín Riquero | 0 a 5 y 7, C-01 | Active Stack y `openspec init`, condujo el Discovery y verificó a mano las cinco fuentes, knowledge-base, roadmap y reglas del proyecto. Especificó e implementó `crear-turno-sin-solapamientos` (52 escenarios). Revisó y mergeó los cuatro pull requests. Evidencia de Engram |
| Julián Fernández Drago | 6, C-02 | Especificó e implementó `reprogramar-turno`, con el registro del detalle de la reprogramación |
| Augusto Quiroga | 6, C-03 | Especificó e implementó `cancelar-turno-con-anticipacion-minima` |
| Maximiliano Di Natale | 6, C-04 | Especificó, implementó y archivó `agendar-sobreturno-autorizado` |
| Emiliano Echavarria | 6, C-05 | Especificó e implementó `registrar-ausencia-y-atencion-de-turno` |

Cada uno trabajó en su rama y abrió pull request a `master`. Los cinco changes quedaron
implementados, probados y archivados. La consigna pedía uno y hicimos cinco, uno por integrante,
para que se viera en los commits lo que hizo cada uno. Las pantallas web que se ven en el video
no son parte del MVP: el Discovery había decidido que la v1 no llevaba interfaz gráfica, y la
persistencia quedó fuera de alcance como C-09.

## Declaración de uso de IA

Usamos Claude Code, Antigravity y Codex como agentes de trabajo durante todo el proyecto. Con
ellos hicimos el Discovery, los borradores de la knowledge-base y del roadmap, y nos asistieron
en la especificación y la implementación de los cinco changes.

Lo que validamos nosotros: las cinco fuentes del Discovery, leídas a mano, con las cuatro
correcciones del punto 1; el spec de cada change antes de aprobarlo; y la suite de 223 tests,
que cualquiera puede reproducir clonando el repositorio.
