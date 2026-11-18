# Etapa 0 — Preparación del entorno

Evidencia de la instalación de Active Stack en **modo Full** para el agente Claude Code,
sobre el repositorio `turnos-odontologia`.

Fecha de captura: **2026-11-18**
Equipo: Windows 11 Pro 26300

## 01-instalador-completado.jpg

Salida del instalador de Active Stack ejecutado en modo Full.
Se ven todos los pasos aplicados con éxito, entre ellos las skills que usa este trabajo
(`active-orchestrator`, `kb-creator`, `roadmap-generator`, `find-skill`, `skill-registry`,
`agent-instruction`, `discovery-research`, `web-scraper`) y las herramientas externas
(`openspec`, `engram`, `context7`).

Cierra con:

```
Verification checks: 0 passed, 0 failed, 0 warnings, 0 skipped
Installation verified. You're ready to start building.
```

## 02-harnesses-instalados.jpg

Verificación posterior del harness instalado:

- Las 9 skills presentes en `C:\Users\zbook\.claude\skills`.
- Versiones de las herramientas externas: openspec 2.1.0, engram 1.20.0, Node v24.22.1,
  Go 1.27.0, Git 2.53.0.
- Archivos de configuración generados: `.claude/CLAUDE.md`, `.claude/settings.json`,
  `.active-stack/config.json`.

## Pendiente

`engram` está en 1.20.0 y hay 3.7.2 disponible. Conviene actualizarlo antes de la Etapa 7,
que se evalúa sobre la memoria persistente del agente.
