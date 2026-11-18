# Informe de Discovery
## Sistema de gestión de turnos y agenda de pacientes para consultorios odontológicos (Argentina)

| | |
|---|---|
| **Materia** | Desarrollo de Software |
| **Trabajo** | Trabajo Global de Integración |
| **Entregable** | Informe de Discovery (investigación de mercado y producto) |
| **Fecha** | 2026-11-18 |
| **Repositorio** | `turnos-odontologia` |

### Integrantes

| Integrante | GitHub |
|---|---|
| Martín Ignacio Riquero (líder) | miriquero |
| Julián Fernández Drago | julianfdrago |
| Augusto Quiroga | augusto163 |
| Maximiliano Di Natale | maximiliano-di-natale |
| [COMPLETAR: nombre del quinto integrante] | [COMPLETAR: usuario de GitHub] |

---

## Resumen ejecutivo

Se relevaron **19 fichas de producto** (18 sistemas distintos; AgendaPro se relevó dos veces, en su oferta argentina y en su oferta mexicana) de gestión de turnos y agenda odontológica, en tres mercados: Argentina (7), Latinoamérica sin Argentina (6) e internacional (7). La investigación se limitó a fuentes públicas: sitios oficiales, páginas de precios y, cuando existía, centros de ayuda. No se crearon cuentas, no se pidieron demos y no se contactó a ningún proveedor. Las páginas se leyeron con herramientas de extracción automática de texto, y los sitios de reseñas independientes (Capterra, G2, TrustRadius, Software Advice) bloquearon el acceso. En consecuencia, **la mayor parte de la evidencia reunida es afirmación comercial del proveedor, no funcionalidad comprobada**, y cada "No evidenciado" del informe significa "no hallado en las páginas consultadas", no "el producto no lo tiene". Esta limitación se declara en detalle en la sección correspondiente y condiciona todas las conclusiones.

El hallazgo central es un vacío consistente en los tres mercados: las cuatro capacidades de agenda que resuelven el problema económico real de un consultorio —**gestión de sillones o boxes, bloqueos de horario, sobreturnos autorizados y prevención explícita de solapamientos**— figuran como "No evidenciado" en la mayoría de los sistemas relevados. Solo tres productos describen el sillón como recurso agendable (AgendaPro, Dentalink, Simples Dental, más tab32 con "columnas por operatorio" en el mercado estadounidense), y **ninguno** documenta la prevención de solapamientos como una regla técnica verificable: lo más explícito que se encontró es "evita sobreagendamientos" (Dentalink) y "control de sillones para evitar horarios duplicados" (Simples Dental), ambas afirmaciones comerciales. La lista de espera y el seguimiento sistemático de ausentes están prácticamente ausentes de toda la región; el único caso donde la lista de espera aparece operativa es Open Dental, mediante su enlace Web Sched ASAP, y se cobra como adicional.

En el mercado argentino el segundo vacío es de integración local: los recordatorios por WhatsApp solo están evidenciados en 3 de los 7 productos locales, la facturación ARCA en 1, y la conformidad con la Ley 25.326 la declaran 2, siempre como afirmación comercial y nunca como certificación. Los precios locales oscilan entre un plan gratuito (DentiClick) y ARS 187.000 mensuales (DENTQ Enterprise); los dos productos mejor posicionados en la matriz de puntuación son **Dentia** (4,50/5) y **Dentalink** (4,15/5), el primero por amplitud funcional publicada a precio bajo y el segundo por madurez, certificación ISO 27001 y módulo argentino, con la contrapartida de un precio no publicado.

**MVP recomendado.** Para un equipo de cinco estudiantes, el núcleo imprescindible debe ser exactamente el vacío que el relevamiento dejó al descubierto: una agenda multi-profesional y multi-sillón con **prevención de solapamientos como regla de dominio probada con tests**, más alta, reprogramación y cancelación de turnos, duración derivada de la prestación y sobreturno como excepción autorizada y auditada. Como diferenciador, el registro de motivos de cancelación y ausencia y la liberación trazable del horario. Las integraciones externas reales —WhatsApp Business API, Mercado Pago y ARCA— quedan para etapas posteriores y en la v1 se modelan como interfaces de dominio con adaptador simulado, para que las pruebas no dependan de credenciales ni de servicios de terceros.

---

## A. Tabla comparativa

Ordenada por relevancia para el mercado argentino: primero los productos con oferta comercial argentina, luego los latinoamericanos sin presencia local, luego los internacionales de referencia. Entre paréntesis, la clasificación de la evidencia: **(C)** Comprobado — lo describe el producto, su documentación o un video oficial con detalle verificable; **(AC)** Afirmación comercial — lo declara el proveedor sin demostrarlo. Las celdas sin evidencia pública dicen "No evidenciado". Todas las fuentes fueron consultadas el **2026-11-18**.

