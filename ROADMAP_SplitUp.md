# SplitUp — Hoja de ruta y progreso (TFG)

> Autor: Alejandro Córdoba Pérez  
> Proyecto: SplitUp  
> Última actualización: 2026-02-08

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

---

## ✅ FASE 1 · Análisis y diseño (COMPLETADA)

- [x] Visión general y requisitos (docs 00–01)
- [x] Casos de uso documentados (docs 02)
- [x] Diagramas ER, UML y secuencias (docs/diagramas)

---

## ✅ FASE 2 · Diseño de base de datos (MySQL) (COMPLETADA)

- [x] Esquema relacional completo (`db/schema.sql`)
- [x] Seeds de ejemplo (`db/seeds.sql`)
- [x] Consultas de prueba (`db/pruebasDb.sql`)
- [x] Incidencia resuelta: `groups` → `expense_groups`

---

## 🟡 FASE 3 · Core en Java (lógica de negocio) (EN PROGRESO)

### ✔ Completado

- [x] Creación del módulo `core`
- [x] Configuración del proyecto como **Maven**
- [x] Definición y validación del `pom.xml`
- [x] Gestión de dependencias (Hibernate, JPA, MySQL, Logback, JUnit)
- [x] Configuración de Hibernate mediante `hibernate.properties`
- [x] Implementación de `HibernateUtil`
- [x] Conexión real a MySQL
- [x] Configuración de logging con Logback
- [x] Entidad `User` mapeada y persistida
- [x] Entidad `ExpenseGroup` mapeada
- [x] Entidad `GroupMember` con clave compuesta (`GroupMemberId`)
- [x] Entidad `Expense` mapeada
- [x] Entidad `Category` mapeada
- [x] Entidad `Attachment` mapeada
- [x] Entidad `AuthIdentity` mapeada
- [x] Entidad `ExpenseShare` con clave compuesta (`ExpenseShareId`)
- [x] Enumeraciones de dominio base (GroupRole, SplitMode, ShareType, AuthProvider, AttachmentType)
- [x] Prueba funcional con `TestHibernate`

### 🔜 Pendiente

- [ ] Repositorios / DAO y consultas específicas
- [ ] Servicios de dominio (grupos, gastos, participantes, balances)
- [ ] Algoritmo de cálculo de balances
- [ ] Validaciones de negocio (importes, roles, participantes)
- [ ] Pruebas unitarias y de integración reales

---

## 🔜 FASE 4 · Persistencia avanzada

- [ ] Consultas complejas
- [ ] Optimización de índices y rendimiento
- [ ] Pruebas de integración con MySQL

---

## 🔜 FASE 5 · Aplicación de escritorio

- [ ] Diseño de interfaz (JavaFX o Swing)
- [ ] Integración completa con el core

---

## 🔜 FASE 6 · Aplicación Android

- [ ] Desarrollo de la interfaz móvil
- [ ] Sistema de autenticación
- [ ] OCR para tickets (funcionalidad adicional)

---

## 🔜 FASE 7 · Pulido y defensa del proyecto

- [ ] Pruebas finales
- [ ] Redacción de la memoria
- [ ] Preparación de la presentación
- [ ] Demostración funcional del sistema

---

## 📝 Registro breve

- 2026-01-29: Configuración completa del entorno Java + Maven.
- 2026-01-30: Persistencia con Hibernate funcional, conexión real a MySQL, primera entidad validada.
- 2026-01-31: Consolidación del core y documentación técnica del proceso.
- 2026-02-03: Barrido del repositorio y actualización del estado real del core.
- 2026-02-08: Modelo completo del core mapeado y validación Hibernate sin errores.
