# SplitUp — Hoja de ruta y progreso (TFG)

> Autor: Alejandro Córdoba Pérez  
> Proyecto: SplitUp  
> Última actualización: 2026-01-29

Este documento sirve como referencia **viva** del progreso del proyecto.  
Cuando avances una tarea, marca su checkbox y (opcional) añade una nota breve.

---

## ✅ FASE 0 · Identidad y base del proyecto (COMPLETADA)

**Objetivo:** Definir identidad, alcance y estructura inicial del proyecto.

- [x] Nombre del proyecto: **SplitUp**
- [x] Concepto definido (app tipo Tricount con mejoras)
- [x] Identidad visual (gradiente rosa/morado/negro, estilo minimalista y profesional)
- [x] Logo principal (beta)
- [x] README base + estética tipo “proyecto serio”
- [x] Estructura inicial del repositorio

**Evidencias**
- `assets/logo/splitup-logo-gradient.png`
- `README.md`

---

## 🟡 FASE 1 · Análisis y diseño (EN CURSO — muy avanzada)

**Objetivo:** Tener el análisis y diseño completos antes de implementar.

### ✔ Completado
- [x] Visión del proyecto (`docs/00-vision.md`)
- [x] Requisitos funcionales y no funcionales (`docs/01-requisitos.md`)
- [x] Casos de uso (documentados) (`docs/02-casos-de-uso.md`)
- [x] Modelo de datos ER (`docs/diagramas/ERD.png`)
- [x] UML de clases (core) (`docs/diagramas/UML_clases.png`)
- [x] Diagrama de casos de uso (`docs/diagramas/casos_uso.png`)
- [x] Diagrama de secuencia — Login (`docs/diagramas/secuencia_login.png`)
- [x] Directorio `docs/` estructurado
- [x] Estética unificada para diagramas (rosa/morado, minimalista, profesional)

### 🔜 Pendiente para cerrar fase (opcional)
- [ ] Revisión final y ampliación de casos de uso (alternativos / excepciones)
- [ ] Añadir “Alcance / No alcance” en la documentación (muy útil para defensa)

---

## 🔴 FASE 2 · Diseño de base de datos (MySQL) (PENDIENTE)

**Objetivo:** Pasar del modelo ER a un esquema MySQL implementable.

- [ ] `db/schema.sql` final (tablas + constraints + índices)
- [ ] `db/seeds.sql` (datos de prueba)
- [ ] Decisión final: almacenamiento de tickets (archivo + ruta/URL en BD)
- [ ] Validación del esquema (crear BD, insertar seed, comprobar integridad)

**Evidencias (cuando esté)**
- `db/schema.sql`
- `db/seeds.sql`

---

## 🔴 FASE 3 · Core en Java (lógica de negocio) (PENDIENTE)

**Objetivo:** Implementar el núcleo reutilizable de SplitUp en Java.

- [ ] Entidades de dominio (User, Group, Expense, Share, Attachment, Category)
- [ ] Servicios de negocio (crear gasto, repartir, balances)
- [ ] Algoritmo de balances (quién debe a quién)
- [ ] Validaciones (importes, miembros, permisos)
- [ ] Tests básicos del core (JUnit)

**Evidencias (cuando esté)**
- `core/src/...`
- `core/tests/...` (o módulo tests)

---

## 🔴 FASE 4 · Persistencia y conexión a BD (PENDIENTE)

**Objetivo:** Conectar el core con MySQL.

- [ ] Conector (JDBC / framework a definir)
- [ ] Repositorios (CRUD de grupos, gastos, miembros, adjuntos)
- [ ] Migraciones/Versionado de BD (opcional pero pro)
- [ ] Tests de integración (opcional)

---

## 🔴 FASE 5 · Aplicación Desktop (PENDIENTE)

**Objetivo:** UI de escritorio conectada al core.

- [ ] Tecnología UI (JavaFX / Swing) — decisión documentada
- [ ] Flujos principales: login, grupos, gastos, balance
- [ ] Integración core + persistencia
- [ ] Capturas para memoria del TFG

---

## 🔴 FASE 6 · Aplicación Android (PENDIENTE)

**Objetivo:** Versión móvil en Android Studio (Java), reutilizando el core.

- [ ] Login social (Google / Facebook) integrado
- [ ] UI móvil (grupos, gastos, balance)
- [ ] Cámara / ticket (MVP: adjuntar imagen; Extra: OCR)
- [ ] Persistencia + sincronización (según enfoque)

---

## 🔴 FASE 7 · Pulido final y defensa (PENDIENTE)

**Objetivo:** Preparar entrega final, memoria y demo.

- [ ] Pruebas finales
- [ ] Documentación final (memoria)
- [ ] Presentación / demo
- [ ] Checklist de entrega

---

## 📝 Registro breve (opcional)
Añade una línea por hito:

- 2026-01-29: README + diagramas (ERD, UML, Casos de uso, Secuencia login) + estética unificada.