| # | Producto / Proveedor | País · Segmento | Reserva online | Recordatorios | Odontograma | Precio publicado | Fuente y fecha de consulta |
|---|---|---|---|---|---|---|---|
| 1 | **Dentia** / proveedor No evidenciado | Argentina (declarado "Hecho en Argentina") · clínica, multisucursal | Vía asistente de WhatsApp con IA que agenda, reprograma y cancela (AC); reserva web: No evidenciado | Sí: email en plan Básico; WhatsApp y confirmaciones desde Pro (AC) | Sí, con periodontograma y dictado por voz (AC) | ARS 20.000 / 30.000 / 40.000 por mes; 30 días gratis (C) | https://dentia.tech/ · 2026-11-18 |
| 2 | **Dentalink** / Engenis SpA | Chile, con versión y módulo argentinos · clínica, multisucursal, escala LATAM | Sí: agenda online 24/7 y agendamiento por WhatsApp (AC) | Sí, por WhatsApp y email, con confirmaciones automáticas (AC) | Sí, con periodontograma (AC) | No evidenciado: planes Esencial, Pro y Titanium por cotización. Promoción publicada de USD 50/mes para un profesional (C). Capterra lista USD 29 iniciales (fuente secundaria, no verificada) | https://www.softwaredentalink.com/ar · https://www.softwaredentalink.com/planes?c=cl · 2026-11-18 |
| 3 | **AgendaPro (oferta Argentina)** / AgendaPro Inc. | País de origen No evidenciado en la página · multirrubro con vertical dental | Sí: turnos online 24/7, sitio de reservas y reserva desde Google Maps (AC) | Sí, por WhatsApp con reenvío si no confirma a 1, 2 o 3 días; cupo de 50 mensajes/mes en Individual y Full, packs adicionales pagos (C) | Sí, sesión por sesión (AC); periodontograma: No evidenciado | **[CORREGIDO 2026-11-18]** Vertical **Salud**, que es la que corresponde a un consultorio odontológico: Individual ARS 19.900, Premium ARS 44.900, PRO IA ARS 189.900 por mes con IVA; profesional adicional ARS 8.000 en Premium y ARS 20.000 en PRO IA; primeros 3 meses a ARS 990 (C). Add-ons: Recepcionista IA ARS 15.000 por profesional, pack WhatsApp desde ARS 7.900, videoconferencia desde ARS 10.900, Charly desde ARS 31.900 (C). Facturación electrónica figura como "Próximamente". El precio de la API **no es público**: la página remite a "Conversemos" | https://agendapro.com/ar/planes · https://agendapro.com/ar/dental/software-odontologico · 2026-11-18 |
| 4 | **DentiClick** / ThunderSkills | Argentina y Venezuela (declarado) · consultorio, plan Centro multisucursal | Sí: página propia con subdominio para que el paciente reserve solo, las 24 horas (AC) | No evidenciado | Sí, interactivo, con denticiones permanente, temporal y mixta e historial versionado (AC) | Plan gratuito (1 profesional, hasta 30 pacientes); USD 12 / 29 / 35 por mes; un mes de prueba sin tarjeta (C) | https://www.denticlick.app/ · 2026-11-18 |
| 5 | **Odonto.ar** / proveedor No evidenciado | País No evidenciado explícitamente; indicios de Argentina (precios en ARS, Mercado Pago, WhatsApp con característica 351) · odontólogo individual y clínica de hasta 10 personas | Sí: enlace de reserva, con anticipación y permisos definidos por el profesional (AC) | No evidenciado | Sí, interactivo, con historial por diente y registro de autor y fecha (C) | USD 29 / 49 / 99 por mes; cobro en pesos "al valor del día"; 30 días gratis sin tarjeta (C). **Inconsistencia interna**: declara que todos los planes incluyen todas las funciones, pero hay funciones que difieren por plan | https://odonto.ar/ · 2026-11-18 |
| 6 | **VecDent** / Vectora | Argentina (inferido de precios en ARS) · odontólogo independiente, consultorio con equipo, clínicas y redes | Parcial: portal del paciente donde "pide turno y ve su historial" (AC); reserva 24/7: No evidenciado | No evidenciado (existe un módulo "Mensajería con pacientes" sin detalle) | Sí, por voz, con periodontograma de 6 puntos con índices y firma (AC) | ARS 30.000 / 50.000 / 80.000 por mes; sin prueba gratuita, con demo sin registro (C) | https://vectora.ar/pages/vecdent/ · 2026-11-18 |
| 7 | **DENTQ** / DosNubesMedia | País No evidenciado explícitamente; precios en ARS y contacto +54 · consultorios, clínicas y multisede | No evidenciado: el título de la página menciona "turnos online" pero no se describe la reserva por el paciente | No evidenciado | Sí, adulto de 32 piezas y pediátrico de 20, con numeración FDI (AC) | ARS 25.000 / 75.000 / 187.000 por mes; 30 días gratis; sin permanencia (C) | https://dosnubesmedia.com/dentq · 2026-11-18 |
| 8 | **Simples Dental** / Simples Dental | Brasil (Santa Catarina, según el pie del sitio) · clínica y dentista | Sí: enlace de agendamiento 24/7 con horarios en tiempo real (AC) | Sí, por WhatsApp y SMS; **la confirmación automática por WhatsApp se cobra aparte** (C) | Sí, odontograma online (AC); periodontograma: No evidenciado | R$ 112,42 / 187,42 / 320,74 por mes en plan anual; prueba de 7 días sin tarjeta (C). **Inconsistencia**: la misma página muestra R$ 137,41 / 229,08 / 320,74 sin aclarar la condición | https://www.simplesdental.com/planos-e-precos · https://www.simplesdental.com/agenda-online · 2026-11-18 |
| 9 | **MR Dental Cloud** / MR Desarrollos Inteligentes | México · consultorio y clínica | Vía bot de WhatsApp que propone horarios, confirma, reagenda y cobra anticipos (AC); enlace de reserva: No evidenciado | Sí, automáticos a 24, 12, 4 y 1 hora, por WhatsApp, SMS y email (AC) | Sí, adulto y pediátrico, con exportación a PDF (AC) | MXN 350 (hasta 5 dentistas) y MXN 750 (hasta 10) por mes; anual con un mes gratis; prueba sin tarjeta (C) | https://www.mrdentalcloud.com · 2026-11-18 |
| 10 | **Clinicorp** / Clinicorp | Brasil · clínicas, consultorios, redes y franquicias | Sí: "agendamento online" en todos los planes, conectado a Google Meu Negócio, con descripción escasa (AC) | Sí, automáticos y alertas de retorno por WhatsApp (AC) | Sí, incluido en todos los planes según /planos (AC); no aparece en la home | Standard R$ 159,90 en /planos y R$ 149,90 en la home (**discrepancia interna**), Premium R$ 369,90, Combo IA R$ 468,90; implementación obligatoria de pago único de R$ 149,90 o R$ 299; sin prueba gratuita (C) | https://www.clinicorp.com/planos · 2026-11-18 |
| 11 | **AgendaPro (oferta México)** / AgendaPro Inc. | Oficina en Ciudad de México según el pie; origen No evidenciado · servicios y clínicas dentales | Sí: reserva por sitio compartible, con pago anticipado y doble confirmación (AC) | Sí, por WhatsApp, cobrados por paquetes de uso; segundo mensaje si no hay respuesta (C) | Ambiguo: solo "puedes anexar odontograma"; odontograma nativo No evidenciado | MXN 299 / 550 / 1.500 / 4.500 por mes más IVA; anual con 2 meses gratis; adicionales: WhatsApp desde MXN 100 por 50 mensajes, Charly desde MXN 1.000 (C) | https://agendapro.com/mx/planes · https://agendapro.com/mx/dental/software-odontologico · 2026-11-18 |
| 12 | **Dentidesk** / razón social No evidenciado | Chile (Providencia, Santiago) y República Dominicana · clínicas, centros médicos, hospitales, versión académica | Mención de "integraciones con agendamiento online" sin detalle; **WhatsApp automático figura como próxima integración, no disponible** | Mencionados, sin canal especificado | Sí: fichas de odontograma, endodoncia, periodoncia y ortodoncia (AC) | No evidenciado en el sitio; prueba gratuita de 15 días (AC). Capterra lista "desde USD 50" sin período ni plan (fuente secundaria, no verificada) | https://dentidesk.com · https://www.capterra.es/software/184029/dentidesk · 2026-11-18 |
| 13 | **Open Dental** / Open Dental Software | País No evidenciado en las páginas leídas (la página institucional devolvió 404) · consultorios de cualquier tamaño | Sí (C): Web Sched con cuatro variantes —Nuevo Paciente, Paciente Existente, Recall y ASAP (lista de espera de facto)—, cada una con su enlace por texto o email. Se cobra como adicional | Sí (C): eReminders por texto y email, eConfirmations, Integrated Texting con respuestas entrantes | Sí (C): odontograma 3D que muestra la dentición y el tratamiento a lo largo del tiempo | USD 199 por mes por ubicación los primeros 12 meses y USD 149 después en EE.UU.; USD 89 en otros países; gratis con soporte limitado en países en desarrollo; USD 20/mes por profesional desde el cuarto; Web Sched USD 75/mes cada módulo, ASAP USD 20, bundle eServices USD 165 (C) | https://opendental.com/site/fees.html · https://www.opendental.com/site/eservices.html · 2026-11-18 |
| 14 | **tab32** / tab32, Inc. | Estados Unidos (Sacramento, California) · consultorio privado y DSO multi-ubicación | Sí (AC): reserva online y lista de espera listadas dentro de la agenda, con estado de sillón en vivo | Sí (AC): recordatorios y recall por texto; confirmación primero por texto y llamada solo a los no confirmados | Charting con plan de tratamiento, notas y periodontograma por voz (AC); el término "odontograma" No evidenciado | USD 95 por mes con usuarios y profesionales ilimitados, mes a mes, sin cargo de instalación; prueba de 14 días con tarjeta; cargos por uso: USD 0,25 por reclamo, USD 0,05 por texto, USD 0,75 por chequeo con IA; migración desde USD 1.250 (C) | http://tab32.com/pricing · 2026-11-18 |
| 15 | **Dentally** / Henry Schein One UK Ltd | Reino Unido (Gillingham, Kent), con versiones para Australia, Canadá, Irlanda y Nueva Zelanda · independientes, multi-sede y NHS | Sí (C): Dentally Portal, con reserva por enlace de email o SMS, enlace en el sitio web o código QR, acceso con móvil verificado y código de un solo uso, y configuración por horarios, duración, tipo de tratamiento, sesiones, profesionales y sede. **No está incluido en el plan Starter** | Sí (AC): recordatorios y confirmaciones automáticas | Charting mencionado; odontograma y periodontograma: No evidenciado | GBP sin IVA, por mes y para 3 consultorios: Starter £134-£618, Essentials £238-£778, Pro £346-£1.021 (C); sin prueba gratuita, solo demo | https://www.dentally.com/en-gb/pricing · https://www.dentally.com/en-gb/dentally-portal · 2026-11-18 |
| 16 | **Ascend by Dentrix** / Henry Schein One | Dirección en American Fork, Utah; país no declarado textualmente · consultorios y grupos multi-ubicación | Sí (AC): reserva online del paciente; lista de espera y reprogramación online: No evidenciado | Sí (AC): recordatorios de cita, recare, confirmaciones y mensajería bidireccional | Quick Exam, detección por IA y notas por voz (AC); odontograma y periodontograma: No evidenciado | Parcial: Ascend Starter USD 199 por mes; el resto es personalizado (No evidenciado); sin prueba, solo demo | https://www.dentrixascend.com/ · 2026-11-18 |
| 17 | **Curve Dental** / CD Newco, LLC | Estados Unidos (Alpharetta, Georgia) y Canadá · consultorios independientes, multi-ubicación y DSO | Sí, listada como parte de la experiencia del paciente, sin detalle (AC) | Sí, listados junto a "Curve GRO Patient Engagement", sin detalle (AC) | Charting, periodontograma, imágenes y plan de tratamiento listados (AC); el término "odontograma" no figura | No evidenciado: la página se titula "Affordable & Transparent Pricing" pero no publica cifras y pide cotización | https://www.curvedental.com/pricing · 2026-11-18 |
| 18 | **EXACT** / Software of Excellence (Henry Schein One) | Reino Unido, Australia y Nueva Zelanda · segmento No evidenciado | Sí (AC): sistema de reserva para pacientes, con alianza HealthEngine de 2020 para reserva online en su red | No evidenciado (sí un lanzamiento de recall online, según prensa sectorial no abierta) | No evidenciado | No evidenciado (el precio no es público) | https://www.henryscheinone.com/products/exact/ · 2026-11-18 |
| 19 | **Doctolib** / Doctolib | Francia · segmento No evidenciado | Solo fuentes secundarias: el sitio oficial devolvió 403 y no pudo leerse | Solo fuentes secundarias | No evidenciado | No evidenciado. Un comparativo de un competidor menciona "desde 139 € TTC por profesional/mes"; dato secundario y sesgado, no confiable | Sitio oficial bloqueado (403) · https://pabau.com/fr/blog/planity-pro-vs-doctolib/ · 2026-11-18 |

### Advertencia de lectura de la tabla

Las columnas "Reserva online", "Recordatorios" y "Odontograma" se eligieron porque son las tres capacidades que todos los proveedores publicitan, y por eso son comparables. **No son las capacidades que deciden el problema de este proyecto.** Las capacidades decisivas —sillón como recurso agendable, bloqueos, sobreturnos y prevención de solapamientos— no están en la tabla porque habrían producido una columna de "No evidenciado" casi completa; se analizan en la sección C.3.

---

## B. Matriz de puntuación

### Nota metodológica

**Qué significa cada nota.** La escala no mide la calidad del software, que no se probó, sino **la cantidad y la calidad de la evidencia pública disponible sobre cada criterio**:

| Nota | Significado |
|---|---|
| **0** | No evidenciado: el criterio no aparece en ninguna de las fuentes consultadas |
| **1** | Mención genérica, de una sola capacidad y sin detalle (p. ej. "agenda personalizable", "facturación") |
| **2** | Una o dos capacidades nombradas con algún detalle, sin las capacidades centrales del criterio |
| **3** | Varias capacidades evidenciadas con detalle, pero faltan las decisivas del criterio |
| **4** | Cobertura amplia y detallada, con alguna capacidad central ausente o cobrada aparte |
| **5** | Cobertura amplia y detallada que incluye las capacidades centrales del criterio |

**Tratamiento de "No evidenciado": puntúa 0.** El criterio es explícito y se aplicó de forma uniforme a los 19 casos: este informe evalúa **lo demostrable públicamente, no lo posible**. Un proveedor que no documenta una función obliga al comprador a descubrirla en una demo, y ese costo de verificación es un atributo real del producto. Dicho esto, **el criterio penaliza a los productos con menos documentación pública, que no son necesariamente los peores productos**. El caso más claro es EXACT (0,70), un sistema con trayectoria larga en el mercado anglosajón cuyo sitio oficial publica muy poca información verificable; y el de Doctolib (0,35), cuyo sitio devolvió 403 y por lo tanto no tiene casi evidencia oficial en este relevamiento. Ninguno de los dos puntajes debe leerse como un juicio sobre el software. Simétricamente, un puntaje alto como el de Dentia (4,50) refleja **un sitio muy completo**, no un producto verificado: casi toda su evidencia es afirmación comercial de una sola página.

