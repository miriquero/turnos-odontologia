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
| Un test por escenario, todos en verde | **Listo** — 104 tests, 0 fallas |
| Change archivado | **Listo** — `2026-11-19-crear-turno-sin-solapamientos` |
| Specs sincronizados | **Listo** — 2 capacidades vigentes |

Nada pendiente para `C-01`. Los changes `C-02` a `C-05` de los compañeros son
trabajo adicional del grupo, no un requisito de este criterio.

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
