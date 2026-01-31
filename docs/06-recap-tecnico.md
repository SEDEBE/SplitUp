# SplitUp — Recapitulación técnica del core

Este documento recoge de forma detallada todo el trabajo técnico realizado hasta el momento en el proyecto **SplitUp**, explicando no solo _qué_ se ha hecho, sino _por qué_ se ha hecho así.

---

## 1. Motivación técnica

Antes de escribir código, el proyecto fue diseñado mediante:

- Casos de uso
- Modelo Entidad/Relación
- UML de clases

Esto permitió construir una base sólida antes de implementar el core.

---

## 2. Decisión: Core independiente

Se decidió implementar la lógica de negocio en un **core independiente** para:

- Reutilizar código en desktop y Android
- Evitar duplicación
- Facilitar mantenimiento y pruebas

---

## 3. Maven

El core se configuró como proyecto Maven para:

- Gestionar dependencias
- Asegurar builds reproducibles
- Facilitar escalabilidad

El `pom.xml` define versiones explícitas para evitar incompatibilidades.

---

## 4. Hibernate

Se eligió Hibernate como ORM por:

- Integración con JPA
- Madurez del framework
- Uso habitual en entornos profesionales

Se descartó el uso de `persistence.xml` y `hibernate.cfg.xml` para evitar duplicidades, optando por:

- `hibernate.properties`
- Configuración centralizada en `HibernateUtil`

---

## 5. HibernateUtil

`HibernateUtil`:

- Carga configuración
- Crea un único `SessionFactory`
- Gestiona sesiones y cierre de recursos

Esto evita errores comunes y simplifica el acceso a la base de datos.

---

## 6. Base de datos y entidades

La base de datos fue diseñada previamente y luego mapeada en Java.

Ejemplo:

- Tabla `users` → Entidad `User`

Se respetan:

- Nombres
- Tipos
- Restricciones

---

## 7. Gestión de transacciones

Se utiliza el patrón:

- beginTransaction
- persist
- commit
- rollback en error

Esto garantiza consistencia y control total.

---

## 8. Errores reales y aprendizaje

Durante el desarrollo surgieron errores reales:

- Palabras reservadas en MySQL
- Duplicados por claves únicas
- Problemas de configuración

Todos fueron resueltos y documentados, aportando valor al TFG.

---

## 9. Estado actual

El core está preparado para:

- Añadir entidades restantes
- Implementar servicios
- Desarrollar el algoritmo de balances