**Pesos aplicados** (suman 100%): Gestión de turnos y automatización 25%; Funcionalidad odontológica clínica 20%; Integraciones locales y WhatsApp 15%; Administración, cobros y facturación 15%; Experiencia del paciente 10%; Seguridad, exportación y trazabilidad 10%; Precio y facilidad de adopción 5%.

En el criterio "Integraciones locales y WhatsApp", "locales" se interpreta **respecto del mercado argentino**, que es el mercado objetivo del producto a construir. Por eso las integraciones fiscales y de pago brasileñas (Pix, boleto, NFS-e), mexicanas (CFDI) o chilenas (SII) puntúan como integración existente pero no local, y las de seguros de Estados Unidos (clearinghouses, ERA) puntúan bajo: son irrelevantes para Argentina. Esto explica por qué productos maduros como tab32 u Open Dental quedan en la mitad de la tabla.

### Matriz

Abreviaturas de columnas: **T** = Gestión de turnos y automatización (25%); **C** = Funcionalidad odontológica clínica (20%); **I** = Integraciones locales y WhatsApp (15%); **A** = Administración, cobros y facturación (15%); **P** = Experiencia del paciente (10%); **S** = Seguridad, exportación y trazabilidad (10%); **$** = Precio y facilidad de adopción (5%). Total ponderado sobre 5,00.

| Puesto | Producto | T (25%) | C (20%) | I (15%) | A (15%) | P (10%) | S (10%) | $ (5%) | **Total** |
|---|---|---|---|---|---|---|---|---|---|
| 1 | **Dentia** | 4 | 5 | 5 | 5 | 3 | 5 | 4 | **4,50** |
| 2 | **Dentalink** | 5 | 5 | 3 | 4 | 4 | 4 | 1 | **4,15** |
| 3 | **AgendaPro (AR)** | 4 | 3 | 5 | 3 | 4 | 3 | 2 | **3,60** |
| 4 | **Simples Dental** | 5 | 3 | 2 | 3 | 4 | 2 | 4 | **3,40** |
| 5 | **MR Dental Cloud** | 4 | 3 | 3 | 3 | 3 | 3 | 5 | **3,35** |
| 6 | **tab32** | 4 | 4 | 1 | 2 | 3 | 3 | 4 | **3,05** |
| 7 | **Clinicorp** | 3 | 3 | 3 | 4 | 3 | 2 | 2 | **3,00** |
| 8 | **Dentally** | 4 | 2 | 2 | 2 | 5 | 1 | 3 | **2,75** |
| 9 | **DentiClick** | 2 | 4 | 1 | 3 | 3 | 2 | 5 | **2,65** |
| 9 | **Open Dental** | 3 | 3 | 2 | 2 | 4 | 1 | 4 | **2,65** |
| 11 | **Ascend by Dentrix** | 3 | 2 | 1 | 2 | 2 | 2 | 2 | **2,10** |
| 12 | **Odonto.ar** | 2 | 3 | 1 | 2 | 2 | 1 | 4 | **2,05** |
| 13 | **AgendaPro (MX)** | 3 | 1 | 1 | 2 | 3 | 1 | 3 | **1,95** |
| 14 | **VecDent** | 1 | 4 | 0 | 2 | 2 | 1 | 2 | **1,75** |
| 15 | **Dentidesk** | 1 | 3 | 1 | 3 | 0 | 0 | 1 | **1,50** |
| 16 | **Curve Dental** | 2 | 2 | 0 | 2 | 1 | 1 | 0 | **1,40** |
| 17 | **DENTQ** | 1 | 3 | 1 | 1 | 0 | 1 | 2 | **1,35** |
| 18 | **EXACT** | 1 | 1 | 1 | 0 | 1 | 0 | 0 | **0,70** |
| 19 | **Doctolib** | 1 | 0 | 0 | 0 | 1 | 0 | 0 | **0,35** |

> **Corrección posterior a la verificación manual (2026-11-18).** La nota de
> AgendaPro (AR) en "Precio y facilidad de adopción" bajó de 3 a 2, y su total
> de 3,65 a 3,60, al comprobarse que los precios citados en el relevamiento
> automático correspondían a la vertical de Belleza y Barbería y no a la de
> Salud, que cuesta 2,5 veces más. El puesto 3 no cambia. Ver la sección
> **Verificación de fuentes**.

### Justificación de las notas que más mueven el ranking

- **Dentia, T=4 y no 5.** Publica sobreturnos, sala de espera, disponibilidad por profesional, sincronización con Google Calendar, recordatorios por dos canales, campañas de recaptación y un asistente de WhatsApp con IA que agenda, reprograma y cancela. No alcanza el 5 porque el sillón como recurso agendable, los bloqueos y la prevención de solapamientos figuran como No evidenciado, y porque no hay reserva web evidenciada: toda la reserva por el paciente depende del canal de WhatsApp.
- **Dentia, S=5 pese a ser un producto joven.** Es la única ficha argentina que reúne 17 perfiles de rol, registro de auditoría de cambios, segundo factor de autenticación, exportación completa de datos, Ley 25.326 declarada, acuerdo de tratamiento de datos y publicación de subprocesadores. Es evidencia de *documentación*, no de auditoría externa: no hay certificaciones.
- **Dentalink, T=5.** Es el único producto del relevamiento que describe la vista de **ocupación de sillones** en tres niveles (global, semanal, diaria) junto con agenda por profesional, estados de cita, aviso de paciente en sala de espera, sobreagendamiento paralelo, tiempos de tratamiento por dentista, agenda online 24/7, reprogramación individual o masiva y recordatorios multicanal. Aun así, la prevención de solapamientos solo aparece como la frase "evita sobreagendamientos", nunca como regla técnica.
- **Dentalink, $=1.** Ningún precio publicado, ninguna prueba gratuita y una dependencia fuerte de adicionales de pago (telemedicina, firma, rayos X con IA, Contact Center IA, Contralor IA, Reportes IA, API). Esta nota, con peso 5%, es la única razón por la que no supera a Dentia.
- **Simples Dental, T=5.** Es la otra ficha con control de sillones, y la única que suma **bloqueo de horarios**, *encaixes* (pacientes en espera que se agendan al liberarse un hueco), estados de turno, colores por tratamiento y actualización en tiempo real contra duplicados. Su I=2 es lo que lo frena: su ecosistema de integraciones es íntegramente brasileño.
- **Dentally, P=5 con S=1.** La disonancia es real y es informativa: su Dentally Portal es la experiencia de reserva mejor documentada de toda la muestra (enlace, QR, verificación de móvil con código de un solo uso, configuración por tipo de tratamiento y duración, elección de sede, actualización en tiempo real), mientras que en seguridad solo hay un enlace a una política y ni el cumplimiento del RGPD está evidenciado.
- **Open Dental, T=3 pese a su madurez.** Su evidencia de reserva y recordatorios es la más concreta del relevamiento, pero la agenda interna —operatorios, bloqueos, duración variable, sobreturnos, solapamientos— quedó No evidenciado: la página de funciones solo indexa enlaces y remite a un manual que no se consultó. Es el ejemplo más nítido de cómo el criterio de puntuación castiga la falta de documentación en la página pública, no la falta de función.

---

## C. Análisis competitivo

### C.1. Funcionalidades que ya son estándar de mercado

Son estándar, es decir, su ausencia descalificaría a un producto sin que su presencia lo diferencie:

1. **Agenda accesible desde el navegador y desde el celular.** Evidenciada en prácticamente todos los sistemas. "Nube" aparece declarada explícitamente en Dentalink, AgendaPro, DENTQ, MR Dental Cloud, Curve Dental y Ascend; en Dentia, DentiClick, Simples Dental, Clinicorp y Dentidesk la modalidad es web o app instalable, pero el carácter de SaaS se infiere y no se declara. Esa vaguedad es llamativa y repetida.
2. **Odontograma.** Presente en 13 de las 19 fichas. Es tan universal que su mera existencia no distingue nada; lo que distingue es su profundidad, y ahí la dispersión es enorme: desde el odontograma 3D con evolución temporal de Open Dental y el historial versionado con autor y fecha de DentiClick y Odonto.ar, hasta el "puedes anexar odontograma" de AgendaPro México, que ni siquiera permite saber si es nativo.
3. **Historia clínica digital con adjuntos.** Evoluciones, antecedentes, alergias, medicación y radiografías adjuntas aparecen en casi todas las fichas locales y regionales.
4. **Reserva online por parte del paciente.** Estándar en los mercados anglosajón y brasileño; en Argentina está evidenciada en 5 de 7 productos locales, aunque con formas muy distintas: enlace simple (Odonto.ar), subdominio propio (DentiClick), portal con historial (VecDent), sitio de reservas más Google Maps (AgendaPro), y solo asistente de WhatsApp (Dentia). Atención al modelo comercial: en Open Dental y en Dentally la reserva online **no está incluida en el plan base**.
5. **Recordatorios automáticos al paciente.** Estándar fuera de Argentina. Dentro de Argentina es la excepción más significativa y se trata en C.3.
6. **Caja, cobros y cuenta corriente del paciente.** Presentes en casi todas las fichas, con grados de detalle muy distintos.
7. **Roles y permisos por usuario.** Mencionados en la mayoría de los productos, casi siempre sin detallar qué permite cada rol.

### C.2. Funcionalidades diferenciadoras reales

Diferenciador se entiende aquí como *presente en pocos productos y con impacto económico demostrable para el consultorio*. Varias de las funciones que los proveedores publicitan como diferenciales no lo son, y conviene decirlo:

**Diferenciadores genuinos:**

