# Visión y Objetivos

> Fuente: `docs/discovery/discovery.md` (11 puntos confirmados) y
> `docs/discovery/informe-discovery.md` (secciones A–D, con el MVP de D.3).

## Propósito del sistema

Un **motor de disponibilidad de agenda** para consultorios odontológicos
argentinos: el turno se valida contra la ocupación del profesional **y** del
sillón, con reglas de solapamiento explícitas, verificables y probadas con
tests.

El Discovery mostró que un consultorio pierde ingresos y horas de sillón por
tres problemas encadenados: **turnos superpuestos** (el mismo profesional o el
mismo sillón agendado dos veces), **ausentismo** (el paciente no se presenta y
no avisa, y esa hora ya no se recupera) y **reserva cautiva del teléfono** (el
paciente solo puede pedir turno llamando en horario de atención). El
relevamiento de 19 fichas de producto en tres mercados confirmó que el primero
de los tres está **mal cubierto por todo el mercado**: la prevención de
solapamientos no está documentada por ningún sistema relevado, y el sillón como
recurso agendable aparece en solo 4 de 19 fichas y en **ninguna de origen
argentino** (ver `04_modelo_de_datos.md` y `09_decisiones_y_supuestos.md`).

Por eso el producto no se plantea como "un calendario donde se escriben cosas"
—que es como el mercado trata la agenda— sino como lógica de dominio pura, sin
dependencias externas, donde la restricción física real del consultorio (dos o
tres sillones, uno o dos profesionales que se alternan) está modelada.

## Objetivos por actor

| Actor | Objetivo principal | Objetivos secundarios |
|---|---|---|
| **Odontólogo dueño** | Que su agenda nunca tenga dos turnos pisados en el mismo sillón ni en su propio horario | Administrar prestaciones y duraciones; autorizar sobreturnos; ver la ocupación por sillón |
| **Odontólogo colega** | Ver y atender su propia agenda sobre sillones compartidos | Que el sistema no le asigne un sillón ya ocupado por el dueño |
| **Recepción / secretaría** | Agendar, reprogramar y cancelar turnos de terceros sin generar superposiciones | Registrar motivo de cancelación y de ausencia; consultar huecos libres para una prestación dada |
| **Paciente** | Tener un turno confiable y recordado | *(v1: sin autogestión — ver "Fuera de alcance")* Reservar online 24/7 (bloque diferenciador) |

El usuario objetivo elegido es el **odontólogo independiente con dos o tres
sillones** que a veces trabaja con un colega. El modelo de dominio se escribe
igualmente **multi-profesional y multi-sillón**, porque es la condición para que
la regla de solapamiento tenga sentido real (ver `SU-01` en
`09_decisiones_y_supuestos.md`).

## Alcance v1 (el MVP de la sección D.3 del informe)

El criterio de corte es doble: cada ítem debe ser **lógica de dominio
verificable con tests automatizados y sin dependencias externas**, y debe atacar
el vacío que el relevamiento dejó al descubierto.

1. **Modelo de dominio multi-profesional y multi-sillón**: profesional, sillón,
   paciente con ficha básica, prestación con su duración, y turno.
2. **Alta de turno con validación de disponibilidad**, con la regla aislada en un
   único servicio reutilizable (`RN-AG-02` a `RN-AG-04`).
3. **Duración derivada de la prestación** (`RN-AG-01`), con catálogo configurable
   y declarado como supuesto no validado.
4. **Reprogramación de turno**, revalidando disponibilidad y respetando que un
   turno cancelado o atendido no se reprograma (`RN-TU-04`).
5. **Cancelación con liberación del horario**, anticipación mínima configurable
   y motivo registrado (`RN-AG-05`).
6. **Sobreturno como excepción autorizada y auditada** (`RN-AG-06`).
7. **Registro de ausencia del paciente** como estado del turno, distinto de la
   cancelación.
8. **Consulta de agenda y de disponibilidad**: por profesional y por sillón en un
   rango, y huecos libres para una prestación dada.
9. **Puerto de notificación al paciente con adaptador simulado** — ningún
   servicio externo real.
10. **Auditoría mínima**: quién creó, modificó, reprogramó o canceló cada turno,
    y cuándo.
11. **Suite de tests automatizados** sobre la regla de disponibilidad y las
    transiciones de estado, con datos ficticios exclusivamente.

## Bloque diferenciador (si el MVP queda firme y hay margen)

Reserva online del paciente por enlace sin cuenta previa; lista de espera sobre
huecos liberados; registro estructurado de motivos de cancelación y ausencia con
reporte de ausentismo; bloqueos de agenda como entidad que participa de la regla
de disponibilidad; exportación completa de datos; roles diferenciados con
permisos efectivos; y —solo si todo lo anterior está cerrado— odontograma y
ficha clínica extendida.

## Fuera de alcance (v1, explícitamente)

- **Odontograma y planes de tratamiento.** Es estándar de mercado (13 de 19
  fichas) pero es el componente más costoso de construir bien, y hacerlo a
  medias no aporta nada.
- **WhatsApp Business API** real: requiere cuenta de negocio, claves, aprobación
  de plantillas y costo por mensaje. En la v1 solo existe el puerto y el
  adaptador simulado.
- **Mercado Pago** para señas y cobros online.
- **Facturación ARCA** (facturas A, B y C, notas de crédito y débito).
- **Google Calendar** u otra sincronización de calendario externo.
- **Obras sociales y prepagas** con nomenclador, cobertura y liquidación: es un
  dominio propio de complejidad comparable a la agenda entera.
- **Caja, cuenta corriente del paciente y planes de cuotas.**
- **Multi-sede** con agenda y caja separadas.
- **Reportes avanzados y métricas de gestión** más allá del reporte de
  ausentismo del bloque diferenciador.
- **Cualquier funcionalidad de inteligencia artificial.** Es la función más
  publicitada del relevamiento y la menos sostenida por evidencia: no hay un solo
  caso con precisión, metodología o validación demostrada.
- **Portal con historia clínica visible para el paciente**, periodontograma,
  consentimientos, firma digital, imágenes y radiografías.
- **Interfaz gráfica**: no es obligatoria. La implementación puede ser una API
  REST o un módulo de dominio.

## Métricas de éxito

| Métrica | Objetivo v1 |
|---|---|
| Casos borde de solapamiento cubiertos por tests | 100% de los enumerados en `RN-AG-02`…`RN-AG-04`, incluido el borde `fin == inicio` |
| Turnos superpuestos posibles sin autorización explícita | 0 |
| Dependencias externas necesarias para correr la suite de tests | 0 (sin credenciales, sin red) |
| Datos reales de pacientes en el repositorio | 0 — restricción dura (ver `08_arquitectura_propuesta.md`) |
| Operaciones de turno sin registro de auditoría | 0 |
