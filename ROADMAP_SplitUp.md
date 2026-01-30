# SplitUp — Hoja de ruta y progreso (TFG)

> Autor: Alejandro Córdoba Pérez  
> Proyecto: SplitUp  
> Última actualización: 2026-01-30

Documento vivo de seguimiento del proyecto.

---

## ✅ FASE 0 · Identidad y base del proyecto (COMPLETADA)

- [x] Nombre del proyecto: SplitUp
- [x] Concepto definido (tipo Tricount)
- [x] Identidad visual
- [x] Logo principal
- [x] README profesional
- [x] Estructura base del repositorio

---

## ✅ FASE 1 · Análisis y diseño (COMPLETADA)

- [x] Visión del proyecto
- [x] Requisitos funcionales y no funcionales
- [x] Casos de uso
- [x] Modelo ER
- [x] UML de clases
- [x] Diagramas de secuencia
- [x] Estética unificada de diagramas

---

## ✅ FASE 2 · Base de datos MySQL (COMPLETADA)

- [x] Esquema MySQL definitivo
- [x] Corrección de palabra reservada (`groups` → `expense_groups`)
- [x] Claves primarias y foráneas
- [x] Índices
- [x] Datos de prueba
- [x] Validación en MySQL Workbench

📌 Incidencia documentada para defensa del TFG.

---

## 🟡 FASE 3 · Core en Java (EN CURSO)

### ✔ Completado

- [x] Proyecto Maven funcional
- [x] `pom.xml` con dependencias
- [x] Configuración Hibernate con `hibernate.properties`
- [x] Utilidad `HibernateUtil`
- [x] Logback configurado (logs controlados)
- [x] Entidad `User` mapeada
- [x] Persistencia real en MySQL
- [x] Control de transacciones y rollback
- [x] Validación de constraints (email único)

### 🔜 Pendiente

- [ ] Entidades restantes (Group, Expense, Category, Attachment…)
- [ ] Servicios de dominio
- [ ] Algoritmo de balances
- [ ] Validaciones de negocio
- [ ] Tests unitarios (JUnit)

---

## 🔴 FASE 4 · Persistencia avanzada (PENDIENTE)

- [ ] Repositorios / DAOs
- [ ] Queries personalizadas
- [ ] Tests de integración

---

## 🔴 FASE 5 · Aplicación Desktop (PENDIENTE)

- [ ] Elección de tecnología (JavaFX / Swing)
- [ ] UI principal
- [ ] Integración con el core

---

## 🔴 FASE 6 · Aplicación Android (PENDIENTE)

- [ ] UI móvil
- [ ] Login
- [ ] Cámara / tickets (OCR – extra)
- [ ] Sincronización

---

## 🔴 FASE 7 · Pulido y defensa (PENDIENTE)

- [ ] Pruebas finales
- [ ] Memoria final
- [ ] Presentación
- [ ] Demo funcional

---

## 📝 Registro

- **2026-01-30**: Hibernate completamente operativo con MySQL. Inserción y control de errores funcionando correctamente.