1. **El sillón o box como recurso agendable de primera clase.** Solo cuatro fichas lo evidencian: Dentalink (vista de ocupación de sillones), AgendaPro (agenda por odontólogo, práctica y sillón), Simples Dental (control de sillones para evitar duplicados) y tab32 (columnas por operatorio, estado de sillón en vivo). Es el diferenciador más valioso y el menos explotado, porque es la única forma de que la agenda refleje la restricción física real del consultorio.
2. **Encaje automático sobre huecos liberados.** Los *encaixes* de Simples Dental y el enlace Web Sched ASAP de Open Dental son los dos únicos mecanismos operativos de lista de espera del relevamiento. Convierten una cancelación en una oportunidad de facturación, que es exactamente el problema económico del ausentismo.
3. **Trazabilidad clínica con autor y fecha.** El historial por diente con registro de quién y cuándo (Odonto.ar), el odontograma versionado (DentiClick) y el periodontograma firmado (VecDent) son diferenciales con valor legal, no solo funcional.
4. **Facturación fiscal local nativa.** Dentia con ARCA (facturas A, B y C, notas de crédito y débito) es **el único caso argentino evidenciado**; Dentidesk con el SII chileno, MR Dental Cloud con CFDI 4.0 y Simples Dental y Clinicorp con NFS-e son sus equivalentes regionales. Es un diferencial defendible porque obliga al competidor extranjero a una inversión específica por país: Dentalink, con toda su madurez, declara explícitamente que no es software contable.
5. **Liquidación por obra social con nomenclador.** Odonto.ar (cálculo de la parte de la obra social y del paciente), DentiClick (liquidación mensual exportable a CSV y PDF), VecDent (convenios con cobertura en la ficha) y Dentalink en su módulo argentino. Es un diferencial estrictamente local, invisible para los sistemas internacionales. **[CORREGIDO 2026-11-18]** La receta electrónica argentina con firma digital certificada que se atribuía a Dentalink pasa a **No evidenciado**: el artículo de su centro de ayuda que la respaldaba está vacío.
6. **Verificación de identidad del paciente en la reserva.** El acceso al Dentally Portal con móvil verificado y código de un solo uso es el único mecanismo de este tipo en toda la muestra. Resuelve el problema de las reservas falsas, que ningún otro producto aborda.
7. **Segmentación de la reserva por intención.** Los cuatro flujos de Open Dental Web Sched —paciente nuevo, paciente existente, recall y ASAP— son un diseño de producto, no una función. Reconocen que "reservar un turno" es cuatro problemas distintos.
8. **Transparencia comercial como atributo de producto.** tab32 (precio único itemizado, cancelación en un clic, exportación gratuita de todos los datos al irse, compromiso de no subir el precio tras el primer año con aviso de 60 días) y Open Dental (precios granulares publicados, incluyendo gratuidad para países en desarrollo) convierten en diferencial lo que el resto del mercado trata como opacidad conveniente.

**Diferenciadores aparentes que el relevamiento no sostiene:**

- **La inteligencia artificial.** Es la función más publicitada del relevamiento (asistentes y recepcionistas de WhatsApp, cefalometría, lectura de panorámicas, diseño de sonrisa, escribas clínicos, Contralor IA, Reportes IA, Revenue Cycle AI, agentes de cobranza) y **no hay un solo caso con precisión, metodología o validación demostrada**. Las fichas lo registran explícitamente: la precisión de las funciones de IA de Dentia es No evidenciado; la Secretária IA de Simples Dental está marcada como pre-lanzamiento; los "agentes" de tab32 tienen alcance no detallado. Como diferencial competitivo, hoy es marketing. Además, varias de estas funciones están pensadas para clínicas con volumen, no para el consultorio de dos o tres sillones.
- **Las cifras de impacto.** "-85% de no-shows" (MR Dental Cloud), "-30% de inasistencias" (Dentalink) y "74% de dentistas reducen tiempo de secretaría" (Doctolib, vía un PDF que devolvió 404) son afirmaciones propias sin metodología publicada. No deben citarse como evidencia.
- **Las cifras de adopción.** "+32 mil clínicas" (Clinicorp), "+15.000 clientes" (Dentalink), "más de 30.000 clínicas en LATAM" (AgendaPro) y "+100 mil profesionales" (Simples Dental) no pudieron verificarse de forma independiente porque los sitios de reseñas bloquearon el acceso. En AgendaPro México las propias cifras no coinciden entre sí ("más de 20.000 negocios" y "135.000 profesionales") y los contadores de resultados de la página aparecen en "0%". En Dentia, "+100 clínicas lo usan" conviene leerlo junto al hecho de que el caso "Clínica Sonrisas" es una demostración ficticia; en DentiClick, la cifra de 1.240 pacientes proviene del panel de demostración.

### C.3. Vacíos frecuentes del mercado argentino

1. **La prevención de solapamientos no está documentada por nadie.** Es el hallazgo principal. En 19 fichas no hay una sola descripción de qué hace el sistema cuando se intenta agendar un turno que pisa otro del mismo profesional o del mismo sillón. Lo más cercano son dos frases comerciales ("evita sobreagendamientos", "control de sillones para evitar horarios duplicados"), y en el mercado argentino ni eso: los siete productos locales tienen "prevención de solapamientos" como No evidenciado. Para un consultorio, un turno superpuesto es una hora de sillón perdida y un paciente molesto; que la función que lo evita no aparezca en ninguna página de producto es, o bien un vacío funcional real, o bien un vacío de comunicación tan grande que equivale a uno.
2. **El sillón no existe como recurso en los productos argentinos.** Solo AgendaPro, que es un producto generalista multirrubro con oferta localizada, menciona la agenda por sillón. En los seis productos de origen argentino el concepto de box o sillón figura como No evidenciado. La agenda se modela por profesional, lo cual es insuficiente en cuanto hay dos sillones y dos profesionales que se alternan.
3. **Recordatorios: evidenciados en 3 de 7 productos argentinos.** Dentia (email en el plan básico, WhatsApp desde Pro), AgendaPro (WhatsApp con reenvío, limitado a 50 mensajes mensuales en los planes bajos y con packs pagos) y Dentalink (WhatsApp y email). En Odonto.ar, DentiClick y DENTQ figura como No evidenciado; en VecDent hay un módulo "Mensajería con pacientes" sin detalle. Siendo el ausentismo uno de los tres problemas que motivan este proyecto, es un vacío notable.
4. **WhatsApp como canal se cobra por uso y con tope.** Donde existe, casi nunca está incluido: AgendaPro limita a 50 recordatorios mensuales con packs adicionales desde ARS 7.900 y pone la Recepcionista IA en ARS 15.000 por profesional; Simples Dental cobra la confirmación automática por WhatsApp aparte del plan. En un país donde WhatsApp es *el* canal de comunicación con el paciente, el canal central del producto es un costo variable sin techo visible.
5. **Facturación ARCA: 1 de 7.** Solo Dentia la evidencia. AgendaPro la anuncia como "próximamente" sin precio; Dentalink aclara que no es software contable; en el resto es No evidenciado. El consultorio queda obligado a un segundo sistema y a la doble carga de datos.
6. **Ley 25.326: declarada por 2, certificada por 0.** Dentia declara tratamiento de datos de salud conforme a la ley y publica acuerdo de tratamiento, subprocesadores y procedimiento de eliminación; AgendaPro declara funcionar "en línea con" la Ley 25.326 y la Ley 26.529. Dentalink, que es el único con certificación real (ISO 27001 con auditoría anual), cita la Ley 26.529 pero no la 25.326. Los cuatro productos argentinos restantes no la mencionan. Se trata de datos sensibles de salud: el vacío es regulatorio, no cosmético.
7. **Exportación y portabilidad de datos.** Solo Dentia (exportación completa) y tab32 (una exportación gratuita de pacientes, fichas, imágenes y libro mayor al cancelar) la evidencian. En el resto es No evidenciado, incluidos productos caros. El odontólogo que contrata no sabe si podrá irse con sus datos.
8. **Auditoría de cambios.** Evidenciada de forma parcial y heterogénea: Odonto.ar solo registra los cambios del odontograma, Dentia audita "cambios importantes" sin definir cuáles, MR Dental Cloud declara una bitácora. En la mayoría es No evidenciado. En una historia clínica, saber quién modificó qué y cuándo no es opcional.
9. **Lista de espera y seguimiento de ausentes: ausentes en toda la región.** No hay ninguna evidencia de estas funciones en los seis productos latinoamericanos ni en los siete argentinos. Los dos únicos casos del relevamiento son estadounidenses (Open Dental ASAP, tab32).
10. **Opacidad y desorden comercial.** Dentalink, Curve Dental, EXACT, Doctolib y Dentidesk no publican precios; VecDent y Clinicorp no ofrecen prueba gratuita; Clinicorp cobra una implementación obligatoria. Y hay **inconsistencias internas de precio en cuatro proveedores distintos**: AgendaPro (ARS 7.900 en la página de planes contra "desde ARS 13.900" en la FAQ dental, y 14 días de prueba que no figuran en planes), Clinicorp (R$ 149,90 en la home contra R$ 159,90 en /planos), Simples Dental (dos escalas de precio en la misma página sin aclarar la condición) y Odonto.ar (declara que todos los planes incluyen todas las funciones, pero las funciones difieren por plan). Para el odontólogo, comparar precios en este mercado es un trabajo en sí mismo.
11. **Identidad del proveedor sin evidenciar.** Odonto.ar, Dentia, DENTQ y Dentidesk no publican razón social; en Odonto.ar, DENTQ y VecDent el país se infiere de los precios en pesos y del prefijo telefónico. Para un sistema que va a alojar historias clínicas, no saber con qué persona jurídica se contrata es un riesgo concreto.
12. **Exposición cambiaria.** Odonto.ar (USD 29/49/99) y DentiClick (USD 12/29/35) publican en dólares; Odonto.ar aclara que cobra en pesos "al valor del día" con renovación manual del usuario. Los productos que cotizan en pesos (Dentia, DENTQ, VecDent, AgendaPro) tienen una ventaja de adopción que no es funcional pero es decisiva.

### C.4. Oportunidades de innovación para un nuevo sistema de gestión odontológica

Ordenadas por relación entre impacto y esfuerzo, y leídas desde el usuario objetivo real de este proyecto: el odontólogo independiente con dos o tres sillones, no la clínica con veinte profesionales.

