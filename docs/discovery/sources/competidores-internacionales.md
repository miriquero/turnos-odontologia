# Competidores internacionales — gestión de turnos y agenda odontológica

**Alcance:** sistemas de EE.UU., Reino Unido, Australia/Nueva Zelanda y Francia, relevantes como referencia para un producto argentino de turnos odontológicos.
**Fecha de relevamiento:** 2026-11-18 (todas las URLs fueron consultadas ese día).

**Metodología:**
1. Solo fuentes públicas (sitio oficial, página de precios, centro de ayuda, documentación, notas de prensa), sin crear cuentas ni contactar proveedores.
2. Cada dato lleva su fuente. Se distingue **[Comprobado]** (documentación, precios o funcionalidad descrita en detalle por el proveedor) de **[Afirmación comercial]** (declaración de marketing sin demostración). Lo que no se encontró figura como **"No evidenciado"**; no se completó nada por deducción.
3. Limitaciones: el contenido se extrajo con una herramienta de lectura automática que resume páginas; varios sitios bloquearon la consulta (Doctolib oficial, Capterra, G2, TrustRadius, Software Advice devolvieron 403/404/error). Por eso "No evidenciado" significa "no hallado en las páginas consultadas", no "el producto no lo tiene". Ninguna reseña de Capterra/G2 pudo verificarse directamente. Se recomienda una verificación manual posterior con la sección "Fuentes consultadas".

Sistemas relevados: **Open Dental, tab32, Ascend by Dentrix, Curve Dental, Dentally, EXACT (Software of Excellence)** con evidencia razonable, y **Doctolib** con evidencia limitada (solo fuentes secundarias y un comunicado, porque el sitio oficial bloqueó la consulta).

---

## 1. Open Dental

