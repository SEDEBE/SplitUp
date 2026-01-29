# SplitUp

<div align="center">
  <img src="assets/logo/splitup-logo-gradient.png" width="420" alt="SplitUp Logo">
  
  <br>

**Aplicación de reparto de gastos en grupo**  
Inspirada en Tricount · Desarrollada en Java

</div>

---

## Descripción general del proyecto

**SplitUp** es una aplicación diseñada para facilitar la gestión y el reparto de gastos compartidos entre grupos de personas.

El proyecto se desarrolla como **Trabajo de Fin de Grado (TFG)** del ciclo de  
**Desarrollo de Aplicaciones Multiplataforma (DAM)** en el **IES Lope de Vega**.

La aplicación permite crear grupos, registrar gastos, asignar participantes y calcular automáticamente los balances finales, indicando de forma clara quién debe dinero y a quién.

---

## Objetivos del proyecto

- Aplicar **Programación Orientada a Objetos** de forma coherente.
- Diseñar una **arquitectura reutilizable en Java**.
- Separar correctamente la **lógica de negocio** de las interfaces.
- Desarrollar una aplicación **multiplataforma**:
  - Aplicación principal en Java
  - Aplicación móvil Android (Java)
- Documentar todo el proceso técnico y las decisiones de diseño.

---

P

## Arquitectura general

El proyecto se divide en tres bloques principales:

- **Core (Java)**  
  Contiene toda la lógica de negocio:
  - Grupos
  - Participantes
  - Gastos
  - Cálculo de balances

- **Aplicación Desktop (Java)**  
  Interfaz de usuario para escritorio.

- **Aplicación Android (Java)**  
  Versión móvil desarrollada en Android Studio reutilizando el core.

---

## Diagramas

<div align="center">

<a href="docs/diagramas/ERD.png">
  <img src="docs/diagramas/ERD.png" style="max-width: 600px; width: 100%;">
</a>

**ERD - Modelo Entidad/Relación**

<br><br>

<a href="docs/diagramas/UML_clases.png">
  <img src="docs/diagramas/UML_clases.png" style="max-width: 600px; width: 100%;">
</a>

**UML - Diagrama de clases (Core)**

<br><br>

<a href="docs/diagramas/casos_uso.png">
  <img src="docs/diagramas/casos_uso.png" style="max-width: 600px; width: 100%;">
</a>

**Diagrama de casos de uso**

<br><br>

<a href="docs/diagramas/secuencia_login.png">
  <img src="docs/diagramas/secuencia_login.png" style="max-width: 600px; width: 100%;">
</a>

**Diagrama de secuencia – Inicio de sesión**

</div>

---

## Estado del proyecto

🟢 Fase 0 – Identidad: Completada  
🟢 Fase 1 – Análisis y diseño: Completada  
🟢 Fase 2 – Base de datos (MySQL): Completada  
🟡 Fase 3 – Core en Java: En progreso

📌 Consulta el progreso detallado en: [`ROADMAP_SplitUp.md`](ROADMAP_SplitUp.md)

---

## Estructura del repositorio

```txt
SplitUp/
├── README.md
├── ROADMAP_SplitUp.md
├── .git/
├── .github/
│
├── docs/
│   ├── 00-vision.md
│   ├── 01-requisitos.md
│   ├── 02-casos-de-uso.md
│   ├── 03-modelo-datos-er.md
│   ├── 04-diagrama-clases.md
│   ├── 05-decisiones-tecnicas.md
│   └── diagramas/
│       ├── ERD.png
│       ├── UML_clases.png
│       ├── casos_uso.png
│       └── secuencia_login.png
│
├── core/
│   └── src/
│       └── splitup/
│           ├── model/
│           ├── service/
│           └── utils/
│
├── app-desktop/
│   └── src/
│
├── app-android/
│   └── SplitUpApp/
│
├── db/
│   ├── schema.sql
│   ├── seeds.sql
│   └── pruebasDb.sql
│
└── assets/
    └── logo/
        ├── splitup-logo-gradient.png
        ├── splitup-logo-dark.png
        └── splitup-icon.png
```
