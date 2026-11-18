# Discovery — turnos-odontologia

Sistema de gestión de turnos y agenda de pacientes para consultorios
odontológicos del mercado argentino.

- **Fecha del Discovery**: 2026-11-18
- **Método**: investigación de mercado sobre fuentes públicas verificables
  (3 relevamientos paralelos: Argentina, Latinoamérica e internacional) más
  Q&A guiada sobre el checklist de once puntos.
- **Fichas de competidores**: `docs/discovery/sources/`
- **Informe completo con secciones A–D**: `docs/discovery/informe-discovery.md`

> **Advertencia de alcance.** El relevamiento automático leyó las páginas con
> herramientas de extracción de texto. La mayoría de lo relevado es, por lo
> tanto, **afirmación comercial del proveedor**, no funcionalidad comprobada.
> Las celdas sin respaldo público quedaron como "No evidenciado". La
> verificación manual de fuentes se documenta en el informe.

## 1. Problema que resuelve

Un consultorio odontológico pierde ingresos y horas de sillón por tres
problemas encadenados: **turnos superpuestos** (el mismo profesional o el mismo
sillón agendado dos veces), **ausentismo** (el paciente no se presenta y no
avisa, y esa hora ya no se recupera) y **reserva cautiva del teléfono** (el
paciente solo puede pedir turno llamando en horario de atención, de modo que
toda demanda fuera de ese horario se pierde o se dispersa en WhatsApp manual).

El relevamiento confirma que el problema es real y está mal cubierto: las
cuatro capacidades de agenda que lo resuelven —gestión de sillones o boxes,
bloqueos, sobreturnos y prevención de solapamientos— figuran como
**"No evidenciado" en casi todos los sistemas relevados**, en los tres mercados.

## 2. Usuarios y roles

| Rol | Verbo principal |
|---|---|
| Odontólogo dueño | Administra la agenda, sus prestaciones y sus sillones |
| Odontólogo colega | Atiende en sillones compartidos, ve su propia agenda |
| Recepción / secretaría | Agenda, reprograma y cancela turnos de terceros |
| Paciente | Reserva, confirma, cancela o reprograma su propio turno |

El usuario objetivo elegido es el **odontólogo independiente** que trabaja con
dos o tres sillones y, a veces, con un colega. El modelo de dominio se escribe
igualmente **multi-profesional y multi-sillón**, porque es la condición para que
la regla de solapamiento tenga sentido real.

## 3. Casos de uso principales

1. Como recepción, quiero **agendar un turno** validando que no pise otro del
   mismo profesional ni del mismo sillón, para no duplicar ocupación.
2. Como recepción, quiero **reprogramar un turno** a un horario libre, para
   reacomodar la agenda sin perder al paciente.
3. Como recepción, quiero **cancelar un turno y liberar el horario**, para que
   ese espacio vuelva a estar disponible.
4. Como paciente, quiero **reservar un turno online 24/7**, para no depender del
   horario de atención del consultorio.
5. Como consultorio, quiero que el sistema **recuerde el turno al paciente**,
   para reducir el ausentismo.

## 4. Competidores y soluciones existentes

Relevados 20 fichas (18 sistemas distintos). Detalle, evidencia y fuentes en
`docs/discovery/sources/`.

- **Argentina**: Odonto.ar, Dentia, DentiClick, DENTQ, VecDent, AgendaPro,
  Dentalink.
- **Latinoamérica**: Simples Dental (BR), Clinicorp (BR), Dentidesk (CL/RD),
  MR Dental Cloud (MX), AgendaPro vertical dental, Dentalink (CL).
- **Internacionales**: Open Dental, tab32, Ascend by Dentrix, Curve Dental,
  Dentally, EXACT, Doctolib.

Hallazgos transversales:

- **Estándar de mercado**: agenda en la nube, odontograma, historia clínica y
  caja. Internacionalmente también reserva online y recordatorios de serie.
- **Vacío argentino**: recordatorios por WhatsApp solo evidenciados en 3 de 7
  productos locales; facturación ARCA solo en 1; conformidad con la Ley 25.326
  declarada por 2, y como afirmación comercial.