1. **Hacer de la disponibilidad el núcleo del producto, no un efecto secundario.** Todo el mercado trata la agenda como un calendario donde se escriben cosas. La oportunidad es tratarla como un **motor de disponibilidad con reglas explícitas y verificables**: el turno se valida contra la ocupación del profesional *y* del sillón, los intervalos son semiabiertos (un turno que empieza exactamente cuando termina otro es válido), la duración se deriva de la prestación, y la regla se puede probar con tests automatizados. Ningún competidor publica nada parecido. Es además la oportunidad perfectamente alineada con un trabajo académico: es lógica de dominio pura, sin dependencias externas.
2. **El sobreturno como excepción explícita, autorizada y auditada.** Varios productos ofrecen sobreturnos (Dentia, Dentalink, Simples Dental), pero ninguno documenta quién puede autorizarlos ni si quedan registrados. Modelar el sobreturno como una ruptura deliberada de la regla, con usuario responsable y motivo asentado, convierte una fuente de caos en información de gestión.
3. **Convertir cada hueco liberado en una oportunidad, no en una pérdida.** Dado que la lista de espera está ausente en toda la región y los dos únicos precedentes son estadounidenses, hay espacio real. La versión mínima no necesita integración: registrar motivo de cancelación y de ausencia, y dejar el hueco visible y ofrecible, ya produce la información que hoy nadie captura.
4. **WhatsApp sin tope y sin sorpresa de costo.** El vacío no es técnico sino comercial: el canal central está tarifado por uso en todos los productos que lo tienen. Un diseño que aísle las notificaciones detrás de una interfaz de dominio permite empezar con un adaptador simulado, seguir con email y conectar WhatsApp después sin rehacer nada. Para el trabajo académico, además, es la única forma de tener pruebas que no dependan de credenciales.
5. **Trazabilidad y portabilidad como promesa explícita.** Auditoría de quién cambió qué y cuándo, y exportación completa de los datos del consultorio, están prácticamente ausentes del mercado y son baratas de implementar si se diseñan desde el principio. Es también la respuesta concreta a la Ley 25.326, frente a competidores que solo la declaran.
6. **Cumplimiento demostrado en lugar de declarado.** Ningún competidor argentino muestra qué hace para cumplir la Ley 25.326: a lo sumo lo afirma. Publicar el mecanismo —cifrado, roles, auditoría, política de retención, procedimiento de eliminación— es un diferencial de confianza que el mercado dejó libre.
7. **Honestidad comercial como posicionamiento.** Con cuatro proveedores que se contradicen a sí mismos en sus propios precios, cinco que no los publican y uno que titula su página "precios transparentes" sin mostrar ninguno, publicar un precio en pesos, sin adicionales escondidos y con exportación garantizada es un diferencial real, al estilo de tab32.
8. **Diseñar para el consultorio chico, no para la clínica.** El relevamiento muestra que casi toda la innovación del mercado (multisucursal, IA, DSO, redes y franquicias, ranking de unidades) apunta al segmento grande. El odontólogo con dos sillones recibe versiones recortadas de productos pensados para otro. Un producto diseñado desde esa escala —donde el dueño es también quien atiende y muchas veces quien agenda— es un posicionamiento desocupado.

---

## D. Recomendación final

### D.1. Cinco competidores prioritarios para analizar mediante demo

La prioridad no la fija el puntaje, sino **qué pregunta abierta de este Discovery resuelve cada demo**. Todos se eligen porque permiten verificar funciones que el relevamiento dejó como No evidenciado.

| # | Competidor | Por qué, y qué verificar en la demo |
|---|---|---|
| 1 | **Dentalink** | Es el único producto del relevamiento que describe la **vista de ocupación de sillones** y el sobreagendamiento paralelo, y es el puesto 2 de la matriz (4,15). La demo es la única vía para responder la pregunta central del proyecto: ¿qué hace el sistema cuando dos turnos se pisan, y cómo trata al sillón como recurso? Además tiene módulo argentino con obras sociales, y su precio no está publicado: la demo es la única forma de conocerlo. **[CORREGIDO 2026-11-18]** La receta electrónica argentina pasa a No evidenciado tras la verificación manual, así que confirmarla en la demo es otro objetivo. |
| 2 | **Dentia** | Puesto 1 de la matriz (4,50) y el competidor argentino más directo. Toda su evidencia proviene de una sola página y es afirmación comercial: la demo debe verificar lo que más pesa en ese puntaje —si la facturación ARCA funciona de verdad, si el asistente de WhatsApp agenda correctamente, si existe reserva web (hoy No evidenciado) y qué audita exactamente el registro de "cambios importantes"—. También es el único caso donde conviene preguntar el costo real de los mensajes de WhatsApp, hoy No evidenciado. |
| 3 | **Simples Dental** | Es la ficha con la agenda mejor descripta de la región: control de sillones, **bloqueo de horarios** y *encaixes* sobre huecos liberados, las tres cosas que el mercado argentino no tiene. No compite en Argentina, así que la demo no sirve para estudiar un rival sino para **estudiar la solución al problema**: es la mejor referencia de cómo se modela una agenda con restricciones. Verificar además la inconsistencia de precios de su propia página. |
| 4 | **AgendaPro (oferta Argentina)** | Puesto 3 (3,60) y el competidor que más probablemente se cruce el equipo en el mercado real, por su reserva desde Google Maps y su marketplace. Es el único producto con oferta argentina que menciona agenda por sillón. **[CORREGIDO 2026-11-18]** La verificación manual mostró que su plan de entrada para odontología no es el de ARS 7.900 (vertical Belleza y Barbería) sino el de **ARS 19.900** de la vertical Salud, de modo que su precio de entrada **no** es bajo. La demo debe aclarar el funcionamiento del tope de 50 recordatorios por WhatsApp, si existe odontograma nativo en los planes de Salud, y el precio real de la API, que no es público. |
| 5 | **Dentally** | No compite en Argentina y su puntaje es medio (2,75), pero tiene el **portal de reserva del paciente mejor documentado de toda la muestra** y el único mecanismo de verificación de identidad (móvil verificado más código de un solo uso). Es la demo que responde la pregunta abierta número 4 del Discovery: ¿qué puede hacer el paciente por sí mismo y con qué garantías? |

Quedan deliberadamente fuera: **Open Dental y tab32**, porque su administración gira alrededor de los seguros de Estados Unidos y su valor ya se extrajo de documentación pública de buena calidad; **Curve Dental, EXACT y Doctolib**, porque su evidencia es demasiado pobre para formular preguntas precisas en una demo (Doctolib ni siquiera pudo leerse, su sitio devolvió 403); y **Odonto.ar, DentiClick, DENTQ y VecDent**, que son competidores locales legítimos pero no aportan evidencia sobre las capacidades de agenda que el proyecto necesita resolver.

### D.2. Tres productos de referencia para experiencia de usuario

| # | Producto | Por qué |
|---|---|---|
| 1 | **Dentally — Dentally Portal** | Es el único flujo de reserva del paciente documentado paso a paso: entrada por enlace de email o SMS, enlace en el sitio o código QR; acceso con móvil verificado y código de un solo uso; y configuración por horarios, duración, tipo de tratamiento, sesiones, profesionales y sede, con actualización de la base en tiempo real. Es un patrón directamente trasladable, y resuelve con una sola decisión de diseño dos problemas que el proyecto tiene abiertos: la identificación del paciente que reserva sin cuenta previa, y la derivación de la duración del turno a partir del tipo de prestación. |
| 2 | **Open Dental — Web Sched** | Referencia de **arquitectura de la interacción**, no de estética: distingue cuatro flujos de reserva según la intención del paciente (nuevo, existente, recall y ASAP sobre lista de espera), cada uno con su propio enlace. Enseña que "reservar un turno" no es una pantalla sino cuatro recorridos distintos, y es el único lugar del relevamiento donde la lista de espera se ve operativa. |
| 3 | **tab32 — confirmación y relación comercial** | Referencia de **reducción de carga administrativa y de fricción**: confirmación primero por texto y llamada telefónica solo a quienes no confirmaron (escalamiento por excepción, no por defecto), cancelación de la suscripción en un clic, un único cobro itemizado y una exportación gratuita de todos los datos al irse. Es el modelo de cómo la honestidad operativa se convierte en experiencia de usuario, y contrasta con los cuatro competidores que se contradicen en sus propios precios. |

Advertencia: estas tres referencias provienen de descripciones del propio proveedor, no de pruebas de uso. Ninguna reseña independiente pudo verificarse. Deben tomarse como patrones de diseño a evaluar, no como calidad de UX demostrada.

### D.3. MVP sugerido

Pensado para un equipo de cinco estudiantes trabajando en paralelo mediante pull requests sobre `master`, con stack Java y Spring Boot, y sin interfaz gráfica obligatoria. El criterio de corte es doble: cada ítem del bloque imprescindible debe ser **lógica de dominio verificable con tests automatizados y sin dependencias externas**, y debe atacar el vacío que el relevamiento dejó al descubierto.

#### Imprescindible (el MVP)

El núcleo es la gestión de turnos con prevención de solapamientos por profesional y por sillón. Todo lo demás existe para sostenerlo.

