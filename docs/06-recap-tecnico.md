# SplitUp — Recapitulación técnica del proyecto

> Última actualización: 2026-02-03

## 1. Introducción

El presente documento recoge la **recapitulación técnica del proyecto SplitUp**, desarrollado como **Trabajo de Fin de Grado (TFG)** del ciclo de **Desarrollo de Aplicaciones Multiplataforma (DAM)**.

SplitUp es una aplicación multiplataforma orientada a la **gestión y reparto equitativo de gastos compartidos entre varios usuarios**, cuyo objetivo principal es aplicar de forma práctica y rigurosa los conocimientos adquiridos durante el ciclo formativo, haciendo especial énfasis en:

- Programación Orientada a Objetos
- Diseño y arquitectura de software
- Persistencia de datos
- Separación de responsabilidades
- Documentación técnica profesional

Este documento tiene carácter **estrictamente técnico**, y describe la arquitectura, las decisiones de diseño, las tecnologías empleadas y el estado real de desarrollo del sistema.

---

## 2. Descripción general del proyecto

SplitUp permite a un conjunto de usuarios organizarse en grupos para registrar gastos comunes y calcular automáticamente los balances finales, determinando de forma clara qué usuarios deben dinero y en qué cuantía.

Desde el punto de vista funcional, el proyecto se inspira en aplicaciones existentes de reparto de gastos; no obstante, su desarrollo se ha realizado **íntegramente desde cero**, con fines académicos, priorizando la calidad del diseño, la claridad del dominio y la mantenibilidad del código.

---

## 3. Arquitectura general del sistema

El sistema se ha diseñado siguiendo una **arquitectura en capas**, con el objetivo de garantizar la separación de responsabilidades y facilitar la escalabilidad y el mantenimiento del proyecto.

### 3.1 Principios de diseño aplicados

Durante el diseño del sistema se han aplicado los siguientes principios:

- Programación Orientada a Objetos (POO)
- Principio de Responsabilidad Única (SRP)
- Bajo acoplamiento entre capas
- Alta cohesión interna
- Separación clara entre dominio y persistencia
- Preparación para pruebas unitarias y de integración

Esta arquitectura permite que el núcleo del sistema sea **independiente de la interfaz**, favoreciendo la reutilización del código y la evolución futura del proyecto.

---

## 4. FASE 0 · Identidad y base del proyecto

### 4.1 Concepto y denominación

El proyecto recibe la denominación **SplitUp**, un nombre breve y representativo de la funcionalidad principal de la aplicación: dividir gastos entre varias personas.

### 4.2 Identidad visual

Se ha definido una identidad visual coherente y profesional, caracterizada por:

- Estilo minimalista
- Uso consistente de gradientes en tonos rosa y morado
- Uniformidad visual en diagramas y documentación técnica

### 4.3 Repositorio y documentación inicial

Desde el inicio del proyecto se estableció una base documental sólida, compuesta por:

- Un archivo `README.md` descriptivo
- Un documento `ROADMAP_SplitUp.md` como hoja de ruta del desarrollo
- Documentación técnica estructurada por fases

---

## 5. FASE 1 · Análisis y diseño del sistema

### 5.1 Requisitos funcionales

El sistema debe permitir:

- La gestión de usuarios
- La creación y administración de grupos de gasto
- El registro de gastos
- La asociación de participantes a cada gasto
- El cálculo automático de balances
- La determinación clara de deudas entre usuarios

### 5.2 Requisitos no funcionales

Entre los requisitos no funcionales se incluyen:

- Persistencia fiable de la información
- Código mantenible y escalable
- Claridad y trazabilidad en los cálculos económicos
- Preparación para futuras ampliaciones
- Diseño comprensible y defendible

### 5.3 Modelado del sistema

Durante esta fase se elaboraron:

- Un modelo Entidad–Relación (ER)
- Diagramas UML de clases
- Diagramas de secuencia para los flujos principales

Todos los diagramas fueron diseñados alineados con el dominio del problema y con una estética uniforme para facilitar su comprensión durante la defensa del TFG.

---

## 6. FASE 2 · Diseño de la base de datos MySQL

### 6.1 Diseño del esquema relacional

La base de datos se diseñó manualmente y se validó mediante MySQL Workbench.

Las principales entidades del esquema son:

