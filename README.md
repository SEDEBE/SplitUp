# SplitUp

<div align="center">

<img src="assets/splitup-logo-beta.png" style="max-width: 420px; width: 100%;">

**Aplicación de reparto de gastos en grupo**  
Inspirada en Tricount · Desarrollada en Java

</div>

---

# Descripción general del proyecto

**SplitUp** es una aplicación diseñada para facilitar la gestión y el reparto de gastos compartidos entre varias personas.  
El proyecto nace como **Trabajo de Fin de Grado (TFG)** del ciclo de **Desarrollo de Aplicaciones Multiplataforma (DAM)** y tiene como objetivo aplicar de forma realista los conocimientos adquiridos durante el ciclo.

La aplicación permite crear grupos, registrar gastos, asignar participantes y calcular automáticamente los balances finales, indicando quién debe dinero y a quién.

---

# Objetivos del proyecto

- Aplicar **Programación Orientada a Objetos** de forma coherente.
- Diseñar una **arquitectura reutilizable en Java**.
- Separar correctamente **lógica de negocio e interfaces**.
- Desarrollar una aplicación **multiplataforma**:
  - Aplicación principal en Java
  - Aplicación móvil Android (Java)
- Documentar todo el proceso técnico y las decisiones de diseño.

---

# Arquitectura general del sistema

El proyecto se estructura en **tres grandes bloques**:

1. **Core (Java)**  
   Contiene toda la lógica de negocio común:
   - Grupos
   - Participantes
   - Gastos
   - Cálculo de balances
   - Reglas de reparto

2. **Aplicación principal (Java Desktop)**  
   Interfaz de usuario para gestionar los grupos y gastos desde escritorio.

3. **Aplicación móvil Android (Java)**  
   Versión móvil desarrollada en Android Studio que reutiliza la lógica del core.

Esta separación permite:

- Reutilización de código
- Mantenimiento sencillo
- Escalabilidad futura

---

# Estructura del repositorio

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
│   └── src/
│
├── app-android/
│   └── SplitUpApp/
│
└── assets/
    └── splitup-logo-beta.png
```