1. **Modelo de dominio multi-profesional y multi-sillón.** Profesional, sillón, paciente con ficha básica (datos de contacto e identificación), prestación con su duración, y turno. El modelo se escribe multi-profesional y multi-sillón incluso para un consultorio de un solo odontólogo, porque es la condición para que la regla de solapamiento tenga sentido.
2. **Alta de turno con validación de disponibilidad.** La regla, aislada en un único servicio reutilizable: un profesional no puede tener dos turnos simultáneos y un sillón no puede estar ocupado dos veces en la misma franja. **Los intervalos son semiabiertos**: un turno que empieza exactamente cuando termina otro es válido. Esta decisión debe quedar cubierta por tests en sus casos borde.
3. **Duración derivada de la prestación.** La duración del turno no se escribe a mano: se deriva del tipo de prestación (por ejemplo, limpieza 30 minutos, conducto 90). El catálogo de duraciones queda como dato configurable y **declarado como supuesto no validado** con un profesional.
4. **Reprogramación de turno**, revalidando la disponibilidad en el nuevo horario con la misma regla, y respetando que un turno cancelado o ya atendido no se reprograma.
5. **Cancelación de turno con liberación del horario**, de modo que la franja vuelva a estar disponible, con anticipación mínima configurable y **motivo de cancelación registrado**.
6. **Sobreturno como excepción autorizada y auditada.** Se permite romper la regla de solapamiento solo con autorización explícita, y el hecho queda registrado con usuario responsable y motivo.
7. **Registro de ausencia del paciente** como estado del turno, distinto de la cancelación.
8. **Consulta de agenda y de disponibilidad**: agenda por profesional y por sillón en un rango de fechas, y huecos libres para una prestación dada.
9. **Interfaz de notificación al paciente con adaptador simulado.** Se define el puerto de dominio (recordar un turno) y se implementa un adaptador de prueba que registra el envío. **No se integra ningún servicio externo real.** Esto es lo que permite que los recordatorios estén en el diseño sin que las pruebas dependan de credenciales.
10. **Auditoría mínima**: quién creó, modificó, reprogramó o canceló cada turno, y cuándo. Es el vacío de mercado más barato de cubrir si se diseña desde el inicio, y es requisito de un sistema que maneja datos de salud.
11. **Suite de tests automatizados** sobre la regla de disponibilidad y las transiciones de estado del turno, con datos ficticios exclusivamente.

Justificación del recorte: el Discovery había marcado cinco funcionalidades como innegociables, incluidas la reserva online por el paciente y los recordatorios automáticos. Ambas se degradan aquí deliberadamente. La reserva online pasa al bloque diferenciador porque depende de un canal de acceso público y de la identificación de un paciente sin cuenta, dos problemas que el relevamiento muestra resueltos de formas muy distintas y que no están cerrados en este proyecto. Los recordatorios quedan como interfaz con adaptador simulado, porque su valor real depende de WhatsApp, que es una integración externa. El MVP se concentra en el único punto donde **todos los competidores relevados, en los tres mercados, dejaron un vacío documental completo**.

#### Diferenciador (si el MVP queda firme y hay margen)

1. **Reserva online del paciente**, con un identificador de acceso por enlace y sin cuenta previa, tomando como patrón el flujo de Dentally Portal y la segmentación por intención de Open Dental Web Sched (paciente nuevo contra paciente existente).
2. **Lista de espera sobre huecos liberados**: cuando se cancela un turno, el hueco queda registrado como ofrecible y la lista de espera se consulta por prestación y franja. Es el vacío ausente en toda la región y no requiere ninguna integración externa.
3. **Registro estructurado de motivos de cancelación y de ausencia**, con un reporte simple de ausentismo por profesional, por sillón y por prestación. Produce la información de gestión que hoy ningún competidor captura.
4. **Bloqueos de agenda** (vacaciones, ausencia del profesional, mantenimiento del sillón) como entidad del dominio que participa de la regla de disponibilidad igual que un turno.
5. **Exportación completa de los datos del consultorio** en un formato abierto. Evidenciada en solo 2 de 19 competidores.
6. **Roles diferenciados** —odontólogo dueño, odontólogo colega, recepción— con permisos efectivos sobre las operaciones de agenda, en particular sobre la autorización de sobreturnos.
7. **Odontograma y ficha clínica extendida**, únicamente si todo lo anterior está cerrado. El relevamiento muestra que es estándar de mercado, pero también que es el componente más costoso de construir bien: la variedad va del odontograma 3D con evolución temporal al adulto y pediátrico con numeración FDI, y hacerlo a medias no aporta nada.

#### A dejar para etapas posteriores

1. **WhatsApp Business API** para recordatorios y confirmaciones: requiere cuenta de negocio, claves, aprobación de plantillas y costo por mensaje. En la v1 solo existe el puerto de dominio y el adaptador simulado.
2. **Mercado Pago** para señas y cobros online.
3. **Facturación ARCA** (facturas A, B y C, notas de crédito y débito).
4. **Google Calendar** u otra sincronización de calendario externo.
5. **Obras sociales y prepagas** con nomenclador, cálculo de cobertura y liquidación mensual. Es un diferencial local valioso, pero es un dominio propio de complejidad comparable a la agenda entera.
6. **Caja, cuenta corriente del paciente y planes de cuotas.**
7. **Multi-sede**, con agenda y caja separadas.
8. **Reportes avanzados y métricas de gestión** más allá del reporte de ausentismo del bloque diferenciador.
9. **Cualquier funcionalidad de inteligencia artificial.** El relevamiento no encontró un solo caso con precisión o metodología demostrada; es la función más publicitada y la menos sostenida por evidencia del mercado. No corresponde a un MVP académico.
10. **Portal con historia clínica visible para el paciente**, periodontograma, consentimientos, firma digital, imágenes y radiografías.

---

## Verificación de fuentes

> **Esta sección la completan los estudiantes a mano.** Abrir cada URL, comparar con lo que afirma este informe y registrar el resultado. Si un dato no se sostiene al abrir la fuente, corregirlo en el informe y anotar la corrección en la última columna. Si la página ya no está disponible o cambió, anotarlo también: es un resultado válido.

### Las 5 fuentes que más conviene verificar, y por qué cada una es dudosa

Se eligió una fuente de cada tipo, priorizando aquellas de las que **dependen las conclusiones del informe**:

1. **Sitio oficial — https://dentia.tech/** · Es la fuente más influyente de todo el informe: sostiene el puesto 1 de la matriz de puntuación (4,50/5), el único caso argentino de facturación ARCA evidenciada y uno de los dos casos de Ley 25.326 declarada. **Toda esa evidencia proviene de una sola página** y es afirmación comercial sin demostración. La propia ficha advierte que la precisión de las funciones de IA es No evidenciado, que el caso "Clínica Sonrisas" es una demostración ficticia y que los costos adicionales de los mensajes de WhatsApp no están evidenciados. Si esta página no sostiene lo que el informe le atribuye, cambia el ranking completo.
2. **Precios — https://agendapro.com/ar/planes** · El informe declara una **inconsistencia explícita entre dos páginas del mismo proveedor**: la página de planes indica ARS 7.900 mensuales en el plan Individual, mientras la FAQ de la página dental indica "desde ARS 13.900" y una prueba de 14 días que no figura en planes. Además hay múltiples costos adicionales (packs de WhatsApp, Recepcionista IA, API a USD 200) y la aclaración de que los precios de lista aplican a cuentas nuevas. Verificar cuál es el precio real que paga un consultorio argentino, y si el tope de 50 recordatorios mensuales está donde el informe dice.
3. **Centro de ayuda — https://ayuda.softwaredentalink.com/es/collections/19196578-modulo-exclusivo-para-argentina** · De esta fuente **solo se leyeron los títulos de los artículos**, no su contenido. Sin embargo, sostiene dos afirmaciones importantes del informe: que Dentalink tiene obras sociales y prepagas con artículos sobre creación, recaudación y conciliaciones, y que incluye receta electrónica argentina con firma digital certificada. Es, además, el principal argumento de la presencia argentina de un producto chileno. Leer los artículos y confirmar si la funcionalidad está realmente descripta o si los títulos prometían más que el contenido.
4. **Reseñas — https://www.capterra.com/p/196361/Dentalink/** · Es la única fuente que atribuye un precio a Dentalink (USD 29 iniciales), un producto que **no publica precios en su sitio oficial**. El informe la cita explícitamente como fuente secundaria no verificada. Es doblemente dudosa: es un listado que puede estar desactualizado o referirse a otro mercado, y los sitios de reseñas bloquearon sistemáticamente el acceso durante el relevamiento (la ficha de Open Dental en Capterra devolvió 403), de modo que **ninguna reseña independiente de ningún producto pudo verificarse**. Abrirla manualmente es también la prueba de si este tipo de fuente es utilizable en adelante.
5. **Otro (sitio oficial bloqueado) — https://www.doctolib.fr/ y https://pro.doctolib.fr/** · Ambas devolvieron **403** y no pudieron leerse. Doctolib figura en la tabla A con prácticamente todas las celdas en "No evidenciado" y es el último puesto de la matriz (0,35/5), un puntaje que **no refleja el producto sino la imposibilidad de leerlo**. El único precio citado ("desde 139 € por profesional/mes") proviene del blog de un competidor, una fuente sesgada, y el comunicado oficial que respaldaría su cifra de adopción devolvió 404. Verificar manualmente desde un navegador permite decidir si Doctolib debe permanecer en el informe, corregirse o retirarse por falta de evidencia.

### Plantilla de verificación

Verificación realizada el **2026-11-18**, abriendo cada URL en el navegador.
Verificado por: **[COMPLETAR: nombre del integrante]**