- users
- auth_identities
- expense_groups
- group_members
- categories
- expenses
- expense_shares
- attachments

### 6.2 Claves y relaciones

El esquema relacional incluye:

- Claves primarias autoincrementales
- Claves foráneas correctamente definidas
- Índices para optimización de consultas
- Garantía de integridad referencial

### 6.3 Incidencia técnica documentada

Durante el diseño se detectó una incidencia relevante:

- Uso del identificador `groups` como nombre de tabla
- Aparición del error MySQL 1064 debido al uso de una palabra reservada

**Solución adoptada**:

- Renombrado de la tabla a `expense_groups`

Esta incidencia se documenta como ejemplo real de detección y resolución de problemas técnicos durante el desarrollo del proyecto.

---

## 7. FASE 3 · Desarrollo del núcleo del sistema en Java

### 7.1 Entorno de desarrollo

- Lenguaje: Java 21 (configurado en Maven)
- Gestión de dependencias: Maven
- Persistencia: Hibernate + JPA
- Base de datos: MySQL

### 7.2 Dependencias principales

- Hibernate ORM 6.6
- Jakarta Persistence 3.1
- MySQL Connector/J 9.0
- SLF4J 2.0 + Logback 1.5
- JUnit 5 (tests básicos)

### 7.3 Configuración de la persistencia

- Uso del archivo `hibernate.properties`
- Eliminación de `persistence.xml`
- Implementación de la clase utilitaria `HibernateUtil`
- Gestión manual de sesiones y transacciones
- `hbm2ddl.auto=validate` para asegurar coherencia con el esquema

### 7.4 Sistema de logging

- Configuración de Logback
- Control de los logs generados por Hibernate
- Salida optimizada para desarrollo y depuración

### 7.5 Modelo implementado actualmente

En el estado actual del proyecto se ha implementado:

- Entidad `User` con email único, nombre visible y timestamps
- Entidad `ExpenseGroup` con creador y metadatos básicos
- Entidad `GroupMember` con clave compuesta y rol (`GroupRole`)
- Clave embebida `GroupMemberId` (`group_id`, `user_id`)
- Enumeraciones de dominio base: `GroupRole`, `SplitMode`, `ShareType`, `AuthProvider`, `AttachmentType`

### 7.6 Prueba técnica y validación local

- Clase `TestHibernate` para validar inserción, lectura y actualización básica
- Control explícito de transacciones con rollback seguro

### 7.7 Estado actual del núcleo del sistema

- Configuración validada
- Persistencia funcional para el modelo base
- Esqueleto de dominio listo para extender entidades de gastos y reparto

---

## 8. Trabajo pendiente en el núcleo del sistema

Quedan pendientes las siguientes tareas:

- Implementación de entidades restantes: `Expense`, `Category`, `Attachment`, `AuthIdentity`, `ExpenseShare`
- Repositorios / DAOs y consultas específicas
- Servicios de dominio (grupos, gastos, participantes, balances)
- Algoritmo de cálculo de balances
- Validaciones de negocio
- Pruebas unitarias y de integración completas

---

## 9. Fases futuras del proyecto

### FASE 4 · Persistencia avanzada

- Consultas complejas
- Optimización de índices
- Pruebas de integración con MySQL

### FASE 5 · Aplicación de escritorio

- Desarrollo de la interfaz en JavaFX o Swing
- Integración completa con el núcleo del sistema

### FASE 6 · Aplicación Android

- Desarrollo de la interfaz móvil
- Sistema de autenticación
- Integración de OCR para tickets (funcionalidad adicional)

### FASE 7 · Pulido y defensa del proyecto

- Pruebas finales
- Redacción de la memoria
- Preparación de la presentación
- Demostración funcional del sistema

---

## 10. Conclusión

SplitUp es un proyecto desarrollado con un enfoque **riguroso, realista y profesional**, priorizando una arquitectura sólida, un diseño claro del dominio y una documentación técnica exhaustiva.

El estado actual del sistema proporciona una base robusta para continuar el desarrollo sin incurrir en deuda técnica significativa, garantizando la trazabilidad de las decisiones adoptadas durante todo el ciclo de vida del proyecto.

---

Documento vivo. Este archivo se actualizará conforme avance el desarrollo del proyecto.