- **Ausentes en toda la región**: lista de espera, seguimiento de ausentes y
  reglas explícitas contra solapamientos.

## 5. Funcionalidades necesarias (innegociables de la v1)

1. Agenda con **prevención de solapamientos** por profesional y por sillón.
2. **Reprogramar** un turno.
3. **Cancelar** un turno liberando el horario.
4. **Recordatorios automáticos** al paciente.
5. **Reserva online** por parte del paciente.

> Nota de criterio: cinco innegociables es un alcance ambicioso. La sección D
> del informe recorta esto a un MVP realista y traslada el resto a etapas
> posteriores.

## 6. Funcionalidades opcionales

Lista de espera; sobreturnos autorizados; obras sociales y prepagas;
facturación ARCA; odontograma y ficha clínica extendida; reportes y métricas;
multi-sede.

## 7. Reglas de negocio

1. **Duración por prestación**: cada tipo de prestación define su duración
   (por ejemplo, limpieza 30 minutos, conducto 90). La duración del turno se
   deriva de la prestación.
2. **Sin solapamientos**: un profesional no puede tener dos turnos simultáneos,
   y un sillón no puede estar ocupado dos veces en la misma franja.
   **Un turno que empieza exactamente cuando termina otro es válido** (los
   intervalos son semiabiertos).
3. **Anticipación mínima para cancelar**: no se puede cancelar con menos de N
   horas (N por definir).
4. **Sobreturno como excepción**: se permite romper la regla de solapamiento
   solo con autorización explícita, y el hecho queda registrado.
5. Un turno **cancelado o ya atendido** no se reprograma.

## 8. Integraciones

Para la v1 se definen como **interfaz del dominio con adaptador simulado**, de
modo que los tests no dependan de servicios externos ni de credenciales:

- Notificaciones al paciente (WhatsApp Business API o email) — **simulada**.
- Cobros / señas (Mercado Pago) — **futura**.
- Facturación (ARCA) — **futura**.
- Calendario externo (Google Calendar) — **futura**.

## 9. Restricciones

- **Stack**: Java con Spring Boot.
- **Sin interfaz gráfica obligatoria**: la implementación puede ser una API o un
  módulo de dominio.
- **Sin secretos en el repositorio**: ninguna clave ni credencial.
- **Datos ficticios**: prohibido usar datos reales de pacientes en ejemplos,
  pruebas y capturas.
- **Equipo de 5 personas** trabajando en paralelo mediante pull requests sobre
  `master`.

## 10. Riesgos

| Riesgo | Por qué | Mitigación |
|---|---|---|
| Evidencia débil del relevamiento | Las páginas se leyeron con extracción automática; los sitios de reseñas bloquearon el acceso | Verificación manual de al menos 5 fuentes, documentada en el informe |
| Alcance ambicioso | Cinco innegociables para el plazo disponible | La sección D recorta el MVP; el resto va a etapas posteriores |
| Supuesto sin probar: el usuario comparte sillones | Se eligió "odontólogo independiente" pero el modelo es multi-sillón | Validar con un odontólogo real, o declararlo como supuesto del dominio |
| Dependencia externa en recordatorios | WhatsApp Business API requiere cuenta, claves y costo | Adaptador simulado en la v1 |
| Conflictos de merge entre los 5 integrantes | Todos los changes tocan la entidad Turno y la validación de horarios | Aislar la validación de disponibilidad en un único servicio reutilizable |

## 11. Preguntas abiertas

1. ¿Cuánto dura realmente cada prestación? El catálogo de duraciones no está
   validado con un profesional.
2. ¿Cuántas horas de anticipación mínima para cancelar?
3. ¿Quién puede autorizar un sobreturno? ¿Queda auditado con usuario y motivo?
4. ¿El paciente puede cancelar o reprogramar solo, o solo la recepción?
5. ¿Qué pasa con el horario liberado? ¿Se ofrece a una lista de espera?
6. Todo lo marcado **"No evidenciado"** en las fichas: en particular, cómo
   resuelven los competidores los sillones, los bloqueos y los solapamientos.
7. ¿Qué exige concretamente la Ley 25.326 para datos de salud, más allá de lo
   que los proveedores declaran comercialmente?
