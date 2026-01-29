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

## Estructura del repositorio

```txt
SplitUp/
├── README.md
├── docs/
│   ├── analisis.md
│   ├── casos_de_uso.md
│   ├── decisiones_tecnicas.md
│   └── diagramas/
│
├── core/
│   └── src/
│       └── splitup/
│           ├── model/
│           ├── service/
│           └── utils/
│
├── app-desktop/
├── app-android/
│
└── assets/
    └── logo/
        ├── splitup-logo-gradient.png
        ├── splitup-logo-dark.png
        └── splitup-icon.png
```