| Fuente (URL) | Tipo | Qué afirma el informe | Qué encontré al abrirla | Qué corregí |
|---|---|---|---|---|
| https://dentia.tech/ | Sitio oficial | Puesto 1 de la matriz (4,50). Precios ARS 20.000/30.000/40.000, facturación ARCA, Ley 25.326 declarada, odontograma con periodontograma, recordatorios por email en Básico y por WhatsApp desde Pro, proveedor No evidenciado | **Se sostiene todo.** La página publica los tres precios, "Facturación electrónica ARCA" en el plan Básico y, en la FAQ, facturas A, B y C con notas de crédito y débito. El bloque de seguridad dice textualmente "Tratamiento de datos de salud conforme a la Ley 25.326", junto con 2FA, roles y registro de auditoría. El odontograma y el periodontograma figuran con carga por dictado de voz. La demo "Clínica Sonrisas" está rotulada como ficticia por el propio sitio. No aparece razón social en ninguna parte | **Nada.** Se confirma también que toda la evidencia proviene de una sola página y es afirmación comercial: no hay demostración, certificación externa ni dato de precisión de las funciones de IA |
| https://agendapro.com/ar/planes | Precios | AgendaPro Argentina cuesta ARS 7.900 / 15.900 / 69.900, con ARS 20.000 por profesional adicional y API a USD 200. Inconsistencia con la FAQ dental, que dice "desde ARS 13.900" | **El informe estaba equivocado.** Esos precios son de la vertical **Belleza y Barbería**, que se muestra por defecto. La propia tabla comparativa de esa vertical aclara que ficha clínica, presupuestos y consentimientos **no están incluidos**, solo en Medspa y Salud. La vertical **Salud**, que es la que corresponde a odontología, cuesta **ARS 19.900 / 44.900 / 189.900**, con profesional adicional a ARS 8.000 en Premium y ARS 20.000 en PRO IA. El precio de la API no figura: dice "Conversemos" | **Se corrigieron los tres precios en la tabla A** y se eliminó el dato de API a USD 200, que no está publicado. La nota de "Precio y facilidad de adopción" bajó de 3 a 2 y el total de 3,65 a 3,60. **La "inconsistencia" denunciada no era del proveedor sino del informe**, por comparar dos verticales distintas |
| https://ayuda.softwaredentalink.com/es/collections/19196578-modulo-exclusivo-para-argentina | Centro de ayuda | Dentalink tiene módulo argentino con obras sociales (creación, recaudación, conciliaciones) y receta electrónica argentina con firma digital certificada | La colección existe y tiene exactamente los 4 artículos citados, con esos títulos. Pero al abrir **"Receta electrónica Argentina"** (artículo 14744244, firmado por Patricia Maturana Palacios el 23 de abril de 2026) **el cuerpo está completamente vacío**: título, autora y fecha, y ni una línea de contenido | **La afirmación sobre receta electrónica pasa a "No evidenciado"**: se apoyaba en un artículo sin texto. Los títulos prometían más que el contenido, que era exactamente la sospecha. Las obras sociales siguen respaldadas por tres títulos, pero tampoco se leyó su contenido |
| https://www.capterra.com/p/196361/Dentalink/ | Reseñas | Única fuente del precio USD 29 de Dentalink, un producto que no publica precios | **[COMPLETAR]** La URL sigue devolviendo 403, ahora con una verificación anti-bot de Cloudflare ("Security Check") que un navegador automatizado no puede pasar. Abrir desde un navegador normal y anotar si el precio de USD 29 aparece, a qué plan corresponde y de qué fecha es | **[COMPLETAR]** |
| https://www.doctolib.fr/ y https://pro.doctolib.fr/ | Otro (sitio bloqueado) | Casi todas las celdas en "No evidenciado" y último puesto de la matriz (0,35), un puntaje que refleja la imposibilidad de leer el sitio y no el producto | **[COMPLETAR]** Ambas siguen devolviendo 403, con la verificación anti-bot de Cloudflare ("Êtes-vous humain ?"). Abrir desde un navegador normal y anotar si publica precios y qué funciones de agenda describe | **[COMPLETAR]** Decidir si Doctolib permanece en el informe con evidencia real, o se retira por falta de evidencia |

### Conclusión de la verificación

De las **tres fuentes que pudieron leerse, dos contenían afirmaciones que no se
sostienen**: un precio equivocado por un factor de 2,5 y una funcionalidad
respaldada por una página en blanco. Las dos fuentes restantes están protegidas
por verificación anti-bot y requieren apertura manual desde un navegador común.

El dato relevante para la metodología de este trabajo es que **ninguno de los
dos errores era evidente en el informe**: ambos estaban correctamente citados,
con URL y fecha, y clasificados como dato publicado. Solo aparecieron al abrir
la fuente y leerla.

---

## Limitaciones metodológicas

Esta sección no es un descargo: define el valor probatorio de todo el informe.

**1. Cómo se leyeron las páginas.** Las tres investigaciones (Argentina, Latinoamérica e internacional) se realizaron el 2026-11-18 exclusivamente sobre fuentes públicas: sitios oficiales, páginas de precios y, donde existían, centros de ayuda. Las páginas se leyeron con **herramientas de extracción automática de texto que resumen el contenido**. En consecuencia, todo lo que dependa de capturas de pantalla, videos, contenido dinámico o interacción quedó fuera del alcance: cuando una página tenía imágenes con texto alternativo (como las cinco de la agenda de Simples Dental), **no fue posible confirmar que fueran capturas reales del producto**. Varias FAQ quedaron con la pregunta visible y la respuesta no capturada (Simples Dental, Odonto.ar, MR Dental Cloud), de modo que preguntas directamente relevantes —seguimiento de ausentes, seguridad y continuidad de los datos— quedaron sin respuesta registrada.

**2. No se probó ningún producto.** No se crearon cuentas, no se usaron pruebas gratuitas, no se solicitaron demos y no se contactó a ningún proveedor. Por lo tanto **ninguna función de ningún producto está comprobada por uso**. Lo que el informe marca como "Comprobado" significa que el proveedor la describe con detalle verificable (precios, listas de planes, listas de módulos, flujos documentados), no que se la haya visto funcionar. En sentido estricto, casi toda la evidencia del informe es afirmación comercial del propio proveedor.

**3. Los sitios de reseñas independientes bloquearon el acceso.** **Capterra, G2, TrustRadius y Software Advice devolvieron 403, 404 o error** en todas las consultas intentadas (por ejemplo, la ficha de Open Dental en Capterra con 403, y la de Bestosys en Capterra México con 403). La consecuencia es grave y debe tenerse presente al leer la sección C.2: **no se pudo verificar de forma independiente ni una sola cifra de adopción, satisfacción o reseña**. Todas las cifras de clientes, profesionales, puntajes y reducciones de ausentismo citadas en el informe provienen del propio proveedor. Donde se citan datos de Capterra (el precio inicial de USD 29 de Dentalink, el "desde USD 50" de Dentidesk, los MXN 350 de MR Dental Cloud), provienen de **resúmenes de buscador y no de la ficha abierta**, y están marcados como no verificados.

**4. Qué significa "No evidenciado".** Significa, siempre, **"no hallado en las páginas consultadas"**, nunca "el producto no lo tiene". Esta distinción es crítica porque el criterio de puntuación de la sección B asigna 0 a "No evidenciado" y, por lo tanto, la matriz mide evidencia pública disponible, no calidad de producto. El caso de Open Dental lo ilustra: sus capacidades de agenda quedaron en No evidenciado porque su página de funciones solo indexa enlaces y remite a un *Quick Manual* que no se consultó, no porque un sistema maduro carezca de ellas.

**5. Sistemas con evidencia parcial o nula.**

- **Doctolib (Francia):** el sitio oficial (`doctolib.fr` y `pro.doctolib.fr`) devolvió **403** y no pudo leerse. Todo lo registrado proviene de fuentes secundarias, incluido un comparativo publicado por un competidor (Pabau), que es una fuente sesgada por definición. El comunicado oficial que sostendría su cifra de adopción (un PDF alojado en `res.cloudinary.com` sobre un estudio UFSBD de 2019) devolvió **404** al abrirlo, de modo que la cifra "74% de dentistas reducen tiempo de secretaría" proviene del resumen de un buscador y **no está verificada**.
- **Doctoralia Pro (Docplanner):** según Capterra tiene disponibilidad en Argentina, pero **su sitio argentino no pudo consultarse por un error de resolución de dominio**. Por esa razón quedó excluido del informe, pese a ser un competidor potencialmente relevante en el mercado argentino. Es una omisión conocida, no un descuido.
- **EXACT (Software of Excellence):** su web oficial publica muy poca información verificable; segmento, modalidad, administración, operación, seguridad y precio quedaron en No evidenciado. El sitio `softwareofexcellence.com.au` devolvió **error de certificado**. Un comunicado de prensa de HealthEngine y una nota de prensa sectorial sobre su recall online fueron citados por la búsqueda pero **no se abrieron**. Una reseña en Software Advice que menciona una caída de casi un mes y soporte lento es una fuente secundaria **no verificada** (el sitio devolvió 403).
- **Curve Dental:** su página de precios se titula "Affordable & Transparent Pricing" y **no publica ninguna cifra**.
- **Open Dental:** varias páginas de su propio sitio (`pricing.html`, `aboutus.html`, `webschedpatient.html`, `apiindex.html`) devolvieron **404**, por lo que el país de origen del proveedor quedó como No evidenciado.
- **Dentidesk:** sin precios ni información de seguridad públicas; sus tutoriales de YouTube no se revisaron.
- **Centros de ayuda no leídos:** `ajuda.simplesdental.com`, `ayuda.agendapro.com`, `clinicorp.com/politica-de-seguranca`, `status.clinicorp.com` y las políticas de privacidad y seguridad de varios proveedores quedaron enlazados pero sin leer. Del módulo argentino de Dentalink **solo se leyeron los títulos de los artículos**.

**6. Inconsistencias de precios detectadas entre páginas del mismo proveedor.** Se detectaron en cuatro proveedores distintos y ninguna pudo resolverse con la evidencia disponible:

| Proveedor | Inconsistencia |
|---|---|
| **AgendaPro (AR)** | La página de planes indica Individual a ARS 7.900/mes; la FAQ de la página dental indica "desde ARS 13.900" y una prueba de 14 días que no figura en planes. Además, la página incluye referencias chilenas (CENS, FONASA) cuyo alcance para Argentina no está confirmado |
| **Clinicorp** | Plan Standard a R$ 159,90 en `/planos` y R$ 149,90 en la home. Hay además recursos marcados con asterisco cuyos costos extra no se listan |
| **Simples Dental** | La misma página de precios muestra dos escalas (R$ 112,42 / 187,42 / 320,74 y R$ 137,41 / 229,08 / 320,74) sin aclarar a qué condición corresponde cada una |
| **Odonto.ar** | Declara que "todos los planes incluyen todas las funciones" y al mismo tiempo lista funciones que difieren por plan |
| **MR Dental Cloud** | La página indica que la facturación CFDI se paga solo por factura emitida, lo que no se concilia con el esquema de planes mensuales |
| **Dentalink** | Sin precios publicados en el sitio oficial, mientras una fuente secundaria (Capterra, vía buscador) atribuye un precio inicial de USD 29 |

