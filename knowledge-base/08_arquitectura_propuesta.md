# Arquitectura Propuesta

> Amplía la sección "Arquitectura general" de `02_descripcion_general.md` con el
> detalle de paquetes, persistencia y estrategia de pruebas. Si algo de este
> archivo contradice al 02, manda el 02.

## Principio rector

El sistema es un **motor de disponibilidad de agenda**. Todo lo demás —REST,
JPA, notificaciones— es periferia reemplazable. La arquitectura hexagonal no se
elige por estilo, sino porque los dos requisitos duros del proyecto la imponen:
las integraciones externas quedan fuera de la v1 pero dentro del diseño, y cinco
personas van a modificar la entidad `Turno` en paralelo.

## Estructura de paquetes

```
ar.edu.turnos
├── dominio
│   ├── modelo            Turno, Profesional, Sillon, Prestacion, Paciente,
│   │                     AutorizacionSobreturno, EstadoTurno, Intervalo
│   ├── servicio
│   │   └── ServicioDisponibilidad      ◄── EL NUCLEO
│   ├── puerto
│   │   ├── salida        NotificadorPaciente, RegistroAuditoria
│   │   └── repositorio   TurnoRepositorio, ProfesionalRepositorio, ...
│   └── excepcion         ReglaDeNegocioViolada y sus subtipos
├── aplicacion
│   └── casouso           AgendarTurno, ReprogramarTurno, CancelarTurno,
│                         AutorizarSobreturno, RegistrarAusencia,
│                         ConsultarAgenda, ConsultarDisponibilidad
└── infraestructura
    ├── rest              Controladores + DTOs + manejador de errores (opcional v1)
    ├── persistencia      Entidades JPA, adaptadores de repositorio
    └── notificacion      AdaptadorNotificacionSimulado
```

**Regla de oro**: el paquete `dominio` no importa nada de Spring, de JPA ni de
HTTP. Un test de `ServicioDisponibilidad` corre sin levantar contexto de Spring,
en milisegundos. Si esa regla se rompe, la arquitectura dejó de cumplir su
función.

## El núcleo: `ServicioDisponibilidad`

Es la única pieza que sabe qué significa "este turno se solapa con aquel".

```java
public interface ServicioDisponibilidad {

    // Lanza ReglaDeNegocioViolada si el intervalo no esta disponible.
    void verificarDisponibilidad(SolicitudDeTurno solicitud);

    // Variante no excepcional, para consultas y para calcular huecos.
    ResultadoDisponibilidad evaluar(SolicitudDeTurno solicitud);

    List<Franja> huecosLibres(LocalDate dia, Profesional p, Sillon s, Prestacion pr);
}
```

Decisiones que lo definen:

- **El intervalo es un objeto de valor**, no dos `LocalDateTime` sueltos.
  `Intervalo` encapsula la semántica semiabierta `[inicio, fin)` y expone un
  único método `seSolapaCon(Intervalo otro)`. La regla `RN-AG-04` vive **en ese
  método y en ningún otro lugar**, lo cual la hace testeable de forma aislada.
- **La solicitud excluye un turno por id**, para que la reprogramación (F-02) no
  choque consigo misma.
- **Solo considera turnos vigentes** (`RN-AG-07`): el filtro es parte de la
  consulta al repositorio, no del servicio.
- **No conoce al sobreturno**: el caso de uso `AutorizarSobreturno` decide saltar
  la verificación. El servicio nunca devuelve "disponible" para un intervalo
  ocupado. Así la excepción queda explícita en la capa de aplicación y no
  contamina la regla.

## Persistencia

- **Spring Data JPA** con entidades separadas de las del dominio, o anotadas
  sobre el dominio si el equipo prefiere simplicidad. Es una decisión abierta
  menor: lo que no se negocia es que los repositorios se **declaren como
  interfaces en el dominio** y se implementen en infraestructura.
- **Base de datos**: H2 en memoria para desarrollo y tests; PostgreSQL como
  destino productivo. El dominio no depende de ninguna de las dos.
- **Índice obligatorio** sobre `(profesional_id, inicio, fin)` y
  `(sillon_id, inicio, fin)`, filtrando por estados vigentes: la consulta de
  solapamiento es la más caliente del sistema y corre en cada alta, cada
  reprogramación y cada cálculo de huecos.
- **Concurrencia**: dos altas simultáneas sobre la misma franja pueden pasar
  ambas la verificación antes de que cualquiera escriba. La mitigación mínima de
  la v1 es una **restricción de exclusión en la base** o un bloqueo pesimista
  sobre el recurso. Queda señalado como riesgo `R-06`, no resuelto.

## Estrategia de pruebas

| Capa | Herramienta | Qué prueba |
|---|---|---|
| `Intervalo` | JUnit 5 puro | La semántica semiabierta, con sus casos de borde |
| `ServicioDisponibilidad` | JUnit 5 + dobles en memoria | Las reglas `RN-AG-02`, `RN-AG-03`, `RN-AG-04`, `RN-AG-07` |
| Casos de uso | JUnit 5 + Mockito | Transiciones de estado, autorización de sobreturno, anticipación mínima |
| Repositorios | `@DataJpaTest` con H2 | Que la consulta de solapamiento devuelva lo mismo que el dominio |
| REST (si existe) | `@WebMvcTest` con MockMvc | Códigos de estado y forma del error |

Reglas de prueba que el trabajo exige:

- **Un test por escenario de la especificación**, incluido el de borde.
- **Datos ficticios siempre** (`RN-PA-04`). Ningún dato real de paciente en
  fixtures, seeds ni capturas.
- **Nada de tests triviales**: una aserción que pasa sin ejercitar la regla no
  cuenta como cobertura del escenario.

## Organización para cinco personas en paralelo

El riesgo `R-05` es real: los cinco changes del roadmap tocan `Turno`. La
mitigación es arquitectónica, no de proceso:

1. **`ServicioDisponibilidad` y `Intervalo` se escriben una sola vez**, en el
   change base, y después **no se modifican**: los demás changes los consumen.
2. Cada change nuevo agrega **su propio caso de uso** en un archivo propio, en
   lugar de ampliar uno existente.
3. Los estados del turno y sus transiciones se concentran en `EstadoTurno`, de
   modo que agregar un estado toque un único archivo.
4. El líder mergea de a un pull request por vez; el resto rebasa sobre `master`
   antes de pedir revisión.

## Lo que NO está en la v1

Sin interfaz gráfica: la consigna admite una API o un módulo de dominio. Sin
integraciones reales: WhatsApp, Mercado Pago, ARCA y Google Calendar existen
solo como puertos. Sin odontograma ni historia clínica. Sin multi-sede.