1. **Identidad:** Open Dental, de Open Dental Software. País de origen: No evidenciado en las páginas leídas (la página institucional devolvió 404). URL: https://www.opendental.com/ (2026-11-18).
2. **Segmento:** "practices of any size" **[Afirmación comercial]** (https://www.opendental.com/, 2026-11-18). Mención de ubicaciones con clave de registro propia (https://www.opendental.com/site/eservices.html).
3. **Modalidad:** Existe "Open Dental Cloud", con precio aparte de licencia y soporte **[Comprobado]** (https://opendental.com/site/fees.html, 2026-11-18). El modelo base (instalación local) No evidenciado explícitamente.
4. **Agenda:** operatorios, proveedores, bloqueos, duración variable, sobreturnos y prevención de solapamientos: No evidenciado (la página de features solo indexa enlaces; remite al Quick Manual, no consultado). Fuente: https://www.opendental.com/site/features.html.
5. **Turnos digitales:** **[Comprobado]** Web Sched con cuatro variantes: Nuevo Paciente, Paciente Existente (reserva en el sitio web del consultorio), Recall (enlace por texto o email) y ASAP (enlace a pacientes en lista ASAP, es decir, lista de espera de facto). Confirmación electrónica por eConfirmations. Fuente: https://www.opendental.com/site/eservices.html (2026-11-18). Chatbot: No evidenciado. Cancelación/reprogramación online: No evidenciado.
6. **Automatización:** **[Comprobado]** eReminders (texto/email), eConfirmations, Mass Email, Integrated Texting con respuestas entrantes, recall vía Web Sched. Seguimiento de ausentes y campañas: No evidenciado (solo Mass Email). Fuente: eservices.html.
7. **Clínica:** **[Comprobado]** odontograma 3D: "3D tooth chart shows patient dentition and treatment over time" (https://www.opendental.com/, 2026-11-18). Portal del paciente muestra turnos, planes de tratamiento e imágenes (eservices.html). Periodontograma, anamnesis, imágenes y presupuestos como módulos: No evidenciado en lo consultado.
8. **Administración:** facturación (versión 26.2 con "Overhauled Billing Defaults and Statement Options"), "20+ Clearinghouses" **[Comprobado, sin detalle]**. Portal de pagos y Message-to-Pay incluidos con soporte (fees.html). Reportes: No evidenciado.
9. **Integraciones:** **[Comprobado]** API REST documentada (GET/POST/PUT/DELETE; separada de una API FHIR); el desarrollador debe tener un BAA con sus clientes; hay niveles de precio para desarrolladores y soporte telefónico "muy limitado" (https://www.opendental.com/site/apispecification.html, 2026-11-18). Integración con prescripción electrónica (DoseSpot, Ensora) y análisis de imágenes con IA como add-ons (fees.html).
10. **Operación:** soporte, sucursales (licencia por ubicación), roles, auditoría y exportación: No evidenciado, salvo garantía de devolución a 90 días y referencias de clientes a pedido (fees.html).
11. **Seguridad:** HIPAA/GDPR: No evidenciado en la página principal. La documentación de la API menciona BAA (apispecification.html).
12. **Modelo comercial:** **[Comprobado]** precios publicados en USD por mes y por ubicación: EE.UU. USD 199 los primeros 12 meses, luego USD 149; Canadá USD 164/137; otros países USD 89; países en desarrollo, gratis con soporte limitado; USD 20/mes por proveedor desde el cuarto. Complementos: Web Sched Recall o Nuevo/Existente USD 75/mes cada uno, ASAP USD 20, bundle eServices USD 165, eClipboard USD 45, Texting desde USD 5 más cargo por mensaje, IA de imágenes USD 149. Prueba gratuita: No evidenciado. Fuente: https://opendental.com/site/fees.html (2026-11-18).
13. **Fortalezas/limitaciones (análisis propio sobre lo comprobado):** precios transparentes y granulares, con API REST abierta; la contracara es un modelo de múltiples add-ons que eleva el costo total (reservas online por separado de la agenda) y una página institucional sin detalle clínico en la home.
14. **Adopción:** cantidad de clientes y reseñas: No evidenciado (Capterra devolvió 403).

## 2. tab32

1. **Identidad:** tab32, Inc., Sacramento, California (EE.UU.). URL: http://tab32.com/ (2026-11-18). Producto "Alpine" (consultorio) y "Summit" (grupos/DSO).
2. **Segmento:** consultorios privados (solo, nuevos, asociados); Summit para multi-ubicación/DSO.
3. **Modalidad:** nube, en navegador, "no server in a closet" **[Afirmación comercial]**.
4. **Agenda:** columnas por operatorio, lista de espera, reserva online, formularios y estado de sillón en vivo **[Comprobado a nivel de lista de funciones]**. Bloqueos, sobreturnos, duración variable: No evidenciado. (http://tab32.com/)
5. **Turnos digitales:** reserva online y lista de espera listadas en agenda. Reprogramación/cancelación online y chatbot: No evidenciado. Hay "agentes" de IA (llamadas a USD 0,05/min) cuyo alcance específico no se detalló.
6. **Automatización:** recordatorios y recall por texto; confirmación primero por texto y llamada solo a no confirmados **[Comprobado, descripción comercial]**. Seguimiento de ausentes: No evidenciado explícitamente.
7. **Clínica:** charting con plan de tratamiento y notas; periodontograma por voz; imágenes desde sensores; IA de hallazgos radiológicos como add-on; escriba IA **[Afirmación comercial / descripción de funciones]**.
8. **Administración:** elegibilidad, armado de reclamos, estado electrónico, auto-posteo de ERA, cola de rechazos, apelaciones, estados de cuenta y pagos electrónicos. Seguros de EE.UU. (no aplica a Argentina).
9. **Integraciones:** API pública: No evidenciado. Solo migración desde Dentrix, Eaglesoft, Open Dental, Curve (≈20 sistemas).
10. **Operación:** usuarios y proveedores ilimitados; exportación única gratuita al cancelar (pacientes, fichas, imágenes, libro mayor) **[Comprobado]**; cancelación en un clic. Roles y auditoría: No evidenciado.
11. **Seguridad:** "HIPAA compliant", BAA incluido, SOC 2, cifrado en tránsito y en reposo **[Afirmación comercial]**; informe SOC 2 no mostrado.
12. **Modelo comercial:** **[Comprobado]** USD 95/mes con usuarios y proveedores ilimitados, mes a mes, sin cargo de instalación, precio sin cambios tras el año 1 (aviso de 60 días); prueba de 14 días con tarjeta; cargos por uso: USD 0,25 por reclamo, USD 0,05 por texto, USD 0,75 por chequeo IA, auto-posteo 1,25 %; agentes USD 25/mes cada uno o bundle USD 325-349; migración desde USD 1.250 (https://tab32.com/pricing, 2026-11-18).
13. **Fortalezas/limitaciones:** transparencia de precios y cancelación excepcionales; fuerte apuesta por IA. Limitación: costos por uso complejos y foco de seguros de EE.UU.; sin API evidenciada.
14. **Adopción:** clientes y reseñas: No evidenciado.

## 3. Ascend by Dentrix (Henry Schein One)

1. **Identidad:** Ascend by Dentrix, Henry Schein One; dirección en American Fork, Utah, EE.UU. (país no declarado textualmente). URL: https://www.dentrixascend.com/ (2026-11-18).
2. **Segmento:** consultorios y grupos multi-ubicación; "Ascend Starter" para nuevos propietarios.
3. **Modalidad:** "100% cloud-based" **[Afirmación comercial]**.
4. **Agenda:** "smart scheduling" que automatiza reservas y verifica seguros **[Afirmación comercial]**. Detalles de operatorios, bloqueos, sobreturnos: No evidenciado.
5. **Turnos digitales:** reserva online del paciente **[Afirmación comercial]**; lista de espera, reprogramación online, chatbot: No evidenciado.
6. **Automatización:** recordatorios de cita, recare, confirmaciones, mensajería bidireccional; "66M+ recare and appointment reminders sent" (cifra del proveedor).
7. **Clínica:** Quick Exam, detección por IA, notas por voz, imágenes en la nube y verificación de calidad de rayos X. Periodontograma y presupuestos como módulo: No evidenciado.
8. **Administración:** elegibilidad, reclamos, ERA, cobros online, "Revenue Cycle AI".
9. **Integraciones:** "Ascend API Exchange" **[Afirmación comercial; documentación pública no verificada]**.
10. **Operación:** roles basados en permisos mencionados; soporte, auditoría, exportación: No evidenciado.
11. **Seguridad:** HIPAA, SOC 2, respaldos cifrados y acceso por roles **[Afirmación comercial]**.
12. **Modelo comercial:** Starter USD 199/mes; resto personalizado (No evidenciado). Prueba: No evidenciado, solo demo.
13. **Fortalezas/limitaciones:** suite completa nativa en la nube con respaldo de un grupo grande; precios mayormente no públicos.
14. **Adopción:** "80k+" usuarios, "83% of insurance claims processed annually through Ascend" (cifras del proveedor, **Afirmación comercial**). Reseñas: No evidenciado.

## 4. Curve Dental

1. **Identidad:** Curve Dental, operada por CD Newco, LLC, Alpharetta, Georgia (EE.UU.). URL: https://www.curvedental.com/ (2026-11-18).
2. **Segmento:** consultorios independientes, multi-ubicación/DSO y startups.
3. **Modalidad:** nube desde 2004, navegador (Mac, PC, Chromebook), sin servidor local.
4. **Agenda:** función "Scheduling" listada; detalles: No evidenciado.
5. **Turnos digitales:** reserva online listada como parte de la experiencia del paciente; lista de espera: No evidenciado.
6. **Automatización:** recordatorios y "Curve GRO Patient Engagement" listados; recall/ausentes: No evidenciado en detalle.
7. **Clínica:** charting, periodontograma, imágenes, plan de tratamiento listados. El término "odontograma" no figura.
8. **Administración:** facturación a seguros y pacientes, verificación, Curve Pay, planes de membresía.
9. **Integraciones:** API: No evidenciado.
10. **Operación:** soporte con base en EE.UU.; el resto No evidenciado.
11. **Seguridad:** infraestructura "HIPAA-compliant" **[Afirmación comercial]**, sin certificaciones.
12. **Modelo comercial:** precios **no públicos** (página titulada "Affordable & Transparent Pricing" pero sin cifras; se pide cotización) (https://www.curvedental.com/pricing, 2026-11-18). Prueba: No evidenciado.
13. **Fortalezas/limitaciones:** cloud nativo veterano; la "transparencia" de precios declarada no se cumple en la página.
14. **Adopción:** "100,000+ active users", "6,000+ practice locations", "4.6/5 across 500+ reviews" (cifras de la propia web, **Afirmación comercial**; no verificadas en un sitio de reseñas independiente).

## 5. Dentally (Henry Schein One UK)

1. **Identidad:** Dentally, Henry Schein One UK Ltd, Gillingham, Kent (Reino Unido). URL: https://www.dentally.com/en-gb/ (2026-11-18). Versiones para Australia, Canadá, Irlanda y NZ.
2. **Segmento:** independientes, multi-sede, nuevas prácticas, NHS.
3. **Modalidad:** "cloud enabled" **[Afirmación comercial]**.
4. **Agenda:** "diary", lista de espera y herramientas de reserva inteligente **[Afirmación comercial]**; detalles: No evidenciado.
5. **Turnos digitales:** **[Comprobado]** Dentally Portal: reserva por enlace (email/SMS), enlace en el sitio web o código QR; acceso con móvil verificado y código de un solo uso; configurable por marca, horarios, duración, tipos de tratamiento, sesiones y profesionales; elección de sede en multi-sede; actualiza la base en tiempo real (https://www.dentally.com/en-gb/dentally-portal). Lista de espera del Portal: No evidenciado.
6. **Automatización:** recordatorios y confirmaciones automáticas **[Afirmación comercial]**.
7. **Clínica:** charting mencionado; imágenes con "Dentally Vision" (IA); notas clínicas con IA; periodontograma y presupuestos: No evidenciado.
8. **Administración:** pagos como funcionalidad del Portal y marketplace; integración NHS incluida.
9. **Integraciones:** marketplace de partners y "Developer Hub" referidos; la documentación de la API no fue consultada: No evidenciado. El plan Pro incluye integraciones "Custom".
10. **Operación:** multi-sede solo desde Essentials; usuarios ilimitados; soporte por chat o chat y teléfono según plan.
11. **Seguridad:** GDPR: No evidenciado (solo enlace a una política).
12. **Modelo comercial:** **[Comprobado]** precios en GBP, sin IVA, por mes y para 3 consultorios: Starter £134-£618, Essentials £238-£778, Pro £346-£1.021 (rangos tal como los muestra la página); sin precio anual ni onboarding (https://www.dentally.com/en-gb/pricing). Prueba: No evidenciado, solo demo.
13. **Fortalezas/limitaciones:** el Portal de reservas es el mejor documentado de la muestra; el plan Starter carece de Portal y multi-sede (reserva online reservada a planes superiores).
14. **Adopción:** clientes y reseñas: No evidenciado (tres testimonios sin puntaje).

## 6. EXACT (Software of Excellence, Henry Schein One)

1. **Identidad:** EXACT de Software of Excellence (Henry Schein One); disponible en Reino Unido, Australia y Nueva Zelanda (https://www.henryscheinone.com/products/exact/, 2026-11-18; vía búsqueda).
2. **Segmento:** No evidenciado.
3. **Modalidad:** No evidenciado (la página no aclara si es nube).
4. **Agenda:** "Take full control of your schedule" **[Afirmación comercial]**; sin detalle.
5. **Turnos digitales:** sistema de reserva para pacientes **[Afirmación comercial]**; conectado al diario del consultorio según referencias secundarias de prensa; alianza con HealthEngine (2020) para reserva online en su red (https://healthengine.com.au/press/2020/03/10/healthengine-and-henry-schein-one-australia-partnership-delivers-online-booking-and-patient-network-integration/). Una reseña en Software Advice menciona caída de casi un mes y soporte lento (fuente secundaria; no verificada).
6. **Automatización:** recall online lanzado según nota de prensa sectorial (dentalcommunity.com.au); recordatorios: No evidenciado.
7. **Clínica:** integración con Pearl AI para detectar condiciones en radiografías; charting, odontograma, periodontograma, presupuestos: No evidenciado.
8. **Administración:** facturación/HICAPS: No evidenciado.
9. **Integraciones:** API pública: No evidenciado; hay integraciones de terceros.
10. **Operación y 11. Seguridad:** No evidenciado.
12. **Modelo comercial:** precio y prueba: No evidenciado (el precio no es público).
13. **Fortalezas/limitaciones:** trayectoria larga en el mercado anglosajón de Oceanía/UK; la web oficial publica muy poca información verificable.
14. **Adopción:** No evidenciado.

## 7. Doctolib (evidencia limitada)

1. **Identidad:** Doctolib, Francia. El sitio oficial (doctolib.fr, pro.doctolib.fr) devolvió 403; no se pudo leer. Es una plataforma de agenda médica, no un software clínico odontológico integral.
2-3. Segmento y modalidad: No evidenciado en fuente oficial leída.
4-6. Agenda, reserva 24/7 y recordatorios SMS/email: **solo afirmaciones de terceros** (comparativos de competidores como Pabau y agregadores); no verificado en sitio oficial.
7-9. Odontograma, periodontograma, administración, API: No evidenciado.
10-11. Operación y cumplimiento (RGPD/HDS): No evidenciado.
12. Precio: figura "desde 139 € TTC por praticien/mes" según un comparativo de un competidor (Pabau, https://pabau.com/fr/blog/planity-pro-vs-doctolib/): dato secundario no confiable; precio oficial **No evidenciado**.
13. Valor para este estudio: es la referencia europea del modelo de "marketplace" donde el paciente busca y reserva con distintos profesionales; hace falta verificación manual de su sitio.
14. Adopción: un comunicado de Doctolib del 2019 sobre un estudio UFSBD (res.cloudinary.com/doctolib/...191127_-_CP_-_Etude_nationale_UFSBD-Doctolib.pdf) fue citado por la búsqueda, pero el PDF devolvió 404 al abrirlo; la cifra "74 % de dentistas reducen tiempo de secretaría" proviene del resumen de la búsqueda, **no verificado** y proveniente del propio proveedor.

---

## Tabla resumen

| Producto | Proveedor | País · Segmento | Reserva online | Recordatorios | Odontograma | API pública | Precio publicado | Fuente y fecha |
|---|---|---|---|---|---|---|---|---|
| Open Dental | Open Dental Software | País No evidenciado · consultorios de cualquier tamaño | Sí, Comprobado (Web Sched, add-on pago) | Sí, Comprobado (eReminders, gratis con soporte) | Sí, odontograma 3D (Comprobado) | Sí, REST documentada | Sí (USD 89-199/mes por ubicación + add-ons) | opendental.com/site/fees.html, eservices.html, apispecification.html · 2026-11-18 |
| tab32 | tab32, Inc. | EE.UU. · consultorio/DSO | Sí (afirmación comercial) | Sí (afirmación comercial) | Charting sí; "odontograma" No evidenciado | No evidenciado | Sí (USD 95/mes + uso) | tab32.com/pricing · 2026-11-18 |
| Ascend by Dentrix | Henry Schein One | EE.UU. (implícito) · consultorio/grupo | Sí (afirmación comercial) | Sí (afirmación comercial) | Charting sí (Quick Exam); odontograma No evidenciado | "API Exchange" (afirmación; docs No evidenciado) | Parcial (Starter USD 199/mes) | dentrixascend.com · 2026-11-18 |
| Curve Dental | CD Newco, LLC | EE.UU./Canadá · consultorio/DSO | Sí (listado) | Sí (listado) | Charting sí; "odontograma" No evidenciado | No evidenciado | No evidenciado (no público) | curvedental.com, /pricing · 2026-11-18 |
| Dentally | Henry Schein One UK | Reino Unido · consultorio/multi-sede/NHS | Sí, Comprobado (Portal; solo Essentials/Pro) | Sí (afirmación comercial) | Charting mencionado; odontograma No evidenciado | No evidenciado (Developer Hub referido) | Sí (rangos GBP por plan) | dentally.com/en-gb/pricing, /dentally-portal · 2026-11-18 |
| EXACT | Software of Excellence (Henry Schein One) | UK/AU/NZ · No evidenciado | Sí (afirmación comercial; HealthEngine) | No evidenciado | No evidenciado | No evidenciado | No evidenciado (no público) | henryscheinone.com/products/exact · 2026-11-18 |
| Doctolib | Doctolib | Francia · No evidenciado | Solo fuentes secundarias | Solo fuentes secundarias | No evidenciado | No evidenciado | No evidenciado (cifra secundaria no confiable) | Sitio oficial bloqueado · 2026-11-18 |

---

## Fuentes consultadas

| URL | Tipo | Resultado (2026-11-18) |
|---|---|---|
| https://www.opendental.com/ | Sitio oficial | Leída |
| https://opendental.com/site/fees.html | Precios | Leída |
| https://www.opendental.com/site/eservices.html | Sitio oficial / funciones | Leída |
| https://www.opendental.com/site/apispecification.html | Documentación API | Leída |
| https://www.opendental.com/site/features.html | Sitio oficial | Leída (solo índice) |
| https://www.opendental.com/site/pricing.html, aboutus.html, webschedpatient.html, apiindex.html | Sitio oficial | 404 |
| https://www.capterra.com/p/133098/Open-Dental/ | Reseñas | 403, sin verificar |
| http://tab32.com/ y http://tab32.com/pricing | Sitio oficial / precios | Leídas |
| https://www.dentrixascend.com/ | Sitio oficial | Leída |
| https://www.curvedental.com/ y /pricing | Sitio oficial / precios | Leídas |
| https://www.dentally.com/en-gb/, /pricing, /dentally-portal | Sitio oficial / precios / ayuda funcional | Leídas |
| https://www.henryscheinone.com/products/exact/ | Sitio oficial | Leída |
| https://healthengine.com.au/press/2020/03/10/healthengine-and-henry-schein-one-australia-partnership-delivers-online-booking-and-patient-network-integration/ | Comunicado de prensa | Citada por búsqueda, no abierta |
| https://www.dentalcommunity.com.au/news/exact-launches-online-recall-system/2e1ecfbe-a16c-2f9b-919a-4fe53a059e33 | Prensa sectorial | Citada por búsqueda, no abierta |
| https://www.softwareadvice.com.au/software/395229/exact | Reseñas | 403, solo resumen de búsqueda |
| https://www.softwareofexcellence.com.au/ | Sitio oficial | Error de certificado |
| https://www.doctolib.fr/ y https://pro.doctolib.fr/ | Sitio oficial | 403 |
| https://pabau.com/fr/blog/planity-pro-vs-doctolib/ | Comparativo de un competidor | Citada por búsqueda; fuente sesgada |
| Doctolib/UFSBD comunicado PDF (res.cloudinary.com) | Comunicado oficial | 404 |

---

## Referencias de experiencia de usuario

Aclaración: la evidencia de UX de este relevamiento proviene de descripciones del proveedor, no de pruebas de uso; ninguna reseña independiente se pudo verificar.

1. **Dentally Portal (reserva del paciente).** Es la experiencia de reserva mejor documentada: acceso por enlace SMS/email, sitio web o QR, verificación con móvil y código único, configuración por profesional, tipo de tratamiento y duración, elección de sede y sincronización en tiempo real (https://www.dentally.com/en-gb/dentally-portal). Es un patrón directamente trasladable.
2. **Open Dental Web Sched.** Distingue cuatro flujos de reserva según el contexto (paciente nuevo, existente, recall, lista ASAP), cada uno con su enlace por texto/email. Esa segmentación de intención es evidencia concreta y el único caso donde la lista de espera se ve operativa (https://www.opendental.com/site/eservices.html). Costo: se paga como add-on separado.
3. **tab32 (confirmación y cancelación).** Confirmación "text-first" y llamada solo a los no confirmados, cancelación de la suscripción en un clic, un solo cobro itemizado y una exportación gratuita de datos al irse (http://tab32.com/pricing). Es una referencia de UX de relación comercial y de reducción de carga administrativa, aunque se trate de afirmaciones del proveedor.
4. **Ascend by Dentrix.** Recordatorios, recare y mensajería bidireccional integrados en la misma plataforma (https://www.dentrixascend.com/), con cifra de uso del proveedor; útil como referencia de integración de comunicación con la agenda, sin evidencia independiente de la calidad de la UX.

Advertencia crítica: los hallazgos negativos ("No evidenciado") deben leerse junto a la limitación metodológica del inicio. Antes de usar este documento como base de decisión de producto se recomienda verificar manualmente Curve, EXACT y Doctolib, y las reseñas en Capterra/G2.