**7. Contradicciones entre fuentes secundarias.** Los comparadores y buscadores devolvieron datos contradictorios: **ComparaSoftware ubica a Dentalink en España**, mientras su sitio oficial lo declara chileno. El criterio aplicado fue que **prevalece siempre el sitio oficial**. Por la misma razón se descartó Dently, cuya ficha en ComparaSoftware indica datos alojados en la Unión Europea y precio en euros, sin origen latinoamericano demostrado. Los blogs de proveedores competidores (turnito.app, dentalcore.app, medesk.net, pabau.com) se usaron únicamente para **descubrir** productos, nunca como evidencia.

**8. Limitación de cobertura geográfica.** No se encontró evidencia verificable de ningún producto odontológico de **origen colombiano, peruano o uruguayo** con sitio oficial legible: en esos países la oferta visible son productos chilenos, brasileños o mexicanos con presencia regional. Productos mencionados solo en buscadores (CentralCliniq en Uruguay; MaxIdent, Dentech e Identalsoft en Perú; Bestosys y Dentimax en México y Colombia) quedaron fuera por falta de fuente oficial verificada. En Argentina se detectaron pero **no se relevaron en profundidad** DentalCore, Benty y Turnito (una agenda genérica con precio de ARS 5.900 según el blog de un competidor, no verificado en su sitio oficial). Tampoco se incluyeron sistemas de instalación local tradicionales, de los que no se halló evidencia pública consultable.

**9. Identidad de los proveedores.** Razón social, domicilio y políticas de privacidad completas de varios proveedores argentinos quedaron en **No evidenciado** (Odonto.ar, Dentia, DENTQ, Dentidesk). En Odonto.ar, DENTQ y VecDent el país se infiere de los precios en pesos y del prefijo telefónico, no de una declaración explícita.

**10. Volatilidad de los datos.** Todos los precios y funciones son los vigentes al **2026-11-18**. Varias páginas tenían promociones activas y por tiempo limitado al momento de la consulta (los tres primeros meses a ARS 990 de AgendaPro, el "Black November 2026" con hasta 50% de descuento de Clinicorp, el Contact Center IA a USD 50/mes de Dentalink). Cualquier reutilización posterior de este informe exige volver a consultar las fuentes.

---

## Referencias completas

Todas las URLs fueron consultadas el **2026-11-18**. Se indica entre paréntesis el tipo de fuente y, cuando corresponde, el resultado de la consulta.

### Argentina

**Odonto.ar**
- https://odonto.ar/ (sitio oficial y precios en la misma página — leída)
- https://odonto.ar/privacidad (política de privacidad — enlazada, no analizada en profundidad)

**Dentia**
- https://dentia.tech/ (sitio oficial y precios en la misma página — leída; rutas legales listadas en la home, no leídas en detalle)

**DentiClick (ThunderSkills)**
- https://www.denticlick.app/ (sitio oficial y precios en la misma página — leída)

**DENTQ (DosNubesMedia)**
- https://dosnubesmedia.com/dentq (sitio oficial y precios en la misma página — leída)

**VecDent (Vectora)**
- https://vectora.ar/pages/vecdent/ (sitio oficial y precios; redirige desde vectora.com.ar — leída)

**AgendaPro (oferta Argentina)**
- https://agendapro.com/ar/dental/software-odontologico (sitio oficial, vertical dental — leída)
- https://agendapro.com/ar/planes (precios — leída)

**Dentalink (Engenis SpA), versión argentina**
- https://www.softwaredentalink.com/ar (sitio oficial — leída)
- https://ayuda.softwaredentalink.com/es/collections/19196578-modulo-exclusivo-para-argentina (centro de ayuda, módulo exclusivo para Argentina — **solo títulos de artículos**)

### Latinoamérica (excluye Argentina)

**Dentalink (Chile)**
- https://www.softwaredentalink.com/?c=cl (sitio oficial — leída)
- https://www.softwaredentalink.com/planes?c=cl (planes, sin precios — leída)
- https://www.softwaredentalink.com/experiencia-de-pacientes/agenda (sitio oficial, producto agenda — leída)
- https://www.dentalink.cl (redirige al sitio oficial)
- https://web-es.softwaredentalink.com/en/planes (precios, versión España/inglés — sin precios)

**Simples Dental (Brasil)**
- https://www.simplesdental.com (sitio oficial — leída)
- https://www.simplesdental.com/planos-e-precos (precios — leída)
- https://www.simplesdental.com/agenda-online (sitio oficial, producto agenda — leída)
- https://ajuda.simplesdental.com/ (centro de ayuda — enlazado, **no leído**)

**Clinicorp (Brasil)**
- https://www.clinicorp.com (sitio oficial — leída)
- https://www.clinicorp.com/planos (precios — leída)
- https://www.clinicorp.com/gestao-financeira-clinipay (sitio oficial, producto financiero — leída)
- https://www.clinicorp.com/software-para-franquias-e-redes-de-odontologia-e-estetica (sitio oficial, segmento redes — listada)
- https://www.clinicorp.com/politica-de-seguranca y https://www.clinicorp.com/politica-de-privacidade (seguridad y privacidad — enlazadas, **no leídas**)
- https://status.clinicorp.com/ (estado del servicio — enlazada, **no leída**)

**AgendaPro (oferta México)**
- https://agendapro.com/mx/dental/software-odontologico (sitio oficial, vertical dental — leída)
- https://agendapro.com/mx/planes (precios — leída)
- https://ayuda.agendapro.com (centro de ayuda — enlazado, **no leído**)

**Dentidesk (Chile / República Dominicana)**
- https://dentidesk.com (sitio oficial — leída; tutoriales de YouTube **no revisados**)
- https://www.capterra.es/software/184029/dentidesk (directorio/reseñas — **fuente secundaria, precio inicial no verificado**)

**MR Dental Cloud (México)**
- https://www.mrdentalcloud.com (sitio oficial — leída)
- https://www.mrdentalcloud.com/security-compliance (seguridad — **no leída en detalle**)
- https://www.capterra.com.au/software/1109793/Mr-dentalcloud (directorio/reseñas — **fuente secundaria, precio inicial no verificado**)

### Internacionales

**Open Dental**
- https://www.opendental.com/ (sitio oficial — leída)
- https://opendental.com/site/fees.html (precios — leída)
- https://www.opendental.com/site/eservices.html (sitio oficial, funciones de eServices y Web Sched — leída)
- https://www.opendental.com/site/apispecification.html (documentación de la API REST — leída)
- https://www.opendental.com/site/features.html (sitio oficial — leída, **solo índice de enlaces**)
- https://www.opendental.com/site/pricing.html, /aboutus.html, /webschedpatient.html, /apiindex.html (sitio oficial — **404**)
- https://www.capterra.com/p/133098/Open-Dental/ (reseñas — **403, sin verificar**)

**tab32**
- http://tab32.com/ (sitio oficial — leída)
- http://tab32.com/pricing (precios — leída)

**Ascend by Dentrix (Henry Schein One)**
- https://www.dentrixascend.com/ (sitio oficial — leída)

**Curve Dental (CD Newco, LLC)**
- https://www.curvedental.com/ (sitio oficial — leída)
- https://www.curvedental.com/pricing (precios — leída, **sin cifras publicadas**)

**Dentally (Henry Schein One UK Ltd)**
- https://www.dentally.com/en-gb/ (sitio oficial — leída)
- https://www.dentally.com/en-gb/pricing (precios — leída)
- https://www.dentally.com/en-gb/dentally-portal (ayuda funcional, portal de reservas — leída)

**EXACT (Software of Excellence, Henry Schein One)**
- https://www.henryscheinone.com/products/exact/ (sitio oficial — leída, vía búsqueda)
- https://www.softwareofexcellence.com.au/ (sitio oficial — **error de certificado**)
- https://healthengine.com.au/press/2020/03/10/healthengine-and-henry-schein-one-australia-partnership-delivers-online-booking-and-patient-network-integration/ (comunicado de prensa — citada por búsqueda, **no abierta**)
- https://www.dentalcommunity.com.au/news/exact-launches-online-recall-system/2e1ecfbe-a16c-2f9b-919a-4fe53a059e33 (prensa sectorial — citada por búsqueda, **no abierta**)
- https://www.softwareadvice.com.au/software/395229/exact (reseñas — **403, solo resumen de búsqueda**)

**Doctolib**
- https://www.doctolib.fr/ y https://pro.doctolib.fr/ (sitio oficial — **403, no pudo leerse**)
- https://pabau.com/fr/blog/planity-pro-vs-doctolib/ (comparativo publicado por un competidor — citada por búsqueda, **fuente sesgada**)
- Comunicado Doctolib/UFSBD en PDF, alojado en res.cloudinary.com (comunicado oficial — **404**)

### Fuentes secundarias, comparadores y blogs (usados solo para descubrimiento, no como evidencia)

- https://www.capterra.com/p/196361/Dentalink/ (reseñas/listado — **fuente secundaria**; único origen del precio inicial de USD 29 de Dentalink, **no verificado** en el sitio oficial)
- https://www.capterra.com/p/253301/Doctoralia-Pro/ (reseñas/listado — solo descubrimiento de Doctoralia Pro)
- https://www.capterra.mx/software/144398/bestosys (directorio — **403, no leído**)
- https://www.comparasoftware.com/dently (comparador — producto Dently **descartado** por falta de origen latinoamericano demostrado)
- https://www.comparasoftware.com/odontologia (comparador — fuente secundaria; **ubica erróneamente a Dentalink en España**)
- https://turnito.app/blog/las-mejores-apps-de-turnos-para-odontologos-en-argentina-2026/ (blog de un proveedor competidor — descubrimiento, **no usado como evidencia**)
- https://dentalcore.app/blog/mejor-software-dental-argentina (blog de proveedor — descubrimiento, **no usado como evidencia**)
- https://www.medesk.net/es/blog/software-para-clinicas-dentales/ y https://www.medesk.net/es/blog/precios-software-medico-colombia/ (blog de un proveedor no latinoamericano — solo contexto regulatorio, **no usado como evidencia**)

### Fichas de relevamiento que respaldan este informe

- `docs/discovery/sources/competidores-argentina.md`
- `docs/discovery/sources/competidores-latam.md`
- `docs/discovery/sources/competidores-internacionales.md`
- `docs/discovery/discovery.md` (checklist de once puntos del Discovery)
