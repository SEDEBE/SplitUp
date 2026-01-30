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

## ✅ FASE 1 · Análisis y diseño (COMPLETADA)

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

---

## ✅ FASE 2 · Diseño de base de datos (MySQL) (COMPLETADA)

**Objetivo:** Pasar del modelo ER a un esquema MySQL implementable.

### ✔ Completado

- [x] Diseño del esquema MySQL definitivo (`schema.sql`)
- [x] Corrección de palabra reservada (`groups` → `expense_groups`)
- [x] Definición de claves primarias y foráneas
- [x] Índices para optimización de consultas
- [x] Script de datos de prueba (`seeds.sql`)
- [x] Creación de la base de datos en entorno local
- [x] Gestión y validación mediante MySQL Workbench
- [x] Inserción correcta de usuarios, grupos, gastos y adjuntos
- [x] Verificación de integridad referencial mediante consultas SQL

### 📌 Incidencias relevantes

- Uso de `groups` como nombre de tabla (palabra reservada en MySQL).
- Solución: renombrado a `expense_groups` y actualización completa del esquema.

> Incidencia documentada como ejemplo real para la defensa del TFG.

**Evidencias**

- `db/schema.sql`
- `db/seeds.sql`
- `db/pruebasDb.sql`

---

## 🟡 FASE 3 · Core en Java (lógica de negocio) (EN CURSO)

**Objetivo:** Implementar el núcleo reutilizable de SplitUp en Java.

### ✔ Completado

- [x] Definición de la estructura del módulo `core`
- [x] Configuración del proyecto Java con **Maven**
- [x] Creación y validación del `pom.xml`
- [x] Gestión de dependencias (Hibernate, JPA, MySQL, Logback)
- [x] Compilación y ejecución correcta (`mvn clean test`)
- [x] Separación clara entre core, apps cliente y documentación

### 🔜 Pendiente

- [ ] Entidades de dominio (User, Group, Expense, Share, Attachment, Category)
- [ ] Servicios de negocio (crear gasto, repartir, balances)
- [ ] Algoritmo de balances (quién debe a quién)
- [ ] Validaciones de dominio
- [ ] Tests unitarios del core (JUnit)

**Evidencias**

- `core/pom.xml`
- `core/src/main/java/...`

---

## 🔴 FASE 4 · Persistencia y conexión a BD (PENDIENTE)

**Objetivo:** Conectar el core con MySQL mediante Hibernate.

- [ ] Configuración JPA (`persistence.xml`)
- [ ] Utilidad de conexión (EntityManager / Session)
- [ ] Repositorios (CRUD)
- [ ] Tests de integración (opcional)

---

## 🔴 FASE 5 · Aplicación Desktop (PENDIENTE)

**Objetivo:** UI de escritorio conectada al core.

- [ ] Tecnología UI (JavaFX / Swing)
- [ ] Flujos principales
- [ ] Integración core + persistencia
- [ ] Capturas para memoria

---

## 🔴 FASE 6 · Aplicación Android (PENDIENTE)

**Objetivo:** Versión móvil reutilizando el core.

- [ ] Login
- [ ] UI móvil
- [ ] Cámara / tickets (extra OCR)
- [ ] Sincronización

---

## 🔴 FASE 7 · Pulido final y defensa (PENDIENTE)

**Objetivo:** Preparar entrega final.

- [ ] Pruebas finales
- [ ] Memoria final
- [ ] Presentación / demo
- [ ] Checklist de entrega

---

## 📝 Registro breve

- 2026-01-29: Entorno Java + Maven configurado. Core preparado como proyecto Maven funcional.
