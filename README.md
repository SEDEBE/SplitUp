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

## Estado del proyecto

🟢 Fase 0 – Identidad: Completada  
🟢 Fase 1 – Análisis y diseño: Completada  
🟢 Fase 2 – Base de datos (MySQL): Completada  
🟡 Fase 3 – Core en Java: En progreso

📌 Consulta el progreso detallado en: [`ROADMAP_SplitUp.md`](ROADMAP_SplitUp.md)

---

## Estado técnico actual del core (NUEVO)

El core del proyecto está actualmente implementado como un **proyecto Maven independiente**, con las siguientes características:

- Gestión de dependencias mediante Maven
- Persistencia implementada con **Hibernate**
- Conexión real a base de datos **MySQL**
- Estructura preparada para reutilización desde aplicaciones cliente
- Logging controlado mediante **Logback**

Se ha validado:

- Compilación correcta
- Ejecución correcta
- Inserción real de datos en la base de datos
- Control de errores y transacciones

---

## Tecnologías utilizadas (NUEVO)

- Java (JDK 25)
- Maven
- Hibernate ORM
- Jakarta Persistence (JPA)
- MySQL
- SLF4J + Logback
- JUnit 5

---

## Estructura del repositorio

```txt
SplitUp/
├── README.md
├── ROADMAP_SplitUp.md
│
├── docs/
│   ├── 00-vision.md
│   ├── 01-requisitos.md
│   ├── 02-casos-de-uso.md
│   ├── 03-modelo-datos-er.md
│   ├── 04-diagrama-clases.md
│   ├── 05-decisiones-tecnicas.md
│   └── diagramas/
│
├── core/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/splitup/
│   │   │   │   ├── app/
│   │   │   │   │   └── TestHibernate.java
│   │   │   │   ├── model/
│   │   │   │   │   └── User.java
│   │   │   │   ├── service/
│   │   │   │   └── utils/
│   │   │   │       └── HibernateUtil.java
│   │   │   └── resources/
│   │   │       ├── hibernate.properties
│   │   │       └── logback.xml
│   │   └── test/
│   │       └── java/com/splitup/
│   │           └── AppTest.java
│   └── target/
│       ├── classes/
│       ├── generated-sources/
│       ├── maven-status/
│       └── test-classes/
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
```
